# Android Best Practices Guide - Code Review

## Problèmes Corrigés

### 1. ❌ Opérateur `!!` (Non-Null Assertion) - CORRIGÉ

**Problème :**
```kotlin
// AVANT - DANGEREUX
uiState.hasData -> PortfolioDataContent(
    summary = uiState.summary!!,  // ⚠️ Peut lancer NullPointerException
    modifier = Modifier.fillMaxSize()
)
```

**Pourquoi c'est mauvais :**
- `!!` dit au compilateur "je sais que c'est non-null"
- Si la valeur est null → crash avec NullPointerException
- Violation du contrat de null-safety de Kotlin

**Solution : Utiliser le Smart Cast**
```kotlin
// APRÈS - SÛR
uiState.summary != null -> PortfolioDataContent(
    summary = uiState.summary,  // ✅ Smart cast: Kotlin sait que c'est non-null
    modifier = Modifier.fillMaxSize()
)
```

**Pourquoi ça fonctionne :**
- Le compilateur Kotlin fait un "smart cast" après la vérification `!= null`
- Le type change automatiquement de `PortfolioSummary?` à `PortfolioSummary`
- Plus besoin de `!!`, pas de risque de crash

**Important :** On ne peut pas utiliser `hasData` pour le smart cast car c'est une computed property. Le compilateur ne peut pas garantir que la valeur n'a pas changé entre l'appel de `hasData` et l'accès à `summary`.

---

### 2. ✅ MutableStateFlow Privé - DÉJÀ CORRECT

**Bonne pratique :**
```kotlin
class PortfolioViewModel : ViewModel() {
    // Privé - seul le ViewModel peut modifier
    private val _uiState = MutableStateFlow(PortfolioUiState())
    
    // Public - UI observe uniquement (read-only)
    val uiState: StateFlow<PortfolioUiState> = _uiState.asStateFlow()
}
```

**Pourquoi :**
- Principe de moindre privilège
- UI ne peut pas modifier l'état directement
- Seules les fonctions du ViewModel peuvent changer l'état
- Prévient les bugs de modification accidentelle

---

### 3. ✅ Gestion des Erreurs avec Result - DÉJÀ CORRECT

**Bonne pratique :**
```kotlin
// Utiliser Result<T> pour les opérations qui peuvent échouer
fun getPortfolio(): Flow<Result<PortfolioSummary>>

// Dans le ViewModel
result.fold(
    onSuccess = { data -> /* ... */ },
    onFailure = { error -> /* ... */ }
)
```

**Pourquoi :**
- Type-safe error handling
- Force le traitement des deux cas (success/failure)
- Pas besoin de try-catch partout
- Plus explicite que les exceptions

---

### 4. ✅ State Hoisting - DÉJÀ CORRECT

**Bonne pratique :**
```kotlin
// Écran principal (avec ViewModel)
@Composable
fun PortfolioScreen(viewModel: PortfolioViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    PortfolioContent(
        uiState = uiState,
        onRefresh = viewModel::onRefresh,  // Event callbacks
        onRetry = viewModel::onRetry
    )
}

// Composable stateless (réutilisable/testable)
@Composable
private fun PortfolioContent(
    uiState: PortfolioUiState,  // State en paramètre
    onRefresh: () -> Unit,        // Events en callbacks
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
)
```

**Pourquoi :**
- Séparation des responsabilités
- Composable testable sans ViewModel
- Prévisualisable dans Android Studio
- Réutilisable dans différents contextes

---

## Anti-Patterns à Éviter

### ❌ Ne pas faire : Exposer MutableStateFlow

```kotlin
// MAUVAIS
class ViewModel : ViewModel() {
    val uiState = MutableStateFlow(UiState())  // Public!
}

// UI peut faire : viewModel.uiState.value = ...
// → Casse le principe de single source of truth
```

### ❌ Ne pas faire : Utiliser `catch` trop générique

```kotlin
// MAUVAIS - Capture tout, même les bugs de programmation
catch { error ->
    // Capture OutOfMemoryError, CancellationException, etc.
}

// MIEUX - Spécifique
catch { error: IOException ->
    // Gestion réseau
}
```

### ❌ Ne pas faire : Appeler suspend functions depuis Composable

```kotlin
// MAUVAIS
@Composable
fun Screen() {
    val data = fetchData()  // ❌ Ne pas appeler directement!
}

// CORRECT
@Composable
fun Screen(viewModel: ViewModel = hiltViewModel()) {
    val data by viewModel.data.collectAsStateWithLifecycle()
}
```

---

## Patterns Recommandés

### 1. Unidirectional Data Flow (UDF)

```
User Action → ViewModel → Updates State → UI Recomposes
     ↑                                      ↓
     └────────── Event Callbacks ←──────────┘
```

**Avantages :**
- Prévisible
- Debuggable (état à un instant T)
- Testable
- Pas de "spaghetti code"

### 2. Single Source of Truth

**Un seul objet d'état par écran :**
```kotlin
data class UiState(
    val isLoading: Boolean = false,
    val data: List<Item> = emptyList(),
    val error: String? = null
)

// PAS ça :
val isLoading = MutableLiveData<Boolean>()  // ❌
val data = MutableLiveData<List<Item>>()    // ❌
val error = MutableLiveData<String>()       // ❌
```

### 3. Repository Pattern

```kotlin
// Interface dans domain layer
interface Repository {
    fun getData(): Flow<Result<Data>>
}

// Implémentation dans data layer
class RepositoryImpl @Inject constructor(
    private val remote: RemoteDataSource,
    private val local: LocalDataSource
) : Repository
```

**Avantages :**
- Testable (mock interface)
- Swappable (change implementation)
- Clean architecture

---

## Règles d'Or Compose

### 1. Pas de logique métier dans les Composables

```kotlin
// MAUVAIS
@Composable
fun Screen() {
    val discount = if (user.isPremium) 0.2 else 0.0  // ❌
}

// CORRECT
@Composable
fun Screen(viewModel: ViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    // uiState contient déjà le discount calculé
}
```

### 2. Utiliser `collectAsStateWithLifecycle` pas `collectAsState`

```kotlin
// MEILLEUR - Gère le lifecycle automatiquement
val state by viewModel.state.collectAsStateWithLifecycle()

// Moins optimal - Continue à collecter en arrière-plan
val state by viewModel.state.collectAsState()
```

### 3. Keys stables pour LazyColumn/LazyRow

```kotlin
LazyColumn {
    items(
        items = items,
        key = { it.id }  // ✅ Stable key pour animations/optimisations
    ) { item ->
        ItemCard(item)
    }
}
```

---

## Checklist Code Review

Avant de commit, vérifier :

- [ ] Pas d'utilisation de `!!`
- [ ] Pas de ` MutableStateFlow` exposé publiquement
- [ ] Smart casts utilisés quand possible
- [ ] `catch` blocks spécifiques (pas trop larges)
- [ ] State hoisting correct
- [ ] Pas de logique métier dans les Composables
- [ ] `collectAsStateWithLifecycle` utilisé
- [ ] Documentation à jour
- [ ] Tests passent
- [ ] `./gradlew build` réussit

---

## Liens de Référence

- [Kotlin Null Safety](https://kotlinlang.org/docs/null-safety.html)
- [Compose State Documentation](https://developer.android.com/jetpack/compose/state)
- [Android Architecture Guide](https://developer.android.com/topic/architecture)
- [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
