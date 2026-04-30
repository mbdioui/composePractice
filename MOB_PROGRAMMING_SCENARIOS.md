# Mob Programming Scenarios - Interview Practice

This document contains scenarios for practicing mob programming, a common interview format at Pictet Technologies.

## What is Mob Programming?

Mob programming is pair programming with more people:
- **Driver**: Types the code (one person)
- **Navigator(s)**: Guide the driver (everyone else)
- **Rotate every 5-10 minutes**
- **Whole team owns the code**

## Interview Format Expectations

During a Senior Android interview, you may be asked to:
1. Add a feature to existing codebase
2. Fix a bug with the team
3. Refactor code collaboratively
4. Design a new component

Key skills being assessed:
- Communication and collaboration
- Technical decision making
- Code quality awareness
- Testing practices
- Architecture understanding

---

## Scenario 1: Add Pull-to-Refresh Feature

### Context
The Posts list screen currently loads data on startup but doesn't allow users to refresh manually.

### Task
Implement pull-to-refresh functionality using Material3's `PullToRefreshBox`.

### Discussion Points

**1. Where should the refresh state be managed?**

**Answer:** In the **ViewModel as part of UiState** with a dedicated `isRefreshing: Boolean` property.

**Explanation:**
- The ViewModel is the single source of truth for all UI state
- `isRefreshing` is separate from `isLoading`—refresh shows inline spinner, loading shows full-screen spinner
- UI just observes and displays; ViewModel decides when refresh starts/stops

**Example:**
```kotlin
data class PostsUiState(
    val isLoading: Boolean = false,    // Full screen loading
    val isRefreshing: Boolean = false, // Inline pull-to-refresh indicator
    val posts: List<Post> = emptyList(),
    val error: String? = null
)
```

---

**2. How to show loading indicator without blocking the list?**

**Answer:** Use `isRefreshing` with `PullToRefreshBox` instead of blocking the entire screen.

**Explanation:**
- `isLoading` → Full-screen spinner (used when no data exists yet)
- `isRefreshing` → Inline indicator at top (used when updating existing data)
- The list remains visible and scrollable during refresh

**Real-world example:** Gmail shows a rotating spinner at the top when you pull down, but you can still scroll through your emails while it's refreshing.

---

**3. What if the refresh fails but we have cached data?**

**Answer:** Show cached data + error banner. Never clear existing data on refresh failure.

**Explanation:**
- Users prefer seeing old data over seeing nothing or an error screen
- Show a subtle error banner (not a full-screen error) saying "Couldn't refresh"
- Allow pull-to-refresh again to retry

**Real-world example:** Twitter shows old tweets with a small red banner "No connection" when refresh fails—you can still read existing content.

---

**4. Should we show an error snackbar?**

**Answer:** Yes, but only if the user **initiated** the action (pull-to-refresh). Don't show for automatic background refreshes.

**Explanation:**
- Manual action (user pulls down) → Show Snackbar with retry
- Automatic action (app refreshes on startup) → Silently keep old data
- This prevents annoying the user with errors they didn't cause

**Example:**
```kotlin
// In ViewModel
fun onRefresh() {
    _uiState.update { it.copy(isRefreshing = true) }
    viewModelScope.launch {
        val result = refreshPostsUseCase()
        result.fold(
            onSuccess = { 
                _uiState.update { it.copy(isRefreshing = false, error = null) }
            },
            onFailure = { error ->
                _uiState.update { 
                    it.copy(isRefreshing = false, error = error.message) 
                }
                // UI will show Snackbar because error is not null
            }
        )
    }
}
```

### Implementation Hints
```kotlin
// In PostsListScreen.kt
PullToRefreshBox(
    isRefreshing = uiState.isRefreshing,
    onRefresh = viewModel::onRefresh
) {
    LazyColumn { ... }
}
```

---

## Scenario 2: Add Error Retry with Snackbar

### Context
When network requests fail, users currently see an error banner but no easy way to retry.

### Task
Add a Snackbar with retry action when refresh fails.

### Discussion Points

**1. Should Snackbar be in the screen or scaffold level?**

**Answer:** **Scaffold level** is the correct choice.

**Explanation:**
- **Screen level:** Snackbar might be cut off by keyboard, navigation bar, or other overlays
- **Scaffold level:** Snackbar always appears at the bottom of the screen with proper positioning and padding
- Scaffold handles the layout automatically including insets

**Example:**
```kotlin
@Composable
fun PostsScreen(viewModel: PostsViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Show snackbar when error occurs
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(
                message = error,
                actionLabel = "Retry"
            )
            viewModel.onErrorShown() // Clear error after showing
        }
    }
    
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        // Screen content
    }
}
```

---

**2. How to handle Snackbar state (Show/Dismiss)?**

**Answer:** Don't put Snackbar state in ViewModel. Use `LaunchedEffect` in UI triggered by ViewModel error state.

**Explanation:**
- Snackbar is a **UI concern**, not business logic
- ViewModel emits `error: String?` → UI decides how to display it
- When user dismisses Snackbar, only UI state changes; ViewModel error stays until `onRetry()` clears it

**Example:**
```kotlin
LaunchedEffect(uiState.error) {
    uiState.error?.let { error ->
        val result = snackbarHostState.showSnackbar(
            message = error,
            actionLabel = "Retry"
        )
        if (result == SnackbarResult.ActionPerformed) {
            viewModel.onRetry() // User clicked Retry
        }
        // Snackbar dismissed naturally after timeout
    }
}
```

---

**3. What if user navigates away while Snackbar is showing?**

**Answer:** Snackbar automatically disappears when the composable leaves composition. No memory leak, no action needed.

**Explanation:**
- Compose handles cleanup automatically when `LaunchedEffect` leaves composition
- If the user navigates back later and error still exists in ViewModel, the `LaunchedEffect` will trigger again
- This is the desired behavior—the user sees the error again when they return

**Best practice:** Keep error in ViewModel until explicitly cleared (user dismisses or retries successfully).

---

**4. Testing Snackbar interactions?**

**Answer:** Test that clicking the retry action triggers the ViewModel callback.

**Explanation:**
- Unit test: Verify that `viewModel.onRetry()` is called when Snackbar action is triggered
- UI test: Use `composeTestRule` to find the Snackbar action and click it

**Example:**
```kotlin
@Test
fun `when snackbar retry clicked, should call onRetry`() {
    // Given error state
    composeTestRule.setContent {
        PostsContent(
            uiState = PostsUiState(error = "Network error"),
            onRetry = { /* mock */ }
        )
    }
    
    // When user clicks retry
    composeTestRule.onNodeWithText("Retry").performClick()
    
    // Then verify callback triggered
    // (In real test, verify mock was called)
}
```

### Implementation Hints
```kotlin
// Use SnackbarHost with Scaffold
val snackbarHostState = remember { SnackbarHostState() }

LaunchedEffect(uiState.error) {
    uiState.error?.let { error ->
        val result = snackbarHostState.showSnackbar(
            message = error,
            actionLabel = "Retry"
        )
        if (result == SnackbarResult.ActionPerformed) {
            viewModel.onRetry()
        }
    }
}
```

---

## Scenario 3: Implement Search/Filter Feature

### Context
The app displays 100 posts. Users want to search by title.

### Task
Add a search bar that filters posts locally.

### Discussion Points

**1. Should filtering happen in ViewModel or UseCase?**

**Answer:** **ViewModel** for simple UI filtering; **UseCase** for complex business logic.

**When to use each:**

| Scenario | Location | Reason |
|----------|----------|--------|
| Simple text search | ViewModel | Just UI logic, no business rules |
| Multi-field filtering | ViewModel | UI state combination |
| Search + sort by relevance | UseCase | Business logic for ranking |
| Cross-repository search | UseCase | Coordinates multiple data sources |

**Example in ViewModel:**
```kotlin
private val _searchQuery = MutableStateFlow("")
val filteredPosts = combine(_searchQuery, allPosts) { query, posts ->
    if (query.isBlank()) posts
    else posts.filter { it.title.contains(query, ignoreCase = true) }
}.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
```

---

**2. Debounce search input? How long?**

**Answer:** Yes, **300ms** is the industry standard.

**Explanation:**
- **Without debounce:** User types "hello" → triggers 5 searches: "h", "he", "hel", "hell", "hello"
- **With debounce:** Wait 300ms after last keystroke → trigger 1 search: "hello"
- **300ms** is the sweet spot—fast enough to feel responsive, slow enough to wait for typing to pause

**Example:**
```kotlin
val searchResults = searchQuery
    .debounce(300.milliseconds) // Wait 300ms after last keystroke
    .flatMapLatest { query ->
        if (query.isBlank()) flowOf(emptyList())
        else repository.search(query)
    }
```

---

**3. Case-insensitive search?**

**Answer:** **Always yes** for user-facing search.

**Explanation:**
- Users don't expect exact case matching
- Use `contains(query, ignoreCase = true)` in Kotlin
- Also consider trimming whitespace: `query.trim()`

**Example:**
```kotlin
posts.filter { post ->
    post.title.contains(query, ignoreCase = true) ||
    post.body.contains(query, ignoreCase = true)
}
```

---

**4. What to show when no results found?**

**Answer:** Empty state with helpful message and clear action.

**Best practices:**
- Explain why there's nothing ("No posts found for 'xyz'")
- Suggest alternatives ("Try a different search term")
- Offer clear action ("Clear search" button)
- Use friendly illustration or icon

**Example:**
```kotlin
if (filteredPosts.isEmpty() && searchQuery.isNotBlank()) {
    EmptyState(
        title = "No results found",
        message = "No posts match '$searchQuery'",
        action = "Clear Search",
        onAction = { viewModel.clearSearch() }
    )
}
```

---

**5. Preserve search across rotation?**

**Answer:** **Yes automatically** if search is in ViewModel state. Use `savedStateHandle` to survive process death.

**Explanation:**
- **ViewModel survives rotation:** State is preserved when user rotates device
- **Process death:** If system kills app (low memory), use `savedStateHandle` to restore

**Example with saved state:**
```kotlin
@HiltViewModel
class PostsViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val getPostsUseCase: GetPostsUseCase
) : ViewModel() {
    
    private val _searchQuery = savedStateHandle.getStateFlow("search_query", "")
    
    fun updateSearch(query: String) {
        savedStateHandle["search_query"] = query
    }
}
```

### Implementation Hints
```kotlin
// In ViewModel
private val _searchQuery = MutableStateFlow("")
val filteredPosts = combine(_searchQuery, _posts) { query, posts ->
    if (query.isBlank()) posts
    else posts.filter { it.title.contains(query, ignoreCase = true) }
}.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
```

---

## Scenario 4: Add Loading Shimmer Effect

### Context
Current loading state shows a simple spinner. Design team wants shimmer loading effect.

### Task
Replace loading spinner with shimmer placeholder cards.

### Discussion Points

**1. Use library (Facebook Shimmer) or custom implementation?**

**Answer:** **Library for production**, **custom for learning/complete control**.

**Comparison:**

| Approach | Pros | Cons | When to Use |
|----------|------|------|-------------|
| Facebook Shimmer | Tested, accessible, easy | Extra dependency | Production apps |
| Custom | Full control, no deps | More code to maintain | Learning or special effects |
| Compose placeholder | Native Compose | Less customizable | Simple cases |

**Recommendation:** Start with `com.facebook.shimmer:shimmer` library. It's widely used and handles edge cases.

---

**2. How many shimmer items to show?**

**Answer:** Match the number of visible items on screen, typically **3-5 items**.

**Explanation:**
- Too few (1-2): Looks like something is missing
- Too many (10+): Creates unnecessary layout work
- Just right: Fill the viewport so user sees where content will appear

**Example:**
```kotlin
@Composable
fun ShimmerList() {
    Column {
        repeat(5) { // Show 5 shimmer cards
            ShimmerCard()
        }
    }
}
```

**Real-world example:** Facebook shows 3-4 shimmer posts when you open the app—enough to fill the screen but not the entire feed.

---

**3. Animate placeholder cards with gradient effect?**

**Answer:** Yes, that's the definition of shimmer—moving gradient suggesting activity.

**How it works:**
- A linear gradient sweeps across the placeholder
- Creates illusion of light reflecting off content
- Indicates "something is loading" better than static gray boxes

**Example with library:**
```kotlin
@Composable
fun ShimmerCard() {
    val shimmer = rememberShimmer(shimmerBounds = ShimmerBounds.View)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .shimmer(shimmer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Placeholder lines
            Box(modifier = Modifier.fillMaxWidth(0.7f).height(20.dp).background(Color.LightGray))
            Spacer(modifier = Modifier.height(8.dp))
            Box(modifier = Modifier.fillMaxWidth().height(60.dp).background(Color.LightGray))
        }
    }
}
```

---

**4. Accessibility considerations?**

**Answer:** Critical—shimmer can trigger motion sensitivity and confuse screen readers.

**Best practices:**

1. **Respect reduce motion settings:**
```kotlin
val shimmer = if (LocalContext.current.isReducedMotionEnabled()) {
    null // Disable animation
} else {
    rememberShimmer(shimmerBounds = ShimmerBounds.View)
}
```

2. **Add content description:**
```kotlin
Box(
    modifier = Modifier.semantics {
        contentDescription = "Loading content"
    }
)
```

3. **Don't autoplay:** Check system settings before starting shimmer animation

**Why it matters:** Users with vestibular disorders can experience dizziness from constant motion.

### Implementation Approach
```kotlin
@Composable
fun ShimmerCard() {
    val shimmer = rememberShimmer(shimmerBounds = ShimmerBounds.View)
    Card(
        modifier = Modifier.shimmer(shimmer)
    ) { /* placeholder content */ }
}
```

---

## Scenario 5: Implement Offline-First with Room

### Context
Currently caching posts in memory. App loses data when killed.

### Task
Add Room database for persistent caching.

### Discussion Points

**1. Entity design for Post table?**

**Answer:** Mirror API response fields + add metadata columns.

**Best practice:**
```kotlin
@Entity(tableName = "posts")
data class PostEntity(
    @PrimaryKey val id: Int,
    val userId: Int,
    val title: String,
    val body: String,
    val cachedAt: Long = System.currentTimeMillis(),
    val syncStatus: SyncStatus = SyncStatus.SYNCED
)

enum class SyncStatus {
    SYNCED,     // Matches server
    PENDING,    // Local changes waiting to sync
    CONFLICT    // Conflicts with server
}
```

**Explanation:**
- Core fields: Match API (id, userId, title, body)
- cachedAt: Know when data was last updated
- syncStatus: Track offline changes

---

**2. Migration strategy if schema changes?**

**Answer:** Room handles simple migrations automatically. For complex changes, write `Migration` objects.

**Example:**
```kotlin
// Version 1 → 2: Add author column
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE posts ADD COLUMN author TEXT DEFAULT ''")
    }
}

// In database builder
Room.databaseBuilder(context, AppDatabase::class.java, "app.db")
    .addMigrations(MIGRATION_1_2)
    .build()
```

**Best practices:**
- Never decrease version number
- Always provide migration path (don't use fallbackToDestructiveMigration in production)
- Test migrations thoroughly

---

**3. Single source of truth pattern?**

**Answer:** UI always observes from Room (local database). Repository syncs remote → Room → UI.

**Data flow:**
```
API (remote) → Repository → Room (local) → Flow → UI
                    ↑
               refresh trigger
```

**Why this matters:**
- UI doesn't care where data comes from
- Always have data available (offline-first)
- Repository coordinates sync logic

**Example:**
```kotlin
class PostRepositoryImpl(
    private val api: JsonPlaceholderApi,
    private val dao: PostDao
) : PostRepository {
    
    override fun getPosts(): Flow<List<Post>> = 
        dao.getAllPosts() // UI observes Room always
    
    override suspend fun refreshPosts() {
        val response = api.getPosts()
        if (response.isSuccessful) {
            response.body()?.let { posts ->
                dao.insertAll(posts.map { it.toEntity() })
            }
        }
    }
}
```

---

**4. Sync strategy: refresh on startup?**

**Answer:** Yes, with these considerations:

**Recommended approach:**
```
App Launch
    ↓
Show cached data immediately (fast)
    ↓
Trigger background refresh
    ↓
Update UI when new data arrives (seamless)
```

**Additional strategies:**
- **Startup refresh:** Always refresh on app launch (with cached data showing first)
- **Pull-to-refresh:** Manual refresh by user
- **Background sync:** Periodic refresh using WorkManager (every 15 mins)
- **Smart refresh:** Only refresh if cache is stale (> 5 minutes old)

---

**5. Handle conflicts between remote and local?**

**Answer:** Three strategies depending on requirements:

| Strategy | When to Use | Implementation |
|----------|-------------|----------------|
| **Last-write-wins** | Simple apps | Server data overwrites local |
| **Merge** | Collaborative features | Combine local + remote changes |
| **User decision** | Critical data | Show conflict dialog to user |

**Example (last-write-wins):**
```kotlin
suspend fun syncPosts() {
    val local = dao.getPendingPosts()
    
    // Send local changes to server
    local.forEach { post ->
        val result = api.updatePost(post)
        if (result.isSuccessful) {
            dao.markAsSynced(post.id)
        }
    }
    
    // Server is now source of truth
    refreshPosts()
}
```

**Example (user decision for conflicts):**
```kotlin
if (local.modifiedAt > remote.modifiedAt) {
    // Show dialog: "Server has newer version. Keep yours or use server?"
}
```

### Architecture Changes
```kotlin
// Data Layer addition
@Entity(tableName = "posts")
data class PostEntity(
    @PrimaryKey val id: Int,
    val userId: Int,
    val title: String,
    val body: String,
    val cachedAt: Long = System.currentTimeMillis()
)
```

---

## Scenario 6: Add Pagination with Infinite Scroll

### Context
API supports pagination but app loads all 100 posts at once.

### Task
Implement infinite scroll with paging.

### Discussion Points

**1. Use Paging 3 library or custom implementation?**

**Answer:** **Paging 3** for standard lists, **custom** only for special requirements.

**Paging 3 provides:**
- Built-in loading states (Loading, Error, Success)
- Automatic error handling with retry
- Placeholder support while loading
- Integration with Room (RemoteMediator)
- Efficient memory management (recycles pages)

**When to use custom:**
- Non-standard pagination (like cursor-based)
- Custom animations between pages
- Complex prefetch logic

**Example with Paging 3:**
```kotlin
class PostPagingSource(
    private val api: JsonPlaceholderApi
) : PagingSource<Int, Post>() {
    
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Post> {
        val page = params.key ?: 1
        return try {
            val response = api.getPosts(page = page, limit = params.loadSize)
            LoadResult.Page(
                data = response,
                prevKey = if (page == 1) null else page - 1,
                nextKey = if (response.isEmpty()) null else page + 1
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}
```

---

**2. Page size selection?**

**Answer:** **20-50 items** is the sweet spot.

**Guidelines:**

| Content Type | Page Size | Reason |
|--------------|-----------|--------|
| Text-heavy (posts, comments) | 20-25 | Smaller payload, faster parsing |
| Mixed (images + text) | 15-20 | Balance of content and performance |
| Images only | 10-15 | Large payloads, memory constraints |
| Simple text (names, IDs) | 50 | Small payloads, fewer requests |

**Why not larger?**
- Slower initial load
- More memory usage
- If user leaves early, wasted data

**Why not smaller?**
- Too many network requests
- Choppy scrolling experience
- More battery drain

---

**3. Loading state at bottom of list?**

**Answer:** Show circular progress indicator as the last item when loading next page.

**Implementation with Paging 3:**
```kotlin
LazyColumn {
    items(lazyPagingItems) { post ->
        post?.let { PostCard(it) }
    }
    
    // Loading indicator at bottom
    lazyPagingItems.apply {
        when {
            loadState.append is LoadState.Loading -> {
                item { CircularProgressIndicator() }
            }
            loadState.append is LoadState.Error -> {
                item { RetryButton(onClick = { retry() }) }
            }
        }
    }
}
```

**UX best practice:**
- Show spinner when loading next page
- Show "Load more" button if error occurs
- Smooth transition—no jarring jumps

---

**4. Error handling for partial page loads?**

**Answer:** Show loaded data + error message at bottom with retry option.

**Scenario:** Page 1 and 2 loaded successfully, page 3 failed.

**Solution:**
```kotlin
when (val state = lazyPagingItems.loadState.append) {
    is LoadState.Error -> {
        item {
            Column {
                Text("Couldn't load more posts")
                Button(onClick = { lazyPagingItems.retry() }) {
                    Text("Retry")
                }
            }
        }
    }
}
```

**Explanation:**
- User keeps already-loaded content (pages 1-2)
- Error is non-blocking (at bottom)
- One-tap retry to load failed page
- No data loss

---

**5. Prefetch distance?**

**Answer:** Start loading next page when user scrolls to **last 3-5 items**.

**Configuration:**
```kotlin
Pager(
    config = PagingConfig(
        pageSize = 20,
        prefetchDistance = 5,  // Load next page when 5 items remain
        initialLoadSize = 40   // Load 2 pages initially
    ),
    pagingSourceFactory = { PostPagingSource(api) }
).flow
```

**Why this matters:**
- **Too early:** Wastes bandwidth if user doesn't scroll
- **Too late:** User sees loading spinner (bad UX)
- **Just right:** Next page loaded just before user reaches end (seamless)

### Implementation Hints
```kotlin
// With Paging 3
val pager = Pager(
    config = PagingConfig(pageSize = 20),
    pagingSourceFactory = { PostPagingSource(api) }
).flow

// In UI
LazyColumn {
    items(lazyPagingItems) { post ->
        PostCard(post)
    }
}
```

---

## Scenario 7: Implement Dark Theme Toggle

### Context
App uses system theme but users want manual toggle.

### Task
Add theme switcher that persists preference.

### Discussion Points

**1. Store preference in DataStore or SharedPreferences?**

**Answer:** **DataStore**—it's the modern replacement for SharedPreferences.

**Comparison:**

| Feature | DataStore | SharedPreferences |
|---------|-----------|-------------------|
| Async API | ✅ Yes (Flow/coroutines) | ❌ No (synchronous) |
| Type safety | ✅ Yes (Preferences DataStore) | ❌ No (String only) |
| Migration | ✅ Automatic | Manual |
| Error handling | ✅ Exception-based | ❌ Silent failures |
| Consistency | ✅ Strong | Weak (apply vs commit) |

**Example with DataStore:**
```kotlin
// ThemeRepository.kt
class ThemeRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    val themeMode: Flow<ThemeMode> = dataStore.data
        .map { prefs ->
            ThemeMode.valueOf(
                prefs[THEME_KEY] ?: ThemeMode.SYSTEM.name
            )
        }
    
    suspend fun setTheme(mode: ThemeMode) {
        dataStore.edit { prefs ->
            prefs[THEME_KEY] = mode.name
        }
    }
    
    companion object {
        val THEME_KEY = stringPreferencesKey("theme_mode")
    }
}
```

---

**2. Apply theme at Application or Activity level?**

**Answer:** **Application level** is cleaner for most apps.

**Application level (recommended):**
```kotlin
@HiltAndroidApp
class PictetApplication : Application()

// In MainActivity
setContent {
    val themeMode by themeRepository.themeMode.collectAsState()
    val darkTheme = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }
    
    PictetTheme(darkTheme = darkTheme) {
        // App content
    }
}
```

**Activity level** (if you need per-activity themes—rare).

**Why Application level:**
- Single place to manage theme
- All Activities inherit automatically
- Consistent across the app

---

**3. Animate theme transition?**

**Answer:** **Difficult in Android**—system handles it automatically on Android 10+ for system theme changes. For manual toggle, smooth animation is complex.

**Reality check:**
- System handles animation when following system theme (Android 10+)
- Manual theme changes typically switch instantly
- Cross-fading is possible but adds complexity

**Recommendation:** Don't animate—users expect instant theme changes.

**If you must animate:**
```kotlin
// Crossfade between themes (simplified)
Crossfade(targetState = darkTheme) { isDark ->
    if (isDark) DarkThemeContent() else LightThemeContent()
}
```

---

**4. Respect system theme as default?**

**Answer:** **Yes, always.** Use `ThemeMode.SYSTEM` as the default.

**Implementation:**
```kotlin
enum class ThemeMode { LIGHT, DARK, SYSTEM }

// Default value
val DEFAULT_THEME = ThemeMode.SYSTEM

// Check system theme
val isSystemDark = isSystemInDarkTheme()

// First app launch
if (userHasNoPreference) {
    setTheme(ThemeMode.SYSTEM) // Respect system
}
```

**Why this matters:**
- Users expect apps to match their device settings
- Many users have scheduled dark mode (sunset to sunrise)
- Manual toggle should override, not replace system default

**Example flow:**
1. First launch: Follow system theme
2. User opens settings: Sees "System default (Dark)"
3. User can switch to Light or Dark manually
4. Choice is saved and persists

### Implementation Hints
```kotlin
// ThemeState
enum class ThemeMode { LIGHT, DARK, SYSTEM }

// In Activity
setContent {
    val themeMode by viewModel.themeMode.collectAsState()
    val darkTheme = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }
    PictetTheme(darkTheme = darkTheme) { ... }
}
```

---

## Communication Best Practices

### As the Driver
1. **Think aloud**: Explain what you're typing
2. **Ask questions**: "Should I extract this to a function?"
3. **Confirm understanding**: "So we want to debounce by 300ms?"
4. **Listen to navigators**: They're seeing the bigger picture

### As the Navigator
1. **Guide, don't dictate**: "What if we tried..." vs "Type this..."
2. **Explain why**: "Extracting this will make it testable"
3. **Watch for pitfalls**: "That might cause a recomposition loop"
4. **Encourage**: "Good idea, let's try that"

### When Rotating
1. **Summarize**: Brief recap of current state
2. **Clarify intent**: "We're implementing the debounce logic"
3. **Handoff smoothly**: New driver takes over context

---

## Common Pitfalls to Avoid

1. **Over-engineering**: Don't add abstraction for simple features
2. **Premature optimization**: Focus on clarity first
3. **Ignoring tests**: Ask "how should we test this?"
4. **Silent coding**: Always verbalize your thought process
5. **Dominating**: Give everyone a chance to contribute

---

## Evaluation Criteria

Interviewers typically assess:

| Category | Indicators |
|----------|------------|
| **Communication** | Clear explanations, asks clarifying questions |
| **Collaboration** | Incorporates others' ideas, gives feedback respectfully |
| **Technical Skill** | Clean code, follows conventions, proper error handling |
| **Testing** | Writes tests, considers edge cases |
| **Architecture** | Understands separation of concerns, makes appropriate abstractions |

---

## Practice Checklist

Before the interview, practice:
- [ ] Adding a feature from scratch (Scenario 1 or 3)
- [ ] Refactoring existing code (see CODE_REVIEW_EXERCISES.md)
- [ ] Explaining your code aloud while typing
- [ ] Reviewing someone else's code constructively
- [ ] Writing tests alongside implementation
