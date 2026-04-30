# Guide de Préparation - Entretien Pictet Technologies

## Objectif Final
Démontrer une expertise en Android moderne (Jetpack Compose, MVVM, Flow, Hilt) lors d'un exercice de mob programming de 3 heures.

---

## 1. Approche Mentale (Comment penser)

### Le Framework de Réflexion
Pour chaque feature que tu codes, suis ce raisonnement :

```
1. QUOI afficher ? → Définir l'UI State (data class)
2. D'OÙ viennent les données ? → Repository → DataSource
3. QUELLE est la logique métier ? → UseCase
4. COMMENT réagir aux changements ? → ViewModel + Flow/StateFlow
5. COMMENT l'afficher ? → Composable Compose
```

### Principes SOLID à démontrer
- **S**ingle Responsibility : Une classe = une raison de changer
- **O**pen/Closed : Extensible sans modification (abstractions)
- **L**iskov Substitution : Implémentations interchangeables
- **I**nterface Segregation : Petites interfaces spécialisées
- **D**ependency Inversion : Dépendre d'abstractions, pas d'implémentations

---

## 2. Architecture MVVM - Guide Pratique

### Schéma de Flux des Données

```
┌─────────────────────────────────────────────────────────────────┐
│                         UI LAYER                                │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │  @Composable Screen                                      │  │
│  │  - Collecte StateFlow : uiState.collectAsState()          │  │
│  │  - Appelle fonctions ViewModel : viewModel::onEvent       │  │
│  └───────────────────┬──────────────────────────────────────┘  │
└──────────────────────┼──────────────────────────────────────────┘
                       │ observe (StateFlow)
                       ▼
┌─────────────────────────────────────────────────────────────────┐
│                     VIEWMODEL LAYER                             │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │  @HiltViewModel                                          │  │
│  │  class XxxViewModel @Inject constructor(                 │  │
│  │      private val useCase: GetXxxUseCase                 │  │
│  │  ) : ViewModel() {                                       │  │
│  │                                                           │  │
│  │      private val _uiState = MutableStateFlow(XxxState())│  │
│  │      val uiState: StateFlow<XxxState> = _uiState.asStateFlow()││
│  │                                                           │  │
│  │      init { loadData() }                                  │  │
│  │                                                           │  │
│  │      private fun loadData() {                            │  │
│  │          viewModelScope.launch {                         │  │
│  │              _uiState.update { it.copy(isLoading = true) }│  │
│  │              useCase().fold(                             │  │
│  │                  onSuccess = { data ->                   │  │
│  │                      _uiState.update {                     │  │
│  │                          it.copy(isLoading = false,       ││
│  │                              data = data)                 ││
│  │                      }                                    │  │
│  │                  },                                       │  │
│  │                  onFailure = { error ->                    │  │
│  │                      _uiState.update {                     ││
│  │                          it.copy(isLoading = false,        ││
│  │                              error = error.message)       ││
│  │                      }                                    │  │
│  │                  }                                        │  │
│  │              )                                           │  │
│  │          }                                                │  │
│  │      }                                                    │  │
│  │  }                                                       │  │
│  └───────────────────┬──────────────────────────────────────┘  │
└──────────────────────┼──────────────────────────────────────────┘
                       │ appelle
                       ▼
┌─────────────────────────────────────────────────────────────────┐
│                      DOMAIN LAYER                             │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │  interface XxxRepository {                                │  │
│  │      suspend fun getXxx(): Result<List<Xxx>>             │  │
│  │  }                                                       │  │
│  │                                                           │  │
│  │  class GetXxxUseCase @Inject constructor(                │  │
│  │      private val repository: XxxRepository               │  │
│  │  ) {                                                      │  │
│  │      suspend operator fun invoke(): Result<List<Xxx>> {  │  │
│  │          // Business logic ici                           │  │
│  │          return repository.getXxx()                     │  │
│  │      }                                                    │  │
│  │  }                                                       │  │
│  └───────────────────┬──────────────────────────────────────┘  │
└──────────────────────┼──────────────────────────────────────────┘
                       │ implémente
                       ▼
┌─────────────────────────────────────────────────────────────────┐
│                       DATA LAYER                              │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │  class XxxRepositoryImpl @Inject constructor(            │  │
│  │      private val remoteDataSource: XxxRemoteDataSource,  │  │
│  │      private val localDataSource: XxxLocalDataSource    │  │
│  │  ) : XxxRepository {                                      │  │
│  │      override suspend fun getXxx(): Result<List<Xxx>> { │  │
│  │          return try {                                     │  │
│  │              // Strategy: Cache-First ou Remote-First     │  │
│  │              val local = localDataSource.getXxx()       │  │
│  │              if (local.isNotEmpty()) {                    │  │
│  │                  Result.success(local)                  │  │
│  │              } else {                                     │  │
│  │                  val remote = remoteDataSource.fetch()  │  │
│  │                  localDataSource.save(remote)           │  │
│  │                  Result.success(remote)                 │  │
│  │              }                                            │  │
│  │          } catch (e: Exception) {                         │  │
│  │              Result.failure(e)                          │  │
│  │          }                                                │  │
│  │      }                                                    │  │
│  │  }                                                       │  │
│  └──────────────────────────────────────────────────────────┘  │
└───────────────────────────────────────────────────────────────┘
```

---

## 3. State Management avec StateFlow

### Pattern UI State Unifié

```kotlin
// Définir UN SEUL état pour l'écran
data class ScreenUiState(
    val isLoading: Boolean = false,
    val data: List<Item> = emptyList(),
    val error: String? = null
)

// Dans le ViewModel
private val _uiState = MutableStateFlow(ScreenUiState())
val uiState: StateFlow<ScreenUiState> = _uiState.asStateFlow()

// Mise à jour atomique
_uiState.update { currentState ->
    currentState.copy(
        isLoading = false,
        data = newData
    )
}
```

### Pourquoi StateFlow et pas LiveData ?
- **StateFlow** : Kotlin pur, pas de dépendance Android, compose-ready
- **LiveData** : Android-specific, observer pattern classique
- Utilise StateFlow pour les nouveaux projets (recommandé par Google)

---

## 4. Dependency Injection avec Hilt

### Pattern d'injection à montrer

```kotlin
// 1. Application class
@HiltAndroidApp
class PictetApplication : Application()

// 2. Module pour Repository
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    abstract fun bindXxxRepository(
        impl: XxxRepositoryImpl
    ): XxxRepository
}

// 3. Module pour DataSource (fournit instance)
@Module
@InstallIn(SingletonComponent::class)
object DataSourceModule {
    @Provides
    @Singleton
    fun provideRemoteDataSource(
        api: ApiService
    ): XxxRemoteDataSource {
        return XxxRemoteDataSource(api)
    }
}

// 4. ViewModel automatiquement injecté
@HiltViewModel
class XxxViewModel @Inject constructor(
    private val getXxxUseCase: GetXxxUseCase
) : ViewModel() {
    // ...
}
```

---

## 5. Jetpack Compose - Best Practices

### State Hoisting
```kotlin
// ❌ Mauvais : State interne
@Composable
fun Counter() {
    var count by remember { mutableIntStateOf(0) } // Non testable
    Button(onClick = { count++ }) { Text("$count") }
}

// ✅ Bon : State hoisted vers ViewModel
@Composable
fun Counter(
    count: Int,
    onIncrement: () -> Unit, // Event callback
) {
    Button(onClick = onIncrement) { Text("$count") }
}
```

### Collecte du StateFlow
```kotlin
@Composable
fun XxxScreen(viewModel: XxxViewModel = hiltViewModel()) {
    // Collecte du state - recomposition automatique
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    // Gestion des différents états
    when {
        uiState.isLoading -> LoadingIndicator()
        uiState.error != null -> ErrorMessage(uiState.error)
        uiState.data.isNotEmpty() -> DataList(uiState.data)
        else -> EmptyState()
    }
}
```

---

## 6. Navigation Compose

```kotlin
// Navigation setup
@Composable
fun PictetNavGraph(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") { HomeScreen() }
        composable(
            "detail/{itemId}",
            arguments = listOf(navArgument("itemId") { type = NavType.StringType })
        ) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getString("itemId")
            DetailScreen(itemId = itemId)
        }
    }
}

// Navigation depuis un écran
navController.navigate("detail/$itemId")
```

---

## 7. Testing - Pattern à démontrer

### Unit Test ViewModel
```kotlin
@OptIn(ExperimentalCoroutinesApi::class)
class XxxViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
    
    private val getXxxUseCase: GetXxxUseCase = mockk()
    private lateinit var viewModel: XxxViewModel
    
    @Before
    fun setup() {
        viewModel = XxxViewModel(getXxxUseCase)
    }
    
    @Test
    fun `when initialized, should load data`() = runTest {
        // Given
        coEvery { getXxxUseCase() } returns Result.success(listOf(Item("1")))
        
        // When - ViewModel initialized in setup
        advanceUntilIdle()
        
        // Then
        assertEquals(false, viewModel.uiState.value.isLoading)
        assertEquals(1, viewModel.uiState.value.data.size)
    }
}
```

### UI Test Compose
```kotlin
@HiltAndroidTest
class XxxScreenTest {
    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)
    
    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()
    
    @Test
    fun screen_showsList() {
        composeTestRule.setContent {
            PictetTheme {
                XxxScreen()
            }
        }
        
        composeTestRule.onNodeWithText("Expected Text")
            .assertIsDisplayed()
    }
}
```

---

## 8. Phrases Clés pour le Mob Programming

### Quand tu expliques ton code :
- *"J'ai choisi StateFlow car c'est le standard moderne pour Compose"*
- *"Le UseCase encapsule la logique métier, testable indépendamment"*
- *"State hoisting rend le composable réutilisable et testable"*
- *"L'injection de dépendances permet de mocker pour les tests"*
- *"Le repository gère la source de vérité des données"*

### Si tu bloques :
- *"Je vais commencer par écrire le test pour clarifier le comportement attendu"*
- *"Pouvez-vous m'indiquer la direction à prendre ?"*
- *"Je propose d'implémenter d'abord la couche domaine"*

---

## 9. Checklist Pré-Entretien

### Code
- [ ] Projet compile sans erreur (`./gradlew build`)
- [ ] Tests passent (`./gradlew test`)
- [ ] Code coverage > 70%
- [ ] Pas de warnings Lint critiques

### Architecture
- [ ] Clean Architecture (domain/data/presentation)
- [ ] MVVM avec StateFlow
- [ ] Repository Pattern avec interfaces
- [ ] UseCases pour logique métier
- [ ] Hilt DI configuré

### Features Implémentées
- [ ] Appels API réels (jsonplaceholder)
- [ ] Affichage liste avec Compose
- [ ] Navigation list → detail
- [ ] Gestion états Loading/Error/Success
- [ ] Pull-to-refresh (si demandé)
- [ ] Tests unitaires (MockK + Turbine)
- [ ] Tests UI (Compose Testing)

### Compréhension
- [ ] Peux expliquer chaque couche de l'architecture
- [ ] Peux comparer Compose vs Legacy Views
- [ ] Peux expliquer Coroutines vs Callbacks
- [ ] Connais les différences Hilt vs Dagger
- [ ] Comprend le pattern StateFlow vs LiveData

### Mob Programming
- [ ] Peux coder et parler en même temps
- [ ] Accepte le feedback constructivement
- [ ] Pose des questions si bloqué
- [ ] Explique mes choix techniques
- [ ] Pratiqué avec les scenarios fournis

---

## 10. Ressources de Référence

### Documentation officielle
- [Android Architecture Guide](https://developer.android.com/topic/architecture)
- [Compose State Guide](https://developer.android.com/jetpack/compose/state)
- [Hilt Documentation](https://developer.android.com/training/dependency-injection/hilt-android)

### Patterns
- [Now in Android](https://github.com/android/nowinandroid) - Référence officielle
- Architecture MVVM + Compose + Hilt

### Fichiers du Projet
| Fichier | Contenu |
|---------|---------|
| `STEP_BY_STEP_GUIDE.md` | Guide complet d'implémentation |
| `INTERVIEW_CHEATSHEET.md` | Référence rapide (1 page) |
| `BEST_PRACTICES_GUIDE.md` | Checklist de qualité |
| `MOB_PROGRAMMING_SCENARIOS.md` | Scenarios d'entretien |
| `CODE_REVIEW_EXERCISES.md` | Exercices de revue |
| `REFACTORING_CHALLENGES.md` | Défis de refactoring |
| `ARCHITECTURE_DISCUSSIONS.md` | Sujets d'architecture |

### Historique des branches
```
task/1-api-integration
  └── task/2-mvvm-flow
        └── task/3-compose-ui
              └── task/4-hilt-di
                    └── task/5-navigation-component
                          └── task/6-comprehensive-testing (17 tests)
                                └── task/7-mob-programming-scenarios
                                      └── task/1-final-review-and-docs
```

---

**Rappel final** : L'entretien évalue ta capacité à collaborer et à écrire du code propre/maintenable, pas à coder vite. Prends le temps de bien structurer ton code et d'expliquer tes choix.
