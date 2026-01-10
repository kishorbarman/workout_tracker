# Google Drive Setup Guide

## Quick Fix: Use the App Without Google Drive

**Good news**: I've disabled the automatic Google Sign-In prompt. The app now works perfectly without Google Drive!

### What Changed
- App no longer prompts for Google Sign-In on launch
- All workout tracking features work locally (saved to device)
- You can optionally set up Google Drive later if you want cloud backup

### Using the App Now
1. **Clean and rebuild** in Android Studio
2. **Run the app** - it should launch without the Google error
3. **Start tracking workouts** - everything is saved locally
4. The sync button will show "Not signed in to Google" if you tap it

---

## Option: Enable Google Drive Sync Later (Optional)

If you want cloud backup, follow these steps:

### Step 1: Create Google Cloud Project

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Click "Select a project" → "New Project"
3. Name it "Workout Tracker"
4. Click "Create"

### Step 2: Enable Google Drive API

1. In your project, go to "APIs & Services" → "Library"
2. Search for "Google Drive API"
3. Click on it and press "Enable"

### Step 3: Create OAuth Consent Screen

1. Go to "APIs & Services" → "OAuth consent screen"
2. Select "External" (unless you have a Google Workspace)
3. Click "Create"

**Fill in required fields:**
- App name: `Workout Tracker`
- User support email: Your email
- Developer contact: Your email
- Click "Save and Continue"

**Scopes:**
- Click "Add or Remove Scopes"
- Search for "Google Drive API"
- Select `.../auth/drive.file` (not full drive access)
- Click "Update" → "Save and Continue"

**Test users:**
- Click "Add Users"
- Add your Gmail address
- Click "Save and Continue"

### Step 4: Create OAuth 2.0 Credentials

1. Go to "APIs & Services" → "Credentials"
2. Click "Create Credentials" → "OAuth client ID"
3. Application type: **Android**

**Configure:**
- Name: `Workout Tracker Android`
- Package name: `com.workouttracker.app`
- SHA-1 certificate fingerprint: Get it by running:

```bash
keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android
```

Copy the SHA1 value (looks like: `AB:CD:EF:12:34:56...`)

4. Click "Create"

### Step 5: Add Your Email as Test User

1. Go back to "OAuth consent screen"
2. Under "Test users", click "Add Users"
3. Enter your Gmail address (the one you use on your phone/emulator)
4. Click "Save"

### Step 6: Publish the App (Optional but Recommended)

**For testing only:**
- Your app is in "Testing" mode
- Only test users you add can sign in
- This is fine for personal use!

**To remove the warning permanently:**
1. Go to "OAuth consent screen"
2. Click "Publish App"
3. Click "Confirm"
4. Note: Google may review it, but for personal use you can keep it in testing

### Step 7: Test in the App

1. Rebuild and run the app
2. Tap the sync button (cloud icon in top bar)
3. Sign in with your Google account
4. Grant permissions
5. Sync should now work!

---

## Troubleshooting

### Still seeing "app is being tested" error?

**Check:**
- ✅ Your email is added as a test user in OAuth consent screen
- ✅ You're using the same Google account on emulator/phone
- ✅ Package name is exactly: `com.workouttracker.app`
- ✅ SHA-1 fingerprint matches (debug keystore for dev builds)

### Different SHA-1 for release builds

If you're making a release build, you'll need to:
1. Get the release keystore SHA-1
2. Add another OAuth client with the release SHA-1

### App works but sync fails?

Check Logcat for error messages:
- Network issues?
- Permissions denied?
- API not enabled?

---

## Summary

**For now (Recommended):**
- ✅ App works without Google Drive
- ✅ Data saved locally on device
- ✅ No setup required
- ✅ Start tracking immediately!

**For later (Optional):**
- Follow steps above to enable cloud backup
- Takes ~10 minutes to set up
- Only needed if you want cross-device sync

---

## Current App Status

✅ **What works without Google Drive:**
- Calendar view
- Workout logging
- Goal setting
- Progress tracking
- Streak counting
- Local data storage
- Everything except cloud sync!

❌ **What needs Google Drive:**
- Automatic cloud backup
- Restore from backup
- Cross-device sync

The app is fully functional without Google Drive. Cloud sync is just a bonus feature! 🎉
