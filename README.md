# StepSync - Android Fitness Tracking Application

A comprehensive Android fitness tracking application built with Kotlin and Jetpack Compose.

## Features

### Core Features
- **User Authentication & Profile Management**: Register, login, and manage your profile
- **Step Tracking**: Real-time step counting with background service support
- **Activity Tracking**: Track multiple activity types (Walking, Running, Cycling, Gym, Swimming)
- **Goals & Challenges**: Set and track daily, weekly, and monthly fitness goals
- **Social Features**: Add friends, view leaderboards, and participate in challenges
- **Data Visualization**: Charts and graphs for step count, activities, and calories
- **Notifications**: Daily reminders and achievement notifications

### Technical Features
- **MVVM Architecture** with Clean Architecture principles
- **Room Database** for local data persistence
- **Hilt** for Dependency Injection
- **Jetpack Compose** for modern UI with Material Design 3
- **Coroutines & Flow** for asynchronous operations
- **Foreground Service** for background step counting
- **WorkManager** for periodic sync tasks

## Project Structure

```
app/
├── src/
│   ├── main/
│   │   ├── java/com/stepsync/
│   │   │   ├── data/                  # Data layer
│   │   │   │   ├── local/             # Room database
│   │   │   │   │   ├── database/      # Database definition
│   │   │   │   │   ├── dao/           # Data Access Objects
│   │   │   │   │   └── entities/      # Database entities
│   │   │   │   ├── repository/        # Repository implementations
│   │   │   │   └── model/             # Data models
│   │   │   ├── domain/                # Domain layer
│   │   │   │   ├── usecase/           # Use cases (business logic)
│   │   │   │   └── repository/        # Repository interfaces
│   │   │   ├── presentation/          # Presentation layer
│   │   │   │   ├── auth/              # Authentication screens
│   │   │   │   ├── home/              # Home/Dashboard
│   │   │   │   ├── profile/           # Profile management
│   │   │   │   ├── activity/          # Activity tracking
│   │   │   │   ├── goals/             # Goals & challenges
│   │   │   │   ├── social/            # Social features
│   │   │   │   ├── statistics/        # Charts & statistics
│   │   │   │   ├── settings/          # App settings
│   │   │   │   └── theme/             # Material Design 3 theme
│   │   │   ├── service/               # Background services
│   │   │   ├── util/                  # Utility classes
│   │   │   ├── di/                    # Dependency injection modules
│   │   │   └── StepSyncApplication.kt # Application class
│   │   ├── res/                       # Resources
│   │   └── AndroidManifest.xml        # App manifest
│   └── build.gradle.kts               # App-level Gradle configuration
└── build.gradle.kts                   # Project-level Gradle configuration
```

## Database Schema

### Entities
- **User**: User profile information
- **StepRecord**: Daily step count records
- **Activity**: Tracked fitness activities
- **Goal**: User-defined fitness goals
- **Friend**: Friend relationships
- **Achievement**: Unlocked achievements
- **Challenge**: Group challenges

## Getting Started

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or later
- Android SDK 26 or higher
- Kotlin 1.9.20 or later
- Gradle 8.2 or later

### Building the Project

1. Clone the repository:
   ```bash
   git clone https://github.com/EliasAzar240935/StepSync.git
   cd StepSync
   ```

2. Open the project in Android Studio

3. Sync Gradle files

4. Build and run the application on an emulator or physical device

### Required Permissions
- `ACTIVITY_RECOGNITION` - For step counting
- `FOREGROUND_SERVICE` - For background step tracking
- `POST_NOTIFICATIONS` - For notifications
- `ACCESS_FINE_LOCATION` - For GPS-based activities (optional)
- `WAKE_LOCK` - For background service

## Technologies Used

### Core Libraries
- **Kotlin** - Primary programming language
- **Jetpack Compose** - Modern Android UI toolkit
- **Material Design 3** - Design system

### Architecture Components
- **Room** - Local database
- **ViewModel** - UI state management
- **LiveData/Flow** - Reactive data streams
- **Navigation Component** - Screen navigation

### Dependency Injection
- **Hilt/Dagger** - Dependency injection framework

### Asynchronous Programming
- **Kotlin Coroutines** - Asynchronous programming
- **Flow** - Reactive streams

### Background Tasks
- **WorkManager** - Background job scheduling
- **Foreground Service** - Continuous step tracking

### Data Visualization
- **MPAndroidChart** - Charts and graphs

### Image Loading
- **Coil** - Image loading library

### Networking (Ready for Backend)
- **Retrofit** - HTTP client
- **OkHttp** - HTTP/HTTPS implementation
- **Gson** - JSON serialization

## Features in Detail

### Authentication
- Email/password registration
- User login with validation
- Profile creation with fitness goals

### Step Tracking
- Real-time step counting using device sensors
- Daily step goal tracking
- Progress visualization
- Step history with charts

### Activity Tracking
- Start/stop activity tracking
- Multiple activity types support
- Distance and calorie calculations
- Activity history logs

### Goals & Achievements
- Create custom fitness goals
- Daily, weekly, and monthly challenges
- Achievement system with badges
- Progress tracking and notifications

### Social Features
- Friend management (add/remove friends)
- Activity feed
- Leaderboards
- Group challenges

### Statistics & Visualization
- Step count charts (daily, weekly, monthly)
- Activity distribution graphs
- Calorie burn visualization
- Trend analysis

## Architecture

The app follows Clean Architecture principles with three main layers:

1. **Data Layer**: Database entities, DAOs, and repository implementations
2. **Domain Layer**: Repository interfaces and business logic (use cases)
3. **Presentation Layer**: ViewModels, UI screens (Composables)

### Key Design Patterns
- **MVVM (Model-View-ViewModel)**: Separation of UI and business logic
- **Repository Pattern**: Abstraction over data sources
- **Dependency Injection**: Loose coupling and testability
- **Observer Pattern**: Reactive UI updates with StateFlow

## Future Enhancements

- Backend API integration for user synchronization
- Cloud data backup
- Advanced analytics with machine learning
- Integration with wearable devices
- Social media sharing
- Premium features (custom themes, advanced stats)

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## License

This project is created as a demonstration of Android development best practices.

## Contact

For questions or suggestions, please open an issue on GitHub.

---

**Note**: This application requires an Android device with a step counter sensor for accurate step tracking. The app will work in emulators but step tracking functionality will be limited.
