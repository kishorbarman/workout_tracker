# Crash Fixes Applied

## Critical Bug Fixed: ViewModel Flow Collection

**Problem**: The app was crashing due to improper Flow collection in `WorkoutViewModel.kt`.

### What was wrong:
The `loadWorkoutsForYear()` function was trying to collect multiple Flows sequentially within a single coroutine:
```kotlin
// WRONG - This causes the coroutine to hang
repository.getWorkoutsByYear(year).collect { ... }
// Never reaches here because collect() suspends indefinitely
repository.getWorkoutDayCountForYear(year).collect { ... }
```

### What was fixed:
Each Flow collection now runs in its own separate coroutine:
```kotlin
// CORRECT - Each Flow gets its own coroutine
viewModelScope.launch {
    repository.getWorkoutsByYear(year).collect { workouts ->
        // Update UI state
    }
}

viewModelScope.launch {
    repository.getWorkoutDayCountForYear(year).collect { count ->
        // Update UI state
    }
}
```

## Changes Made

### 1. Fixed WorkoutViewModel.kt (line 52-105)
- Split multiple Flow collections into separate coroutines
- Each data stream (workouts, count, goal) now observes independently
- Added proper error handling for each stream
- Removed double initialization in `init` block

### 2. Fixed AndroidManifest.xml (line 13-15)
- Changed icon references from `@mipmap/` to `@drawable/`
- This prevents crashes related to missing bitmap launcher icons
- The app now uses the vector drawable icon

## How to Test

1. **Clean the project in Android Studio**:
   - Build → Clean Project
   - Build → Rebuild Project

2. **Uninstall existing app from emulator** (if installed):
   ```bash
   adb uninstall com.workouttracker.app
   ```

3. **Run the app**:
   - Click Run button in Android Studio
   - Or use: `./gradlew installDebug`

## What to Expect

The app should now:
- ✅ Launch without crashing
- ✅ Show the dashboard with calendar
- ✅ Display "No goal set for 2026" initially
- ✅ Allow you to tap "Set Goal" button
- ✅ Allow you to tap calendar dates to log workouts

## Still Seeing Crashes?

If the app still crashes, check Android Studio's Logcat:

1. Open Logcat (View → Tool Windows → Logcat)
2. Filter by "AndroidRuntime" or "FATAL EXCEPTION"
3. Look for the crash stack trace
4. Common issues to check:
   - Missing Google Drive credentials (won't crash, but sync won't work)
   - Database migration issues (uninstall and reinstall)
   - Compose compatibility issues (check Gradle sync)

## Additional Notes

### Google Drive Sync
- The app will prompt for Google sign-in on first launch
- You can skip this - the app works fine without Google Drive
- To enable sync, configure OAuth credentials as per README.md

### Initial State
- No workouts logged
- No goal set (you need to set one manually)
- Calendar shows current month of current year
- All streak counts will be 0

## Files Modified

1. `app/src/main/java/com/workouttracker/app/ui/viewmodel/WorkoutViewModel.kt`
   - Lines 39-105: Fixed Flow collection logic

2. `app/src/main/AndroidManifest.xml`
   - Lines 13-15: Fixed icon references

## Next Steps

After confirming the app launches:

1. **Set a goal**: Tap the "Set Goal" button and enter 200 days
2. **Log a workout**: Tap any date on the calendar
3. **Fill details**: Choose time, type, and notes
4. **Save**: The date should now be highlighted
5. **Check progress**: Progress card should update

The app is now ready to use! 🎉
