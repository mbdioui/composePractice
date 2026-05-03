# SpaceX Project - Step-by-Step Implementation Plan

**Approach:** Each commit is a small, focused task. You review and understand before moving to the next.

**Starting Point:** Empty Android project (commit "Initial Android project setup")

---

## Phase 1: Foundation (Project Setup)

### Step 1.1: Configure Gradle Dependencies
**Branch:** `spacex/1-setup-gradle`

**Files to modify:**
- `gradle/libs.versions.toml` - Add all dependency versions
- `app/build.gradle.kts` - Apply plugins and dependencies

**What it does:**
- Adds Compose, Hilt, Retrofit, Room, Coil dependencies
- Configures kapt for annotation processing
- Sets up Java 17 compatibility

**Verify:** `./gradlew build` succeeds

---

### Step 1.2: Create Project Structure
**Branch:** `spacex/2-project-structure`

**Create empty directories:**
```
app/src/main/java/com/bms/spacexplorer/
├── data/
│   ├── remote/
│   │   ├── api/
│   │   └── dto/
│   ├── local/
│   │   ├── dao/
│   │   ├── database/
│   │   └── entity/
│   └── repository/
├── domain/
│   ├── model/
│   ├── repository/
│   └── usecase/
├── presentation/
│   ├── screens/
│   ├── viewmodels/
│   ├── navigation/
│   └── theme/
└── di/
```

**What it does:**
- Creates package structure for Clean Architecture
- Empty directories ready for files

---

### Step 1.3: Application Class
**Branch:** `spacex/3-application-class`

**Files to create:**
- `app/src/main/java/com/bms/spacexplorer/SpaceXplorerApplication.kt`

**Code:**
```kotlin
@HiltAndroidApp
class SpaceXplorerApplication : Application()
```

**Files to modify:**
- `AndroidManifest.xml` - Add `android:name=".SpaceXplorerApplication"`

**What it does:**
- Hilt entry point for dependency injection
- Required for Hilt to work

---

## Phase 2: Data Layer - Remote (API)

### Step 2.1: Create API Interface
**Branch:** `spacex/4-api-interface`

**Files to create:**
- `data/remote/api/SpaceXApi.kt`

**Code:**
```kotlin
interface SpaceXApi {
    @GET("launches")
    suspend fun getAllLaunches(): Response<List<LaunchDto>>

    @GET("launches/{id}")
    suspend fun getLaunch(@Path("id") id: String): Response<LaunchDto>

    @GET("rockets/{id}")
    suspend fun getRocket(@Path("id") id: String): Response<RocketDto>
}
```

**What it does:**
- Defines API endpoints
- Uses Retrofit annotations

---

### Step 2.2: Create DTOs (Data Transfer Objects)
**Branch:** `spacex/5-dto-models`

**Files to create:**
- `data/remote/dto/LaunchDto.kt` - Launch data class
- `data/remote/dto/LinksDto.kt` - Links nested class
- `data/remote/dto/PatchDto.kt` - Patch images nested class
- `data/remote/dto/RocketDto.kt` - Rocket data class

**What it does:**
- Maps JSON response structure
- Uses @SerializedName for field mapping

---

### Step 2.3: Network Module (DI)
**Branch:** `spacex/6-network-module`

**Files to create:**
- `di/NetworkModule.kt`

**What it does:**
- Provides OkHttpClient with logging
- Provides Retrofit instance
- Provides SpaceXApi implementation

**Verify:** Build succeeds, no compilation errors

---

## Phase 3: Data Layer - Domain Models

### Step 3.1: Create Domain Models
**Branch:** `spacex/7-domain-models`

**Files to create:**
- `domain/model/Launch.kt` - Business Launch object
- `domain/model/LaunchLinks.kt` - Links in domain layer
- `domain/model/Rocket.kt` - Business Rocket object

**What it does:**
- Pure Kotlin data classes (no Android dependencies)
- Business entities used by ViewModels and UI

---

### Step 3.2: Repository Interface & Result Sealed Class
**Branch:** `spacex/8-repository-interface`

**Files to create:**
- `domain/model/Result.kt` - Sealed class for UI states
- `domain/repository/LaunchRepository.kt`

**Code:**
```kotlin
// domain/model/Result.kt
sealed class Result<out T> {
    data class Loading<T>(val data: T? = null) : Result<T>()
    data class Success<T>(val data: T) : Result<T>()
    data class Error<T>(val message: String, val data: T? = null) : Result<T>()
}

// Helper functions
fun <T> Result<T>.isLoading(): Boolean = this is Result.Loading
fun <T> Result<T>.isSuccess(): Boolean = this is Result.Success
fun <T> Result<T>.isError(): Boolean = this is Result.Error

fun <T> Result<T>.data(): T? = when (this) {
    is Result.Loading -> data
    is Result.Success -> data
    is Result.Error -> data
}

inline fun <T> Result<T>.onSuccess(action: (T) -> Unit): Result<T> {
    if (this is Result.Success) action(data)
    return this
}

inline fun <T> Result<T>.onError(action: (String) -> Unit): Result<T> {
    if (this is Result.Error) action(message)
    return this
}
```

```kotlin
// domain/repository/LaunchRepository.kt
interface LaunchRepository {
    fun getLaunches(): Flow<Result<List<Launch>>>
    suspend fun refreshLaunches(): Result<Unit>
    suspend fun getLaunch(id: String): Result<Launch>
    suspend fun getRocket(id: String): Result<Rocket>
}
```

**What it does:**
- Contract for data access
- Exposes Flow for reactive data
- Returns Result for error handling

---

## Phase 4: Data Layer - Local (Room)

### Step 4.1: Create Entity
**Branch:** `spacex/9-room-entity`

**Files to create:**
- `data/local/entity/LaunchEntity.kt`

**What it does:**
- Room entity with @Entity annotation
- Primary key, fields matching API response
- cachedAt timestamp for freshness

---

### Step 4.2: Create DAO
**Branch:** `spacex/10-room-dao`

**Files to create:**
- `data/local/dao/LaunchDao.kt`

**Code:**
```kotlin
@Dao
interface LaunchDao {
    @Query("SELECT * FROM launches ORDER BY flightNumber DESC")
    fun getAllLaunches(): Flow<List<LaunchEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLaunches(launches: List<LaunchEntity>)

    @Query("DELETE FROM launches")
    suspend fun clearAll()
}
```

**What it does:**
- Database access methods
- Flow for reactive queries

---

### Step 4.3: Create Database
**Branch:** `spacex/11-room-database`

**Files to create:**
- `data/local/database/AppDatabase.kt`

**What it does:**
- RoomDatabase abstract class
- Entities list
- DAO accessors

---

### Step 4.4: Database Module (DI)
**Branch:** `spacex/12-database-module`

**Files to create:**
- `di/DatabaseModule.kt`

**What it does:**
- Provides AppDatabase singleton
- Provides LaunchDao

**Verify:** Build succeeds

---

## Phase 5: Repository Implementation

### Step 5.1: Result Sealed Class
**Branch:** `spacex/13-result-class`

**Files to create:**
- `domain/model/Result.kt`

**What it does:**
- Sealed class representing UI states: Loading, Success, Error
- Extension functions for type-safe handling
- Used throughout the app for reactive state management

**Code:**
```kotlin
sealed class Result<out T> {
    data class Loading<T>(val data: T? = null) : Result<T>()
    data class Success<T>(val data: T) : Result<T>()
    data class Error<T>(val message: String, val data: T? = null) : Result<T>()
}

// Extension functions
fun <T> Result<T>.isLoading(): Boolean = this is Result.Loading
fun <T> Result<T>.isSuccess(): Boolean = this is Result.Success
fun <T> Result<T>.isError(): Boolean = this is Result.Error
fun <T> Result<T>.data(): T? = when (this) {
    is Result.Loading -> data
    is Result.Success -> data
    is Result.Error -> data
}
```

---

### Step 5.2: Repository Implementation with Mapping
**Branch:** `spacex/14-repository-impl`

**Files to create:**
- `data/repository/LaunchRepositoryImpl.kt`

**What it does:**
- Implements LaunchRepository interface
- Maps DTO ↔ Entity ↔ Domain
- getLaunches() emits Result states from Room Flow
- refreshLaunches() returns Result from API

**Key:** Extension functions for mapping at bottom of file

---

### Step 5.3: Repository Binding (DI)
**Branch:** `spacex/15-repository-binding`

**Files to create:**
- `di/RepositoryModule.kt`

**Code:**
```kotlin
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindLaunchRepository(
        impl: LaunchRepositoryImpl
    ): LaunchRepository
}
```

**What it does:**
- Binds interface to implementation
- Enables constructor injection

---

## Phase 6: Domain Layer - Use Cases

### Step 6.1: Create Use Cases
**Branch:** `spacex/16-usecases`

**Files to create:**
- `domain/usecase/GetLaunchesUseCase.kt`
- `domain/usecase/RefreshLaunchesUseCase.kt`
- `domain/usecase/GetLaunchDetailsUseCase.kt`
- `domain/usecase/FilterLaunchesUseCase.kt`
- `domain/usecase/LaunchFilterStatus.kt` (enum)

**What it does:**
- Encapsulates single business operation
- Combines repository calls with logic
- Filter use case for search + status filtering

---

## Phase 7: Presentation Layer - ViewModels

### Step 7.1: UI State Classes
**Branch:** `spacex/17-ui-state`

**Files to create:**
- `presentation/viewmodels/LaunchesUiState.kt`
- `presentation/viewmodels/LaunchDetailUiState.kt`

**What it does:**
- Data classes representing UI state
- isLoading, error, data fields

---

### Step 7.2: Launches ViewModel
**Branch:** `spacex/18-launches-viewmodel`

**Files to create:**
- `presentation/viewmodels/LaunchesViewModel.kt`

**What it does:**
- Injects UseCases
- Exposes uiState: StateFlow<LaunchesUiState>
- init { loadLaunches() }
- Methods: onRefresh(), onSearchQueryChange(), onFilterStatusChange()

---

### Step 7.3: Launch Detail ViewModel
**Branch:** `spacex/19-detail-viewmodel`

**Files to create:**
- `presentation/viewmodels/LaunchDetailViewModel.kt`

**What it does:**
- Loads single launch by ID
- Loads rocket details
- Exposes LaunchDetailUiState

---

## Phase 8: Presentation Layer - UI

### Step 8.1: Theme Setup
**Branch:** `spacex/20-theme`

**Files to create:**
- `presentation/theme/Color.kt` - SpaceX colors (black, blue, etc.)
- `presentation/theme/Theme.kt` - Dark theme
- `presentation/theme/Type.kt` - Typography

**What it does:**
- SpaceX-inspired dark theme
- Material3 setup

---

### Step 8.2: Launches List Screen - Part 1 (Structure)
**Branch:** `spacex/21-launches-screen-structure`

**Files to create:**
- `presentation/screens/LaunchesScreen.kt` - Basic scaffold + TopAppBar

**What it does:**
- Screen scaffold with title
- Empty content area

---

### Step 8.3: Launches List Screen - Part 2 (List)
**Branch:** `spacex/22-launches-list`

**Modify:** `presentation/screens/LaunchesScreen.kt`

**Add:**
- LazyColumn
- LaunchCard composable
- AsyncImage for mission patches

**What it does:**
- Displays list of launches
- Basic card layout

---

### Step 8.4: Launches List Screen - Part 3 (States)
**Branch:** `spacex/23-launches-states`

**Modify:** `presentation/screens/LaunchesScreen.kt`

**Add:**
- LoadingContent() - CircularProgressIndicator
- ErrorContent() - Retry button
- EmptyContent() - No results message

**What it does:**
- Handles all UI states
- when { } expression for state switching

---

### Step 8.5: Search Bar
**Branch:** `spacex/24-search-bar`

**Modify:** `presentation/screens/LaunchesScreen.kt`

**Add:**
- OutlinedTextField for search
- onSearchQueryChange callback
- Debounced search (optional)

**What it does:**
- Real-time filtering

---

### Step 8.6: Filter Chips
**Branch:** `spacex/25-filter-chips`

**Modify:** `presentation/screens/LaunchesScreen.kt`

**Add:**
- Row of FilterChips
- ALL, SUCCESS, FAILED, UPCOMING options
- Selected state management

**What it does:**
- Status filtering

---

### Step 8.7: Pull-to-Refresh
**Branch:** `spacex/26-pull-to-refresh`

**Modify:** `presentation/screens/LaunchesScreen.kt`

**Add:**
- PullToRefreshBox wrapper
- isRefreshing state
- onRefresh callback

**What it does:**
- Manual refresh gesture

---

### Step 8.8: Launch Detail Screen
**Branch:** `spacex/27-detail-screen`

**Files to create:**
- `presentation/screens/LaunchDetailScreen.kt`

**What it does:**
- Large mission patch
- Status badge
- Launch info rows
- Description
- Webcast/Article buttons

---

## Phase 9: Navigation

### Step 9.1: Navigation Routes
**Branch:** `spacex/28-navigation-routes`

**Files to create:**
- `presentation/navigation/Routes.kt`

**Code:**
```kotlin
object Routes {
    const val LAUNCHES = "launches"
    const val LAUNCH_DETAIL = "launch/{launchId}"
    
    fun launchDetail(launchId: String) = "launch/$launchId"
}
```

---

### Step 9.2: NavGraph
**Branch:** `spacex/29-navgraph`

**Files to create:**
- `presentation/navigation/NavGraph.kt`

**What it does:**
- NavHost setup
- composable(Routes.LAUNCHES)
- composable(Routes.LAUNCH_DETAIL) with arguments

---

### Step 9.3: Main Activity Integration
**Branch:** `spacex/30-main-activity`

**Files to modify:**
- `MainActivity.kt`

**Change:**
- Set content to SpaceXplorerTheme + NavGraph
- Add @AndroidEntryPoint

**Verify:** App launches, shows list screen

---

## Phase 10: Testing

### Step 10.1: Repository Unit Test
**Branch:** `spacex/31-repository-test`

**Files to create:**
- `src/test/java/.../data/repository/LaunchRepositoryImplTest.kt`

**Test cases:**
- getLaunches with empty cache emits failure
- refreshLaunches fetches from API and caches

---

### Step 10.2: ViewModel Unit Test
**Branch:** `spacex/32-viewmodel-test`

**Files to create:**
- `src/test/java/.../presentation/viewmodels/LaunchesViewModelTest.kt`

**Test cases:**
- when initialized, should load launches
- when search query changes, should filter launches
- when refresh fails, should show error

---

### Step 10.3: UI Test
**Branch:** `spacex/33-ui-test`

**Files to create:**
- `src/androidTest/.../presentation/screens/LaunchesScreenTest.kt`

**Test cases:**
- launchesList displays launch names
- loading state shows indicator
- error state shows retry button

---

## Phase 11: Polish

### Step 11.1: Add Placeholder Images
**Branch:** `spacex/34-placeholders`

**Add:**
- Placeholder drawable for mission patches
- Error drawable

**Modify:** AsyncImage calls to show placeholders

---

### Step 11.2: Date Formatting
**Branch:** `spacex/35-date-formatting`

**Modify:** `Launch` domain model

**Add:**
- Computed property formattedDate
- Format from ISO to readable

---

### Step 11.3: Error Messages
**Branch:** `spacex/36-error-messages`

**Modify:** ViewModels

**Add:**
- User-friendly error messages
- Network error vs. unknown error

---

## Final Verification

### Checklist
- [ ] `./gradlew build` succeeds
- [ ] App launches without crash
- [ ] List displays launches
- [ ] Mission patches load
- [ ] Search filters work
- [ ] Filter chips work
- [ ] Pull-to-refresh works
- [ ] Detail screen opens
- [ ] Back navigation works
- [ ] Offline mode works (after initial load)
- [ ] All tests pass

---

## Time Estimates

| Phase | Estimated Time |
|-------|----------------|
| Phase 1: Foundation | 1 hour |
| Phase 2: Remote Data | 1 hour |
| Phase 3: Domain Models | 30 minutes |
| Phase 4: Local Data | 1 hour |
| Phase 5: Repository | 45 minutes |
| Phase 6: Use Cases | 30 minutes |
| Phase 7: ViewModels | 1 hour |
| Phase 8: UI | 2 hours |
| Phase 9: Navigation | 30 minutes |
| Phase 10: Testing | 1.5 hours |
| Phase 11: Polish | 30 minutes |
| **Total** | **~10 hours** |

---

## Workflow

For each step:

1. **Create branch** from previous step
   ```bash
   git checkout -b spacex/XX-task-name
   ```

2. **Implement** the specific task

3. **Test** - Build and verify
   ```bash
   ./gradlew build
   ```

4. **Commit** with clear message
   ```bash
   git add .
   git commit -m "Add [specific thing]
   
   - What was added
   - Why it was added
   - How it works"
   ```

5. **Review** the changes before moving on

6. **Move to next step**

---

**Remember:** Don't rush. Understand each step before moving to the next. If something doesn't work, debug it before continuing.
