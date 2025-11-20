# StepSync - Architecture Documentation

## Overview
StepSync is a comprehensive Android fitness tracking application built with modern Android development best practices, following Clean Architecture principles with MVVM pattern.

## Architecture Layers

### 1. Presentation Layer (UI)
Located in `presentation/` package. Implements MVVM pattern using Jetpack Compose.

#### Components:
- **Screens**: Composable functions for UI
  - `auth/` - Login and Registration
  - `home/` - Dashboard with step count
  - `profile/` - User profile management
  - `activity/` - Activity tracking and history
  - `goals/` - Goals and challenges
  - `social/` - Friends and leaderboards
  
- **ViewModels**: State management and business logic coordination
  - Each screen has its corresponding ViewModel
  - Uses StateFlow for reactive state management
  - Handles user interactions and data transformations

- **Navigation**: Single-activity architecture with Navigation Compose
  - Defined in `Navigation.kt` and `StepSyncApp.kt`
  - Type-safe navigation between screens

- **Theme**: Material Design 3 implementation
  - `Color.kt` - Light and dark color schemes
  - `Type.kt` - Typography definitions
  - `Theme.kt` - Theme composition

### 2. Domain Layer
Located in `domain/` package. Contains business logic interfaces.

#### Components:
- **Repository Interfaces**: Contracts for data operations
  - `UserRepository` - User authentication and profile
  - `StepRecordRepository` - Step tracking data
  - `ActivityRepository` - Activity records
  - `GoalRepository` - Fitness goals
  - `FriendRepository` - Social features
  - `AchievementRepository` - Achievement system

### 3. Data Layer
Located in `data/` package. Implements data operations.

#### Components:
- **Local Database** (Room):
  - **Entities**: Database tables
    - `UserEntity` - User accounts
    - `StepRecordEntity` - Daily step records
    - `ActivityEntity` - Activity logs
    - `GoalEntity` - User goals
    - `FriendEntity` - Friend relationships
    - `AchievementEntity` - Unlocked achievements
    - `ChallengeEntity` - Group challenges
    
  - **DAOs**: Data Access Objects with CRUD operations
    - Reactive queries using Flow
    - Suspend functions for one-time operations
    
  - **Database**: StepSyncDatabase with Room configuration

- **Repository Implementations**:
  - Implement domain repository interfaces
  - Transform entities to domain models
  - Handle data operations and error handling

- **Models**: Domain data models
  - Clean data structures without Android dependencies
  - Used in ViewModels and UI

### 4. Service Layer
Located in `service/` package.

#### Components:
- **StepCounterService**: Foreground service
  - Uses TYPE_STEP_COUNTER sensor
  - Runs continuously in background
  - Updates notification with step count
  - Persists data to database
  - Requires ACTIVITY_RECOGNITION permission

### 5. Dependency Injection
Located in `di/` package. Uses Hilt/Dagger.

#### Modules:
- **AppModule**: Application-wide dependencies
  - SharedPreferences
  - Context
  
- **DatabaseModule**: Database and DAO providers
  - Room database instance
  - All DAO instances
  
- **RepositoryModule**: Repository bindings
  - Binds implementations to interfaces

### 6. Utilities
Located in `util/` package.

#### Components:
- **Constants**: App-wide constants
- **DateUtils**: Date formatting and calculations
- **CalculationUtils**: Fitness calculations
  - Distance from steps
  - Calorie burn estimation
  - BMI calculation
  - Activity-specific calculations

## Key Features Implementation

### Step Tracking
1. **Sensor Registration**: StepCounterService registers for TYPE_STEP_COUNTER
2. **Step Detection**: Sensor events tracked in real-time
3. **Data Persistence**: Steps saved to database periodically
4. **UI Updates**: Home screen observes database changes via Flow
5. **Notifications**: Foreground service shows ongoing notification

### Activity Tracking
1. **Manual Tracking**: User starts/stops activities manually
2. **Duration Calculation**: Track start and end times
3. **Distance Estimation**: Based on activity type and duration
4. **Calorie Calculation**: Uses MET values and user profile
5. **History**: All activities stored in database

### Goals & Achievements
1. **Goal Creation**: Users set custom goals (steps, distance, calories)
2. **Progress Tracking**: Real-time progress updates
3. **Achievement Unlocking**: Automatic based on milestones
4. **Notifications**: Alert users when goals are reached

### Social Features
1. **Friend System**: Add friends by email
2. **Leaderboards**: Compare stats with friends
3. **Challenges**: Create and participate in group challenges
4. **Activity Feed**: See friend activities (ready for backend)

## Data Flow

### Example: Step Tracking
```
Sensor → Service → Repository → Database
                      ↓
                  ViewModel ← Repository
                      ↓
                     UI (Compose)
```

1. **Sensor** detects steps
2. **Service** receives sensor events
3. **Service** saves to database via Repository
4. **Repository** transforms and persists data
5. **ViewModel** observes database via Flow
6. **UI** recomposes with new data

## State Management

### StateFlow Pattern
- ViewModels expose StateFlow for UI state
- UI collects state changes in Compose
- Automatic recomposition on state updates
- Lifecycle-aware with `collectAsState()`

### Example:
```kotlin
// ViewModel
val steps: StateFlow<StepRecord?> = flow {
    emit(repository.getSteps(userId, date))
}.stateIn(scope, SharingStarted.WhileSubscribed(5000), null)

// UI
val steps by viewModel.steps.collectAsState()
Text("${steps?.steps ?: 0} steps")
```

## Background Processing

### Foreground Service
- Runs continuously while app is in use
- Shows persistent notification
- Survives app closure
- Requires FOREGROUND_SERVICE permission
- Uses FOREGROUND_SERVICE_TYPE_HEALTH

### WorkManager (Ready for Implementation)
- Periodic sync tasks
- Database cleanup
- Achievement checks
- Goal notifications

## Security & Privacy

### Authentication
- Password hashing with SHA-256
- Secure storage in Room database
- Session management with SharedPreferences

### Data Protection
- Local-only storage by default
- No data sent to external servers
- User consent for permissions
- Transparent data usage

## Testing Strategy

### Unit Tests (Ready to be added)
- ViewModel logic
- Repository operations
- Utility functions
- Calculation accuracy

### Integration Tests (Ready to be added)
- Database operations
- Repository-DAO interactions
- Service lifecycle

### UI Tests (Ready to be added)
- Navigation flows
- User interactions
- State changes

## Performance Considerations

### Database Optimization
- Indexed columns for frequent queries
- Flow for reactive queries
- Proper suspend function usage
- Background thread operations

### UI Performance
- Jetpack Compose lazy loading
- Efficient recomposition
- StateFlow with proper scoping
- Minimal state updates

### Memory Management
- Proper lifecycle handling
- ViewModel scope for coroutines
- Service cleanup on destroy
- Sensor unregistration

## Scalability

### Backend Integration (Ready)
- Repository pattern allows easy API integration
- Retrofit dependencies already included
- Data models separated from entities
- Sync logic ready to be implemented

### Feature Extensions
- Modular architecture allows easy additions
- Clean separation of concerns
- Interface-based design
- Dependency injection for flexibility

## Build Configuration

### Gradle Setup
- Kotlin DSL for type-safe configuration
- Version catalogs ready
- Compose compiler configuration
- KSP for annotation processing

### Dependencies
- Core Android libraries
- Jetpack Compose
- Room database
- Hilt/Dagger
- Coroutines
- Material Design 3
- Navigation Compose
- Charts library
- Coil image loading

## Conclusion

StepSync demonstrates modern Android development with:
- Clean Architecture for maintainability
- MVVM for clear separation of concerns
- Jetpack Compose for modern UI
- Room for reliable local storage
- Hilt for dependency management
- Coroutines for asynchronous operations
- Material Design 3 for beautiful UI
- Comprehensive feature set for fitness tracking

The architecture is scalable, testable, and ready for production deployment or backend integration.
