# SpaceX Project - Dependency Technical Specification

## Document Purpose

This document provides detailed technical specifications for every dependency in the SpaceX project, explaining exactly what functionality each library provides and why it's necessary for the application.

---

## Table of Contents

1. [UI Layer Dependencies](#1-ui-layer-dependencies)
2. [Dependency Injection](#2-dependency-injection)
3. [Networking Layer](#3-networking-layer)
4. [Local Database](#4-local-database)
5. [Image Loading](#5-image-loading)
6. [Testing Dependencies](#6-testing-dependencies)

---

## 1. UI Layer Dependencies

### 1.1 Compose BOM (Bill of Materials)

**Dependency:**
```toml
[versions]
composeBom = "2025.04.01"

[libraries]
androidx-compose-bom = { group = "androidx.compose", name = "compose-bom", version.ref = "composeBom" }
```

**What It Grants:**
- **Version Alignment**: Ensures all Compose libraries (UI, Material3, Foundation, Runtime, Animation) use tested, compatible versions
- **Simplified Updates**: Change one version number to update 10+ libraries simultaneously
- **Conflict Prevention**: Eliminates runtime crashes from incompatible Compose versions

**Technical Details:**
- BOM 2025.04.01 provides:
  - `androidx.compose.ui:ui` → 1.7.8
  - `androidx.compose.material3:material3` → 1.3.1
  - `androidx.compose.foundation:foundation` → 1.7.8
  - `androidx.compose.animation:animation` → 1.7.8

**SpaceX Project Impact:**
- All screens use Compose for UI rendering
- BOM guarantees `Material3` components work with `UI` foundation
- Without BOM: Risk of `material3:1.0.0` conflicting with `ui:1.7.0` causing runtime crashes

**Usage:**
```kotlin
// In app/build.gradle.kts
implementation(platform(libs.androidx.compose.bom))
```

---

### 1.2 Activity Compose

**Dependency:**
```toml
[versions]
activityCompose = "1.10.1"

[libraries]
androidx-activity-compose = { group = "androidx.activity", name = "activity-compose", version.ref = "activityCompose" }
```

**What It Grants:**
- **ComponentActivity Support**: Base class for Activities using Compose
- **setContent() Function**: Entry point for Compose UI tree
- **Lifecycle Integration**: Automatic lifecycle management for Compose compositions

**Technical Details:**
- Version 1.10.1 is compatible with compileSdk 36 (Android 16)
- Provides `ComponentActivity.setContent { }` DSL
- Handles configuration changes (rotation, dark mode) properly

**SpaceX Project Impact:**
```kotlin
// MainActivity.kt - Without this library, this code doesn't compile
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {  // <-- This function comes from activity-compose
            SpaceXplorerTheme {
                NavGraph()
            }
        }
    }
}
```

**Why This Version:**
- Android 16 (SDK 36) support requires Activity 1.10.x
- Kotlin 2.0.x compatibility verified

---

### 1.3 Lifecycle ViewModel Compose

**Dependency:**
```toml
[versions]
lifecycle = "2.8.7"

[libraries]
androidx-lifecycle-viewmodel-compose = { group = "androidx.lifecycle", name = "lifecycle-viewmodel-compose", version.ref = "lifecycle" }
androidx-lifecycle-runtime = { group = "androidx.lifecycle", name = "lifecycle-runtime-ktx", version.ref = "lifecycle" }
```

**What It Grants:**
- **ViewModel Survival**: Data survives configuration changes (screen rotation)
- **viewModelScope**: Coroutine scope tied to ViewModel lifecycle (auto-cancellation)
- **StateFlow Integration**: Reactive UI updates with lifecycle awareness

**Technical Details:**
- `ViewModel` class survives Activity/Fragment recreation
- `viewModelScope.launch { }` cancels when ViewModel is cleared
- Version 2.8.7 supports compileSdk 36

**SpaceX Project Impact:**
```kotlin
@HiltViewModel
class LaunchesViewModel @Inject constructor(
    private val getLaunchesUseCase: GetLaunchesUseCase
) : ViewModel() {  // <-- From lifecycle library

    private val _uiState = MutableStateFlow(LaunchesUiState())
    val uiState: StateFlow<LaunchesUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {  // <-- Automatically cancels when ViewModel destroyed
            loadLaunches()
        }
    }
}

// In Compose screen
val uiState by viewModel.uiState.collectAsStateWithLifecycle()  // Lifecycle-aware collection
```

**Without This Library:**
- Data lost on rotation
- Manual coroutine cancellation required
- Memory leaks from uncollected flows

---

### 1.4 Navigation Compose

**Dependency:**
```toml
[versions]
navigation_version = "2.8.9"

[libraries]
androidx-navigation-compose = { group = "androidx.navigation", name = "navigation-compose", version.ref = "navigation_version" }
```

**What It Grants:**
- **Type-Safe Navigation**: Navigate between screens with compile-time safety
- **Back Stack Management**: Automatic handling of back button behavior
- **Deep Links**: URL-based navigation support
- **Arguments Passing**: Pass data between screens safely

**Technical Details:**
- `NavHost`: Container for navigation destinations
- `composable()` DSL for defining screens
- `rememberNavController()` for navigation control

**SpaceX Project Impact:**
```kotlin
@Composable
fun NavGraph() {
    val navController = rememberNavController()
    
    NavHost(navController = navController, startDestination = Routes.LAUNCHES) {
        composable(Routes.LAUNCHES) {
            LaunchesScreen(
                onLaunchClick = { launchId ->
                    navController.navigate(Routes.launchDetail(launchId))
                }
            )
        }
        composable(Routes.LAUNCH_DETAIL) { backStackEntry ->
            val launchId = backStackEntry.arguments?.getString("launchId")
            LaunchDetailScreen(launchId = launchId)
        }
    }
}
```

**Why This Version:**
- 2.8.9 is latest stable, supports compileSdk 36
- Required for `hilt-navigation-compose` 1.2.0 compatibility

---

## 2. Dependency Injection

### 2.1 Hilt Runtime

**Dependency:**
```toml
[versions]
hilt = "2.56.1"

[libraries]
hilt-android = { group = "com.google.dagger", name = "hilt-android", version.ref = "hilt" }
```

**What It Grants:**
- **Automatic Object Creation**: Dependencies injected without manual instantiation
- **Singleton Management**: `@Singleton` objects created once and reused
- **Lifecycle-Aware Injection**: Different scopes for Application, Activity, ViewModel
- **Compile-Time Safety**: DI graph validated at build time

**Technical Details:**
- Based on Dagger 2 (Google's DI framework)
- Code generation at compile time (no reflection)
- Annotation-driven: `@Inject`, `@Module`, `@Provides`, `@Singleton`

**SpaceX Project Impact:**
```kotlin
// Application entry point
@HiltAndroidApp
class SpaceXplorerApplication : Application()

// ViewModel receives dependencies automatically
@HiltViewModel
class LaunchesViewModel @Inject constructor(
    private val getLaunchesUseCase: GetLaunchesUseCase,  // Auto-provided
    private val refreshLaunchesUseCase: RefreshLaunchesUseCase  // Auto-provided
) : ViewModel()

// Repository injected with its dependencies
class LaunchRepositoryImpl @Inject constructor(
    private val api: SpaceXApi,          // From NetworkModule
    private val dao: LaunchDao           // From DatabaseModule
) : LaunchRepository
```

**Without Hilt:**
```kotlin
// Manual dependency chain (error-prone)
val client = OkHttpClient()
val retrofit = Retrofit.Builder().client(client).build()
val api = retrofit.create(SpaceXApi::class.java)
val db = Room.databaseBuilder(...).build()
val dao = db.launchDao()
val repository = LaunchRepositoryImpl(api, dao)
val useCase = GetLaunchesUseCase(repository)
val viewModel = LaunchesViewModel(useCase)  // Tedious!
```

---

### 2.2 Hilt Compiler

**Dependency:**
```toml
hilt-compiler = { group = "com.google.dagger", name = "hilt-compiler", version.ref = "hilt" }
```

**What It Grants:**
- **Code Generation**: Processes annotations and generates DI boilerplate
- **Component Building**: Creates `DaggerAppComponent`, `Hilt_LaunchesViewModel` classes
- **Error Detection**: Reports missing bindings at compile time

**Technical Details:**
- Annotation Processor (KAPT/KSP)
- Generates implementation classes at compile time
- Runtime has zero overhead from annotation processing

**SpaceX Project Impact:**
```kotlin
// Module providing dependencies
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideSpaceXApi(retrofit: Retrofit): SpaceXApi {
        return retrofit.create(SpaceXApi::class.java)
    }
}

// Compiler generates the wiring code automatically
```

**Build Configuration:**
```kotlin
// In app/build.gradle.kts
dependencies {
    implementation(libs.hilt.android)     // Runtime in APK
    kapt(libs.hilt.compiler)               // Build-time only
}
```

**Why Separate:**
- `hilt-android` → Goes into APK (40KB+)
- `hilt-compiler` → Build machine only (not in APK)
- Keeps runtime APK smaller

---

### 2.3 Hilt Navigation Compose

**Dependency:**
```toml
hilt-navigation-compose = { group = "androidx.hilt", name = "hilt-navigation-compose", version = "1.2.0" }
```

**What It Grants:**
- **hiltViewModel() Function**: Creates ViewModels scoped to navigation destinations
- **Navigation-Aware Scoping**: ViewModels cleared when leaving navigation route
- **SavedStateHandle Support**: Access to navigation arguments in ViewModel

**Technical Details:**
- Bridge between Hilt and Navigation Compose
- From `androidx.hilt` (not `com.google.dagger`)
- Released separately from core Hilt

**SpaceX Project Impact:**
```kotlin
// Without this library - complex ViewModel creation
val viewModel = ViewModelProvider(
    navBackStackEntry,
    HiltViewModelFactory(LocalContext.current, navBackStackEntry)
).get(LaunchesViewModel::class.java)

// With this library - simple
val viewModel: LaunchesViewModel = hiltViewModel()

// Navigation with proper scoping
composable(Routes.LAUNCH_DETAIL) { backStackEntry ->
    // ViewModel scoped to this destination, cleared when user navigates back
    val viewModel: LaunchDetailViewModel = hiltViewModel()
    LaunchDetailScreen(viewModel)
}
```

**Why Version 1.2.0:**
- Compatible with Navigation 2.8.x
- Required for Compose integration
- Latest stable release

---

## 3. Networking Layer

### 3.1 Retrofit

**Dependency:**
```toml
[versions]
retrofit = "2.11.0"

[libraries]
retrofit = { group = "com.squareup.retrofit2", name = "retrofit", version.ref = "retrofit" }
```

**What It Grants:**
- **Type-Safe API Calls**: Convert HTTP responses to Kotlin objects
- **Interface-Based API Definition**: Define endpoints as annotated interface methods
- **Customizable Client**: Pluggable HTTP client (OkHttp), converters (Gson)
- **Synchronous/Asynchronous**: Supports coroutines (`suspend` functions)

**Technical Details:**
- Creates implementation of API interface at runtime
- Uses reflection for method dispatch
- Supports RxJava, Coroutines, or blocking calls

**SpaceX Project Impact:**
```kotlin
// API Definition
interface SpaceXApi {
    @GET("launches")
    suspend fun getAllLaunches(): Response<List<LaunchDto>>

    @GET("launches/{id}")
    suspend fun getLaunch(@Path("id") id: String): Response<LaunchDto>

    @GET("rockets/{id}")
    suspend fun getRocket(@Path("id") id: String): Response<RocketDto>
}

// Usage in Repository
class LaunchRepositoryImpl @Inject constructor(
    private val api: SpaceXApi
) : LaunchRepository {
    override suspend fun refreshLaunches(): Result<Unit> {
        return try {
            val response = api.getAllLaunches()  // Type-safe, returns List<LaunchDto>
            if (response.isSuccessful) {
                // Save to database
                Result.success(Unit)
            } else {
                Result.failure(HttpException(response))
            }
        } catch (e: IOException) {
            Result.failure(e)
        }
    }
}
```

**Why This Version:**
- 2.11.0 is latest stable
- Kotlin coroutines support built-in
- Square actively maintains

---

### 3.2 Retrofit Gson Converter

**Dependency:**
```toml
retrofit-converter-gson = { group = "com.squareup.retrofit2", name = "converter-gson", version.ref = "retrofit" }
```

**What It Grants:**
- **Automatic JSON Parsing**: Converts HTTP response body to Kotlin objects
- **@SerializedName Support**: Maps JSON keys to Kotlin properties
- **Type Adapter**: Handles complex types automatically

**Technical Details:**
- Gson (Google's JSON library) integrated with Retrofit
- Configured during Retrofit building
- Handles null safety, default values, custom adapters

**SpaceX Project Impact:**
```kotlin
// DTO with JSON mapping
class LaunchDto(
    @SerializedName("flight_number") val flightNumber: Int,
    @SerializedName("mission_name") val missionName: String,
    @SerializedName("launch_date_utc") val launchDate: String,
    @SerializedName("links") val links: LinksDto,
    @SerializedName("rocket") val rocket: RocketReferenceDto
)

// Retrofit configuration
val retrofit = Retrofit.Builder()
    .baseUrl("https://api.spacexdata.com/v3/")
    .addConverterFactory(GsonConverterFactory.create())  // <-- This library
    .build()

// Now Retrofit automatically deserializes JSON to LaunchDto
```

**Without This:**
```kotlin
// Manual JSON parsing (tedious and error-prone)
val jsonString = response.body()?.string()
val jsonObject = JSONObject(jsonString)
val flightNumber = jsonObject.getInt("flight_number")
val missionName = jsonObject.getString("mission_name")
// ... repeat for every field
```

**Alternatives:**
- Moshi (Square) - More Kotlin-friendly
- Kotlinx Serialization - Official Kotlin, compiler plugin

---

### 3.3 OkHttp Logging Interceptor

**Dependency:**
```toml
[versions]
okhttp = "4.12.0"

[libraries]
okhttp-logging = { group = "com.squareup.okhttp3", name = "logging-interceptor", version.ref = "okhttp" }
```

**What It Grants:**
- **HTTP Request Logging**: See full URL, headers, body sent to server
- **HTTP Response Logging**: See status code, headers, response body
- **Debug Visibility**: Essential for troubleshooting API issues

**Technical Details:**
- Intercepts HTTP calls before/after network request
- Configurable log levels: NONE, BASIC, HEADERS, BODY
- Only for debug builds (should be removed in production)

**SpaceX Project Impact:**
```kotlin
// In NetworkModule
@Provides
@Singleton
fun provideOkHttpClient(): OkHttpClient {
    return OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY  // Log everything
        })
        .build()
}

// Logcat output:
// D/OkHttp: --> GET https://api.spacexdata.com/v3/launches
// D/OkHttp: --> END GET
// D/OkHttp: <-- 200 OK https://api.spacexdata.com/v3/launches (234ms)
// D/OkHttp: Content-Type: application/json
// D/OkHttp: [{"flight_number": 1, "mission_name": "FalconSat"...}]
```

**Security Note:**
```kotlin
// Only log in debug builds
val builder = OkHttpClient.Builder()
if (BuildConfig.DEBUG) {
    builder.addInterceptor(HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    })
}
```

---

## 4. Local Database

### 4.1 Room Runtime

**Dependency:**
```toml
[versions]
room = "2.6.1"

[libraries]
room-runtime = { group = "androidx.room", name = "room-runtime", version.ref = "room" }
```

**What It Grants:**
- **SQLite Abstraction**: Object-relational mapping (ORM) for SQLite
- **Compile-Time Safety**: SQL queries validated at compile time
- **Database Migration**: Versioned schema updates
- **Kotlin Coroutines**: Suspend functions for database operations

**Technical Details:**
- Three components: Entity (table), DAO (queries), Database (holder)
- Generates SQLite code at compile time
- Thread-safe by design (enforces main-thread safety)

**SpaceX Project Impact:**
```kotlin
// Entity - Table definition
@Entity(tableName = "launches")
class LaunchEntity(
    @PrimaryKey val id: String,
    val flightNumber: Int,
    val missionName: String,
    val launchDate: String,
    val status: String,
    val patchUrl: String?,
    val cachedAt: Long = System.currentTimeMillis()
)

// DAO - Data Access
@Dao
interface LaunchDao {
    @Query("SELECT * FROM launches ORDER BY flightNumber DESC")
    suspend fun getAllLaunches(): List<LaunchEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLaunches(launches: List<LaunchEntity>)

    @Query("DELETE FROM launches")
    suspend fun clearAll()
}

// Database - Container
@Database(entities = [LaunchEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun launchDao(): LaunchDao
}
```

**Why Room Over Raw SQLite:**
- Compile-time SQL validation (catches typos)
- Boilerplate reduction (no Cursor management)
- Migration framework for schema updates
- Kotlin coroutines integration

---

### 4.2 Room Compiler

**Dependency:**
```toml
room-compiler = { group = "androidx.room", name = "room-compiler", version.ref = "room" }
```

**What It Grants:**
- **Code Generation**: Creates `_Impl` classes for Database and DAO
- **SQL Validation**: Checks queries for errors at compile time
- **Schema Export**: Generates database schema files for testing

**Technical Details:**
- Annotation Processor (KAPT/KSP)
- Generates `AppDatabase_Impl` with actual SQLite code
- Generates `LaunchDao_Impl` with query implementations

**SpaceX Project Impact:**
```kotlin
// What you write:
@Query("SELECT * FROM launches WHERE status = :status")
suspend fun getLaunchesByStatus(status: String): List<LaunchEntity>

// What Room generates:
public class LaunchDao_Impl implements LaunchDao {
    @Override
    public List<LaunchEntity> getLaunchesByStatus(String status) {
        final String _sql = "SELECT * FROM launches WHERE status = ?";
        final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
        _statement.bindString(1, status);
        // ... cursor handling, object mapping, etc.
    }
}
```

**Build Configuration:**
```kotlin
dependencies {
    implementation(libs.room.runtime)
    kapt(libs.room.compiler)  // Required!
}
```

**Compile-Time Validation:**
```kotlin
@Query("SELECT * FROM launch")  // Typo: should be "launches"
// ERROR: SQL error or missing database (code 1): no such table: launch
// Room catches this at compile time, not runtime!
```

---

### 4.3 Room KTX

**Dependency:**
```toml
room-ktx = { group = "androidx.room", name = "room-ktx", version.ref = "room" }
```

**What It Grants:**
- **Flow Support**: Returns `Flow<T>` for reactive database queries
- **Suspend Functions**: Non-blocking database operations
- **Transaction Support**: `@Transaction` annotation for atomic operations

**Technical Details:**
- Extension functions for Kotlin coroutines
- Room without KTX: Only blocking methods
- Room with KTX: `suspend` and `Flow` methods

**SpaceX Project Impact:**
```kotlin
// Without room-ktx (blocking - BAD for UI thread)
@Dao
interface LaunchDao {
    @Query("SELECT * FROM launches")
    fun getAllLaunches(): List<LaunchEntity>  // Blocking! Must run on background thread
}

// With room-ktx (non-blocking - GOOD)
@Dao
interface LaunchDao {
    // Returns Flow - emits automatically when data changes
    @Query("SELECT * FROM launches ORDER BY flightNumber DESC")
    fun getAllLaunches(): Flow<List<LaunchEntity>>
    
    // Suspend - runs asynchronously, non-blocking
    @Insert
    suspend fun insertLaunches(launches: List<LaunchEntity>)
}

// Repository can now return Flow
class LaunchRepositoryImpl @Inject constructor(
    private val dao: LaunchDao
) : LaunchRepository {
    override fun getLaunches(): Flow<Result<List<Launch>>> {
        return dao.getAllLaunches()  // Flow emits on each database change
            .map { entities -> 
                Result.success(entities.map { it.toDomain() })
            }
    }
}

// UI automatically updates when database changes
val uiState by viewModel.uiState.collectAsStateWithLifecycle()
// When database updates → Flow emits → UI recomposes automatically
```

**Critical Difference:**
- Without room-ktx: Manual polling or callbacks needed
- With room-ktx: Reactive data flow, automatic UI updates

---

## 5. Image Loading

### 5.1 Coil Compose

**Dependency:**
```toml
[versions]
coil = "2.6.0"

[libraries]
coil-compose = { group = "io.coil-kt", name = "coil-compose", version.ref = "coil" }
```

**What It Grants:**
- **AsyncImage Composable**: Load and display images from URLs
- **Automatic Caching**: Memory + disk cache for offline viewing
- **Placeholders/Error States**: Loading and error UI
- **Transformations**: Rounded corners, grayscale, etc.
- **Cancellation**: Stops loading when composable leaves composition

**Technical Details:**
- Kotlin-first image loading (unlike Glide/Java)
- Coroutines-based (suspend functions for loading)
- Deep Compose integration
- Smaller than Glide (~200KB vs ~500KB)

**SpaceX Project Impact:**
```kotlin
// Display mission patch image
@Composable
fun LaunchCard(launch: Launch) {
    Card {
        Row {
            AsyncImage(
                model = launch.patchUrl,  // URL from API
                contentDescription = "${launch.missionName} patch",
                modifier = Modifier.size(64.dp),
                placeholder = painterResource(R.drawable.placeholder_rocket),
                error = painterResource(R.drawable.error_image),
                contentScale = ContentScale.Crop
            )
            
            Column {
                Text(launch.missionName)
                Text(launch.formattedDate)
            }
        }
    }
}
```

**Features Used:**
- **Memory Cache**: Images stay in RAM for fast scrolling
- **Disk Cache**: Previously loaded images work offline
- **Lazy Loading**: Only loads images for visible items
- **Automatic Cancellation**: Stops loading if user scrolls away

**Configuration:**
```kotlin
// Application-wide configuration
Coil.setImageLoader {
    ImageLoader.Builder(context)
        .crossfade(true)
        .memoryCache { MemoryCache.Builder(context).maxSizePercent(0.25).build() }
        .diskCache { DiskCache.Builder().directory(context.cacheDir.resolve("image_cache")).build() }
        .build()
}
```

**Alternatives:**
- Glide (Google) - More mature, larger community
- Fresco (Meta) - Facebook's library, complex
- Coil is recommended for Compose projects

---

## 6. Testing Dependencies

### 6.1 MockK

**Dependency:**
```toml
[versions]
mockk = "1.14.0"

[libraries]
mockk = { group = "io.mockk", name = "mockk", version.ref = "mockk" }
```

**What It Grants:**
- **Kotlin-Friendly Mocking**: Designed for Kotlin language features
- **Coroutines Support**: `coEvery`/`coVerify` for suspend functions
- **Relaxed Mocks**: Returns default values without explicit stubbing
- **Spy Creation**: Mock real objects (partial mocking)
- **Static Mocking**: Mock top-level functions, objects

**Technical Details:**
- Alternative to Mockito (Java-focused)
- Better nullable type support
- Built-in coroutine testing support
- No `when()` keyword conflict with Kotlin

**SpaceX Project Impact:**
```kotlin
class LaunchRepositoryImplTest {
    @Test
    fun `getLaunches should return cached data when available`() = runTest {
        // Arrange
        val mockDao = mockk<LaunchDao>()
        val mockApi = mockk<SpaceXApi>()
        val fakeLaunches = listOf(
            LaunchEntity("1", 1, "FalconSat", "2006-03-24", "Success", null)
        )
        
        // Stub - when this is called, return fake data
        coEvery { mockDao.getAllLaunches() } returns flowOf(fakeLaunches)
        
        val repository = LaunchRepositoryImpl(mockApi, mockDao)
        
        // Act
        val result = repository.getLaunches().first()
        
        // Assert
        assertTrue(result.isSuccess)
        assertEquals("FalconSat", result.getOrNull()?.first()?.missionName)
        
        // Verify interactions
        coVerify { mockDao.getAllLaunches() }
        coVerify(exactly = 0) { mockApi.getAllLaunches() }  // Shouldn't hit API
    }
}
```

**MockK vs Mockito:**
```kotlin
// Mockito (verbose for Kotlin)
`when`(mockRepository.getLaunches()).thenReturn(flowOf(emptyList()))

// MockK (Kotlin-native)
coEvery { mockRepository.getLaunches() } returns flowOf(emptyList())
```

**Why This Version:**
- 1.14.0 is latest stable
- Supports Kotlin 2.0
- Active maintenance

---

### 6.2 Kotlinx Coroutines Test

**Dependency:**
```toml
[versions]
coroutinesTest = "1.9.0"

[libraries]
coroutines-test = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-test", version.ref = "coroutinesTest" }
```

**What It Grants:**
- **runTest { }**: Runs coroutines synchronously in tests
- **Test Dispatchers**: Control coroutine timing
- **advanceTimeBy()**: Fast-forward delays
- **TestScope**: Manages structured concurrency in tests

**Technical Details:**
- `StandardTestDispatcher` - Coroutines run immediately
- `UnconfinedTestDispatcher` - Coroutines run eagerly
- `Dispatchers.setMain(testDispatcher)` - Replace Android's Main dispatcher

**SpaceX Project Impact:**
```kotlin
class LaunchesViewModelTest {
    @Test
    fun `refresh should show loading then success`() = runTest {
        // runTest = coroutines execute synchronously, no real delays
        val mockUseCase = mockk<RefreshLaunchesUseCase>()
        coEvery { mockUseCase() } returns Result.success(Unit)
        
        val viewModel = LaunchesViewModel(
            getLaunchesUseCase = mockk(relaxed = true),
            refreshLaunchesUseCase = mockUseCase
        )
        
        // Initial state
        assertEquals(false, viewModel.uiState.value.isRefreshing)
        
        // Action
        viewModel.onRefresh()
        
        // State after refresh (synchronous in test)
        assertEquals(false, viewModel.uiState.value.isRefreshing)
        assertEquals(null, viewModel.uiState.value.errorMessage)
    }
    
    @Test
    fun `debounced search should wait for user to stop typing`() = runTest {
        val viewModel = createViewModel()
        
        viewModel.onSearchQueryChange("Falcon")
        viewModel.onSearchQueryChange("Falcon 9")
        viewModel.onSearchQueryChange("Falcon 9 Heavy")
        
        // Fast-forward 300ms (debounce delay)
        advanceTimeBy(300)
        
        // Now search should execute
        coVerify { mockFilterUseCase("Falcon 9 Heavy") }
    }
}
```

**Without This Library:**
- Tests with coroutines are flaky
- Real delays make tests slow
- Can't control timing of concurrent operations

---

### 6.3 Turbine

**Dependency:**
```toml
[versions]
turbine = "1.1.0"

[libraries]
turbine = { group = "app.cash.turbine", name = "turbine", version.ref = "turbine" }
```

**What It Grants:**
- **Flow Testing DSL**: Clean API for testing Flow emissions
- **Automatic Collection**: Handles subscription/cleanup
- **Timeout Handling**: Detects missing emissions
- **Hot/Cold Flow Support**: Works with StateFlow, SharedFlow, etc.

**Technical Details:**
- Cash App's testing library
- `test { }` block manages Flow collection
- `awaitItem()` suspends until emission
- Takes care of lifecycle (no manual `Job` management)

**SpaceX Project Impact:**
```kotlin
@Test
fun `uiState should emit loading then content`() = runTest {
    val mockUseCase = mockk<GetLaunchesUseCase>()
    val fakeLaunches = listOf(Launch("1", "FalconSat", ...))
    
    coEvery { mockUseCase() } returns flowOf(
        Result.success(fakeLaunches)
    )
    
    val viewModel = LaunchesViewModel(mockUseCase, mockk())
    
    // Test Flow emissions with Turbine
    viewModel.uiState.test {
        // First emission (initial state)
        assertEquals(false, awaitItem().isLoading)
        
        // Trigger loading
        viewModel.onRefresh()
        
        // Second emission (loading state)
        assertEquals(true, awaitItem().isLoading)
        
        // Third emission (content loaded)
        val contentState = awaitItem()
        assertEquals(false, contentState.isLoading)
        assertEquals(1, contentState.launches.size)
        
        // Cleanup
        cancelAndIgnoreRemainingEvents()
    }
}
```

**Without Turbine:**
```kotlin
// Manual collection (messy)
val emissions = mutableListOf<LaunchesUiState>()
val job = launch {
    viewModel.uiState.collect { emissions.add(it) }
}
viewModel.onRefresh()
delay(100)  // Wait for emission
assertEquals(3, emissions.size)
job.cancel()  // Must manually cleanup
```

**With Turbine:**
```kotlin
// Clean DSL
viewModel.uiState.test {
    assertEquals(state1, awaitItem())
    assertEquals(state2, awaitItem())
    cancelAndIgnoreRemainingEvents()
}
```

**Key Functions:**
- `awaitItem()` - Wait for next emission
- `awaitComplete()` - Flow completed
- `expectNoEvents()` - Verify nothing emitted
- `cancelAndConsumeRemainingEvents()` - Cleanup + check all emissions

---

## Summary Table

| Category | Library | Core Function | In APK? |
|----------|---------|---------------|---------|
| **UI** | Compose BOM | Version alignment for all Compose libs | BOM: No, Libraries: Yes |
| **UI** | Activity Compose | `setContent()` for Activities | Yes |
| **UI** | Lifecycle ViewModel | Survive rotation, `viewModelScope` | Yes |
| **UI** | Navigation Compose | Screen navigation, back stack | Yes |
| **DI** | Hilt Android | Dependency injection container | Yes |
| **DI** | Hilt Compiler | Generate DI code at build | No |
| **DI** | Hilt Navigation | `hiltViewModel()` for Compose | Yes |
| **Network** | Retrofit | Type-safe HTTP client | Yes |
| **Network** | Gson Converter | JSON ↔ Kotlin objects | Yes |
| **Network** | OkHttp Logging | Debug HTTP requests/responses | No (debug only) |
| **Database** | Room Runtime | SQLite ORM, compile-time SQL | Yes |
| **Database** | Room Compiler | Generate database code | No |
| **Database** | Room KTX | `Flow<T>`, `suspend` support | Yes |
| **Image** | Coil Compose | Async image loading | Yes |
| **Test** | MockK | Kotlin mocking framework | No |
| **Test** | Coroutines Test | Test coroutines synchronously | No |
| **Test** | Turbine | Flow testing DSL | No |

---

## Version Compatibility Matrix

| Library | Version | Kotlin | Compile SDK | Notes |
|---------|---------|--------|-------------|-------|
| Compose BOM | 2025.04.01 | 2.0.x | 36 | Latest stable |
| Activity Compose | 1.10.1 | 2.0.x | 36 | Android 16 support |
| Lifecycle | 2.8.7 | 2.0.x | 36 | ViewModel + Coroutines |
| Navigation | 2.8.9 | 2.0.x | 36 | Compose navigation |
| Hilt | 2.56.1 | 2.0.x | Any | Google maintained |
| Retrofit | 2.11.0 | Any | Any | Square maintained |
| Room | 2.6.1 | 2.0.x | 36 | AndroidX |
| Coil | 2.6.0 | 2.0.x | Any | Kotlin-first |
| MockK | 1.14.0 | 2.0.x | N/A | Test only |
| Coroutines Test | 1.9.0 | 2.0.x | N/A | JetBrains |
| Turbine | 1.1.0 | 2.0.x | N/A | Cash App |

---

## Dependency Decision Tree

```
Does your app need...?

UI Framework
├── Use XML? → androidx.appcompat
└── Use Compose? → androidx.compose (via BOM)
    ├── Multiple screens? → navigation-compose
    └── ViewModels in Compose? → lifecycle-viewmodel-compose

Dependency Management
├── Simple app? → Manual construction
└── Complex app? → Hilt
    ├── Activities/Fragments? → hilt-android
    ├── ViewModels? → hilt-lifecycle-viewmodel
    └── Compose Navigation? → hilt-navigation-compose

Networking
├── REST API? → Retrofit
│   ├── JSON responses? → converter-gson (or moshi)
│   └── Debug logging? → logging-interceptor
└── GraphQL? → Apollo Kotlin

Local Storage
├── Simple key-value? → DataStore
├── Complex relational data? → Room
│   ├── Basic queries? → room-runtime
│   └── Reactive + async? → room-ktx
└── Complex queries? → SQLDelight

Image Loading
├── Compose project? → Coil
├── Java/Android Views? → Glide
└── Advanced features? → Fresco

Testing
├── Unit tests? → JUnit + MockK
├── Coroutines in tests? → kotlinx-coroutines-test
├── Flow testing? → Turbine
└── UI tests? → Espresso / Compose Testing
```
