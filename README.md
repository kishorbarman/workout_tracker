# Workout Tracker

A simple and intuitive Android app to track your workout days throughout the year. Set yearly goals, log workouts with a tap, and sync your data to Google Drive.

## Features

- **Yearly Goals**: Set a custom workout goal for each year (e.g., 200 days in 2026)
- **Quick Logging**: Tap any date on the calendar to quickly log a workout
- **Workout Details**: Track date/time, workout type, and optional notes
- **Progress Tracking**:
  - Visual progress bar toward your yearly goal
  - Current and longest workout streaks
  - Calendar view with marked workout days
- **Google Drive Backup**: Auto-sync your data to Google Drive after each workout
- **Multi-Year Support**: View and track workouts across different years
- **Local Storage**: All data stored locally using Room database

## Tech Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose with Material Design 3
- **Database**: Room (SQLite)
- **Cloud Sync**: Google Drive API
- **Architecture**: MVVM with Repository pattern

## Prerequisites

- Android Studio Hedgehog (2023.1.1) or later
- Android SDK 26+ (Android 8.0+)
- Google account for Drive backup

## Setup Instructions

### 1. Clone the Repository

```bash
cd /Users/kishorbarman/projects/workout_tracker
```

### 2. Configure Google Drive API

To enable Google Drive backup, you need to set up Google Cloud credentials:

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project or select an existing one
3. Enable the **Google Drive API**
4. Create OAuth 2.0 credentials:
   - Go to "Credentials" → "Create Credentials" → "OAuth client ID"
   - Application type: **Android**
   - Package name: `com.workouttracker.app`
   - Get your SHA-1 fingerprint by running:
     ```bash
     keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android
     ```
   - Enter the SHA-1 fingerprint
   - Click "Create"

### 3. Build and Run

1. Open the project in Android Studio
2. Wait for Gradle sync to complete
3. Connect an Android device or start an emulator
4. Click "Run" or press Shift+F10

## Project Structure

```
workout_tracker/
├── app/
│   ├── src/main/
│   │   ├── java/com/workouttracker/app/
│   │   │   ├── data/
│   │   │   │   ├── backup/          # Google Drive backup service
│   │   │   │   ├── local/           # Room database entities, DAOs
│   │   │   │   ├── model/           # Data models
│   │   │   │   └── repository/      # Repository layer
│   │   │   ├── ui/
│   │   │   │   ├── components/      # Reusable UI components
│   │   │   │   ├── screens/         # App screens
│   │   │   │   ├── theme/           # Material theme
│   │   │   │   └── viewmodel/       # ViewModels
│   │   │   └── MainActivity.kt
│   │   ├── res/                     # Resources (layouts, strings, etc.)
│   │   └── AndroidManifest.xml
│   └── build.gradle.kts
├── build.gradle.kts
├── settings.gradle.kts
└── README.md
```

## Usage

### First Launch

1. Sign in to your Google account when prompted
2. Grant permissions for Google Drive access
3. Set your yearly goal (default: 200 days)

### Logging a Workout

1. Tap any date on the calendar
2. Select the time
3. Choose a workout type (Cardio, Strength, Yoga, etc.)
4. Add optional notes
5. Tap "Save"
6. Data automatically syncs to Google Drive

### Viewing Progress

- The progress card shows your current count toward the goal
- Streak cards display your current and longest workout streaks
- Switch between years using the year selector
- Navigate months with the calendar arrows

### Setting/Updating Goals

1. Tap the edit icon on the progress card
2. Enter your desired goal (1-366 days)
3. Tap "Save"

### Manual Sync

- Tap the cloud sync icon in the top bar to manually sync
- Sync status appears at the bottom of the screen

## Database Schema

### Workout Entity
- `id`: Primary key (auto-generated)
- `dateTime`: LocalDateTime of the workout
- `workoutType`: Type of workout (enum)
- `notes`: Optional notes
- `year`: Year of the workout

### YearlyGoal Entity
- `year`: Primary key
- `goalDays`: Target number of workout days

## Backup Format

Data is backed up to Google Drive in JSON format:

```json
{
  "workouts": [
    {
      "id": 1,
      "dateTime": "2026-01-09T14:30:00",
      "workoutType": "CARDIO",
      "notes": "30 min run",
      "year": 2026
    }
  ],
  "goals": [
    {
      "year": 2026,
      "goalDays": 200
    }
  ],
  "lastBackupTime": "2026-01-09T14:35:00"
}
```

## Features Not Implemented (Future Enhancements)

- Workout history list view
- Edit/delete existing workouts
- Weekly/monthly statistics charts
- Export data to CSV
- Workout reminders/notifications
- Multiple workout sessions per day tracking
- Dark mode toggle

## Troubleshooting

### Google Sign-In Issues
- Ensure you've configured OAuth credentials correctly
- Check that the package name matches: `com.workouttracker.app`
- Verify the SHA-1 fingerprint is correct

### Build Errors
- Clean and rebuild: Build → Clean Project, then Build → Rebuild Project
- Invalidate caches: File → Invalidate Caches / Restart

### Sync Not Working
- Check internet connection
- Ensure you're signed in to Google
- Check Google Drive API is enabled in Cloud Console

## License

This project is for personal use. Feel free to modify and adapt it to your needs.

## Contact

Created by Kishor Barman
