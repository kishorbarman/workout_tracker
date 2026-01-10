# Calendar Sync Troubleshooting Guide

## Common Issues and Solutions

### Issue 1: "Calendar sync failed" Error

**Most Common Cause:** The app was signed in BEFORE you enabled the Calendar API or added the calendar scope to your OAuth consent screen.

**Solution:**
1. **Sign out** from the app:
   - Scroll down to the Google Account card
   - Tap "Sign out"

2. **Sign in again**:
   - Tap "Sign in with Google"
   - You should now see a consent screen requesting **both** Drive and Calendar permissions
   - Grant both permissions

3. **Try syncing** again

---

### Issue 2: Missing Calendar Permission

**Symptoms:**
- Error message like "403 Forbidden" or "Insufficient Permission"
- "Calendar access denied"

**Solution:**

#### Step 1: Verify Calendar API is Enabled
1. Go to https://console.cloud.google.com/apis/library/calendar-json.googleapis.com
2. Select your project
3. Make sure it shows "API Enabled" (green checkmark)
4. If not, click "Enable"

#### Step 2: Verify OAuth Consent Screen Has Calendar Scope
1. Go to https://console.cloud.google.com/apis/credentials/consent
2. Click "Edit App"
3. Navigate to "Scopes" page
4. Verify these scopes are added:
   - ✅ `https://www.googleapis.com/auth/drive.file`
   - ✅ `https://www.googleapis.com/auth/calendar`
5. If Calendar scope is missing:
   - Click "Add or Remove Scopes"
   - Search for "Google Calendar API"
   - Check the box for `https://www.googleapis.com/auth/calendar`
   - Click "Update"
   - Click "Save and Continue"

#### Step 3: Force Re-authentication
1. In the app, sign out
2. Sign in again
3. The consent screen should now show Calendar permission request

---

### Issue 3: "Not signed in to Google"

**Solution:**
- Make sure you're signed in with Google
- Look for the green "Google Account" card on the dashboard
- If you see "Google Drive Backup" card instead, you're not signed in
- Tap "Sign in with Google"

---

### Issue 4: Calendar Created but No Events

**Possible Causes:**
- No workouts logged yet
- Workouts exist but sync hasn't been triggered

**Solution:**
1. Make sure you have some workouts logged
2. Tap the sync button (cloud icon) in the top bar
3. Wait for "Synced successfully to Drive and Calendar" message
4. Open Google Calendar
5. Check that "Kishor's workout schedule" calendar is visible and checked

---

### Issue 5: Can't Find "Kishor's workout schedule" Calendar

**Solution:**

**On Google Calendar Web:**
1. Go to https://calendar.google.com
2. Look in the left sidebar under "My calendars"
3. If not visible, click the "+" next to "Other calendars"
4. Click "Browse calendars of interest"
5. Look for "Kishor's workout schedule"
6. Check the box to show it

**On Google Calendar Mobile:**
1. Open Google Calendar app
2. Tap hamburger menu (≡)
3. Scroll down to find "Kishor's workout schedule"
4. Tap the checkbox to show events

**If still not there:**
- The calendar hasn't been created yet (sync failed or never ran)
- Try syncing again from the app

---

### Issue 6: Events Have Wrong Times

**Cause:** Time zone mismatch

**Solution:**
- Events use your device's time zone
- Make sure your device has the correct time zone set
- If you travel to a different time zone, events will adjust automatically

---

### Issue 7: Duplicate Events in Calendar

**This shouldn't happen** - the app clears all events before syncing.

**If it does happen:**
1. Delete the calendar manually:
   - Go to Google Calendar settings
   - Find "Kishor's workout schedule"
   - Click "Remove calendar"
2. Sync from the app again
3. A fresh calendar will be created

---

## How to Get Error Details

If sync is failing and you want to see the exact error:

1. The error message appears in the sync status card on the dashboard
2. It will say something like:
   - "Sync failed: Calendar: [error message]"
   - "Sync failed: Drive: [error message]"
3. Take a screenshot and share it for more specific help

---

## Testing Calendar Sync Step-by-Step

### Step 1: Verify Google Cloud Setup
- [ ] Calendar API is enabled
- [ ] OAuth consent screen has calendar scope
- [ ] You've signed out and signed back in to the app

### Step 2: Log a Test Workout
1. Open the app
2. Tap any date on the calendar
3. Log a workout with:
   - Time: Current time
   - Duration: 60 minutes
   - Type: CARDIO
   - Notes: "Test workout"
4. Tap "Save"

### Step 3: Sync
1. Tap the sync button (cloud icon) in top bar
2. Watch for sync status message
3. Should say "Synced successfully to Drive and Calendar"

### Step 4: Verify in Google Calendar
1. Open https://calendar.google.com
2. Look for "Kishor's workout schedule" in left sidebar
3. Check the box to show events
4. You should see:
   - Event title: "Workout: CARDIO"
   - Event time: Matches what you logged
   - Event duration: 60 minutes
   - Event description: "Test workout"
   - Event color: Blue (for CARDIO)

---

## What Happens During Sync

When you tap the sync button, the app:

1. **Drive Backup:**
   - Creates a JSON file with all workouts and goals
   - Uploads to Google Drive in app folder
   - File name: "workout_backup.json"

2. **Calendar Sync:**
   - Checks if "Kishor's workout schedule" calendar exists
   - Creates it if needed
   - Deletes all existing events from this calendar
   - Creates a new event for each workout
   - Sets event times, duration, type, notes, and color

Both operations happen in parallel (at the same time).

---

## Still Having Issues?

If you've tried all the above and sync still fails:

1. **Check the exact error message** in the app's sync status card
2. **Try these nuclear options:**
   - Uninstall and reinstall the app
   - Clear app data (Settings → Apps → Workout Tracker → Clear Data)
   - Revoke app access in Google Account settings, then sign in again
3. **Verify your Google account** has Calendar enabled:
   - Some G Suite/Workspace accounts have Calendar disabled
   - Try with a personal Gmail account to test

---

## Success Checklist

You'll know calendar sync is working when:
- ✅ Sync status says "Synced successfully to Drive and Calendar"
- ✅ "Kishor's workout schedule" appears in Google Calendar
- ✅ Workout events appear at correct times
- ✅ Events have correct duration (in minutes)
- ✅ Events are color-coded by type
- ✅ Event descriptions show your notes
- ✅ Events update when you edit workouts
- ✅ Events disappear when you delete workouts
