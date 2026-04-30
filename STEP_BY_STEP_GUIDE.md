# Guide Étape par Étape - Architecture Android Moderne

Ce guide explique comment créer une application Android complète avec appels réseau, depuis un projet vide jusqu'à une architecture MVVM propre.

---

## Étape 1: Créer le projet (No Activity)

```bash
# Dans Android Studio:
# File → New → New Project → "No Activity"
# 
# Configuration:
# - Name: PictetInterview
# - Package: com.bms.pictet
# - Language: Kotlin
# - Minimum SDK: API 24
# - Build configuration: Kotlin DSL (build.gradle.kts)
```

**Pourquoi "No Activity" ?**
- Contrôle total sur la structure
- On apprend chaque composant
- Pas de code généré mystérieux

---

## Étape 2: Configurer Gradle (libs.versions.toml)

### 2.1 Ajouter les versions

```toml
[versions]
agp = "8.10.1"
kotlin = "2.0.21"
coreKtx = "1.18.0"

# Compose
composeBom = "2025.04.01"
activityCompose = "1.10.1"
lifecycleViewmodelCompose = "2.8.7"

# Hilt
hilt = "2.56.1"

# Networking
retrofit = "2.11.0"
okhttp = "4.12.0"
```

**Principe:** Toutes les versions centralisées = mise à jour facilitée.

### 2.2 Déclarer les librairies

```toml
[libraries]
# AndroidX Core
androidx-core-ktx = { group = "androidx.core", name = "core-ktx", version.ref = "coreKtx" }

# Compose BOM (Bill of Materials)
androidx-compose-bom = { group = "androidx.compose", name = "compose-bom", version.ref = "composeBom" }
androidx-compose-ui = { group = "androidx.compose.ui", name = "ui" }
androidx-compose-material3 = { group = "androidx.compose.material3", name = "material3" }

# Hilt
hilt-android = { group = "com.google.dagger", name = "hilt-android", version.ref = "hilt" }
hilt-compiler = { group = "com.google.dagger", name = "hilt-compiler", version.ref = "hilt" }

# Networking
retrofit = { group = "com.squareup.retrofit2", name = "retrofit", version.ref = "retrofit" }
retrofit-converter-gson = { group = "com.squareup.retrofit2", name = "converter-gson", version.ref = "retrofit" }
okhttp-logging = { group = "com.squareup.okhttp3", name = "logging-interceptor", version.ref = "okhttp" }
```

### 2.3 Déclarer les plugins

```toml
[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
compose-compiler = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
hilt = { id = "com.google.dagger.hilt.android", version.ref = "hilt" }
```

---

## Étape 3: Configurer le module app (build.gradle.kts)

```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.hilt)
    id("org.jetbrains.kotlin.kapt")
}

android {
    namespace = "com.bms.pictet"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.bms.pictet"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }

    buildFeatures {
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    // Core
    implementation(libs.androidx.core.ktx)
    
    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.activity.compose)
    
    // Hilt
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)
    
    // Networking
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp.logging)
}
```

**Points clés:**
- `buildFeatures { compose = true }` active Compose
- `kapt` pour l'annotation processing (Hilt)
- BOM gère les versions Compose cohérentes

---

## Étape 4: Créer la structure des packages

```
app/src/main/java/com/bms/pictet/
├── PictetApplication.kt          # Application class avec @HiltAndroidApp
├── MainActivity.kt               # Entry point
├── data/
│   ├── remote/
│   │   ├── api/                # Interfaces Retrofit
│   │   │   └── JsonPlaceholderApi.kt
│   │   └── model/              # DTOs (Data Transfer Objects)
│   │       └── PostDto.kt
│   └── repository/
│       └── PostRepositoryImpl.kt
├── domain/
│   ├── model/                  # Modèles métier
│   │   └── Post.kt
│   ├── repository/             # Interfaces Repository
│   │   └── PostRepository.kt
│   └── usecase/                # Cas d'utilisation
│       └── GetPostsUseCase.kt
├── presentation/
│   ├── screens/                # Composables UI
│   │   └── PostsScreen.kt
│   ├── viewmodels/             # ViewModels
│   │   └── PostsViewModel.kt
│   └── theme/                  # Thème Material3
│       ├── Color.kt
│       ├── Theme.kt
│       └── Type.kt
└── di/                         # Modules Hilt
    ├── NetworkModule.kt
    └── RepositoryModule.kt
```

**Architecture: Clean Architecture + MVVM**
- `domain`: Indépendant, contient la logique métier
- `data`: Implémentation techniques (API, DB)
- `presentation`: UI (Compose)

---

## Étape 5: Configurer Hilt

### 5.1 Application class

```kotlin
// PictetApplication.kt
@HiltAndroidApp
class PictetApplication : Application()
```

**AndroidManifest.xml:**
```xml
<manifest ... >
    <!-- CRITICAL: Internet permission for network calls -->
    <uses-permission android:name="android.permission.INTERNET" />

    <application
        android:name=".PictetApplication"
        ... >
    </application>
</manifest>
```

**Important:** Sans cette permission, les appels réseau échoueront silencieusement!

### 5.2 Module Réseau

```kotlin
// di/NetworkModule.kt
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://jsonplaceholder.typicode.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideApi(retrofit: Retrofit): JsonPlaceholderApi {
        return retrofit.create(JsonPlaceholderApi::class.java)
    }
}
```

**Explication:**
- `@InstallIn(SingletonComponent::class)` = une instance pour toute l'app
- `@Provides` indique comment créer l'instance
- Hilt injecte automatiquement les dépendances

---

## Étape 6: Définir les modèles

### 6.1 DTO (Data Transfer Object)

```kotlin
// data/remote/model/PostDto.kt
data class PostDto(
    val userId: Int,
    val id: Int,
    val title: String,
    val body: String
)
```

**Rôle:** Correspond exactement à la structure JSON de l'API.

### 6.2 Modèle Domain

```kotlin
// domain/model/Post.kt
data class Post(
    val id: Int,
    val userId: Int,
    val title: String,
    val body: String
) {
    // Computed properties
    val displayTitle: String
        get() = title.replaceFirstChar { it.uppercase() }
    
    val excerpt: String
        get() = if (body.length > 100) body.take(100) + "..." else body
}
```

**Pourquoi séparer ?**
- L'API peut changer sans impacter le domaine
- Le domaine ajoute la logique métier
- Testabilité

---

## Étape 7: Créer l'interface API

```kotlin
// data/remote/api/JsonPlaceholderApi.kt
interface JsonPlaceholderApi {

    @GET("posts")
    suspend fun getPosts(): Response<List<PostDto>>

    @GET("posts/{id}")
    suspend fun getPostById(@Path("id") id: Int): Response<PostDto>
}
```

**Points importants:**
- `suspend` = fonction asynchrone (coroutine)
- `Response<T>` = wrapper Retrofit avec code HTTP
- `@GET`, `@Path` = annotations pour construire l'URL

---

## Étape 8: Implémenter le Repository

### 8.1 Interface

```kotlin
// domain/repository/PostRepository.kt
interface PostRepository {
    fun getPosts(): Flow<Result<List<Post>>>
    suspend fun refreshPosts(): Result<Unit>
}
```

### 8.2 Implémentation

```kotlin
// data/repository/PostRepositoryImpl.kt
@Singleton
class PostRepositoryImpl @Inject constructor(
    private val api: JsonPlaceholderApi
) : PostRepository {

    private val _posts = MutableStateFlow<List<Post>>(emptyList())

    override fun getPosts(): Flow<Result<List<Post>>> {
        return _posts.map { posts ->
            if (posts.isNotEmpty()) {
                Result.success(posts)
            } else {
                Result.failure(IllegalStateException("No data"))
            }
        }
    }

    override suspend fun refreshPosts(): Result<Unit> {
        return try {
            val response = api.getPosts()
            
            if (response.isSuccessful) {
                val posts = response.body()?.map { it.toDomain() } ?: emptyList()
                _posts.value = posts
                Result.success(Unit)
            } else {
                Result.failure(Exception("HTTP ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun PostDto.toDomain(): Post {
        return Post(id, userId, title, body)
    }
}
```

**Pattern:**
- `Flow` = réactivité (UI se met à jour automatiquement)
- `Result<T>` = gestion d'erreurs type-safe
- Cache en mémoire avec `MutableStateFlow`

---

## Étape 9: Créer le UseCase

```kotlin
// domain/usecase/GetPostsUseCase.kt
class GetPostsUseCase @Inject constructor(
    private val repository: PostRepository
) {
    operator fun invoke(): Flow<Result<List<Post>>> {
        return repository.getPosts()
    }
}
```

**Rôle:**
- Encapsule une opération métier
- Peut contenir la logique de transformation
- Réutilisable entre ViewModels

---

## Étape 10: Créer le ViewModel

```kotlin
// presentation/viewmodels/PostsViewModel.kt
@HiltViewModel
class PostsViewModel @Inject constructor(
    private val getPostsUseCase: GetPostsUseCase,
    private val refreshPostsUseCase: RefreshPostsUseCase  // AJOUTÉ pour le chargement initial
) : ViewModel() {

    private val _uiState = MutableStateFlow(PostsUiState())
    val uiState: StateFlow<PostsUiState> = _uiState.asStateFlow()

    init {
        loadPosts()          // 1. Démarrer l'observation du cache
        refreshInitialData() // 2. Charger depuis le réseau (CRITIQUE!)
    }

    /**
     * Démarrer la collecte du Flow depuis Repository.
     * Le Flow émet les données du cache (mises à jour automatiques).
     */
    private fun loadPosts() {
        _uiState.update { it.copy(isLoading = true) }
        
        viewModelScope.launch {
            getPostsUseCase().collect { result ->
                result.fold(
                    onSuccess = { posts ->
                        _uiState.update {
                            it.copy(isLoading = false, posts = posts)
                        }
                    },
                    onFailure = { error ->
                        _uiState.update {
                            it.copy(isLoading = false, error = error.message)
                        }
                    }
                )
            }
        }
    }

    /**
     * Déclencher le chargement initial depuis le réseau.
     * SANS CET APPEL: le cache reste vide et on affiche une erreur!
     */
    private fun refreshInitialData() {
        viewModelScope.launch {
            refreshPostsUseCase()
            // Le résultat arrive via le Flow grâce au cache mis à jour
        }
    }
}

data class PostsUiState(
    val isLoading: Boolean = false,
    val posts: List<Post> = emptyList(),
    val error: String? = null
)
```

### ⚠️ ERREUR COMMUNE: Oublier le chargement initial!

**Problème:** Si vous ne déclenchez pas `refreshPostsUseCase()`, le cache reste vide et le Flow émet immédiatement `Result.failure("No cached data")`.

**Solution:** Toujours appeler le refresh dans `init` après avoir démarré la collecte du Flow.

**Principes:**
- `MutableStateFlow` privé (modification uniquement dans ViewModel)
- `StateFlow` public (UI observe)
- `viewModelScope` = lifecycle-aware (auto-cancel quand ViewModel détruit)
- `update { it.copy(...) }` = mise à jour immuable thread-safe

---

## Étape 11: Créer l'écran Compose

```kotlin
// presentation/screens/PostsScreen.kt
@Composable
fun PostsScreen(
    viewModel: PostsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    // UI selon état
    when {
        uiState.isLoading -> LoadingContent()
        uiState.error != null -> ErrorContent(uiState.error)
        else -> PostsList(uiState.posts)
    }
}

@Composable
private fun PostsList(posts: List<Post>) {
    LazyColumn {
        items(posts, key = { it.id }) { post ->
            PostCard(post)
        }
    }
}

@Composable
private fun PostCard(post: Post) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = post.displayTitle, style = MaterialTheme.typography.titleMedium)
            Text(text = post.excerpt, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
```

**Patterns Compose:**
- `collectAsStateWithLifecycle()` = optimise pour Android lifecycle
- `key = { it.id }` = stable key pour performance LazyColumn
- Composables stateless = testables et réutilisables

---

## Étape 12: Lier tout dans MainActivity

```kotlin
// MainActivity.kt
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            PictetTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    PostsScreen()
                }
            }
        }
    }
}
```

---

## Points Clés de l'Architecture

### 1. **Séparation des couches**
- Domain ne dépend de rien
- Data dépend de Domain (implémente interfaces)
- Presentation dépend de Domain

### 2. **Injection de dépendances**
- Hilt fournit les instances
- Facile à tester (on injecte des mocks)
- Pas de `new` manuel

### 3. **Gestion d'état**
- Un seul `StateFlow` par écran
- Immuable (copie avec `copy()`)
- Unidirectional Data Flow (UDF)

### 4. **Gestion d'erreurs**
- `Result<T>` au lieu d'exceptions
- UI affiche état d'erreur
- Retry possible

### 5. **Réactivité**
- `Flow` = stream de données
- UI se met à jour automatiquement
- Pas besoin de callbacks manuels

---

## Tests

### Test du ViewModel

```kotlin
@Test
fun `when initialized, should load posts`() = runTest {
    // Given
    val fakePosts = listOf(Post(1, 1, "Title", "Body"))
    val useCase = FakeGetPostsUseCase(fakePosts)
    
    // When
    val viewModel = PostsViewModel(useCase)
    
    // Then
    assertEquals(fakePosts, viewModel.uiState.value.posts)
}
```

---

## Commandes utiles

```bash
# Build
./gradlew build

# Tests unitaires
./gradlew test

# Installation debug
./gradlew installDebug

# Clean
./gradlew clean
```

---

## Checklist finale

- [ ] Appels réseau fonctionnels
- [ ] Chargement affiché
- [ ] Erreurs gérées avec retry
- [ ] Données affichées dans une liste
- [ ] Architecture MVVM respectée
- [ ] Hilt configure correctement
- [ ] Code sans `!!` (null-safety)
- [ ] Composables stateless
- [ ] Tests passent

---

## Étape 13: Ajouter Navigation Component

### 13.1 Définir les routes

```kotlin
// presentation/navigation/NavigationRoutes.kt
object NavigationRoutes {
    const val POSTS_LIST = "posts_list"
    const val POST_DETAIL = "post_detail/{postId}"
    
    fun postDetailRoute(postId: Int) = "post_detail/$postId"
}

object NavigationArguments {
    const val POST_ID = "postId"
}
```

### 13.2 Créer le NavGraph

```kotlin
// presentation/navigation/NavGraph.kt
@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: String = NavigationRoutes.POSTS_LIST
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(NavigationRoutes.POSTS_LIST) {
            PostsListScreen(
                onPostClick = { postId ->
                    navController.navigate(NavigationRoutes.postDetailRoute(postId))
                }
            )
        }
        
        composable(
            route = NavigationRoutes.POST_DETAIL,
            arguments = listOf(
                navArgument(NavigationArguments.POST_ID) {
                    type = NavType.IntType
                }
            )
        ) { backStackEntry ->
            val postId = backStackEntry.arguments?.getInt(NavigationArguments.POST_ID)
                ?: throw IllegalArgumentException("postId is required")
            
            PostDetailScreen(
                postId = postId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
```

### 13.3 Intégrer dans MainActivity

```kotlin
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            PictetTheme {
                val navController = rememberNavController()
                NavGraph(navController = navController)
            }
        }
    }
}
```

**Points clés:**
- Routes en constantes pour éviter les typos
- Arguments type-safe avec `NavType`
- `popBackStack()` pour navigation arrière
- Composables stateless avec callbacks

---

## Étape 14: Ajouter Tests Complets

### 14.1 Configurer les dépendances de test

```toml
# gradle/libs.versions.toml
[versions]
mockk = "1.14.0"
coroutinesTest = "1.9.0"
turbine = "1.1.0"

[libraries]
mockk = { group = "io.mockk", name = "mockk", version.ref = "mockk" }
coroutines-test = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-test", version.ref = "coroutinesTest" }
turbine = { group = "app.cash.turbine", name = "turbine", version.ref = "turbine" }

# Bundles
[plugins]
android-test = { id = "com.android.test", version.ref = "agp" }
```

### 14.2 Test du ViewModel avec Turbine

```kotlin
// test/presentation/viewmodels/PostsViewModelTest.kt
@OptIn(ExperimentalCoroutinesApi::class)
class PostsViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    
    @MockK
    private lateinit var getPostsUseCase: GetPostsUseCase
    
    @MockK
    private lateinit var refreshPostsUseCase: RefreshPostsUseCase
    
    @Before
    fun setup() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(testDispatcher)
    }
    
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
    
    @Test
    fun `when initialized, should load posts successfully`() = runTest {
        // Given
        every { getPostsUseCase() } returns flowOf(Result.success(samplePosts))
        coEvery { refreshPostsUseCase() } returns Result.success(Unit)
        
        // When
        viewModel = PostsViewModel(getPostsUseCase, refreshPostsUseCase)
        advanceUntilIdle()
        
        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(samplePosts, state.posts)
    }
}
```

### 14.3 Test UI avec Compose

```kotlin
// androidTest/presentation/screens/PostsListScreenTest.kt
class PostsListScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()
    
    @Test
    fun screen_withPosts_shouldDisplayPostTitles() {
        // Given
        val posts = listOf(
            Post(id = 1, userId = 1, title = "Test Post", body = "Body")
        )
        
        // When
        composeTestRule.setContent {
            PictetTheme {
                PostsListContent(
                    uiState = PostsUiState(posts = posts),
                    onPostClick = {},
                    onRefresh = {},
                    onRetry = {}
                )
            }
        }
        
        // Then
        composeTestRule.onNodeWithText("Test Post").assertIsDisplayed()
    }
}
```

### 14.4 Stratégie de tests

| Type | Couche | Outils | Quantité |
|------|--------|--------|----------|
| **Unit** | ViewModel, UseCase | JUnit, MockK, Turbine | 70% |
| **Integration** | Repository | MockWebServer, Room | 20% |
| **UI** | Screens | Compose Test, Espresso | 10% |

---

## Checklist finale complète

### Architecture
- [ ] Clean Architecture avec 3 couches (domain/data/presentation)
- [ ] MVVM avec StateFlow unidirectional
- [ ] Repository Pattern avec abstraction
- [ ] UseCases pour logique métier

### UI
- [ ] Jetpack Compose avec Material3
- [ ] Navigation Component avec type-safe args
- [ ] Gestion d'état Loading/Error/Success
- [ ] Composables stateless et réutilisables

### DI & Networking
- [ ] Hilt configuré avec modules
- [ ] Retrofit avec logging interceptor
- [ ] API réelle (jsonplaceholder)
- [ ] Gestion d'erreurs avec Result type

### Tests
- [ ] Unit tests: ViewModel (MockK + Turbine)
- [ ] Unit tests: Repository (coroutines-test)
- [ ] Unit tests: UseCase (MockK)
- [ ] UI tests: Compose screens
- [ ] 17+ tests passent

### Qualité
- [ ] Pas de `!!` (null-safety)
- [ ] Pas de GlobalScope (coroutines)
- [ ] Pas de memory leaks
- [ ] Code documenté

---

**Ressources additionnelles:**
- `MOB_PROGRAMMING_SCENARIOS.md` - Pratiques d'interview
- `CODE_REVIEW_EXERCISES.md` - Exercices de revue
- `REFACTORING_CHALLENGES.md` - Défis de refactoring
- `ARCHITECTURE_DISCUSSIONS.md` - Sujets d'architecture