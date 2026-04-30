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

1. When would you choose MVI over MVVM?
2. How do you handle navigation events in MVI?
3. What about side effects (snackbar, toast) in MVI?

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

1. Should Repository expose Flow or suspend functions?
2. Where should caching logic live?
3. How do you handle data mapping (DTO ↔ Entity ↔ Domain)?

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

1. Where should retry logic live?
2. How do you handle partial failures?
3. What about global error handling (e.g., 401 logout)?

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

1. How do you handle deep links?
2. Where should navigation logic live? (ViewModel vs UI)
3. How to handle bottom navigation with state preservation?

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

1. What's the right balance of test types?
2. How do you test navigation?
3. Should you test Compose previews?

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

1. How do you profile Compose recomposition?
2. When should you use derivedStateOf?
3. How to handle large lists efficiently?

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

1. How do you convince stakeholders to allow migration time?
2. What metrics show migration success?
3. How to handle mixed architectures during transition?

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
