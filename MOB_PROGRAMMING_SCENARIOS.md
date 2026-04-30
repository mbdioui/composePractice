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
- Where should the refresh state be managed?
- How to show loading indicator without blocking the list?
- What if the refresh fails but we have cached data?
- Should we show an error snackbar?

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
- Should Snackbar be in the screen or scaffold level?
- How to handle Snackbar state (Show/Dismiss)?
- What if user navigates away while Snackbar is showing?
- Testing Snackbar interactions?

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
- Should filtering happen in ViewModel or UseCase?
- Debounce search input? How long?
- Case-insensitive search?
- What to show when no results found?
- Preserve search across rotation?

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
- Use library (Facebook Shimmer) or custom implementation?
- How many shimmer items to show?
- Animate placeholder cards with gradient effect?
- Accessibility considerations?

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
- Entity design for Post table?
- Migration strategy if schema changes?
- Single source of truth pattern?
- Sync strategy: refresh on startup?
- Handle conflicts between remote and local?

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
- Use Paging 3 library or custom implementation?
- Page size selection?
- Handle loading state at bottom of list?
- Error handling for partial page loads?
- Prefetch distance?

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
- Store preference in DataStore or SharedPreferences?
- Apply theme at Application or Activity level?
- Animate theme transition?
- Respect system theme as default?

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
