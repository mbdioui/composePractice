# Android Dependency Setup Guide

## How to Think About Dependencies

### Step 1: Identify What Your App Needs

Before searching for libraries, list your app's requirements:

```
My SpaceX app needs:
- Display lists and details (UI framework → Jetpack Compose)
- Fetch data from API (Networking → Retrofit + OkHttp)
- Save data locally (Database → Room)
- Manage object creation (DI → Hilt)
- Load images from URLs (Image loading → Coil)
- Handle async operations (Async → Coroutines)
```

### Step 2: Find the Right Library

**Official Sources (Priority 1):**
- [Android Developers - Jetpack](https://developer.android.com/jetpack)
- [Kotlin Documentation](https://kotlinlang.org/docs/)
- Library's GitHub repository (check Stars, last update, issues)

**Search Strategy:**
```
"Android [feature] library"
"Jetpack [feature]"
"Kotlin [feature]"
"Best [feature] library Android 2025"
```

**Red Flags to Avoid:**
- Last update > 2 years ago
- Many unresolved critical issues
- No longer maintained (archived repo)
- Doesn't support your minSdk version

### Step 3: Check Compatibility

**Version Compatibility Matrix:**

| Your Setup | Library Constraints |
|------------|---------------------|
| Kotlin 2.0.x | Requires compatible compiler plugins |
| compileSdk 36 | Use latest stable libraries |
| minSdk 24 | Library must support API 24+ |
| AGP 8.10.x | Must work with Gradle 8.7+ |

**Tools to Check:**
- [Maven Central](https://search.maven.org/) - Search artifacts
- [Google Maven](https://maven.google.com/web/index.html) - AndroidX versions

---

## Essential Libraries & Their Roles

### UI Layer

| Library | Group | Purpose | When You Need It |
|---------|-------|---------|------------------|
| `androidx.compose:compose-bom` | BOM | Manages all Compose versions together | Using Jetpack Compose |
| `androidx.activity:activity-compose` | Activity | Enables `setContent { }` in Activities | Compose in Activities |
| `androidx.lifecycle:lifecycle-viewmodel-compose` | Lifecycle | `viewModel()` function in Compose | ViewModels in Compose |
| `androidx.navigation:navigation-compose` | Navigation | Navigation between screens | Multiple screens |

**Dependency Pattern:**
```kotlin
// BOM first - sets versions for all Compose libs
implementation(platform(libs.androidx.compose.bom))

// Then add Compose libs without versions
implementation(libs.androidx.compose.ui)
implementation(libs.androidx.compose.material3)
implementation(libs.androidx.activity.compose)
```

---

### Dependency Injection (Hilt)

| Library | Group | Purpose | When You Need It |
|---------|-------|---------|------------------|
| `com.google.dagger:hilt-android` | Dagger | Runtime DI container | Managing object creation |
| `com.google.dagger:hilt-compiler` | Dagger | Generates DI code at build time | Using Hilt annotations |
| `androidx.hilt:hilt-navigation-compose` | AndroidX | `hiltViewModel()` for Compose | ViewModels with Navigation |

**Think:** "Who creates my objects?"
- Without Hilt: You manually `val repo = LaunchRepository(api, dao)`
- With Hilt: Just declare `@Inject constructor()` - Hilt creates and provides

---

### Networking (Retrofit + OkHttp)

| Library | Group | Purpose | When You Need It |
|---------|-------|---------|------------------|
| `com.squareup.retrofit2:retrofit` | Square | Type-safe HTTP client | Calling REST APIs |
| `com.squareup.retrofit2:converter-gson` | Square | JSON ↔ Kotlin objects | JSON responses |
| `com.squareup.okhttp3:logging-interceptor` | Square | Logs HTTP requests/responses | Debugging API calls |

**Architecture:**
```
Your Code → Retrofit Interface → OkHttp Client → Server
                ↓
           Gson Converter (JSON → Objects)
```

**Alternative:** Ktor Client (Kotlin-native, coroutines-first)

---

### Local Database (Room)

| Library | Group | Purpose | When You Need It |
|---------|-------|---------|------------------|
| `androidx.room:room-runtime` | AndroidX | Database operations | Local data storage |
| `androidx.room:room-compiler` | AndroidX | Generates database code | Using Room annotations |
| `androidx.room:room-ktx` | AndroidX | Flow + suspend support | Reactive DB + Coroutines |

**Think:** "Do I need data to survive app restarts?"
- Yes → Use Room (or DataStore for simple key-value)
- No → In-memory cache is enough

**Without room-ktx:** DAOs return `List<Thing>` (blocking)
**With room-ktx:** DAOs return `Flow<List<Thing>>` (reactive)

---

### Image Loading (Coil)

| Library | Group | Purpose | When You Need It |
|---------|-------|---------|------------------|
| `io.coil-kt:coil-compose` | Coil | Load images from URLs/bitmap | Displaying remote images |

**Usage:**
```kotlin
AsyncImage(
    model = "https://example.com/image.jpg",
    contentDescription = "Mission patch"
)
```

**Alternatives:** Glide (Google), Fresco (Meta)
- Coil is Kotlin-first and works best with Compose

---

### Async Programming (Coroutines)

| Library | Group | Purpose | When You Need It |
|---------|-------|---------|------------------|
| `org.jetbrains.kotlinx:kotlinx-coroutines-android` | Kotlin | Coroutines on Android | Any async operation |
| `org.jetbrains.kotlinx:kotlinx-coroutines-test` | Kotlin | Testing coroutines | Unit tests with suspend |

**Coroutines solve:**
- Callback hell → Sequential code
- Thread blocking → Suspended (lightweight)
- Manual lifecycle → Structured concurrency

---

## Version Selection Strategy

### Step 1: Use BOM for Related Libraries

```toml
# Compose - use BOM
composeBom = "2025.04.01"  # One version for all Compose libs

# Then omit versions in libraries:
androidx-compose-ui = { group = "androidx.compose.ui", name = "ui" }
```

### Step 2: Align Versions by Compatibility

```toml
[versions]
# These must work together:
kotlin = "2.0.21"          # Language version
hilt = "2.56.1"            # Supports Kotlin 2.0
composeCompiler = "2.0.21"   # Matches Kotlin version
```

**Check compatibility:**
- Hilt release notes: "Requires Kotlin 2.0+"
- Compose Compiler: Must match Kotlin version

### Step 3: Latest Stable vs Specific Version

| Strategy | When to Use |
|----------|-------------|
| Latest stable | New projects, active development |
| Pin version | Production apps, stability critical |
| Version catalog | Share versions across modules |

**Rule of thumb:**
- BOM versions → Latest stable
- Critical libraries (Hilt, Room) → Check release notes first
- Test libraries → Latest (doesn't affect production)

---

## Dependency Verification Checklist

After adding a dependency, verify:

```bash
# 1. Sync works
./gradlew build --configuration-cache

# 2. No version conflicts
./gradlew dependencies --configuration implementation | grep "FAILED"

# 3. APK size impact (optional)
./gradlew app:analyzeDebugBundle
```

---

## Common Mistakes

### 1. Mixing Incompatible Versions
```kotlin
// BAD - May crash at runtime
kotlin = "2.0.21"
hilt = "2.48"        # Old version, incompatible with Kotlin 2.0
```

### 2. Forgetting the Compiler
```kotlin
// BAD - Annotations do nothing
implementation(libs.room.runtime)
// Missing: kapt(libs.room.compiler)
```

### 3. Duplicate Dependencies
```kotlin
// BAD - Both do the same thing
implementation(libs.retrofit)
implementation(libs.ktor.client)  // Pick one!
```

### 4. Using Implementation Instead of Test
```kotlin
// BAD - Test libraries in production APK
implementation(libs.mockk)  // Should be testImplementation
```

---

## Quick Reference: When to Use What

| Feature | Library | Alternative |
|---------|---------|-------------|
| UI Framework | Jetpack Compose | XML Layouts |
| Navigation | Navigation Compose | Manual navigation |
| Dependency Injection | Hilt | Koin, manual |
| Networking | Retrofit | Ktor Client, OkHttp only |
| JSON Parsing | Gson | Moshi, Kotlinx Serialization |
| Database | Room | SQLDelight, Realm |
| Image Loading | Coil | Glide, Fresco |
| Async | Coroutines | RxJava, Callbacks |
| Testing | JUnit + MockK + Turbine | Espresso, Robolectric |

---

## Example: Adding a New Feature

**Scenario:** Add user authentication

```
Step 1: Identify needs
- Login API call → Retrofit (already have)
- Store token securely → DataStore or EncryptedSharedPreferences
- Auth header on requests → OkHttp Interceptor

Step 2: Find libraries
Search: "Android secure token storage"
Result: androidx.security:security-crypto or DataStore

Step 3: Check compatibility
- minSdk: 24? Yes
- Kotlin: 2.0.x? Yes
- Last update: Recent? Yes

Step 4: Add to version catalog
datastore = "1.1.0"
androidx-datastore = { group = "androidx.datastore", name = "datastore-preferences", version.ref = "datastore" }

Step 5: Use in code
val Context.dataStore by preferencesDataStore(name = "auth")
```

---

## Resources

- [AndroidX Releases](https://developer.android.com/jetpack/androidx/versions)
- [Maven Central Search](https://search.maven.org/)
- [Gradle Version Catalogs](https://docs.gradle.org/current/userguide/platforms.html)
- [Square Open Source](https://square.github.io/) (Retrofit, OkHttp, etc.)
