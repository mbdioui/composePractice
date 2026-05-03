# Practice Project: SpaceX Launches Explorer

Build this project from **scratch** to practice all concepts from the Pictet interview preparation.

---

## Project Overview

**App Name:** SpaceXplorer

**API:** [SpaceX API](https://github.com/r-spacex/SpaceX-API) (Free, no authentication required)

**Base URL:** `https://api.spacexdata.com/v4/`

**Features:**
- List all SpaceX launches
- View launch details (rocket, mission patch, video links)
- Filter launches by success/failure
- Search launches by mission name
- Pull-to-refresh
- Offline caching with Room

---

## API Endpoints

### 1. Get All Launches
```
GET https://api.spacexdata.com/v4/launches

Response: Array of Launch objects
```

### 2. Get Single Launch
```
GET https://api.spacexdata.com/v4/launches/{id}

Response: Single Launch object
```

### 3. Get Rocket Details
```
GET https://api.spacexdata.com/v4/rockets/{rocket_id}

Response: Rocket details
```

### 4. Get Launchpad Details
```
GET https://api.spacexdata.com/v4/launchpads/{launchpad_id}

Response: Launchpad details
```

---

## Data Models (JSON Structure)

### Launch DTO
```json
{
  "id": "5eb87cd9ffd86e000604b32a",
  "flight_number": 1,
  "name": "FalconSat",
  "date_utc": "2006-03-24T22:30:00.000Z",
  "upcoming": false,
  "success": false,
  "details": "Engine failure at 33 seconds and loss of vehicle",
  "links": {
    "patch": {
      "small": "https://images2.imgbox.com/3c/...",
      "large": "https://images2.imgbox.com/40/..."
    },
    "webcast": "https://www.youtube.com/watch?v=0a_00nJ_Y88",
    "article": "https://www.space.com/...",
    "wikipedia": "https://en.wikipedia.org/wiki/..."
  },
  "rocket": "5e9d0d95eda69955f709d1eb",
  "launchpad": "5e9e4502f509094188566f88",
  "cores": [{
    "core": "5e9e289df35918033d3b2623",
    "flight": 1,
    "landing_attempt": false,
    "landing_success": null,
    "landing_type": null
  }],
  "payloads": ["5eb0e4b5b6c3bb0006eeb1e1"]
}
```

### Rocket DTO
```json
{
  "id": "5e9d0d95eda69955f709d1eb",
  "name": "Falcon 1",
  "type": "rocket",
  "active": false,
  "stages": 2,
  "boosters": 0,
  "cost_per_launch": 6700000,
  "success_rate_pct": 40,
  "first_flight": "2006-03-24",
  "company": "SpaceX",
  "wikipedia": "https://en.wikipedia.org/wiki/Falcon_1",
  "description": "The Falcon 1 was an expendable launch system...",
  "flickr_images": [
    "https://farm5.staticflickr.com/4599/..."
  ]
}
```

---

## Step-by-Step Implementation Guide

### Phase 1: Project Setup (30 minutes)

**1.1 Create New Project**
```bash
# In Android Studio:
# File → New → New Project → "Empty Activity"
# Name: SpaceXplorer
# Package: com.bms.spacexplorer
# Minimum SDK: 24
# Language: Kotlin
# Build config: Kotlin DSL
```

**1.2 Setup Gradle Dependencies**

Edit `gradle/libs.versions.toml`:
```toml
[versions]
agp = "8.10.1"
kotlin = "2.0.21"
coreKtx = "1.18.0"

# Compose
composeBom = "2025.04.01"
activityCompose = "1.10.1"
lifecycle = "2.8.7"

# Hilt
hilt = "2.56.1"

# Networking
retrofit = "2.11.0"
okhttp = "4.12.0"
gson = "2.11.0"

# Room
room = "2.6.1"

# Coil (Image Loading)
coil = "2.6.0"

# Testing
mockk = "1.14.0"
coroutinesTest = "1.9.0"
turbine = "1.1.0"

[libraries]
# Core
androidx-core-ktx = { group = "androidx.core", name = "core-ktx", version.ref = "coreKtx" }
androidx-lifecycle-runtime = { group = "androidx.lifecycle", name = "lifecycle-runtime-ktx", version.ref = "lifecycle" }

# Compose
androidx-compose-bom = { group = "androidx.compose", name = "compose-bom", version.ref = "composeBom" }
androidx-compose-ui = { group = "androidx.compose.ui", name = "ui" }
androidx-compose-material3 = { group = "androidx.compose.material3", name = "material3" }
androidx-compose-icons = { group = "androidx.compose.material", name = "material-icons-extended" }
androidx-activity-compose = { group = "androidx.activity", name = "activity-compose", version.ref = "activityCompose" }
androidx-lifecycle-viewmodel-compose = { group = "androidx.lifecycle", name = "lifecycle-viewmodel-compose", version.ref = "lifecycle" }

# Navigation
androidx-navigation-compose = { group = "androidx.navigation", name = "navigation-compose", version = "2.8.9" }

# Hilt
hilt-android = { group = "com.google.dagger", name = "hilt-android", version.ref = "hilt" }
hilt-compiler = { group = "com.google.dagger", name = "hilt-compiler", version.ref = "hilt" }
hilt-navigation-compose = { group = "androidx.hilt", name = "hilt-navigation-compose", version = "1.2.0" }

# Networking
retrofit = { group = "com.squareup.retrofit2", name = "retrofit", version.ref = "retrofit" }
retrofit-converter-gson = { group = "com.squareup.retrofit2", name = "converter-gson", version.ref = "retrofit" }
okhttp-logging = { group = "com.squareup.okhttp3", name = "logging-interceptor", version.ref = "okhttp" }

# Room
room-runtime = { group = "androidx.room", name = "room-runtime", version.ref = "room" }
room-compiler = { group = "androidx.room", name = "room-compiler", version.ref = "room" }
room-ktx = { group = "androidx.room", name = "room-ktx", version.ref = "room" }

# Image Loading
coil-compose = { group = "io.coil-kt", name = "coil-compose", version.ref = "coil" }

# Testing
mockk = { group = "io.mockk", name = "mockk", version.ref = "mockk" }
coroutines-test = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-test", version.ref = "coroutinesTest" }
turbine = { group = "app.cash.turbine", name = "turbine", version.ref = "turbine" }

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
compose-compiler = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
hilt = { id = "com.google.dagger.hilt.android", version.ref = "hilt" }
kapt = { id = "org.jetbrains.kotlin.kapt", version.ref = "kotlin" }
room = { id = "androidx.room", version.ref = "room" }
```

Edit `app/build.gradle.kts`:
```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.hilt)
    alias(libs.plugins.kapt)
    alias(libs.plugins.room)
}

android {
    namespace = "com.bms.spacexplorer"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.bms.spacexplorer"
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

    room {
        schemaDirectory("$projectDir/schemas")
    }
}

dependencies {
    // Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.icons)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // Navigation
    implementation(libs.androidx.navigation.compose)

    // Hilt
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    // Networking
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp.logging)

    // Room
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    kapt(libs.room.compiler)

    // Image Loading
    implementation(libs.coil.compose)

    // Testing
    testImplementation(libs.mockk)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.turbine)
}
```

---

### Phase 2: Data Layer Setup (45 minutes)

**2.1 Create API Interface**

```kotlin
// data/remote/api/SpaceXApi.kt
interface SpaceXApi {
    @GET("launches")
    suspend fun getAllLaunches(): Response<List<LaunchDto>>

    @GET("launches/{id}")
    suspend fun getLaunch(@Path("id") id: String): Response<LaunchDto>

    @GET("rockets/{id}")
    suspend fun getRocket(@Path("id") id: String): Response<RocketDto>
}
```

**2.2 Create DTOs**

```kotlin
// data/remote/dto/LaunchDto.kt
data class LaunchDto(
    @SerializedName("id")
    val id: String,

    @SerializedName("flight_number")
    val flightNumber: Int,

    @SerializedName("name")
    val name: String,

    @SerializedName("date_utc")
    val dateUtc: String,

    @SerializedName("upcoming")
    val upcoming: Boolean,

    @SerializedName("success")
    val success: Boolean?,

    @SerializedName("details")
    val details: String?,

    @SerializedName("links")
    val links: LinksDto?,

    @SerializedName("rocket")
    val rocketId: String,

    @SerializedName("launchpad")
    val launchpadId: String
)

data class LinksDto(
    @SerializedName("patch")
    val patch: PatchDto?,

    @SerializedName("webcast")
    val webcast: String?,

    @SerializedName("article")
    val article: String?,

    @SerializedName("wikipedia")
    val wikipedia: String?
)

data class PatchDto(
    @SerializedName("small")
    val small: String?,

    @SerializedName("large")
    val large: String?
)
```

```kotlin
// data/remote/dto/RocketDto.kt
data class RocketDto(
    @SerializedName("id")
    val id: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("type")
    val type: String,

    @SerializedName("active")
    val active: Boolean,

    @SerializedName("description")
    val description: String?,

    @SerializedName("flickr_images")
    val flickrImages: List<String>?
)
```

**2.3 Create Domain Models**

```kotlin
// domain/model/Launch.kt
data class Launch(
    val id: String,
    val flightNumber: Int,
    val name: String,
    val dateUtc: String,
    val upcoming: Boolean,
    val success: Boolean?,
    val details: String?,
    val links: LaunchLinks?,
    val rocketId: String,
    val launchpadId: String
) {
    val formattedDate: String
        get() = // Format date from "2006-03-24T22:30:00.000Z" to "Mar 24, 2006"

    val isSuccessDisplay: String
        get() = when (success) {
            true -> "✅ Success"
            false -> "❌ Failed"
            null -> "⏳ Upcoming"
        }
}

data class LaunchLinks(
    val patchSmall: String?,
    val patchLarge: String?,
    val webcast: String?,
    val article: String?,
    val wikipedia: String?
)
```

```kotlin
// domain/model/Rocket.kt
data class Rocket(
    val id: String,
    val name: String,
    val type: String,
    val active: Boolean,
    val description: String?,
    val flickrImages: List<String>?
)
```

**2.4 Create Room Entities**

```kotlin
// data/local/entity/LaunchEntity.kt
@Entity(tableName = "launches")
data class LaunchEntity(
    @PrimaryKey
    val id: String,

    val flightNumber: Int,
    val name: String,
    val dateUtc: String,
    val upcoming: Boolean,
    val success: Boolean?,
    val details: String?,
    val patchSmall: String?,
    val patchLarge: String?,
    val webcast: String?,
    val article: String?,
    val wikipedia: String?,
    val rocketId: String,
    val launchpadId: String,
    val cachedAt: Long = System.currentTimeMillis()
)
```

```kotlin
// data/local/dao/LaunchDao.kt
@Dao
interface LaunchDao {
    @Query("SELECT * FROM launches ORDER BY flightNumber DESC")
    fun getAllLaunches(): Flow<List<LaunchEntity>>

    @Query("SELECT * FROM launches WHERE id = :id")
    suspend fun getLaunchById(id: String): LaunchEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLaunches(launches: List<LaunchEntity>)

    @Query("DELETE FROM launches")
    suspend fun clearAll()
}
```

```kotlin
// data/local/database/AppDatabase.kt
@Database(
    entities = [LaunchEntity::class],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun launchDao(): LaunchDao
}
```

---

### Phase 3: Repository Layer (30 minutes)

**3.1 Result Sealed Class & Repository Interface**

```kotlin
// domain/model/Result.kt - UI State representation
sealed class Result<out T> {
    data class Loading<T>(val data: T? = null) : Result<T>()
    data class Success<T>(val data: T) : Result<T>()
    data class Error<T>(val message: String, val data: T? = null) : Result<T>()
}

// Extension functions for cleaner handling
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
    /**
     * Offline-First: Returns cached data immediately (if available),
     * then silently refreshes from remote.
     * Flow emits:
     *   - Loading (on first launch when cache empty)
     *   - Success (cached data immediately)
     *   - Success (fresh data after background refresh)
     *   - Error (if network fails AND cache is empty)
     */
    fun getLaunches(): Flow<Result<List<Launch>>>
    
    /**
     * Force refresh from remote (for pull-to-refresh)
     */
    suspend fun refreshLaunches(): Result<Unit>
    
    /**
     * Get single launch (cache first, then remote)
     */
    suspend fun getLaunch(id: String): Result<Launch>
    
    suspend fun getRocket(id: String): Result<Rocket>
}
```

**3.2 Repository Implementation (Offline-First)**

**Offline-First Strategy:**
```
1. Query Room Flow → If data exists, emit Success immediately (fast!)
2. Launch background coroutine → Fetch from API
3. Save fresh data to Room → Room Flow auto-emits updated data
4. First launch only → Show Loading while fetching
5. No network → Still show cached data (stale but functional)
```

```kotlin
// data/repository/LaunchRepositoryImpl.kt
@Singleton
class LaunchRepositoryImpl @Inject constructor(
    private val api: SpaceXApi,
    private val launchDao: LaunchDao
) : LaunchRepository {

    /**
     * Offline-First: Returns cached data immediately, refreshes in background
     * 
     * Flow behavior:
     * - First launch (empty cache): Emits Loading → Success (after fetch)
     * - Subsequent opens: Emits Success (cached) → Success (fresh after refresh)
     * - Offline with cache: Emits Success (cached), silent refresh fails
     */
    override fun getLaunches(): Flow<Result<List<Launch>>> = 
        launchDao.getAllLaunches()
            .map { entities ->
                if (entities.isEmpty()) {
                    // First launch: Show loading while we fetch
                    Result.Loading()
                } else {
                    // Have cached data: Show immediately, refresh in background
                    Result.Success(entities.map { it.toDomain() })
                }
            }
            .onStart {
                // Launch background refresh (doesn't block Flow emission)
                coroutineScope { launch { refreshLaunches() } }
            }
            .catch { emit(Result.Error(it.message ?: "Unknown error")) }

    /**
     * Force refresh from remote
     * Called by: Pull-to-refresh, first launch (via onStart), retry button
     */
    override suspend fun refreshLaunches(): Result<Unit> = try {
        val response = api.getAllLaunches()
        if (response.isSuccessful) {
            response.body()?.let { launches ->
                // Save to Room → Triggers Flow re-emission with fresh data
                launchDao.insertLaunches(launches.map { it.toEntity() })
                Result.Success(Unit)
            } ?: Result.Error("Empty response")
        } else {
            Result.Error("HTTP ${response.code()}: ${response.message()}")
        }
    } catch (e: Exception) {
        Result.Error(e.message ?: "Network error")
    }

    override suspend fun getLaunch(id: String): Result<Launch> = try {
        // Try cache first
        launchDao.getLaunchById(id)?.let {
            return Result.Success(it.toDomain())
        }
        // Fall back to API
        val response = api.getLaunch(id)
        if (response.isSuccessful) {
            response.body()?.let {
                Result.Success(it.toDomain())
            } ?: Result.Error("Empty response")
        } else {
            Result.Error("HTTP ${response.code()}: ${response.message()}")
        }
    } catch (e: Exception) {
        Result.Error(e.message ?: "Network error")
    }

    override suspend fun getRocket(id: String): Result<Rocket> = try {
        val response = api.getRocket(id)
        if (response.isSuccessful) {
            response.body()?.let {
                Result.Success(it.toDomain())
            } ?: Result.Error("Empty response")
        } else {
            Result.Error("HTTP ${response.code()}: ${response.message()}")
        }
    } catch (e: Exception) {
        Result.Error(e.message ?: "Network error")
    }

    // Mapping functions
    private fun LaunchDto.toDomain(): Launch = Launch(
        id = id,
        flightNumber = flightNumber,
        name = name,
        dateUtc = dateUtc,
        upcoming = upcoming,
        success = success,
        details = details,
        links = links?.toDomain(),
        rocketId = rocketId,
        launchpadId = launchpadId
    )

    private fun LinksDto.toDomain(): LaunchLinks = LaunchLinks(
        patchSmall = patch?.small,
        patchLarge = patch?.large,
        webcast = webcast,
        article = article,
        wikipedia = wikipedia
    )

    private fun LaunchDto.toEntity(): LaunchEntity = LaunchEntity(
        id = id,
        flightNumber = flightNumber,
        name = name,
        dateUtc = dateUtc,
        upcoming = upcoming,
        success = success,
        details = details,
        patchSmall = links?.patch?.small,
        patchLarge = links?.patch?.large,
        webcast = links?.webcast,
        article = links?.article,
        wikipedia = links?.wikipedia,
        rocketId = rocketId,
        launchpadId = launchpadId
    )

    private fun LaunchEntity.toDomain(): Launch = Launch(
        id = id,
        flightNumber = flightNumber,
        name = name,
        dateUtc = dateUtc,
        upcoming = upcoming,
        success = success,
        details = details,
        links = LaunchLinks(
            patchSmall = patchSmall,
            patchLarge = patchLarge,
            webcast = webcast,
            article = article,
            wikipedia = wikipedia
        ),
        rocketId = rocketId,
        launchpadId = launchpadId
    )

    private fun RocketDto.toDomain(): Rocket = Rocket(
        id = id,
        name = name,
        type = type,
        active = active,
        description = description,
        flickrImages = flickrImages
    )
}
```

---

### Phase 4: Use Cases (15 minutes)

```kotlin
// domain/usecase/GetLaunchesUseCase.kt
class GetLaunchesUseCase @Inject constructor(
    private val repository: LaunchRepository
) {
    operator fun invoke(): Flow<Result<List<Launch>>> =
        repository.getLaunches()
}

// domain/usecase/RefreshLaunchesUseCase.kt
class RefreshLaunchesUseCase @Inject constructor(
    private val repository: LaunchRepository
) {
    suspend operator fun invoke(): Result<Unit> =
        repository.refreshLaunches()
}

// domain/usecase/GetLaunchDetailsUseCase.kt
class GetLaunchDetailsUseCase @Inject constructor(
    private val repository: LaunchRepository
) {
    suspend operator fun invoke(id: String): Result<Launch> =
        repository.getLaunch(id)
}

// domain/usecase/FilterLaunchesUseCase.kt
class FilterLaunchesUseCase @Inject constructor() {
    operator fun invoke(
        launches: List<Launch>,
        query: String,
        status: LaunchFilterStatus
    ): List<Launch> {
        return launches.filter { launch ->
            val matchesQuery = query.isBlank() ||
                    launch.name.contains(query, ignoreCase = true)

            val matchesStatus = when (status) {
                LaunchFilterStatus.ALL -> true
                LaunchFilterStatus.SUCCESS -> launch.success == true
                LaunchFilterStatus.FAILED -> launch.success == false
                LaunchFilterStatus.UPCOMING -> launch.upcoming
            }

            matchesQuery && matchesStatus
        }
    }
}

enum class LaunchFilterStatus {
    ALL, SUCCESS, FAILED, UPCOMING
}
```

---

### Phase 5: ViewModel (30 minutes)

**Offline-First Notes:**
- Repository handles initial refresh automatically (via `onStart`)
- ViewModel just collects from Flow, no manual refresh needed on init
- `isLoading` only true on first launch (empty cache)
- Subsequent opens show cached data immediately

```kotlin
// presentation/viewmodels/LaunchesViewModel.kt
@HiltViewModel
class LaunchesViewModel @Inject constructor(
    private val getLaunchesUseCase: GetLaunchesUseCase,
    private val refreshLaunchesUseCase: RefreshLaunchesUseCase,
    private val filterLaunchesUseCase: FilterLaunchesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(LaunchesUiState())
    val uiState: StateFlow<LaunchesUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    private val _filterStatus = MutableStateFlow(LaunchFilterStatus.ALL)

    init {
        loadLaunches()
        observeFilters()
    }

    /**
     * Collects from Repository Flow.
     * Repository handles:
     *   - Emitting cached data immediately (if available)
     *   - Background refresh on first launch
     *   - Auto-emitting fresh data when Room updates
     */
    private fun loadLaunches() {
        viewModelScope.launch {
            getLaunchesUseCase().collect { result ->
                when (result) {
                    is Result.Loading -> {
                        // Only shows loading on first launch (empty cache)
                        _uiState.update {
                            it.copy(
                                isLoading = true,
                                launches = result.data ?: it.launches
                            )
                        }
                    }
                    is Result.Success -> {
                        // Called twice: 
                        // 1. With cached data (fast!)
                        // 2. With fresh data after background refresh
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                launches = result.data,
                                error = null
                            )
                        }
                    }
                    is Result.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = result.message
                            )
                        }
                    }
                }
            }
        }
        // Note: No manual refresh here! Repository handles it via onStart {}
    }

    private fun observeFilters() {
        viewModelScope.launch {
            combine(_searchQuery, _filterStatus, _uiState) { query, status, state ->
                Triple(query, status, state.launches)
            }.collect { (query, status, launches) ->
                val filtered = filterLaunchesUseCase(launches, query, status)
                _uiState.update { it.copy(filteredLaunches = filtered) }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onFilterStatusChange(status: LaunchFilterStatus) {
        _filterStatus.value = status
    }

    fun onRefresh() {
        _uiState.update { it.copy(isRefreshing = true) }

        viewModelScope.launch {
            when (val result = refreshLaunchesUseCase()) {
                is Result.Success -> {
                    _uiState.update { it.copy(isRefreshing = false) }
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isRefreshing = false,
                            error = result.message
                        )
                    }
                }
                else -> { /* Loading not applicable for refresh */ }
            }
        }
    }

    fun onRetry() {
        _uiState.update { it.copy(error = null) }
        loadLaunches()
    }

    fun onLaunchClick(launchId: String) {
        _uiState.update { it.copy(selectedLaunchId = launchId) }
    }
}

data class LaunchesUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val launches: List<Launch> = emptyList(),
    val filteredLaunches: List<Launch> = emptyList(),
    val error: String? = null,
    val selectedLaunchId: String? = null
) {
    val hasLaunches: Boolean
        get() = filteredLaunches.isNotEmpty()
}
```

---

### Phase 6: UI Layer (60 minutes)

**6.1 Theme Setup**

```kotlin
// presentation/theme/Color.kt
val SpaceBlack = Color(0xFF0B0B0B)
val SpaceGray = Color(0xFF1C1C1C)
val SpaceBlue = Color(0xFF1E88E5)
val SpaceRed = Color(0xFFE53935)
val SpaceGreen = Color(0xFF43A047)
```

```kotlin
// presentation/theme/Theme.kt
@Composable
fun SpaceXplorerTheme(
    darkTheme: Boolean = true, // Space theme is dark
    content: @Composable () -> Unit
) {
    val colorScheme = darkColorScheme(
        primary = SpaceBlue,
        secondary = SpaceGray,
        background = SpaceBlack,
        surface = SpaceGray,
        error = SpaceRed
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
```

**6.2 Launches List Screen**

```kotlin
// presentation/screens/LaunchesScreen.kt
@Composable
fun LaunchesScreen(
    viewModel: LaunchesViewModel = hiltViewModel(),
    onLaunchClick: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchesContent(
        uiState = uiState,
        onRefresh = viewModel::onRefresh,
        onRetry = viewModel::onRetry,
        onSearchQueryChange = viewModel::onSearchQueryChange,
        onFilterStatusChange = viewModel::onFilterStatusChange,
        onLaunchClick = onLaunchClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LaunchesContent(
    uiState: LaunchesUiState,
    onRefresh: () -> Unit,
    onRetry: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onFilterStatusChange: (LaunchFilterStatus) -> Unit,
    onLaunchClick: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("SpaceXplorer") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { 
                    searchQuery = it
                    onSearchQueryChange(it)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Search launches...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                singleLine = true
            )

            // Filter Chips
            FilterChips(
                selectedStatus = LaunchFilterStatus.ALL,
                onStatusChange = onFilterStatusChange
            )

            // Content
            when {
                uiState.isLoading && !uiState.hasLaunches -> {
                    LoadingContent()
                }
                uiState.error != null && !uiState.hasLaunches -> {
                    ErrorContent(
                        message = uiState.error,
                        onRetry = onRetry
                    )
                }
                else -> {
                    LaunchesList(
                        launches = uiState.filteredLaunches,
                        isRefreshing = uiState.isRefreshing,
                        onRefresh = onRefresh,
                        onLaunchClick = onLaunchClick
                    )
                }
            }
        }
    }
}

@Composable
fun FilterChips(
    selectedStatus: LaunchFilterStatus,
    onStatusChange: (LaunchFilterStatus) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        LaunchFilterStatus.values().forEach { status ->
            FilterChip(
                selected = selectedStatus == status,
                onClick = { onStatusChange(status) },
                label = { Text(status.name) }
            )
        }
    }
}

@Composable
fun LaunchesList(
    launches: List<Launch>,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onLaunchClick: (String) -> Unit
) {
    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh
    ) {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(
                items = launches,
                key = { it.id }
            ) { launch ->
                LaunchCard(
                    launch = launch,
                    onClick = { onLaunchClick(launch.id) }
                )
            }
        }
    }
}

@Composable
fun LaunchCard(
    launch: Launch,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Mission Patch
            AsyncImage(
                model = launch.links?.patchSmall,
                contentDescription = "${launch.name} patch",
                modifier = Modifier.size(64.dp),
                placeholder = painterResource(R.drawable.ic_rocket_placeholder),
                error = painterResource(R.drawable.ic_rocket_placeholder)
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Launch Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = launch.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = launch.formattedDate,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Text(
                    text = launch.isSuccessDisplay,
                    style = MaterialTheme.typography.labelMedium
                )
            }

            // Arrow
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null
            )
        }
    }
}

@Composable
fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
fun ErrorContent(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Something went wrong",
            style = MaterialTheme.typography.headlineSmall
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
}
```

**6.3 Launch Detail Screen**

```kotlin
// presentation/screens/LaunchDetailScreen.kt
@Composable
fun LaunchDetailScreen(
    launchId: String,
    viewModel: LaunchDetailViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(launchId) {
        viewModel.loadLaunch(launchId)
    }

    LaunchDetailContent(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onOpenWebcast = viewModel::openWebcast,
        onOpenArticle = viewModel::openArticle
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LaunchDetailContent(
    uiState: LaunchDetailUiState,
    onNavigateBack: () -> Unit,
    onOpenWebcast: () -> Unit,
    onOpenArticle: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.launch?.name ?: "Launch Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            uiState.error != null -> {
                ErrorContent(message = uiState.error, onRetry = {})
            }
            uiState.launch != null -> {
                LaunchDetail(
                    launch = uiState.launch,
                    rocket = uiState.rocket,
                    modifier = Modifier.padding(padding),
                    onOpenWebcast = onOpenWebcast,
                    onOpenArticle = onOpenArticle
                )
            }
        }
    }
}

@Composable
fun LaunchDetail(
    launch: Launch,
    rocket: Rocket?,
    modifier: Modifier = Modifier,
    onOpenWebcast: () -> Unit,
    onOpenArticle: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Large Patch
        AsyncImage(
            model = launch.links?.patchLarge,
            contentDescription = null,
            modifier = Modifier
                .size(200.dp)
                .align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Status
        StatusBadge(success = launch.success)

        Spacer(modifier = Modifier.height(16.dp))

        // Flight Number
        InfoRow("Flight #", launch.flightNumber.toString())
        InfoRow("Date", launch.formattedDate)
        InfoRow("Rocket", rocket?.name ?: "Loading...")

        Spacer(modifier = Modifier.height(16.dp))

        // Description
        launch.details?.let { details ->
            Text(
                text = "Details",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = details,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Actions
        launch.links?.webcast?.let { _ ->
            OutlinedButton(
                onClick = onOpenWebcast,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.PlayArrow, null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Watch Webcast")
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        launch.links?.article?.let { _ ->
            OutlinedButton(
                onClick = onOpenArticle,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Article, null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Read Article")
            }
        }
    }
}

@Composable
fun StatusBadge(success: Boolean?) {
    val (text, color) = when (success) {
        true -> "SUCCESS" to SpaceGreen
        false -> "FAILED" to SpaceRed
        null -> "UPCOMING" to SpaceBlue
    }

    Surface(
        color = color.copy(alpha = 0.2f),
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            text = text,
            color = color,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelLarge
        )
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
```

---

### Phase 7: Navigation (15 minutes)

```kotlin
// presentation/navigation/NavigationRoutes.kt
object Routes {
    const val LAUNCHES = "launches"
    const val LAUNCH_DETAIL = "launch/{launchId}"

    fun launchDetail(launchId: String) = "launch/$launchId"
}

// presentation/navigation/NavGraph.kt
@Composable
fun SpaceXNavGraph(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Routes.LAUNCHES
    ) {
        composable(Routes.LAUNCHES) {
            LaunchesScreen(
                onLaunchClick = { launchId ->
                    navController.navigate(Routes.launchDetail(launchId))
                }
            )
        }

        composable(
            route = Routes.LAUNCH_DETAIL,
            arguments = listOf(
                navArgument("launchId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val launchId = backStackEntry.arguments?.getString("launchId") ?: return@composable
            LaunchDetailScreen(
                launchId = launchId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
```

---

### Phase 8: DI Setup (15 minutes)

```kotlin
// di/NetworkModule.kt
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.spacexdata.com/v4/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideSpaceXApi(retrofit: Retrofit): SpaceXApi {
        return retrofit.create(SpaceXApi::class.java)
    }
}

// di/DatabaseModule.kt
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "spacexplorer.db"
        ).build()
    }

    @Provides
    fun provideLaunchDao(database: AppDatabase): LaunchDao {
        return database.launchDao()
    }
}

// di/RepositoryModule.kt
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

---

### Phase 9: Main Activity (5 minutes)

```kotlin
// MainActivity.kt
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            SpaceXplorerTheme {
                SpaceXNavGraph()
            }
        }
    }
}

// SpaceXplorerApplication.kt
@HiltAndroidApp
class SpaceXplorerApplication : Application()
```

---

### Phase 10: Testing (60 minutes)

**10.1 Repository Test**

```kotlin
// test/data/repository/LaunchRepositoryImplTest.kt
@OptIn(ExperimentalCoroutinesApi::class)
class LaunchRepositoryImplTest {
    private val testDispatcher = StandardTestDispatcher()

    @MockK
    private lateinit var api: SpaceXApi

    @MockK
    private lateinit var dao: LaunchDao

    private lateinit var repository: LaunchRepositoryImpl

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(testDispatcher)
        repository = LaunchRepositoryImpl(api, dao)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `getLaunches with empty cache should emit loading`() = runTest {
        // Given
        every { dao.getAllLaunches() } returns flowOf(emptyList())

        // When
        repository.getLaunches().test {
            val result = awaitItem()
            assertTrue(result is Result.Loading)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `getLaunches with cached data should emit success`() = runTest {
        // Given
        val entities = listOf(
            LaunchEntity("1", 1, "FalconSat", "2006-03-24", false, false, null, null, null, null, null, null, "rocket1", "pad1")
        )
        every { dao.getAllLaunches() } returns flowOf(entities)

        // When
        repository.getLaunches().test {
            val result = awaitItem()
            assertTrue(result is Result.Success)
            assertEquals(1, result.data()?.size)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `refreshLaunches should fetch from API and cache`() = runTest {
        // Given
        val launches = listOf(
            LaunchDto(
                id = "1",
                flightNumber = 1,
                name = "FalconSat",
                dateUtc = "2006-03-24T22:30:00.000Z",
                upcoming = false,
                success = false,
                details = null,
                links = null,
                rocketId = "rocket1",
                launchpadId = "pad1"
            )
        )
        coEvery { api.getAllLaunches() } returns Response.success(launches)
        coEvery { dao.insertLaunches(any()) } just Runs

        // When
        val result = repository.refreshLaunches()

        // Then
        assertTrue(result is Result.Success)
        coVerify { dao.insertLaunches(any()) }
    }

    @Test
    fun `refreshLaunches should return error on API failure`() = runTest {
        // Given
        coEvery { api.getAllLaunches() } throws IOException("Network error")

        // When
        val result = repository.refreshLaunches()

        // Then
        assertTrue(result is Result.Error)
        assertEquals("Network error", (result as Result.Error).message)
    }
}
```

**10.2 ViewModel Test**

```kotlin
// test/presentation/viewmodels/LaunchesViewModelTest.kt
@OptIn(ExperimentalCoroutinesApi::class)
class LaunchesViewModelTest {
    private val testDispatcher = StandardTestDispatcher()

    @MockK
    private lateinit var getLaunchesUseCase: GetLaunchesUseCase

    @MockK
    private lateinit var refreshLaunchesUseCase: RefreshLaunchesUseCase

    @MockK
    private lateinit var filterLaunchesUseCase: FilterLaunchesUseCase

    private lateinit var viewModel: LaunchesViewModel

    private val sampleLaunches = listOf(
        Launch("1", 1, "FalconSat", "2006-03-24", false, false, null, null, "rocket1", "pad1"),
        Launch("2", 2, "DemoSat", "2007-03-21", false, false, null, null, "rocket1", "pad1")
    )

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
    fun `when initialized, should load launches from repository`() = runTest {
        // Given - Repository emits cached data immediately (Offline-First)
        every { getLaunchesUseCase() } returns flowOf(Result.Success(sampleLaunches))
        every { filterLaunchesUseCase(any(), any(), any()) } returns sampleLaunches
        // Note: No refreshLaunchesUseCase call - Repository handles it!

        // When
        viewModel = LaunchesViewModel(getLaunchesUseCase, refreshLaunchesUseCase, filterLaunchesUseCase)
        advanceUntilIdle()

        // Then - Shows cached data immediately
        val state = viewModel.uiState.value
        assertEquals(sampleLaunches, state.launches)
        assertFalse(state.isLoading)
        // Verify refresh was NOT called by ViewModel (Repository handles it)
        coVerify(exactly = 0) { refreshLaunchesUseCase() }
    }

    @Test
    fun `when first launch with empty cache, should show loading`() = runTest {
        // Given - Empty cache, Repository emits Loading first
        every { getLaunchesUseCase() } returns flowOf(Result.Loading(), Result.Success(sampleLaunches))
        every { filterLaunchesUseCase(any(), any(), any()) } returns sampleLaunches

        // When
        viewModel = LaunchesViewModel(getLaunchesUseCase, refreshLaunchesUseCase, filterLaunchesUseCase)
        advanceUntilIdle()

        // Then - Eventually shows data
        val state = viewModel.uiState.value
        assertEquals(sampleLaunches, state.launches)
        assertFalse(state.isLoading)
    }

    @Test
    fun `when search query changes, should filter launches`() = runTest {
        // Given
        val filtered = listOf(sampleLaunches[0])
        every { getLaunchesUseCase() } returns flowOf(Result.Success(sampleLaunches))
        every { filterLaunchesUseCase(sampleLaunches, "Falcon", LaunchFilterStatus.ALL) } returns filtered

        viewModel = LaunchesViewModel(getLaunchesUseCase, refreshLaunchesUseCase, filterLaunchesUseCase)
        advanceUntilIdle()

        // When
        viewModel.onSearchQueryChange("Falcon")
        advanceUntilIdle()

        // Then
        assertEquals(filtered, viewModel.uiState.value.filteredLaunches)
    }

    @Test
    fun `when pull to refresh fails, should show error`() = runTest {
        // Given
        every { getLaunchesUseCase() } returns flowOf(Result.Success(sampleLaunches))
        coEvery { refreshLaunchesUseCase() } returns Result.Error("Network error")
        every { filterLaunchesUseCase(any(), any(), any()) } returns sampleLaunches

        viewModel = LaunchesViewModel(getLaunchesUseCase, refreshLaunchesUseCase, filterLaunchesUseCase)
        advanceUntilIdle()

        // When - User pulls to refresh
        viewModel.onRefresh()
        advanceUntilIdle()

        // Then
        assertEquals("Network error", viewModel.uiState.value.error)
        assertFalse(viewModel.uiState.value.isRefreshing)
    }
}
```

**10.3 UI Test**

```kotlin
// androidTest/presentation/screens/LaunchesScreenTest.kt
class LaunchesScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun launchesList_withLaunches_shouldDisplayLaunchNames() {
        // Given
        val launches = listOf(
            Launch("1", 1, "FalconSat", "2006-03-24", false, false, null, null, "rocket1", "pad1")
        )

        // When
        composeTestRule.setContent {
            LaunchesContent(
                uiState = LaunchesUiState(
                    allLaunches = launches,
                    filteredLaunches = launches,
                    isLoading = false
                ),
                onRefresh = {},
                onRetry = {},
                onSearchQueryChange = {},
                onFilterStatusChange = {},
                onLaunchClick = {}
            )
        }

        // Then
        composeTestRule.onNodeWithText("FalconSat").assertIsDisplayed()
    }

    @Test
    fun launchesList_whenLoading_shouldShowLoading() {
        // When
        composeTestRule.setContent {
            LaunchesContent(
                uiState = LaunchesUiState(isLoading = true),
                onRefresh = {},
                onRetry = {},
                onSearchQueryChange = {},
                onFilterStatusChange = {},
                onLaunchClick = {}
            )
        }

        // Then
        composeTestRule.onNodeWithContentDescription("Loading").assertIsDisplayed()
    }
}
```

---

## Build Instructions

**Step 1:** Create project from scratch
```bash
# In Android Studio: File → New → New Project
```

**Step 2:** Add all files following the guide above

**Step 3:** Build and run
```bash
./gradlew build
./gradlew installDebug
```

**Step 4:** Verify functionality
- [ ] List displays with mission patches
- [ ] Pull-to-refresh works
- [ ] Search filters by name
- [ ] Filter chips work (All/Success/Failed/Upcoming)
- [ ] Detail screen opens with full info
- [ ] Webcast/Article links work
- [ ] Offline mode works (after initial load)

---

## Extra Challenges

Once you complete the basic app, try these:

### Challenge 1: Animations
Add shared element transition when clicking a launch card (mission patch grows into detail screen)

### Challenge 2: Pagination
Implement infinite scroll with Paging 3 (SpaceX API supports pagination)

### Challenge 3: Offline-first
Add Room database caching for rockets and launchpads too

### Challenge 4: Deep Links
Add deep link support so `https://spacexplorer.app/launch/{id}` opens directly to launch detail

### Challenge 5: Widget
Create a home screen widget showing the next upcoming launch

---

## Learning Outcomes

By completing this project, you'll have practiced:

✅ **Clean Architecture** - domain/data/presentation layers
✅ **MVVM with StateFlow** - reactive state management
✅ **Hilt DI** - dependency injection throughout
✅ **Room Database** - offline caching
✅ **Retrofit + OkHttp** - network layer with logging
✅ **Jetpack Compose** - modern UI toolkit
✅ **Navigation Component** - type-safe navigation
✅ **Image Loading** - Coil with placeholders
✅ **Search & Filter** - Flow operators (combine, debounce)
✅ **Pull-to-Refresh** - Material3 components
✅ **Error Handling** - Result type pattern
✅ **Unit Testing** - MockK, Turbine, coroutines-test
✅ **UI Testing** - Compose testing framework

---

**Good luck! Build it from scratch without looking at the solution code first.**
