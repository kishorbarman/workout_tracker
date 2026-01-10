# Quick Start Guide

## Building and Running the App

### Option 1: Using Android Studio (Recommended)

1. **Install Android Studio**
   - Download from https://developer.android.com/studio
   - Install Android SDK (API level 26+)

2. **Open the Project**
   ```bash
   # Open Android Studio
   # Click "Open" and navigate to:
   /Users/kishorbarman/projects/workout_tracker
   ```

3. **Wait for Gradle Sync**
   - Android Studio will automatically sync Gradle dependencies
   - This may take a few minutes on first run

4. **Set Up Google Drive OAuth (Important!)**
   - See detailed instructions in README.md
   - You'll need to create OAuth credentials in Google Cloud Console
   - Without this, the app will work but Google Drive sync won't function

5. **Run the App**
   - Connect an Android device (USB debugging enabled) or start an emulator
   - Click the "Run" button (green play icon) or press Shift+F10
   - Select your device/emulator from the list

### Option 2: Using Command Line

```bash
cd /Users/kishorbarman/projects/workout_tracker

# Build debug APK
./gradlew assembleDebug

# Install on connected device
./gradlew installDebug

# Or build and install in one step
./gradlew build
adb install app/build/outputs/apk/debug/app-debug.apk
```

## First-Time Setup

### 1. Google Sign-In
- On first launch, you'll be prompted to sign in to Google
- Grant permissions for Google Drive access
- **Note**: If OAuth isn't configured, you can skip this but won't have cloud backup

### 2. Set Your Goal
- The app will prompt you to set a yearly goal
- Default suggestion: 200 days
- You can change this anytime by tapping the edit icon on the dashboard

### 3. Log Your First Workout
- Tap any date on the calendar
- Fill in the time, workout type, and optional notes
- Tap "Save"
- The date will be marked on the calendar!

## Key Features to Try

1. **Calendar View**: See all your workout days at a glance
2. **Progress Card**: Track your progress toward your yearly goal
3. **Streaks**: View your current and longest workout streaks
4. **Year Selector**: Switch between different years
5. **Sync Button**: Top-right cloud icon syncs to Google Drive

## Troubleshooting

### Build Fails
```bash
# Clean and rebuild
./gradlew clean
./gradlew build
```

### Google Sign-In Not Working
- Check that OAuth credentials are configured
- Verify SHA-1 fingerprint matches
- Ensure package name is `com.workouttracker.app`

### App Crashes
- Check Android version (minimum API 26 / Android 8.0)
- View logs in Android Studio: View → Tool Windows → Logcat

## Project Structure Overview

```
Key Files to Know:
├── MainActivity.kt                    # App entry point
├── ui/screens/DashboardScreen.kt      # Main screen
├── ui/viewmodel/WorkoutViewModel.kt   # Business logic
├── data/local/database/               # Database
└── data/backup/                       # Google Drive sync
```

## Development Tips

### Making Changes

1. **Modify UI**: Edit files in `ui/screens/` and `ui/components/`
2. **Change Database**: Edit entities in `data/local/entity/`
   - Remember to increment database version in `WorkoutDatabase.kt`
3. **Add Features**: Start with ViewModel, then Repository, then UI

### Testing Changes

- Use Android Studio's hot reload (Ctrl+\ or Cmd+\)
- For database changes, uninstall and reinstall the app
- Check Logcat for debugging info

## Next Steps

- Set up Google Drive OAuth for cloud backup
- Customize workout types in `WorkoutType.kt`
- Add your own theme colors in `ui/theme/Theme.kt`
- Start logging your workouts!

## Need Help?

- Check the main README.md for detailed documentation
- Review code comments in key files
- Android Studio has built-in documentation (hover over code + F1)

Happy tracking! 💪
