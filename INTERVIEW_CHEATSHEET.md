# Interview Cheatsheet - Quick Reference

One-page reference for key concepts and code patterns.

---

## Architecture Quick Reference

```
┌─────────────────────────────────────────────────────────────┐
│                    PRESENTATION LAYER                        │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐        │
│  │   Screens    │  │  ViewModels  │  │    Theme     │        │
│  │  (Compose)   │← │  (StateFlow) │  │  (Material3) │        │
│  └──────────────┘  └──────────────┘  └──────────────┘        │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│                     DOMAIN LAYER                             │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐        │
│  │    Models    │  │ Repositories │  │  UseCases    │        │
│  │    (Post)    │  │ (Interfaces) │  │(GetPostsUC)  │        │
│  └──────────────┘  └──────────────┘  └──────────────┘        │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│                      DATA LAYER                              │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐        │
│  │     API      │  │     DTOs     │  │ Repositories │        │
│  │  (Retrofit)  │  │  (PostDto)   │  │    (Impl)    │        │
│  └──────────────┘  └──────────────┘  └──────────────┘        │
└─────────────────────────────────────────────────────────────┘
```

---

## StateFlow Pattern

```kotlin
// ViewModel State Management
class PostsViewModel @Inject constructor(
    private val getPostsUseCase: GetPostsUseCase
) : ViewModel() {
    
    // Private mutable
    private val _uiState = MutableStateFlow(PostsUiState())
    
    // Public immutable  
    val uiState: StateFlow<PostsUiState> = _uiState.asStateFlow()
    
    init { loadPosts() }
    
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
}

// UI Usage
@Composable
fun PostsScreen(viewModel: PostsViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    when {
        uiState.isLoading -> LoadingScreen()
        uiState.error != null -> ErrorScreen(uiState.error)
        else -> PostsList(uiState.posts)
    }
}
```

---

## Hilt DI Pattern

```kotlin
// Application
@HiltAndroidApp
class PictetApplication : Application()

// Module - Third party
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit = Retrofit.Builder()
        .baseUrl("https://jsonplaceholder.typicode.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    
    @Provides
    @Singleton
    fun provideApi(retrofit: Retrofit): JsonPlaceholderApi =
        retrofit.create(JsonPlaceholderApi::class.java)
}

// Module - Interface binding
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindPostRepository(impl: PostRepositoryImpl): PostRepository
}

// Injection points
@HiltViewModel
class PostsViewModel @Inject constructor(
    private val getPostsUseCase: GetPostsUseCase
) : ViewModel()

@AndroidEntryPoint
class MainActivity : ComponentActivity()
```

---

## Repository Pattern

```kotlin
// Interface (Domain layer)
interface PostRepository {
    fun getPosts(): Flow<Result<List<Post>>>
    suspend fun refreshPosts(): Result<Unit>
}

// Implementation (Data layer)
@Singleton
class PostRepositoryImpl @Inject constructor(
    private val api: JsonPlaceholderApi
) : PostRepository {
    
    private val _cachedPosts = MutableStateFlow<List<Post>>(emptyList())
    
    override fun getPosts(): Flow<Result<List<Post>>> = 
        _cachedPosts.map { posts ->
            if (posts.isEmpty()) {
                Result.failure(IllegalStateException("No cached data"))
            } else {
                Result.success(posts)
            }
        }
    
    override suspend fun refreshPosts(): Result<Unit> = try {
        val response = api.getPosts()
        if (response.isSuccessful) {
            response.body()?.let { posts ->
                _cachedPosts.value = posts.map { it.toDomain() }
                Result.success(Unit)
            } ?: Result.failure(IllegalStateException("Empty body"))
        } else {
            Result.failure(HttpException(response))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}
```

---

## Testing Pattern

```kotlin
// ViewModel Test
@OptIn(ExperimentalCoroutinesApi::class)
class PostsViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    
    @MockK
    private lateinit var getPostsUseCase: GetPostsUseCase
    
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
    fun `when initialized, should load posts`() = runTest {
        // Given
        every { getPostsUseCase() } returns flowOf(Result.success(samplePosts))
        
        // When
        val viewModel = PostsViewModel(getPostsUseCase, refreshUseCase)
        advanceUntilIdle()
        
        // Then
        assertEquals(samplePosts, viewModel.uiState.value.posts)
    }
}
```

---

## Common Interview Questions

### Q: MVVM vs MVI?
**A:** MVVM is more flexible with multiple state streams; MVI has single state object and sealed class events. MVVM works well for most Android apps, MVI better for complex state machines.

### Q: Why StateFlow over LiveData?
**A:** StateFlow is part of Kotlin coroutines, supports Flow operators, has better compose integration with `collectAsStateWithLifecycle()`.

### Q: Repository with Flow or suspend?
**A:** Flow for observable data that changes (getPosts), suspend for one-time operations (refreshPosts, createPost).

### Q: Error handling strategy?
**A:** Repository returns `Result<T>`, ViewModel converts to user messages, UI displays error state with retry.

### Q: When to use UseCases?
**A:** When you have complex business logic, need to coordinate multiple repositories, or want to share logic between ViewModels.

### Q: Compose recomposition optimization?
**A:** Use `key` in LazyColumn, mark classes as `@Immutable`, hoist state, use `remember` for expensive calculations.

---

## Key Numbers

| Concept | Value |
|---------|-------|
| Min SDK | 24 (Android 7) |
| Compile SDK | 36 |
| Kotlin | 2.0.21 |
| Java | 17 |
| AGP | 8.10.1 |
| Hilt | 2.56.1 |
| Retrofit | 2.11.0 |
| MockK | 1.14.0 |
| Coroutines | 1.9.0 |

---

## Quick Commands

```bash
# Build
./gradlew build

# Run tests
./gradlew testDebugUnitTest

# Single test
./gradlew testDebugUnitTest --tests "PostsViewModelTest"

# Install
./gradlew installDebug
```

---

## Branch History

```
main (empty project)
  └── task/1-api-integration
        └── task/2-mvvm-flow
              └── task/3-compose-ui
                    └── task/4-hilt-di
                          └── task/5-navigation-component
                                └── task/6-comprehensive-testing
                                      └── task/7-mob-programming-scenarios
                                            └── task/1-final-review-and-docs (current)
```

**All branches compile and pass tests.**
