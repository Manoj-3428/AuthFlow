## App Screenshots

<p align="center">
  <img src="https://github.com/user-attachments/assets/7797ee6b-12b7-4fb1-ab12-4ad26e25b787" width="180"/>
  <img src="https://github.com/user-attachments/assets/2a386007-3b5b-4c0d-ba25-6e18e6151167" width="180"/>
  <img src="https://github.com/user-attachments/assets/4636c6ba-b902-4b94-b128-6ab3b1adf333" width="180"/>
  <img src="https://github.com/user-attachments/assets/1e690aa1-1ae9-422a-b044-9c17722f4030" width="180"/>
  
</p>
![auth5](https://github.com/user-attachments/assets/93cd8d16-6688-4466-a750-5e53c9d496b7)

# AuthFlow - Passwordless Authentication App

A passwordless authentication Android application built with Jetpack Compose, implementing Email + OTP login flow with session tracking.

## Features Implemented

### ✅ 1. Email + OTP Login
- User enters email address
- Taps "Send OTP" button
- 6-digit OTP is generated locally
- User enters OTP in 6 individual input fields
- Automatic focus movement between OTP fields
- OTP verification on button click

### ✅ 2. OTP Rules (Fully Implemented)
- **OTP Length**: 6 digits (enforced in `OtpManager`)
- **OTP Expiry**: 60 seconds (countdown timer displayed)
- **Maximum Attempts**: 3 attempts per OTP
- **Per-Email Storage**: OTP data stored in `Map<String, OtpData>` - each email has its own OTP
- **Resend Logic**: 
  - Generating new OTP invalidates the old one
  - Resets attempt count to 0
  - Clears previous OTP data before generating new one

### ✅ 3. Session Screen
- **Session Start Time**: Displays formatted time (HH:mm:ss)
- **Session Start Date**: Displays formatted date (MMM dd, yyyy)
- **Live Session Duration**: Updates every second in mm:ss format
- **Logout Button**: Properly stops timer and cleans up state
- **Timer Implementation**: 
  - Uses `LaunchedEffect` with coroutines
  - Survives recompositions (state stored in ViewModel)
  - Properly cancelled on logout

### ✅ 4. External SDK Integration - Firebase Analytics
- **Dependency**: Firebase Analytics integrated via `build.gradle.kts`
- **Initialization**: Properly initialized in `MainActivity.onCreate()`
- **AnalyticsLogger**: Singleton pattern with thread-safe initialization
- **Events Logged**:
  - ✅ OTP generated (`logOtpSent`)
  - ✅ OTP validation success (`logOtpVerified`)
  - ✅ OTP validation failure (`logOtpVerificationFailed`)
  - ✅ Logout (`logSessionEnded`)
  - ✅ Additional events: OTP expired, max attempts exceeded, OTP resent, session started

### ✅ 5. Edge Cases Handled
- ✅ **Expired OTP**: Auto-detected, shows error message and snackbar
- ✅ **Incorrect OTP**: Tracks attempts, shows error after each failed attempt
- ✅ **Exceeded OTP Attempts**: Blocks further attempts, shows error message
- ✅ **Resend OTP Flow**: Validates if resend is allowed, clears old OTP, generates new one
- ✅ **Screen Rotation**: State preserved using ViewModel (StateFlow) and proper lifecycle management

### ✅ 6. Technical Implementation

#### Jetpack Compose
- ✅ `@Composable` functions for all screens
- ✅ `remember` for local UI state (OTP input, focus requesters)
- ✅ `rememberSaveable` not needed (state in ViewModel)
- ✅ `LaunchedEffect` for:
  - OTP countdown timer
  - Session duration timer
  - Auto-focus management
  - State-based side effects
- ✅ State hoisting: All business logic in ViewModel, UI only handles display
- ✅ Recomposition handling: Proper use of `remember`, `LaunchedEffect`, and state management

#### Architecture
- ✅ **ViewModel**: `AuthViewModel` manages all business logic
- ✅ **One-way Data Flow**: 
  - UI → Events → ViewModel → State → UI
  - No direct state mutations from UI
- ✅ **Separation of Concerns**:
  - UI Layer: `LoginScreen`, `OtpScreen`, `SessionScreen`
  - ViewModel Layer: `AuthViewModel`, `AuthState`, `AuthEvent`
  - Data Layer: `OtpManager`
  - Analytics Layer: `AnalyticsLogger`

#### Kotlin / Data Structures
- ✅ **Map Usage**: `MutableMap<String, OtpData>` for per-email OTP storage
- ✅ **Sealed Classes**: `AuthState`, `OtpValidationResult` for type-safe state management
- ✅ **Data Classes**: `OtpData` for structured OTP information
- ✅ **Time-based Logic**: 
  - System.currentTimeMillis() for expiry tracking
  - Coroutines with delay() for timers
  - TimeUnit conversions for readability
- ✅ **Defensive Coding**:
  - Mutex for thread-safe OTP operations
  - Null checks and safe calls
  - Input validation (email blank check, OTP length check)
  - Proper error handling with sealed result types

### ✅ 7. Project Structure
```
app/src/main/java/com/example/authflow/
├── analytics/
│   └── AnalyticsLogger.kt          # Firebase Analytics integration
├── data/
│   └── OtpManager.kt               # OTP generation, validation, storage
├── ui/
│   ├── LoginScreen.kt             # Email input screen
│   ├── OtpScreen.kt               # OTP verification screen
│   ├── SessionScreen.kt           # Active session screen
│   └── TopBar.kt                  # Reusable top bar component
├── viewmodel/
│   ├── AuthViewModel.kt           # Business logic & state management
│   └── AuthState.kt               # Sealed class for UI states
└── MainActivity.kt                # App entry point
```

## OTP Logic and Expiry Handling

### OTP Generation
- Random 6-digit number generated (100000-999999)
- Stored in `OtpData` with:
  - `code`: The 6-digit OTP string
  - `expiryTimeMillis`: Timestamp when OTP expires
  - `attemptCount`: Number of failed verification attempts

### Expiry Handling
1. **Generation**: OTP expiry set to current time + 60 seconds
2. **Validation**: Checks if `System.currentTimeMillis() > expiryTimeMillis`
3. **Countdown**: Live countdown displayed to user (updates every second)
4. **Auto-Expiry**: When countdown reaches 0, state automatically changes to `OtpError.Expired`
5. **Cleanup**: Expired OTPs are removed from storage

### Attempt Tracking
- Each failed verification increments `attemptCount`
- When `attemptCount >= 3`, further attempts are blocked
- OTP is removed from storage after max attempts
- Resending OTP resets attempt count to 0

### Per-Email Storage
- Uses `Map<String, OtpData>` where key is email address
- Each email maintains its own OTP, expiry, and attempt count
- Multiple users can have active OTPs simultaneously
- Thread-safe operations using `Mutex` for concurrent access

## Data Structures Used and Why

### 1. `MutableMap<String, OtpData>`
- **Why**: Efficient key-value lookup by email
- **Benefits**: O(1) access time, easy per-email isolation
- **Thread Safety**: Protected by `Mutex` for concurrent access

### 2. `Sealed Class AuthState`
- **Why**: Type-safe state representation, exhaustive when expressions
- **Benefits**: Compiler enforces all state cases handled
- **States**: `EmailInput`, `OtpSent`, `OtpVerifying`, `OtpError`, `SessionActive`

### 3. `Sealed Class OtpValidationResult`
- **Why**: Type-safe result handling without exceptions
- **Benefits**: Clear error types, exhaustive handling
- **Results**: `Success`, `Incorrect`, `Expired`, `MaxAttemptsExceeded`, `NotFound`

### 4. `Data Class OtpData`
- **Why**: Immutable data structure for OTP information
- **Benefits**: Easy to reason about, copy() for updates
- **Fields**: `code`, `expiryTimeMillis`, `attemptCount`

### 5. `StateFlow<AuthState>`
- **Why**: Reactive state management, survives configuration changes
- **Benefits**: Automatic UI updates, lifecycle-aware
- **Usage**: Single source of truth for UI state

## External SDK: Firebase Analytics

### Why Firebase Analytics?
- **Industry Standard**: Widely used for mobile app analytics
- **Easy Integration**: Simple setup with Google Services
- **Real-time DebugView**: Can test events in real-time during development
- **Rich Features**: User properties, custom events, conversion tracking
- **Free Tier**: No cost for basic analytics

### Implementation Details
- **Dependency**: `com.google.firebase:firebase-analytics:23.0.0`
- **Initialization**: In `MainActivity.onCreate()` before UI setup
- **Singleton Pattern**: `AnalyticsLogger` ensures single instance
- **Thread Safety**: All operations are thread-safe
- **Event Parameters**: Structured data with email, error types, durations

### Events Tracked
1. `otp_sent` - When OTP is generated and sent
2. `otp_verified` - When OTP validation succeeds
3. `otp_verification_failed` - When OTP validation fails (with error type)
4. `otp_resent` - When user requests new OTP
5. `otp_expired` - When OTP expires
6. `max_attempts_exceeded` - When user exceeds 3 attempts
7. `session_started` - When user successfully logs in
8. `session_ended` - When user logs out (with duration)

## GPT Usage vs Self-Implementation

### What I Used GPT For:
1. **Initial Project Setup**: Getting started with Jetpack Compose structure
2. **Firebase Analytics Integration**: Understanding initialization and event logging
3. **Code Fixes**: Resolving compilation errors and type mismatches



### What I Understood and Implemented Myself:
1. **OTP Manager Logic**: 
   - Designed the data structure (`OtpData`, `Map` storage)
   - Implemented expiry logic using timestamps
   - Created attempt tracking mechanism
   - Thread-safe implementation with Mutex
2. **State Management**:
   - Designed sealed class hierarchy for states
   - Implemented one-way data flow
   - Created event-based architecture
3. **UI Implementation**:
   - Built all three screens from scratch
   - Implemented OTP input with focus management
   - Created session timer with live updates
   - Designed error handling UI
4. **Architecture Decisions**:
   - Separation of concerns (UI, ViewModel, Data layers)
   - State hoisting patterns
   - Coroutine usage for timers
5. **Edge Case Handling**:
   - Expiry detection and cleanup
   - Attempt limit enforcement
   - Resend validation logic
   - Screen rotation state preservation

## Setup Instructions

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd AuthFlow
   ```

2. **Open in Android Studio**
   - Open Android Studio
   - Select "Open an Existing Project"
   - Navigate to the AuthFlow directory

3. **Sync Gradle**
   - Android Studio will automatically sync Gradle dependencies
   - Wait for sync to complete

4. **Enable Firebase Analytics Debug Mode** (Optional, for testing)
   ```bash
   adb shell setprop debug.firebase.analytics.app com.example.authflow
   ```

5. **Run the App**
   - Connect an Android device or start an emulator
   - Click "Run" or press Shift+F10
   - App should build and install automatically

## Build Requirements

- **Android Studio**: Latest version (Hedgehog or newer)
- **JDK**: 11 or higher
- **Min SDK**: 24 (Android 7.0)
- **Target SDK**: 36
- **Gradle**: 9.1.0
- **Kotlin**: Latest stable version

## Testing the App

1. **Login Flow**:
   - Enter any email address
   - Tap "Send OTP"
   - Enter the 6-digit OTP shown in the card
   - Tap "Verify"

2. **OTP Expiry**:
   - Wait 60 seconds after OTP generation
   - OTP will automatically expire
   - Error message will appear

3. **Max Attempts**:
   - Enter wrong OTP 3 times
   - Further attempts will be blocked
   - Resend button becomes enabled

4. **Session Timer**:
   - After successful login, session timer starts
   - Timer updates every second
   - Logout button stops the timer

## Known Limitations

- OTP storage is in-memory (lost on app restart)
- No persistent storage for OTP data
- No network connectivity required (fully local)

## Future Enhancements

- Persistent OTP storage using Room database
- Biometric authentication option
- Remember me functionality
- Multiple device session management
