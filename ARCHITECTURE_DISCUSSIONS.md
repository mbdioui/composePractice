# Architecture Discussion Topics

Common interview questions about Android architecture decisions.

---

## Topic 1: MVVM vs MVI

### MVVM (Model-View-ViewModel)

**Current Project Choice**

```kotlin
// State is exposed as multiple streams
class PostsViewModel : ViewModel() {
    val posts: StateFlow<List<Post>>
    val isLoading: StateFlow<Boolean>
    val error: StateFlow<String?>
    
    fun onRefresh()
    fun onPostClick(post: Post)
    fun onRetry()
}
```

**Pros:**
- Simple and widely understood
- Flexible state management
- Works well with Compose

**Cons:**
- Multiple state sources
- Can have inconsistent UI state

---

### MVI (Model-View-Intent)

**Alternative Approach**

```kotlin
// Single state object, sealed class events
sealed class PostsEvent {
    data object Refresh : PostsEvent()
    data class PostClick(val postId: Int) : PostsEvent()
    data object Retry : PostsEvent()
}

data class PostsState(
    val posts: List<Post> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class PostsViewModel : ViewModel() {
    val state: StateFlow<PostsState>
    
    fun onEvent(event: PostsEvent) // Single entry point
}
```

**Pros:**
- Single source of truth
- Predictable state changes
- Easier to debug

**Cons:**
- More boilerplate
- Can be overkill for simple screens

---

### Discussion Questions

**1. When would you choose MVI over MVVM?**

**Answer:** Choose MVI when you have **complex state machines**, need **time-travel debugging**, or work with a **large team requiring strict patterns**.

**When MVI shines:**
- **Complex state transitions:** Banking apps with states (loading → authenticated → locked → error)
- **Undo/redo functionality:** Time-travel debugging through sealed class events
- **Large teams:** Strict pattern prevents inconsistent implementations
- **Predictable state:** Every action creates a new state deterministically

**When MVVM is better:**
- Simple CRUD screens (lists, forms)
- Rapid prototyping
- Small teams where flexibility helps
- Most Android apps (industry standard)

**Example decision:**
```kotlin
// Simple list screen - MVVM is fine
class PostsViewModel : ViewModel() {
    val uiState: StateFlow<PostsUiState>
    fun onRefresh()
    fun onPostClick(post: Post)
}

// Complex trading app with many states - MVI better
sealed class TradingState {
    data object Loading : TradingState()
    data class Active(val portfolio: Portfolio) : TradingState()
    data class Suspended(val reason: String) : TradingState()
    data class Error(val message: String) : TradingState()
}
```

---

**2. How do you handle navigation events in MVI?**

**Answer:** Navigation is a **side effect**—use a separate event channel or handle in UI layer.

**Approach 1: Event Channel (recommended)**
```kotlin
class PostsViewModel : ViewModel() {
    private val _events = Channel<PostsEvent>()
    val events = _events.receiveAsFlow()
    
    fun onPostClick(postId: Int) {
        viewModelScope.launch {
            _events.send(PostsEvent.NavigateToDetail(postId))
        }
    }
}

// In UI
LaunchedEffect(Unit) {
    viewModel.events.collect { event ->
        when (event) {
            is PostsEvent.NavigateToDetail -> {
                navController.navigate("detail/${event.postId}")
            }
        }
    }
}
```

**Approach 2: State with handled flag**
```kotlin
data class PostsState(
    val posts: List<Post> = emptyList(),
    val navigateToDetail: Int? = null // null = no navigation
)

// After navigation
viewModel.onNavigationHandled() // Sets navigateToDetail back to null
```

**Key principle:** Navigation is platform-specific (Android), keep it in UI layer. ViewModel emits events, UI handles navigation.

---

**3. What about side effects (snackbar, toast) in MVI?**

**Answer:** Side effects are **one-time events** that shouldn't be in state. Use a `Channel<SideEffect>`.

**Why not put in state?**
- State change persists → Snackbar would show on every recomposition
- Side effects are transient (one-time)
- State should be idempotent (same state = same UI)

**Implementation:**
```kotlin
sealed class PostsSideEffect {
    data class ShowSnackbar(val message: String) : PostsSideEffect()
    data class ShowToast(val message: String) : PostsSideEffect()
    data class Navigate(val route: String) : PostsSideEffect()
}

class PostsViewModel : ViewModel() {
    private val _sideEffects = Channel<PostsSideEffect>()
    val sideEffects = _sideEffects.receiveAsFlow()
    
    fun onRefresh() {
        viewModelScope.launch {
            try {
                refreshPosts()
            } catch (e: Exception) {
                _sideEffects.send(PostsSideEffect.ShowSnackbar("Refresh failed"))
            }
        }
    }
}

// In UI
LaunchedEffect(Unit) {
    viewModel.sideEffects.collect { effect ->
        when (effect) {
            is PostsSideEffect.ShowSnackbar -> {
                scaffoldState.showSnackbar(effect.message)
            }
        }
    }
}
```

**Best practice:** Keep side effects separate from state. They are consumed once and gone.

---

## Topic 2: Repository Pattern

### Interface vs Concrete Implementation

```kotlin
// Domain Layer (shared between layers)
interface PostRepository {
    fun getPosts(): Flow<Result<List<Post>>>
    suspend fun refreshPosts(): Result<Unit>
    suspend fun getPostById(id: Int): Result<Post>
}

// Data Layer (implementation detail)
class PostRepositoryImpl @Inject constructor(
    private val api: JsonPlaceholderApi,
    private val dao: PostDao // Room for offline-first
) : PostRepository {
    
    override fun getPosts() = dao.getPosts()
        .map { entities ->
            Result.success(entities.map { it.toDomain() })
        }
        .catch { emit(Result.failure(it)) }
    
    override suspend fun refreshPosts() = try {
        val response = api.getPosts()
        if (response.isSuccessful) {
            response.body()?.let { posts ->
                dao.insertAll(posts.map { it.toEntity() })
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

### Discussion Questions

**1. Should Repository expose Flow or suspend functions?**

**Answer:** 
- **`Flow`** for data that changes over time (observable data streams)
- **`suspend`** for one-time operations (mutations, single requests)

**When to use each:**

| Return Type | Use Case | Example |
|-------------|----------|---------|
| `Flow<T>` | Observable data, cached data, reactive streams | `getPosts(): Flow<List<Post>>` |
| `suspend fun` | One-time operations, mutations | `createPost(post): Result<Post>` |
| `suspend fun` | Refresh/sync operations | `refreshPosts(): Result<Unit>` |

**Why this distinction matters:**
- **Flow:** UI observes continuously, receives updates automatically
- **suspend:** Single execution, one result, done

**Example:**
```kotlin
interface PostRepository {
    // Flow = observable, UI collects and gets updates
    fun getPosts(): Flow<Result<List<Post>>>
    
    // Suspend = one-time, trigger a refresh
    suspend fun refreshPosts(): Result<Unit>
    
    // Suspend = one-time, create and return result
    suspend fun createPost(post: Post): Result<Post>
}
```

**Real-world analogy:**
- Flow = Live TV stream (keeps coming)
- Suspend = Requesting a photo print (one request, one result)

---

**2. Where should caching logic live?**

**Answer:** **In the Repository.** Repository coordinates between remote and local data sources.

**Why not elsewhere:**
- **ViewModel:** Shouldn't know if data is cached or fresh
- **UseCase:** Should focus on business logic, not data storage
- **UI:** Definitely not—platform-agnostic logic only

**Repository responsibilities:**
```kotlin
class PostRepositoryImpl(
    private val remote: PostRemoteDataSource,
    private val local: PostLocalDataSource
) : PostRepository {
    
    override fun getPosts(): Flow<List<Post>> = 
        local.getPosts() // Always observe local (single source of truth)
    
    override suspend fun refreshPosts(): Result<Unit> {
        return try {
            val posts = remote.fetchPosts()
            local.savePosts(posts) // Cache to local
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

**Pattern:**
```
ViewModel → Repository → Local (Room) ← Remote (API)
                ↓
           Coordinates sync
```

---

**3. How do you handle data mapping (DTO ↔ Entity ↔ Domain)?**

**Answer:** Map at **layer boundaries**. Repository handles all mapping.

**Data flow:**
```
API Layer          Repository Layer           Domain/UI Layer
┌─────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   PostDto   │ →  │ PostDto.toDomain │ →  │ Post (domain)   │
│  (JSON)     │    │    mapping       │    │  (UI uses this) │
└─────────────┘    └──────────────────┘    └─────────────────┘

Database Layer     Repository Layer           Domain/UI Layer
┌─────────────┐    ┌──────────────────┐    ┌─────────────────┐
│ PostEntity  │ →  │ PostEntity.toDomain│→  │ Post (domain)   │
│  (Room)     │    │    mapping         │    │  (UI uses this) │
└─────────────┘    └──────────────────┘    └─────────────────┘
```

**Implementation:**
```kotlin
// In RepositoryImpl - Extension functions for mapping

// DTO → Domain
private fun PostDto.toDomain(): Post = Post(
    id = id,
    userId = userId,
    title = title,
    body = body
)

// Entity → Domain  
private fun PostEntity.toDomain(): Post = Post(
    id = id,
    userId = userId,
    title = title,
    body = body
)

// Domain → Entity
private fun Post.toEntity(): PostEntity = PostEntity(
    id = id,
    userId = userId,
    title = title,
    body = body,
    cachedAt = System.currentTimeMillis()
)
```

**Why this matters:**
- UI and ViewModel only work with Domain models
- API changes don't affect Domain layer (just mapping)
- Database schema changes don't affect Domain layer (just mapping)
- Easy to swap implementations (different APIs, different DBs)

---

## Topic 3: Use Cases (Interactors)

### To Use or Not to Use

**With Use Cases:**

```kotlin
// Domain layer - business logic isolated
class GetPostsUseCase @Inject constructor(
    private val repository: PostRepository
) {
    operator fun invoke(): Flow<Result<List<Post>>> =
        repository.getPosts()
            .map { result ->
                result.map { posts ->
                    posts.sortedBy { it.title } // Business rule
                }
            }
}

class RefreshPostsUseCase @Inject constructor(
    private val repository: PostRepository
) {
    suspend operator fun invoke(): Result<Unit> {
        // Could add analytics, logging, etc.
        return repository.refreshPosts()
    }
}

// In ViewModel
@HiltViewModel
class PostsViewModel @Inject constructor(
    private val getPostsUseCase: GetPostsUseCase,
    private val refreshPostsUseCase: RefreshPostsUseCase
) : ViewModel()
```

**Without Use Cases (Direct Repository):**

```kotlin
@HiltViewModel
class PostsViewModel @Inject constructor(
    private val repository: PostRepository
) : ViewModel() {
    // Simpler, but business logic leaks to ViewModel
}
```

### When to Use Use Cases

| Scenario | Recommendation |
|----------|----------------|
| Simple CRUD | Direct repository access |
| Multiple repositories needed | Use Case for coordination |
| Complex business rules | Use Case for isolation |
| Reusable operations | Use Case for sharing |
| Team > 5 people | Use Cases for clear boundaries |

---

## Topic 4: Error Handling Strategy

### Levels of Error Handling

```kotlin
// Level 1: Repository - Convert exceptions to Result
class PostRepositoryImpl : PostRepository {
    override suspend fun refreshPosts(): Result<Unit> = try {
        // API call
    } catch (e: IOException) {
        Result.failure(NetworkException("No internet connection"))
    } catch (e: HttpException) {
        Result.failure(when (e.code()) {
            401 -> UnauthorizedException()
            404 -> NotFoundException()
            in 500..599 -> ServerException()
            else -> UnknownException()
        })
    }
}

// Level 2: ViewModel - Convert to UI messages
class PostsViewModel : ViewModel() {
    private fun handleError(error: Throwable): String {
        return when (error) {
            is NetworkException -> "Please check your internet connection"
            is UnauthorizedException -> "Session expired. Please login again"
            is ServerException -> "Server error. Please try again later"
            else -> "Something went wrong"
        }
    }
}

// Level 3: UI - Display error appropriately
@Composable
fun ErrorContent(message: String, onRetry: () -> Unit) {
    when {
        message.contains("internet") -> NetworkErrorCard(message, onRetry)
        message.contains("Session") -> UnauthorizedDialog(message)
        else -> GenericErrorCard(message, onRetry)
    }
}
```

### Discussion Questions

**1. Where should retry logic live?**

**Answer:** **ViewModel** decides whether to retry; Repository reports errors; UseCase may have retry policy.

**Layer responsibilities:**

| Layer | Retry Responsibility |
|-------|---------------------|
| Repository | No retry—just report what happened |
| UseCase | Optional retry policy (e.g., 3 attempts with exponential backoff) |
| ViewModel | Decides whether to show retry UI and handles user-initiated retry |
| UI | Shows retry button, calls ViewModel.onRetry() |

**Example with UseCase retry:**
```kotlin
class RefreshPostsUseCase(
    private val repository: PostRepository
) {
    suspend operator fun invoke(): Result<Unit> {
        repeat(3) { attempt ->
            val result = repository.refreshPosts()
            if (result.isSuccess) return result
            
            if (attempt < 2) { // Don't delay after last attempt
                delay(1000L * (attempt + 1)) // Exponential backoff: 1s, 2s
            }
        }
        return repository.refreshPosts() // Final attempt
    }
}
```

**User-initiated retry (ViewModel):**
```kotlin
fun onRetry() {
    _uiState.update { it.copy(error = null, isLoading = true) }
    viewModelScope.launch {
        val result = refreshPostsUseCase()
        result.fold(
            onSuccess = { /* Update state */ },
            onFailure = { error ->
                _uiState.update { it.copy(error = error.message) }
            }
        )
    }
}
```

---

**2. How do you handle partial failures?**

**Answer:** Return **partial success** with metadata about what failed.

**Scenario:** Syncing 10 photos—8 succeed, 2 fail.

**Options:**

| Strategy | When to Use | Result |
|----------|-------------|--------|
| Partial success | Most cases | Return success + list of failures |
| All-or-nothing | Critical operations | Fail entire operation if any part fails |
| Silent ignore | Background non-critical | Log failure, continue |

**Example (partial success):**
```kotlin
data class SyncResult(
    val succeeded: List<Photo>,
    val failed: List<FailedPhoto>
)

data class FailedPhoto(
    val photo: Photo,
    val error: Throwable
)

class SyncPhotosUseCase {
    suspend operator fun invoke(photos: List<Photo>): Result<SyncResult> {
        val succeeded = mutableListOf<Photo>()
        val failed = mutableListOf<FailedPhoto>()
        
        photos.forEach { photo ->
            try {
                uploadPhoto(photo)
                succeeded.add(photo)
            } catch (e: Exception) {
                failed.add(FailedPhoto(photo, e))
            }
        }
        
        return Result.success(SyncResult(succeeded, failed))
    }
}

// In ViewModel
result.fold(
    onSuccess = { syncResult ->
        if (syncResult.failed.isNotEmpty()) {
            // Show "8 uploaded, 2 failed" with retry option
        }
    }
)
```

---

**3. What about global error handling (e.g., 401 logout)?**

**Answer:** Use OkHttp **Authenticator** or **Interceptor** to catch 401s globally, then broadcast via event.

**Implementation:**
```kotlin
class AuthInterceptor(
    private val tokenRepository: TokenRepository
) : Interceptor {
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .addHeader("Authorization", "Bearer ${tokenRepository.getToken()}")
            .build()
        
        val response = chain.proceed(request)
        
        if (response.code == 401) {
            // Broadcast logout event
            GlobalEventBus.post(AuthEvent.SessionExpired)
            
            // Clear local token
            tokenRepository.clearToken()
        }
        
        return response
    }
}

// In Activity
lifecycleScope.launch {
    GlobalEventBus.events.collect { event ->
        when (event) {
            is AuthEvent.SessionExpired -> {
                navController.navigate("login") {
                    popUpTo(0) { inclusive = true } // Clear back stack
                }
            }
        }
    }
}
```

**Why global handling?**
- Don't repeat logout logic in every Repository
- Consistent behavior across app
- Single place to modify auth flow

---

## Topic 5: Navigation Architecture

### Compose Navigation Approaches

**Type-Safe Navigation (Recommended):**

```kotlin
// Navigation Routes
sealed class Screen(val route: String) {
    data object Posts : Screen("posts")
    data object PostDetail : Screen("post/{postId}") {
        fun createRoute(postId: Int) = "post/$postId"
    }
}

// Type-safe arguments
fun NavGraphBuilder.postDetailScreen(
    onNavigateBack: () -> Unit
) {
    composable(
        route = Screen.PostDetail.route,
        arguments = listOf(
            navArgument("postId") { type = NavType.IntType }
        )
    ) { backStackEntry ->
        val postId = backStackEntry.arguments?.getInt("postId") ?: return@composable
        PostDetailScreen(
            postId = postId,
            onNavigateBack = onNavigateBack
        )
    }
}
```

### Discussion Questions

**1. How do you handle deep links?**

**Answer:** Navigation Component supports deep links via `navDeepLink`.

**Implementation:**
```kotlin
// Define deep link pattern
composable(
    route = "post/{postId}",
    deepLinks = listOf(
        navDeepLink {
            uriPattern = "https://myapp.com/post/{postId}"
        }
    ),
    arguments = listOf(
        navArgument("postId") { type = NavType.IntType }
    )
) { backStackEntry ->
    val postId = backStackEntry.arguments?.getInt("postId")
    PostDetailScreen(postId = postId)
}
```

**AndroidManifest setup:**
```xml
<activity android:name=".MainActivity"
    android:exported="true"
    android:launchMode="singleTask"
    android:windowSoftInputMode="adjustResize"
    android:theme="@style/Theme.App"
    android:screenOrientation="unspecified"
    tools:ignore="DiscouragedApi">
    
    <intent-filter>
        <action android:name="android.intent.action.MAIN" />
        <category android:name="android.intent.category.LAUNCHER" />
    </intent-filter>
    
    <!-- Deep link intent filter -->
    <intent-filter android:autoVerify="true">
        <action android:name="android.intent.action.VIEW" />
        <category android:name="android.intent.category.DEFAULT" />
        <category android:name="android.intent.category.BROWSABLE" />
        <data android:scheme="https"
              android:host="myapp.com"
              android:pathPrefix="/post" />
    </intent-filter>
</activity>
```

**How it works:**
- User clicks link in browser → Opens your app
- Navigation Component parses URL
- Extracts arguments (postId)
- Navigates to correct screen

---

**2. Where should navigation logic live? (ViewModel vs UI)**

**Answer:** **UI layer** handles navigation. ViewModel emits **events**, UI calls `navController.navigate()`.

**Why not in ViewModel:**
- Navigation is platform-specific (Android)
- ViewModel should be testable without Android framework
- Navigation is a side effect, not state

**Correct pattern:**
```kotlin
// ViewModel emits events (navigation commands)
class PostsViewModel : ViewModel() {
    private val _events = Channel<NavigationEvent>()
    val events = _events.receiveAsFlow()
    
    fun onPostClick(postId: Int) {
        viewModelScope.launch {
            _events.send(NavigationEvent.NavigateToDetail(postId))
        }
    }
}

// UI handles navigation
@Composable
fun PostsScreen(
    viewModel: PostsViewModel = hiltViewModel(),
    navController: NavController
) {
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is NavigationEvent.NavigateToDetail -> {
                    navController.navigate("post/${event.postId}")
                }
            }
        }
    }
}
```

**Alternative (simpler):** Pass callback to composable
```kotlin
PostsScreen(
    onPostClick = { postId -> 
        navController.navigate("post/$postId") 
    }
)
```

---

**3. How to handle bottom navigation with state preservation?**

**Answer:** Use **separate NavHost** per tab with Navigation Component's `rememberNavController`.

**Implementation:**
```kotlin
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val bottomNavController = rememberNavController()
    
    Scaffold(
        bottomBar = { 
            BottomNavigation {
                BottomNavigationItem(
                    selected = currentTab == Tab.HOME,
                    onClick = { 
                        bottomNavController.navigate("home") {
                            // Save state
                            popUpTo(bottomNavController.graph.startDestinationId) {
                                saveState = true
                            }
                            // Avoid multiple copies
                            launchSingleTop = true
                            // Restore state
                            restoreState = true
                        }
                    },
                    icon = { Icon(Icons.Default.Home, null) },
                    label = { Text("Home") }
                )
                // More tabs...
            }
        }
    ) { padding ->
        NavHost(
            navController = bottomNavController,
            startDestination = "home",
            modifier = Modifier.padding(padding)
        ) {
            composable("home") { HomeScreen() }
            composable("profile") { ProfileScreen() }
            composable("settings") { SettingsScreen() }
        }
    }
}
```

**Key options explained:**
- `saveState = true`: Remember scroll position and form input
- `restoreState = true`: Restore that state when returning to tab
- `launchSingleTop = true`: Don't create duplicate destinations

---

## Topic 6: State Management at Scale

### Hierarchical State

```kotlin
// App-level state (very stable)
data class AppState(
    val theme: ThemeMode,
    val isLoggedIn: Boolean,
    val user: User?
)

// Screen-level state (frequently changes)
data class PostsUiState(
    val posts: List<Post>,
    val isLoading: Boolean,
    val error: String?
)

// Component-level state (most volatile)
@Composable
fun ExpandableCard(post: Post) {
    var isExpanded by remember { mutableStateOf(false) }
    // Local state only, doesn't leave component
}
```

### State Hoisting Principle

```kotlin
// Before: State in child
@Composable
fun Counter() {
    var count by remember { mutableIntStateOf(0) }
    Button(onClick = { count++ }) {
        Text("Count: $count")
    }
}

// After: State hoisted to parent
@Composable
fun Counter(count: Int, onIncrement: () -> Unit) {
    Button(onClick = onIncrement) {
        Text("Count: $count")
    }
}

// Parent owns state
@Composable
fun CounterScreen() {
    var count by remember { mutableIntStateOf(0) }
    Counter(
        count = count,
        onIncrement = { count++ }
    )
}
```

---

## Topic 7: Testing Strategy

### Test Pyramid

```
    /\
   /  \  E2E Tests (few, slow)
  /____\  UI Tests (Compose tests)
 /      \  Integration Tests (Repository + API)
/________\  Unit Tests (many, fast)
```

### What to Test at Each Layer

```kotlin
// Unit Test: ViewModel
@Test
fun `when refresh fails, should show error`() = runTest {
    coEvery { refreshPostsUseCase() } returns Result.failure(error)
    
    viewModel.onRefresh()
    advanceUntilIdle()
    
    assertEquals(error.message, viewModel.uiState.value.error)
}

// Integration Test: Repository
@Test
fun `repository should fetch from API and cache`() = runTest {
    coEvery { api.getPosts() } returns Response.success(postDtos)
    
    repository.refreshPosts()
    
    repository.getPosts().test {
        assertEquals(expectedPosts, awaitItem().getOrNull())
    }
}

// UI Test: Screen
@Test
fun `when error shown, should display retry button`() {
    composeTestRule.setContent {
        PostsContent(uiState = PostsUiState(error = "Failed"))
    }
    
    composeTestRule.onNodeWithText("Retry").assertIsDisplayed()
}
```

### Discussion Questions

**1. What's the right balance of test types?**

**Answer:** Follow the **Test Pyramid**: 70% unit tests, 20% integration tests, 10% UI tests.

**The Pyramid:**
```
    /\        E2E Tests (1-2 tests per critical flow)
   /  \       Slow, flaky, expensive
  /____\      UI Tests (Compose tests)
 /      \     Medium speed
/________\    Unit Tests (many, fast)
              Fast, reliable, cheap
```

**Distribution by type:**

| Test Type | % | What to Test | Tools |
|-----------|---|--------------|-------|
| **Unit** | 70% | ViewModels, UseCases, business logic | JUnit, MockK |
| **Integration** | 20% | Repository with real DB/API | Room, MockWebServer |
| **UI** | 10% | Critical user flows | Compose Testing |

**Why this balance:**
- Unit tests are fast (<1 second), isolated, reliable
- UI tests are slow (>10 seconds), flaky, expensive to maintain
- Integration tests catch real bugs that unit tests miss

**Example for a screen:**
- 5-8 unit tests for ViewModel (loading, success, error states)
- 2-3 integration tests for Repository (cache, network, error)
- 1-2 UI tests for critical flows (load screen → tap item → navigate)

---

**2. How do you test navigation?**

**Answer:** Test that **ViewModel emits correct events**. Don't test the actual navigation (framework code).

**Approach 1: Test ViewModel events**
```kotlin
@Test
fun `when post clicked, should emit navigate event`() = runTest {
    // Given
    viewModel = PostsViewModel(repository)
    
    // When
    viewModel.onPostClick(123)
    
    // Then
    val event = viewModel.events.first()
    assertTrue(event is NavigationEvent.NavigateToDetail)
    assertEquals(123, event.postId)
}
```

**Approach 2: UI test with real navigation**
```kotlin
@AndroidComposeTest
fun `when post clicked, should navigate to detail`() {
    composeTestRule.setContent {
        val navController = rememberTestNavController()
        NavHost(navController = navController) {
            composable("posts") { PostsScreen(navController) }
            composable("post/{id}") { /* detail */ }
        }
        
        // Navigate to posts
        navController.navigate("posts")
        
        // Click post
        composeTestRule.onNodeWithText("Post 1").performClick()
        
        // Verify navigation happened
        assertEquals("post/1", navController.currentDestination?.route)
    }
}
```

**Best practice:** Unit test the ViewModel (fast). Use UI tests sparingly for critical flows only.

---

**3. Should you test Compose previews?**

**Answer:** **No.** Previews are for development, not testing.

**What are previews for:**
- Visual development (see UI without running app)
- Design iteration
- Documentation

**What to test instead:**
- Test the **actual composable function** with different parameters
- Test state combinations
- Test user interactions

**Example:**
```kotlin
// ❌ Don't test @Preview
@Preview
@Composable
fun PostsScreenPreview() { /* ... */ }

// ✅ Test the actual composable
@Test
fun `posts screen with data should show list`() {
    composeTestRule.setContent {
        PostsContent(
            uiState = PostsUiState(posts = samplePosts),
            onPostClick = {},
            onRefresh = {},
            onRetry = {}
        )
    }
    
    composeTestRule.onNodeWithText("Post 1").assertIsDisplayed()
}
```

**Previews are visual tools, not testable code.**

---

## Topic 8: Performance Optimization

### Compose Performance

```kotlin
// Problem: Unstable parameters cause recomposition
@Composable
fun PostList(posts: List<Post>) { // List is unstable
    LazyColumn {
        items(posts) { post ->
            PostCard(post) // Recomposes even if post unchanged
        }
    }
}

// Solution 1: Immutable data classes
@Immutable
data class Post(
    val id: Int,
    val title: String,
    val body: String
)

// Solution 2: Stable keys
LazyColumn {
    items(
        items = posts,
        key = { it.id } // Only recompose if id changes
    ) { post ->
        PostCard(post)
    }
}

// Solution 3: Remember expensive calculations
@Composable
fun PostCard(post: Post) {
    // Cached unless post.body changes
    val wordCount by remember(post.body) {
        mutableIntStateOf(post.body.split(" ").count())
    }
}
```

### Discussion Questions

**1. How do you profile Compose recomposition?**

**Answer:** Use **Layout Inspector** in Android Studio with the **Recomposition Counts** feature.

**Steps:**
1. Run app on device/emulator
2. Open Layout Inspector (Tools → Layout Inspector)
3. Enable "Show Recomposition Counts" 
4. Interact with UI and watch the numbers

**What to look for:**
- **Red/high numbers:** Component is recomposing too often
- "Recomposition happened but parameters unchanged" → optimization opportunity
- Skipping recomposition is good (shown in green)

**Example:**
```
PostCard                    // 5 recompositions
  ├─ Title: "Hello"        // 5 recompositions
  └─ Body: "World"         // 5 recompositions
```

If PostCard recomposes 5 times but title/body never changed → you need `@Immutable` or `key`.

**Code fixes based on profiling:**
```kotlin
// If Post recomposes unnecessarily
@Immutable  // Add this
data class Post(val id: Int, val title: String, val body: String)

// If LazyColumn items recompose unnecessarily
items(
    items = posts,
    key = { it.id }  // Add stable keys
) { post ->
    PostCard(post)
}
```

---

**2. When should you use derivedStateOf?**

**Answer:** Use `derivedStateOf` when you calculate something from **frequently changing state**, but the **result rarely changes**.

**Classic use case:** Scroll position → derived boolean
```kotlin
@Composable
fun LazyListWithScrollToTop() {
    val listState = rememberLazyListState()
    
    // ✅ CORRECT: Use derivedStateOf
    // scrollPosition changes constantly (every pixel)
    // showScrollToTop changes rarely (only at threshold)
    val showScrollToTop by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 5
        }
    }
    
    // Without derivedStateOf: Every pixel scroll would recompose this component
    // With derivedStateOf: Only recomposes when showScrollToTop changes (true/false)
    
    if (showScrollToTop) {
        ScrollToTopButton(onClick = { /* scroll up */ })
    }
}
```

**Another example:**
```kotlin
val isScrolled by remember {
    derivedStateOf {
        scrollState.value > 0  // true/false rarely changes
    }
}
```

**When NOT to use:**
```kotlin
// ❌ DON'T: Calculate from stable state
val wordCount by remember {
    derivedStateOf { post.body.split(" ").count() }
}
// Just use: remember(post.body) { ... }
```

---

**3. How to handle large lists efficiently?**

**Answer:** Four techniques for lists with 100+ items:

**A. Use LazyColumn (not Column)**
```kotlin
// ❌ BAD: Loads all items into memory
Column {
    posts.forEach { PostCard(it) }
}

// ✅ GOOD: Only composes visible items
LazyColumn {
    items(posts, key = { it.id }) { PostCard(it) }
}
```

**B. Add keys for stability**
```kotlin
items(
    items = posts,
    key = { it.id }  // Prevents unnecessary recomposition
) { post ->
    PostCard(post)
}
```

**C. Use paging for 1000+ items**
```kotlin
val pager = Pager(PagingConfig(pageSize = 20)) {
    PostPagingSource(api)
}.flow

LazyColumn {
    items(lazyPagingItems) { post ->
        post?.let { PostCard(it) }
    }
}
```

**D. Optimize item composables**
```kotlin
// Mark data class as stable
@Immutable
data class Post(...)

// Use remember for expensive calculations
@Composable
fun PostCard(post: Post) {
    val wordCount = remember(post.body) {
        post.body.split(" ").count()
    }
}
```

**Performance comparison:**

| List Size | Column | LazyColumn | LazyColumn + Paging |
|-----------|--------|------------|---------------------|
| 10 items | ✅ Good | ✅ Good | Overkill |
| 100 items | ❌ Laggy | ✅ Good | Optional |
| 1000 items | ❌ Crash | ⚠️ Slow start | ✅ Good |
| 10000 items | ❌ OOM | ❌ Memory | ✅ Good |

---

## Topic 9: Modularization Strategy

### Module Types

```
app/                    # Application module
├── feature-posts/      # Feature module
├── feature-profile/    # Feature module
├── core-common/       # Shared utilities
├── core-ui/           # Shared UI components
├── core-network/      # Networking layer
├── core-database/     # Database layer
└── domain/            # Domain models (shared)
```

### Dependency Rules

```kotlin
// Domain module has no dependencies
// All modules depend on domain

// Feature modules don't depend on each other
// They communicate via domain or events

// App module depends on all feature modules
// Orchestrates navigation
```

---

## Topic 10: Migration Strategies

### Legacy to Modern Android

**Incremental Migration:**

```kotlin
// Step 1: Add Compose alongside XML
class PostsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Keep existing XML
        setContentView(R.layout.activity_posts)
        
        // Add Compose for new feature
        findViewById<ComposeView>(R.id.compose_view).setContent {
            NewFeature()
        }
    }
}

// Step 2: Migrate screens one by one
// Step 3: Migrate to Navigation Component
// Step 4: Remove XML entirely
```

### Discussion Questions

**1. How do you convince stakeholders to allow migration time?**

**Answer:** Frame it as **developer velocity investment**, not technical debt.

**The business case:**

| Metric | Before Migration | After Migration | Impact |
|--------|------------------|-----------------|--------|
| New feature time | 2 weeks | 1 week | **2x faster** |
| Bug reports | 10/week | 2/week | **80% fewer bugs** |
| Developer onboarding | 4 weeks | 1 week | **4x faster ramp** |
| Build time | 5 min | 2 min | **60% faster builds** |

**How to present:**
```
"Instead of saying 'We need to refactor to MVVM...'

Say: 'Investing 2 weeks now will make new features 
      50% faster to ship, reducing time-to-market 
      and developer costs.'"
```

**What executives care about:**
- **Speed to market** (ship features faster)
- **Quality** (fewer crashes, better ratings)
- **Team scaling** (new devs productive faster)
- **Maintenance cost** (less time fixing bugs)

**Tactics:**
- Start with one screen as proof-of-concept
- Show before/after metrics
- Propose migration during feature work ("As we build X, let's modernize Y")
- Bundle with necessary work ("To add dark mode, we need to migrate theming")

---

**2. What metrics show migration success?**

**Answer:** Track these metrics before and after migration:

**Build/Developer Metrics:**
```kotlin
// Build time (faster is better)
Before: ./gradlew build = 5 min 30 sec
After:  ./gradlew build = 2 min 10 sec

// Lines of legacy code
Before: 50,000 lines XML/Fragments
After:  5,000 lines XML (90% reduction)

// Code coverage
Before: 45% coverage
After:  78% coverage
```

**Product Metrics:**
- **Crash rate** (should decrease)
- **ANR rate** (Application Not Responding)
- **App startup time** (cold start milliseconds)
- **Play Store rating** (indirect metric)

**Team Metrics:**
- **Feature development velocity** (story points per sprint)
- **Time to fix bugs** (hours from report to fix)
- **Developer satisfaction** (surveys)
- **Onboarding time** (days to first PR)

**Example dashboard:**
```
Migration Progress Dashboard

┌─────────────────────────────────────────────┐
│ Legacy Code Remaining: 30% ▼ (from 100%)   │
│ Test Coverage: 76% ▲ (from 42%)            │
│ Build Time: 2.1 min ▼ (from 5.8 min)      │
│ Crash Rate: 0.5% ▼ (from 2.3%)             │
└─────────────────────────────────────────────┘
```

---

**3. How to handle mixed architectures during transition?**

**Answer:** **Feature-by-feature migration.** Never big-bang.

**The Strategy:**
```
Phase 1: Add Compose alongside existing XML (1-2 sprints)
         - New features in Compose
         - Old features stay XML
         
Phase 2: Migrate high-value screens (2-3 sprints)
         - Most used screens first
         - Maintain both architectures
         
Phase 3: Migrate remaining screens (ongoing)
         - As you touch code for bug fixes
         - Migrate screen by screen
         
Phase 4: Remove old architecture (when 100% migrated)
         - Delete XML layouts
         - Remove legacy dependencies
```

**Bridging old and new:**

**A. XML hosts Compose:**
```kotlin
// In existing XML layout
<androidx.compose.ui.platform.ComposeView
    android:id="@+id/compose_view"
    android:layout_width="match_parent"
    android:layout_height="wrap_content" />

// In Fragment/Activity
findViewById<ComposeView>(R.id.compose_view).setContent {
    NewFeatureInCompose()
}
```

**B. Compose hosts XML:**
```kotlin
@Composable
fun LegacyFeatureWrapper() {
    AndroidView(
        factory = { context ->
            LayoutInflater.from(context)
                .inflate(R.layout.legacy_layout, null)
        },
        update = { view ->
            // Update legacy view
        }
    )
}
```

**C. Shared ViewModel pattern:**
```kotlin
// Both XML and Compose can use same ViewModel
class PostsViewModel : ViewModel() {
    val uiState: StateFlow<PostsUiState>
}

// XML Fragment
viewModel.uiState.observe(viewLifecycleOwner) { /* update views */ }

// Compose
val uiState by viewModel.uiState.collectAsStateWithLifecycle()
```

**Golden rule:**
- Never migrate without tests
- Maintain feature parity (no regression)
- Allow rollback (keep old code until new is stable)

---

## Quick Reference: Architecture Decisions

| Decision | When to Choose |
|----------|----------------|
| **Use Cases** | Complex business logic, multiple repos, team > 5 |
| **MVI** | Complex state machines, need time-travel debugging |
| **MVVM** | Most cases, simpler, works well with Compose |
| **Repository Interface** | Always (testability, swapping implementations) |
| **Offline First** | User expects app to work without internet |
| **Pagination** | Lists > 50 items |
| **Module per Feature** | Team > 3, parallel development |
