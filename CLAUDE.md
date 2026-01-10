# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

```bash
# Debug build
./gradlew assembleDebug

# Release build (requires signing config)
./gradlew assembleRelease

# Install on connected device/emulator
./gradlew installDebug

# Run unit tests
./gradlew test

# Run instrumented tests
./gradlew connectedAndroidTest

# Clean build
./gradlew clean
```

## Architecture

This is a Kotlin Android app using **MVVM with Repository pattern** and Jetpack Compose.

### Layer Structure

```
UI Layer (Compose) → ViewModel → Repository → Data Sources (Room DB + Google APIs)
```

- **MainActivity** (`MainActivity.kt`): Entry point. Initializes database, repository, and backup services. Handles Google Sign-In via `ActivityResultContracts`.

- **WorkoutViewModel** (`ui/viewmodel/WorkoutViewModel.kt`): Central state holder using `StateFlow<WorkoutUiState>`. Manages workout CRUD operations, streak calculations, and sync coordination. Contains business logic for calculating current/longest streaks.

- **WorkoutRepository** (`data/repository/WorkoutRepository.kt`): Coordinates between Room DAOs. Uses Kotlin Flow for reactive data.

- **Room Database** (`data/local/database/WorkoutDatabase.kt`): SQLite with two tables - `workouts` and `yearly_goals`. Uses KSP for annotation processing.

### Key Data Flow

1. UI triggers ViewModel methods (e.g., `addWorkout()`)
2. ViewModel calls Repository
3. Repository updates Room database
4. Flow emissions propagate back to UI via `StateFlow`
5. Auto-sync to Google Drive/Calendar after mutations

### Google Integration

Two services handle cloud sync:
- `GoogleDriveBackupService`: JSON backup/restore to Drive
- `GoogleCalendarService`: Syncs workouts as calendar events

Both require OAuth setup with `DRIVE_FILE` and `CALENDAR` scopes.

## Key Dependencies

- **UI**: Jetpack Compose with Material 3, Navigation Compose
- **Database**: Room 2.6.1 with KSP
- **Async**: Kotlin Coroutines + Flow
- **Google APIs**: Play Services Auth, Drive API, Calendar API
- **Serialization**: Gson

## Project Configuration

- **Min SDK**: 26 (Android 8.0)
- **Target/Compile SDK**: 34 (Android 14)
- **Kotlin**: 1.9.20
- **Java**: 17
- **Compose Compiler**: 1.5.4
