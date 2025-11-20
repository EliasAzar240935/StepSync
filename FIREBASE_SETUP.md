# Firebase Setup Guide for StepSync

This guide will help you set up Firebase for the StepSync Android application.

## Prerequisites

- Android Studio Hedgehog (2023.1.1) or later
- A Google account
- The StepSync project cloned and opened in Android Studio

## Step 1: Create a Firebase Project

1. Go to the [Firebase Console](https://console.firebase.google.com/)
2. Click "Add project" or "Create a project"
3. Enter project name: `StepSync` (or your preferred name)
4. (Optional) Enable Google Analytics for your project
5. Click "Create project" and wait for the setup to complete

## Step 2: Add Android App to Firebase

1. In the Firebase Console, click on the Android icon to add an Android app
2. Register your app with the following details:
   - **Android package name**: `com.stepsync` (must match exactly)
   - **App nickname**: StepSync (optional)
   - **Debug signing certificate SHA-1**: (optional, but recommended for development)
3. Click "Register app"

### Getting Debug SHA-1 Certificate (Optional but Recommended)

Run this command in your terminal:
```bash
keytool -list -v -alias androiddebugkey -keystore ~/.android/debug.keystore -storepass android -keypass android
```

Copy the SHA-1 certificate fingerprint and paste it in the Firebase Console.

## Step 3: Download google-services.json

1. After registering your app, Firebase will provide a `google-services.json` file
2. Click "Download google-services.json"
3. Move the downloaded file to your project's `app/` directory:
   ```
   StepSync/
   └── app/
       └── google-services.json  ← Place file here
   ```

**Important**: The `google-services.json` file contains your Firebase project configuration and should be placed in the `app/` directory (NOT in the root directory).

## Step 4: Enable Firebase Authentication

1. In the Firebase Console, go to **Build** → **Authentication**
2. Click "Get started"
3. Go to the **Sign-in method** tab
4. Enable **Email/Password** authentication:
   - Click on "Email/Password"
   - Toggle "Enable" to ON
   - Click "Save"

## Step 5: Set Up Firestore Database

1. In the Firebase Console, go to **Build** → **Firestore Database**
2. Click "Create database"
3. Select **Start in production mode** (we'll add security rules next)
4. Choose a Cloud Firestore location (select the one closest to your users)
5. Click "Enable"

## Step 6: Configure Firestore Security Rules

1. In the Firestore Database section, go to the **Rules** tab
2. Replace the default rules with the following:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Helper function to check if user is authenticated
    function isAuthenticated() {
      return request.auth != null;
    }
    
    // Helper function to check if user owns the resource
    function isOwner(userId) {
      return isAuthenticated() && request.auth.uid == userId;
    }
    
    // Users collection - users can only read/write their own data
    match /users/{userId} {
      allow read, write: if isOwner(userId);
    }
    
    // Step records - users can only access their own records
    match /stepRecords/{recordId} {
      allow read: if isAuthenticated() && resource.data.userId == request.auth.uid;
      allow create: if isAuthenticated() && request.resource.data.userId == request.auth.uid;
      allow update, delete: if isAuthenticated() && resource.data.userId == request.auth.uid;
    }
    
    // Activities - users can only access their own activities
    match /activities/{activityId} {
      allow read: if isAuthenticated() && resource.data.userId == request.auth.uid;
      allow create: if isAuthenticated() && request.resource.data.userId == request.auth.uid;
      allow update, delete: if isAuthenticated() && resource.data.userId == request.auth.uid;
    }
    
    // Goals - users can only access their own goals
    match /goals/{goalId} {
      allow read: if isAuthenticated() && resource.data.userId == request.auth.uid;
      allow create: if isAuthenticated() && request.resource.data.userId == request.auth.uid;
      allow update, delete: if isAuthenticated() && resource.data.userId == request.auth.uid;
    }
    
    // Friends - allow authenticated users to read and manage friendships
    match /friends/{friendId} {
      allow read: if isAuthenticated();
      allow create: if isAuthenticated() && 
                      (request.resource.data.userId == request.auth.uid || 
                       request.resource.data.friendUserId == request.auth.uid);
      allow update, delete: if isAuthenticated() && 
                              (resource.data.userId == request.auth.uid || 
                               resource.data.friendUserId == request.auth.uid);
    }
    
    // Achievements - users can only access their own achievements
    match /achievements/{achievementId} {
      allow read: if isAuthenticated() && resource.data.userId == request.auth.uid;
      allow create: if isAuthenticated() && request.resource.data.userId == request.auth.uid;
      allow update, delete: if isAuthenticated() && resource.data.userId == request.auth.uid;
    }
  }
}
```

3. Click "Publish" to save the rules

## Step 7: Enable Offline Persistence (Optional)

Firestore supports offline data persistence automatically in Android. The app is configured to enable this by default, allowing users to access and modify data even when offline. Changes will sync automatically when connectivity is restored.

## Step 8: Set Up Firebase Cloud Messaging (Optional)

If you want to enable push notifications:

1. In the Firebase Console, go to **Build** → **Cloud Messaging**
2. No additional setup is required for Android
3. The app is already configured to receive FCM messages

## Firestore Database Structure

The app uses the following Firestore collections:

```
firestore/
├── users/{userId}
│   ├── email: string
│   ├── name: string
│   ├── age: number
│   ├── weight: number
│   ├── height: number
│   ├── fitnessGoal: string
│   ├── dailyStepGoal: number
│   ├── createdAt: timestamp
│   └── updatedAt: timestamp
│
├── stepRecords/{recordId}
│   ├── userId: string
│   ├── date: string
│   ├── steps: number
│   ├── distance: number
│   ├── calories: number
│   └── timestamp: timestamp
│
├── activities/{activityId}
│   ├── userId: string
│   ├── activityType: string
│   ├── startTime: timestamp
│   ├── endTime: timestamp
│   ├── duration: number
│   ├── distance: number
│   ├── calories: number
│   ├── steps: number
│   └── notes: string
│
├── goals/{goalId}
│   ├── userId: string
│   ├── goalType: string
│   ├── targetValue: number
│   ├── currentValue: number
│   ├── period: string
│   ├── startDate: string
│   ├── endDate: string
│   ├── isCompleted: boolean
│   └── createdAt: timestamp
│
├── friends/{friendId}
│   ├── userId: string
│   ├── friendUserId: string
│   ├── friendName: string
│   ├── friendEmail: string
│   ├── status: string
│   └── createdAt: timestamp
│
└── achievements/{achievementId}
    ├── userId: string
    ├── achievementType: string
    ├── title: string
    ├── description: string
    ├── iconName: string
    └── unlockedAt: timestamp
```

## Testing Your Setup

1. Sync your Gradle files in Android Studio
2. Build the project - it should compile without errors
3. Run the app on an emulator or physical device
4. Try registering a new user
5. Check the Firebase Console:
   - Go to **Authentication** → **Users** to see the new user
   - Go to **Firestore Database** to see the created documents

## Troubleshooting

### Issue: "google-services.json not found"
**Solution**: Make sure the `google-services.json` file is in the `app/` directory, not in the project root.

### Issue: "Default FirebaseApp is not initialized"
**Solution**: 
- Ensure `google-services.json` is in the correct location
- Clean and rebuild the project
- Make sure the package name in `google-services.json` matches `com.stepsync`

### Issue: Authentication fails
**Solution**:
- Check that Email/Password authentication is enabled in Firebase Console
- Verify internet connectivity
- Check Android logs for specific error messages

### Issue: Firestore permission denied
**Solution**:
- Verify that security rules are properly configured
- Ensure the user is authenticated before accessing Firestore
- Check that the userId in the document matches the authenticated user's UID

## Security Best Practices

1. **Never commit google-services.json to public repositories** - Add it to `.gitignore` if needed
2. **Use production mode security rules** - The rules above ensure users can only access their own data
3. **Enable App Check** (Optional) - For additional security against abuse
4. **Monitor usage** - Regularly check the Firebase Console for unusual activity
5. **Set up billing alerts** - To avoid unexpected charges

## Additional Resources

- [Firebase Documentation](https://firebase.google.com/docs)
- [Firestore Documentation](https://firebase.google.com/docs/firestore)
- [Firebase Authentication Documentation](https://firebase.google.com/docs/auth)
- [Android Setup Guide](https://firebase.google.com/docs/android/setup)

## Support

If you encounter any issues during setup, please:
1. Check the troubleshooting section above
2. Review Firebase Console logs
3. Check Android Studio logcat for error messages
4. Refer to Firebase documentation
5. Open an issue on the GitHub repository

---

**Note**: The `google-services.json` file is required to run the app. Without it, Firebase services will not work. Follow the steps above to obtain your configuration file.
