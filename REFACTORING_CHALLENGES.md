# Refactoring Challenges

Practice refactoring messy code into clean, maintainable solutions.

---

## Challenge 1: God Object Refactoring

### Before (God Repository)

```kotlin
class GodRepository {
    suspend fun getPosts(): List<Post>
    suspend fun getPost(id: Int): Post
    suspend fun createPost(post: Post): Post
    suspend fun deletePost(id: Int): Boolean
    suspend fun getUsers(): List<User>
    suspend fun getUser(id: Int): User
    suspend fun getComments(): List<Comment>
    suspend fun getCommentsForPost(postId: Int): List<Comment>
    suspend fun createComment(comment: Comment): Comment
    suspend fun login(username: String, password: String): Token
    suspend fun logout(): Boolean
    suspend fun getUserProfile(): UserProfile
    suspend fun updateUserProfile(profile: UserProfile): UserProfile
    suspend fun uploadImage(image: File): String
    suspend fun downloadFile(url: String): File
    suspend fun syncData(): Boolean
    suspend fun clearCache()
    suspend fun getSettings(): Settings
    suspend fun updateSettings(settings: Settings)
}
```

### Issues
- Violates Single Responsibility Principle
- 500+ lines of code
- Hard to test
- Changes for one feature require touching this giant class

### After (Split by Feature)

```kotlin
// PostRepository.kt
interface PostRepository {
    fun getPosts(): Flow<Result<List<Post>>>
    suspend fun getPost(id: Int): Result<Post>
    suspend fun createPost(post: Post): Result<Post>
    suspend fun deletePost(id: Int): Result<Unit>
}

// UserRepository.kt
interface UserRepository {
    fun getUsers(): Flow<Result<List<User>>>
    suspend fun getUser(id: Int): Result<User>
    suspend fun getProfile(): Result<UserProfile>
    suspend fun updateProfile(profile: UserProfile): Result<UserProfile>
}

// CommentRepository.kt
interface CommentRepository {
    fun getComments(): Flow<Result<List<Comment>>>
    fun getCommentsForPost(postId: Int): Flow<Result<List<Comment>>>
    suspend fun createComment(comment: Comment): Result<Comment>
}

// AuthRepository.kt
interface AuthRepository {
    suspend fun login(username: String, password: String): Result<Token>
    suspend fun logout(): Result<Unit>
    suspend fun isAuthenticated(): Boolean
}

// SettingsRepository.kt
interface SettingsRepository {
    fun getSettings(): Flow<Settings>
    suspend fun updateSettings(settings: Settings)
}
```

### Refactoring Steps

1. **Extract interfaces** for each domain area
2. **Create separate implementations** for each interface
3. **Update DI modules** to provide new repositories
4. **Migrate incrementally** - don't refactor everything at once
5. **Update tests** for each extracted repository

---

## Challenge 2: Callback Hell to Coroutines

### Before (Callback-based)

```kotlin
class PostRepository {
    fun getPosts(callback: (Result<List<Post>>) -> Unit) {
        api.getPosts().enqueue(object : Callback<List<PostDto>> {
            override fun onResponse(call: Call<List<PostDto>>, response: Response<List<PostDto>>) {
                if (response.isSuccessful) {
                    val posts = response.body()?.map { it.toDomain() }
                    if (posts != null) {
                        saveToCache(posts) { success ->
                            if (success) {
                                callback(Result.success(posts))
                            } else {
                                callback(Result.failure(CacheException()))
                            }
                        }
                    } else {
                        callback(Result.failure(NullBodyException()))
                    }
                } else {
                    callback(Result.failure(HttpException(response)))
                }
            }

            override fun onFailure(call: Call<List<PostDto>>, t: Throwable) {
                getFromCache { cachedPosts ->
                    if (cachedPosts != null) {
                        callback(Result.success(cachedPosts))
                    } else {
                        callback(Result.failure(t))
                    }
                }
            }
        })
    }

    private fun saveToCache(posts: List<Post>, callback: (Boolean) -> Unit) { /* ... */ }
    private fun getFromCache(callback: (List<Post>?) -> Unit) { /* ... */ }
}
```

### After (Coroutine-based)

```kotlin
class PostRepository(
    private val api: JsonPlaceholderApi,
    private val cache: PostCache
) {
    suspend fun refreshPosts(): Result<Unit> = try {
        val response = api.getPosts()
        if (response.isSuccessful) {
            response.body()?.let { posts ->
                cache.save(posts.map { it.toDomain() })
                Result.success(Unit)
            } ?: Result.failure(IllegalStateException("Empty body"))
        } else {
            Result.failure(HttpException(response))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    fun getPosts(): Flow<Result<List<Post>>> = cache.getPosts()
        .map { posts ->
            if (posts.isEmpty()) {
                Result.failure(IllegalStateException("No cached posts"))
            } else {
                Result.success(posts)
            }
        }
}
```

### Refactoring Steps

1. **Add suspend modifier** to Retrofit API interface
2. **Replace Callback with suspend** function returns
3. **Use try/catch** for error handling instead of onFailure
4. **Return Result type** for explicit error handling
5. **Use Flow** for observable data streams

---

## Challenge 3: Imperative to Declarative UI

### Before (Imperative XML-style)

```kotlin
class PostsActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PostsAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var errorView: TextView
    private lateinit var viewModel: PostsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_posts)

        recyclerView = findViewById(R.id.recyclerView)
        progressBar = findViewById(R.id.progressBar)
        errorView = findViewById(R.id.errorView)

        adapter = PostsAdapter { post ->
            navigateToDetail(post.id)
        }
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        viewModel = ViewModelProvider(this).get(PostsViewModel::class.java)

        viewModel.posts.observe(this) { posts ->
            adapter.submitList(posts)
            recyclerView.visibility = View.VISIBLE
            progressBar.visibility = View.GONE
        }

        viewModel.isLoading.observe(this) { loading ->
            progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(this) { error ->
            error?.let {
                errorView.text = it
                errorView.visibility = View.VISIBLE
            } ?: run {
                errorView.visibility = View.GONE
            }
        }

        findViewById<Button>(R.id.retryButton).setOnClickListener {
            viewModel.loadPosts()
        }

        viewModel.loadPosts()
    }
}
```

### After (Declarative Compose)

```kotlin
@Composable
fun PostsScreen(
    viewModel: PostsViewModel = hiltViewModel(),
    onPostClick: (Int) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    PostsContent(
        uiState = uiState,
        onPostClick = onPostClick,
        onRefresh = viewModel::onRefresh,
        onRetry = viewModel::onRetry
    )
}

@Composable
fun PostsContent(
    uiState: PostsUiState,
    onPostClick: (Int) -> Unit,
    onRefresh: () -> Unit,
    onRetry: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        when {
            uiState.showLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            uiState.error != null && !uiState.hasPosts -> {
                ErrorState(
                    message = uiState.error,
                    onRetry = onRetry,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            else -> {
                PullToRefreshBox(
                    isRefreshing = uiState.isRefreshing,
                    onRefresh = onRefresh
                ) {
                    LazyColumn {
                        items(
                            items = uiState.posts,
                            key = { it.id }
                        ) { post ->
                            PostCard(
                                post = post,
                                onClick = { onPostClick(post.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}
```

### Refactoring Steps

1. **Identify state** that drives UI (loading, error, posts)
2. **Create single UiState** class combining all states
3. **Replace View visibility** with conditional composables
4. **Extract reusable components** (PostCard, ErrorState)
5. **Hoist state** to ViewModel, pass events up

---

## Challenge 4: Mutable State to Immutable StateFlow

### Before (LiveData with mutations)

```kotlin
class PostsViewModel(private val repository: PostRepository) : ViewModel() {
    private val _posts = MutableLiveData<MutableList<Post>>(mutableListOf())
    val posts: LiveData<MutableList<Post>> = _posts

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    fun addPost(post: Post) {
        _posts.value?.add(post) // Mutation!
        _posts.value = _posts.value // Trigger update
    }

    fun updatePost(id: Int, newTitle: String) {
        val post = _posts.value?.find { it.id == id }
        post?.title = newTitle // Mutation of item!
        _posts.value = _posts.value
    }

    fun removePost(id: Int) {
        _posts.value?.removeAll { it.id == id }
        _posts.value = _posts.value
    }

    fun loadPosts() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val result = repository.getPosts()
                _posts.value = result.toMutableList()
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }
}
```

### After (StateFlow with immutable updates)

```kotlin
data class PostsUiState(
    val posts: List<Post> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class PostsViewModel @Inject constructor(
    private val getPostsUseCase: GetPostsUseCase,
    private val refreshPostsUseCase: RefreshPostsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(PostsUiState())
    val uiState: StateFlow<PostsUiState> = _uiState.asStateFlow()

    init { loadPosts() }

    private fun loadPosts() {
        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            getPostsUseCase().collect { result ->
                result.fold(
                    onSuccess = { posts ->
                        _uiState.update {
                            it.copy(isLoading = false, posts = posts, error = null)
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

    fun addPost(post: Post) {
        _uiState.update { current ->
            current.copy(posts = current.posts + post) // New list
        }
    }

    fun updatePost(id: Int, update: (Post) -> Post) {
        _uiState.update { current ->
            current.copy(
                posts = current.posts.map { post ->
                    if (post.id == id) update(post) else post
                }
            )
        }
    }

    fun removePost(id: Int) {
        _uiState.update { current ->
            current.copy(posts = current.posts.filter { it.id != id })
        }
    }
}
```

### Refactoring Steps

1. **Combine multiple states** into single UiState data class
2. **Make Post immutable** (val properties only)
3. **Use copy()** for updates instead of mutation
4. **Replace LiveData** with StateFlow for consistency with Flow
5. **Use update { }** for atomic state changes

---

## Challenge 5: Manual DI to Hilt

### Before (Manual DI with Service Locator)

```kotlin
object ServiceLocator {
    private var retrofit: Retrofit? = null
    private var api: JsonPlaceholderApi? = null
    private var repository: PostRepository? = null

    fun provideRetrofit(): Retrofit {
        return retrofit ?: Retrofit.Builder()
            .baseUrl("https://jsonplaceholder.typicode.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .also { retrofit = it }
    }

    fun provideApi(): JsonPlaceholderApi {
        return api ?: provideRetrofit().create(JsonPlaceholderApi::class.java)
            .also { api = it }
    }

    fun provideRepository(): PostRepository {
        return repository ?: PostRepositoryImpl(provideApi())
            .also { repository = it }
    }

    fun clear() {
        retrofit = null
        api = null
        repository = null
    }
}

// In Activity
class PostsActivity : AppCompatActivity() {
    private val viewModel: PostsViewModel by lazy {
        PostsViewModel(ServiceLocator.provideRepository())
    }
}
```

### After (Hilt DI)

```kotlin
// NetworkModule.kt
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

// RepositoryModule.kt
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindPostRepository(
        impl: PostRepositoryImpl
    ): PostRepository
}

// ViewModel
@HiltViewModel
class PostsViewModel @Inject constructor(
    private val getPostsUseCase: GetPostsUseCase,
    private val refreshPostsUseCase: RefreshPostsUseCase
) : ViewModel() { /* ... */ }

// In Activity
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    // ViewModel injected automatically
}
```

### Refactoring Steps

1. **Create @Module classes** for different layers
2. **Add @Provides methods** for third-party dependencies
3. **Use @Binds** for interface-to-implementation bindings
4. **Annotate Application** with @HiltAndroidApp
5. **Annotate Activities** with @AndroidEntryPoint
6. **Use @HiltViewModel** for ViewModels

---

## Refactoring Best Practices

### Before Refactoring
- [ ] Ensure existing tests pass
- [ ] Understand the code thoroughly
- [ ] Identify the "smell" you're fixing
- [ ] Plan incremental steps

### During Refactoring
- [ ] Make small, focused changes
- [ ] Run tests after each step
- [ ] Use IDE refactoring tools (Extract, Rename, etc.)
- [ ] Commit frequently

### After Refactoring
- [ ] All tests pass
- [ ] Code is more readable
- [ ] No behavior changes (pure refactoring)
- [ ] Update documentation

---

## Signs You Need Refactoring

1. **Duplicated code** - Copy-paste patterns
2. **Long methods** - 50+ lines
3. **Large classes** - 500+ lines
4. **Feature envy** - Method uses more of another class
5. **Shotgun surgery** - Small change requires many files
6. **Divergent change** - One class changes for different reasons
