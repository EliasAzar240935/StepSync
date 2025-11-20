# Quick Start Guide - Firebase Setup

This is a condensed guide to get you started quickly. For detailed instructions, see [FIREBASE_SETUP.md](FIREBASE_SETUP.md).

## 🚀 5-Minute Setup

### Step 1: Create Firebase Project (2 min)
1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Click "Create a project" → Name it "StepSync" → Continue
3. (Optional) Enable Google Analytics → Continue
4. Wait for project creation → Click "Continue"

### Step 2: Add Android App (2 min)
1. Click the Android icon to add an Android app
2. Enter package name: **`com.stepsync`** (must match exactly!)
3. App nickname: StepSync
4. Click "Register app"
5. **Download `google-services.json`**
6. Move file to: `StepSync/app/google-services.json`

### Step 3: Enable Authentication (30 sec)
1. In Firebase Console → **Build** → **Authentication**
2. Click "Get started"
3. Click "Email/Password" → Toggle **Enable** → Save

### Step 4: Create Firestore Database (30 sec)
1. In Firebase Console → **Build** → **Firestore Database**
2. Click "Create database"
3. Select **"Start in production mode"**
4. Choose location (closest to your users)
5. Click "Enable"

### Step 5: Set Security Rules (1 min)
1. In Firestore → **Rules** tab
2. Copy security rules from [FIREBASE_SETUP.md#step-6-configure-firestore-security-rules](FIREBASE_SETUP.md)
3. Click "Publish"

### Step 6: Build & Test
```bash
# Open in Android Studio
# Sync Gradle files
# Build project
# Run on emulator/device
```

## ✅ Verification Checklist

- [ ] Firebase project created
- [ ] Android app registered with package name `com.stepsync`
- [ ] `google-services.json` downloaded and placed in `app/` directory
- [ ] Email/Password authentication enabled
- [ ] Firestore database created
- [ ] Security rules published
- [ ] Project builds successfully in Android Studio
- [ ] App runs on emulator/device

## 📱 Test User Registration

1. Run the app
2. Click "Register"
3. Fill in user details
4. Submit registration
5. Check Firebase Console → Authentication → Users (should see new user)
6. Check Firestore Database → users collection (should see user document)

## 🐛 Troubleshooting

### "Default FirebaseApp is not initialized"
- ✅ Verify `google-services.json` is in `app/` directory
- ✅ Clean and rebuild project
- ✅ Check package name matches: `com.stepsync`

### "Permission denied" in Firestore
- ✅ Verify user is logged in
- ✅ Check security rules are published
- ✅ Verify user is accessing only their own data

### Build errors
- ✅ Sync Gradle files
- ✅ Clean project: Build → Clean Project
- ✅ Rebuild: Build → Rebuild Project
- ✅ Invalidate caches: File → Invalidate Caches / Restart

## 📚 Full Documentation

- **Setup Details**: [FIREBASE_SETUP.md](FIREBASE_SETUP.md) - Complete setup guide
- **Migration Info**: [FIREBASE_MIGRATION.md](FIREBASE_MIGRATION.md) - Technical details
- **Security**: [SECURITY_SUMMARY.md](SECURITY_SUMMARY.md) - Security analysis
- **Project Overview**: [README.md](README.md) - Project information

## 🔐 Security Rules Template

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    function isAuthenticated() {
      return request.auth != null;
    }
    
    function isOwner(userId) {
      return isAuthenticated() && request.auth.uid == userId;
    }
    
    match /users/{userId} {
      allow read, write: if isOwner(userId);
    }
    
    match /stepRecords/{recordId} {
      allow read, write: if isAuthenticated() && 
                           resource.data.userId == request.auth.uid;
    }
    
    match /activities/{activityId} {
      allow read, write: if isAuthenticated() && 
                           resource.data.userId == request.auth.uid;
    }
    
    match /goals/{goalId} {
      allow read, write: if isAuthenticated() && 
                           resource.data.userId == request.auth.uid;
    }
    
    match /friends/{friendId} {
      allow read: if isAuthenticated();
      allow create, update, delete: if isAuthenticated();
    }
    
    match /achievements/{achievementId} {
      allow read, write: if isAuthenticated() && 
                           resource.data.userId == request.auth.uid;
    }
  }
}
```

## 💡 Pro Tips

1. **Use Test Project**: Create a separate Firebase project for testing
2. **Monitor Usage**: Check Firebase Console regularly for activity
3. **Set Billing Alerts**: Configure alerts to avoid surprise costs
4. **Backup Rules**: Save security rules in version control
5. **Test Offline**: Firebase works offline - test with airplane mode

## 🎯 Next Steps After Setup

1. Test user registration and login
2. Create sample data (steps, activities, goals)
3. Test real-time sync (use multiple devices/emulators)
4. Test offline mode
5. Verify security rules (try accessing other users' data)
6. Monitor Firebase Console for errors

## 📞 Need Help?

- Check [FIREBASE_SETUP.md](FIREBASE_SETUP.md) for detailed instructions
- Check [SECURITY_SUMMARY.md](SECURITY_SUMMARY.md) for security questions
- Review Firebase Console logs for errors
- Check Android Studio logcat for error messages

---

**Time to Complete**: 5-10 minutes
**Difficulty**: Easy
**Prerequisites**: Google account, Android Studio

Ready to build amazing things! 🚀
