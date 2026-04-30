# Code Review Exercises

These exercises simulate reviewing pull requests with intentional issues. Practice identifying problems and suggesting improvements.

---

## Exercise 1: ViewModel Anti-Patterns

### Code to Review

```kotlin
class PostsViewModel : ViewModel() {
    // Problem: Public mutable state
    var posts = mutableListOf<Post>()
    var isLoading = false
    
    fun loadPosts() {
        // Problem: Hardcoded dependency
        val api = Retrofit.Builder()
            .baseUrl("https://api.example.com/")
            .build()
            .create(Api::class.java)
        
        // Problem: Launching coroutine without viewModelScope
        GlobalScope.launch {
            isLoading = true
            val response = api.getPosts()
            posts.addAll(response)
            isLoading = false
        }
    }
    
    // Problem: Manual state management
    fun clearPosts() {
        posts.clear()
    }
}
```

### Issues to Identify

1. **Public mutable state** - Breaks encapsulation, UI can modify state directly
2. **Hardcoded dependencies** - Not testable, violates DI principle
3. **GlobalScope** - Outlives ViewModel, causes memory leaks
4. **No error handling** - Silent failures
5. **No StateFlow/LiveData** - UI won't observe changes automatically

### Suggested Fix

```kotlin
@HiltViewModel
class PostsViewModel @Inject constructor(
    private val getPostsUseCase: GetPostsUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(PostsUiState())
    val uiState: StateFlow<PostsUiState> = _uiState.asStateFlow()
    
    init { loadPosts() }
    
    private fun loadPosts() {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            getPostsUseCase().collect { result ->
                result.fold(
                    onSuccess = { posts ->
                        _uiState.update { it.copy(isLoading = false, posts = posts) }
                    },
                    onFailure = { error ->
                        _uiState.update { it.copy(isLoading = false, error = error.message) }
                    }
                )
            }
        }
    }
}
```

---

## Exercise 2: Compose UI Issues

### Code to Review

```kotlin
@Composable
fun PostsScreen() {
    // Problem: Creating ViewModel incorrectly
    val viewModel = PostsViewModel()
    val posts = viewModel.posts
    
    // Problem: Launching side effect directly
    LaunchedEffect(Unit) {
        viewModel.loadPosts()
    }
    
    LazyColumn {
        items(posts) { post ->
            // Problem: No key for items
            PostCard(post)
        }
    }
}

@Composable
fun PostCard(post: Post) {
    // Problem: Recomposing with unstable type
    var isExpanded by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.clickable { isExpanded = !isExpanded }
    ) {
        // Problem: Heavy computation in composition
        val wordCount = post.body.split(" ").count()
        
        Text(text = "${post.title} ($wordCount words)")
    }
}
```

### Issues to Identify

1. **Manual ViewModel creation** - Use `hiltViewModel()` for DI
2. **Direct collection** - Use `collectAsStateWithLifecycle()`
3. **Side effect in composition** - Should be in ViewModel init
4. **No keys in LazyColumn** - Inefficient recompositions
5. **Unstable class** - `Post` should be `@Immutable` or data class with stable properties
6. **Heavy computation** - Use `remember` to cache calculation

### Suggested Fix

```kotlin
@Composable
fun PostsScreen(
    viewModel: PostsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    LazyColumn {
        items(
            items = uiState.posts,
            key = { it.id }  // Stable key for efficient recomposition
        ) { post ->
            PostCard(post = post)
        }
    }
}

@Immutable  // Mark as stable for Compose
@Composable
fun PostCard(post: Post) {
    var isExpanded by remember { mutableStateOf(false) }
    
    // Cache heavy computation
    val wordCount by remember(post.body) {
        mutableIntStateOf(post.body.split(" ").count())
    }
    
    Card(
        modifier = Modifier.clickable { isExpanded = !isExpanded }
    ) {
        Text(text = "${post.title} ($wordCount words)")
    }
}
```

---

## Exercise 3: Repository Layer Issues

### Code to Review

```kotlin
class PostRepository {
    // Problem: Singleton pattern instead of DI
    companion object {
        @Volatile
        private var instance: PostRepository? = null
        
        fun getInstance(): PostRepository {
            return instance ?: synchronized(this) {
                instance ?: PostRepository().also { instance = it }
            }
        }
    }
    
    private val posts = mutableListOf<Post>()
    
    // Problem: Synchronous blocking call
    fun getPosts(): List<Post> {
        return posts
    }
    
    // Problem: No Result type, throws exceptions
    suspend fun refreshPosts(): Boolean {
        val response = api.getPosts()
        if (response.isSuccessful) {
            posts.clear()
            posts.addAll(response.body()!!)
            return true
        }
        return false
    }
    
    // Problem: No interface, concrete implementation exposed
}
```

### Issues to Identify

1. **Singleton pattern** - Use Hilt for singleton scoping
2. **No Flow/observable pattern** - UI can't observe changes
3. **Blocking calls** - Should return Flow for async operations
4. **No Result type** - Exceptions escape, no error context
5. **No abstraction** - Depends on concrete class, hard to mock
6. **Non-null assertion** - Crashes on null body

### Suggested Fix

```kotlin
// Interface in domain layer
interface PostRepository {
    fun getPosts(): Flow<Result<List<Post>>>
    suspend fun refreshPosts(): Result<Unit>
}

// Implementation in data layer
@Singleton
class PostRepositoryImpl @Inject constructor(
    private val api: JsonPlaceholderApi
) : PostRepository {
    
    private val _cachedPosts = MutableStateFlow<List<Post>>(emptyList())
    
    override fun getPosts(): Flow<Result<List<Post>>> = 
        _cachedPosts.map { posts ->
            if (posts.isEmpty()) {
                Result.failure(IllegalStateException("No cached posts"))
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
            } ?: Result.failure(IllegalStateException("Empty response"))
        } else {
            Result.failure(HttpException(response))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}
```

---

## Exercise 4: Test Issues

### Code to Review

```kotlin
class PostsViewModelTest {
    // Problem: Real dependencies instead of mocks
    private val repository = PostRepository()
    private val useCase = GetPostsUseCase(repository)
    private val viewModel = PostsViewModel(useCase)
    
    @Test
    fun testLoadPosts() {
        // Problem: No test dispatcher
        viewModel.loadPosts()
        
        // Problem: Thread.sleep instead of proper synchronization
        Thread.sleep(1000)
        
        // Problem: Asserting on mutable state directly
        assert(viewModel.posts.size == 100)
    }
    
    // Problem: No cleanup
}
```

### Issues to Identify

1. **Real dependencies** - Tests are slow and flaky
2. **No test dispatcher** - Uses real dispatchers, timing issues
3. **Thread.sleep** - Slow, unreliable
4. **No proper assertions** - Using assert() instead of Assert methods
5. **No state collection** - Not testing StateFlow emissions
6. **No @Before/@After** - No setup/cleanup

### Suggested Fix

```kotlin
@OptIn(ExperimentalCoroutinesApi::class)
class PostsViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    
    @MockK
    private lateinit var getPostsUseCase: GetPostsUseCase
    
    @MockK
    private lateinit var refreshPostsUseCase: RefreshPostsUseCase
    
    private lateinit var viewModel: PostsViewModel
    
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
        assertEquals(samplePosts, state.posts)
        assertFalse(state.isLoading)
    }
}
```

---

## Exercise 5: Architecture Violations

### Code to Review

```kotlin
// In UI layer - DON'T DO THIS
@Composable
fun LoginScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    Button(onClick = {
        // Problem: Business logic in UI
        scope.launch {
            val response = RetrofitClient.api.login(username, password)
            if (response.isSuccessful) {
                // Problem: Navigation logic in UI
                context.startActivity(Intent(context, HomeActivity::class.java))
                // Problem: Direct SharedPreferences access
                context.getSharedPreferences("app", Context.MODE_PRIVATE)
                    .edit()
                    .putString("token", response.body()?.token)
                    .apply()
            } else {
                // Problem: Direct Toast from composable
                Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show()
            }
        }
    }) {
        Text("Login")
    }
}
```

### Issues to Identify

1. **Business logic in UI** - Should be in ViewModel/UseCase
2. **Direct API calls** - Should go through Repository pattern
3. **Navigation in composable** - Use Navigation Component
4. **Direct SharedPreferences** - Use DataStore with Repository
5. **Side effects in composable** - Use ViewModel events

### Suggested Fix

```kotlin
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val tokenRepository: TokenRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState = _uiState.asStateFlow()
    
    private val _events = Channel<LoginEvent>()
    val events = _events.receiveAsFlow()
    
    fun login(username: String, password: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            loginUseCase(username, password)
                .onSuccess { token ->
                    tokenRepository.saveToken(token)
                    _events.send(LoginEvent.NavigateToHome)
                }
                .onFailure { error ->
                    _uiState.update { it.copy(error = error.message) }
                }
        }
    }
}

// In UI
@Composable
fun LoginScreen(
    viewModel: LoginViewModel = hiltViewModel(),
    onNavigateToHome: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Collect events
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is LoginEvent.NavigateToHome -> onNavigateToHome()
            }
        }
    }
    
    // Show errors
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
        }
    }
    
    Button(onClick = { viewModel.login(username, password) }) {
        Text("Login")
    }
}
```

---

## Review Checklist

When reviewing code, check for:

### Architecture
- [ ] Proper separation of concerns
- [ ] Dependencies injected, not created
- [ ] Interface-based repositories
- [ ] Single source of truth

### Android Specific
- [ ] No memory leaks (GlobalScope, listeners)
- [ ] Lifecycle-aware components
- [ ] Proper coroutine scopes

### Compose
- [ ] Stable keys for lazy lists
- [ ] No heavy work in composition
- [ ] Proper state hoisting
- [ ] Stable types for parameters

### Testing
- [ ] Mocked dependencies
- [ ] Test dispatchers
- [ ] Proper assertions
- [ ] Cleanup in @After

### Error Handling
- [ ] Result type or try/catch
- [ ] No silent failures
- [ ] User-friendly error messages
