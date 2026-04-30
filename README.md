# Pictet Technologies - Android Interview Project

Complete Android application demonstrating modern architecture patterns for Senior Android Developer interview.

## Project Overview

This project was built as interview preparation for **Pictet Technologies** (interview date: May 5th, 2026). It showcases modern Android development skills through a fully functional app consuming the [JSONPlaceholder API](https://jsonplaceholder.typicode.com/).

### Features

- **Real API Integration**: Live data from jsonplaceholder.typicode.com
- **Jetpack Compose UI**: Material3 design with responsive layouts
- **MVVM Architecture**: Clean separation of concerns
- **StateFlow**: Reactive state management
- **Hilt DI**: Dependency injection throughout
- **Navigation Component**: Type-safe navigation with list-detail flow
- **Comprehensive Testing**: 17+ unit tests with MockK and Turbine
- **Error Handling**: Graceful error states with retry functionality

### Tech Stack

| Category | Technologies |
|----------|--------------|
| **UI** | Jetpack Compose, Material3 |
| **Architecture** | MVVM, Clean Architecture |
| **State Management** | StateFlow, Flow |
| **DI** | Hilt |
| **Networking** | Retrofit, OkHttp, Gson |
| **Testing** | JUnit 4, MockK, Turbine, Compose UI Test |
| **Async** | Kotlin Coroutines |

---

## Quick Start

### Prerequisites

- Android Studio Ladybug (2024.2.1) or newer
- JDK 17
- Android SDK 36

### Build

```bash
# Build the project
./gradlew build

# Run all tests
./gradlew test

# Install debug APK
./gradlew installDebug
```

### Run Tests

```bash
# Unit tests only
./gradlew testDebugUnitTest

# Specific test class
./gradlew testDebugUnitTest --tests "PostsViewModelTest"

# UI tests (requires emulator/device)
./gradlew connectedDebugAndroidTest
```

---

## Project Structure

```
pictet/
├── app/
│   └── src/
│       ├── main/
│       │   └── java/com/bms/pictet/
│       │       ├── data/
│       │       │   ├── remote/api/          # Retrofit interfaces
│       │       │   ├── remote/model/        # DTOs
│       │       │   └── repository/          # Repository implementations
│       │       ├── domain/
│       │       │   ├── model/               # Domain models
│       │       │   ├── repository/          # Repository interfaces
│       │       │   └── usecase/             # Use cases
│       │       ├── presentation/
│       │       │   ├── navigation/          # Navigation setup
│       │       │   ├── screens/             # Compose UI
│       │       │   ├── theme/               # Material3 theme
│       │       │   └── viewmodels/          # ViewModels
│       │       └── di/                      # Hilt modules
│       ├── test/                          # Unit tests
│       └── androidTest/                     # Instrumented tests
├── gradle/
│   └── libs.versions.toml                   # Version catalog
└── Documentation/
    ├── CLAUDE.md                           # Project guide for Claude
    ├── STEP_BY_STEP_GUIDE.md               # Complete implementation guide
    ├── INTERVIEW_PREP_GUIDE.md            # Interview preparation
    ├── INTERVIEW_CHEATSHEET.md            # Quick reference
    ├── BEST_PRACTICES_GUIDE.md            # Code review checklist
    ├── MOB_PROGRAMMING_SCENARIOS.md       # Practice scenarios
    ├── CODE_REVIEW_EXERCISES.md           # Code review practice
    ├── REFACTORING_CHALLENGES.md          # Refactoring exercises
    └── ARCHITECTURE_DISCUSSIONS.md        # Architecture topics
```

---

## Architecture

The project implements **Clean Architecture + MVVM** with three distinct layers:

### Data Layer
- Retrofit API interfaces
- DTOs mapping to API responses
- Repository implementations with caching

### Domain Layer
- Business models (Post, User, etc.)
- Repository interfaces
- Use cases for business operations

### Presentation Layer
- Jetpack Compose screens
- ViewModels with StateFlow
- Navigation Component integration

### Data Flow
```
UI (Compose) → ViewModel → UseCase → Repository → API (Retrofit)
     ↑              ↓           ↓            ↓
StateFlow      StateFlow   Result<T>   Response<T>
```

---

## Branch Workflow

This project follows a branch-per-task workflow:

| Branch | Content | Status |
|--------|---------|--------|
| `main` | Empty project template | ✅ |
| `task/1-api-integration` | Retrofit + API models | ✅ |
| `task/2-mvvm-flow` | Repository + StateFlow | ✅ |
| `task/3-compose-ui` | Jetpack Compose screens | ✅ |
| `task/4-hilt-di` | Dependency Injection | ✅ |
| `task/5-navigation-component` | Navigation list→detail | ✅ |
| `task/6-comprehensive-testing` | 17+ unit tests | ✅ |
| `task/7-mob-programming-scenarios` | Interview practice materials | ✅ |
| `task/1-final-review-and-docs` | Final documentation | ✅ |

**Each branch compiles and is functional.**

---

## Testing

### Test Coverage

- **ViewModel Tests**: 8 tests covering state management, refresh, retry, selection
- **Repository Tests**: 5 tests for caching and API integration
- **UseCase Tests**: 3 tests for delegation verification
- **UI Tests**: 5 Compose UI tests for screen interactions

**Total: 17+ tests, all passing**

### Test Technologies

- **MockK**: Kotlin-friendly mocking
- **Turbine**: Flow testing library
- **coroutines-test**: Structured concurrency testing
- **Compose UI Test**: UI interaction testing

---

## Documentation

### Essential Reading

| Document | Purpose |
|----------|---------|
| [INTERVIEW_CHEATSHEET.md](INTERVIEW_CHEATSHEET.md) | Quick one-page reference |
| [STEP_BY_STEP_GUIDE.md](STEP_BY_STEP_GUIDE.md) | Complete implementation guide |
| [INTERVIEW_PREP_GUIDE.md](INTERVIEW_PREP_GUIDE.md) | Interview strategy |

### Practice Materials

| Document | Purpose |
|----------|---------|
| [MOB_PROGRAMMING_SCENARIOS.md](MOB_PROGRAMMING_SCENARIOS.md) | 7 coding scenarios |
| [CODE_REVIEW_EXERCISES.md](CODE_REVIEW_EXERCISES.md) | 5 review exercises |
| [REFACTORING_CHALLENGES.md](REFACTORING_CHALLENGES.md) | 5 refactoring exercises |
| [ARCHITECTURE_DISCUSSIONS.md](ARCHITECTURE_DISCUSSIONS.md) | 10 discussion topics |

---

## API Reference

The app consumes [JSONPlaceholder](https://jsonplaceholder.typicode.com/):

- `GET /posts` - Returns 100 posts
- `GET /posts/{id}` - Returns single post
- `GET /users` - Returns users

---

## Configuration

| Property | Value |
|----------|-------|
| Application ID | `com.bms.pictet` |
| Compile SDK | 36 |
| Min SDK | 24 (Android 7.0) |
| Target SDK | 35 |
| Kotlin | 2.0.21 |
| Java | 17 |
| AGP | 8.10.1 |

---

## License

This project is for interview preparation purposes.

---

**Good luck with the interview! 🚀**
