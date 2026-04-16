# User Session Management

This document describes the user session lifecycle and authentication flow in DouyinLite.

## Session State
The application uses a unified `SessionState` to track the user's authentication status:
- `Initializing`: The app is checking for existing credentials.
- `Guest`: The user is not logged in. Some features may be restricted.
- `LoggedIn(user)`: The user is authenticated. Contains the user's profile information.
- `Error(message)`: An error occurred while managing the session.

## Components
- `AuthManager`: Low-level component that manages token and user info persistence using `SharedPreferences`. Provides a reactive `StateFlow<SessionState>`.
- `AuthRepository`: High-level entry point for login, registration, and logout operations. It coordinates with `DouyinApiService` and `AuthManager`.
- `AuthInterceptor`: OkHttp interceptor that attaches the token and user ID to requests and handles 401 Unauthorized errors by clearing the session.
- `feature_auth`: Module containing the UI for login and registration.

## Typical Flows

### App Launch (Silent Login)
1. `MainActivity` calls `SilentLoginInitializer.initialize()`.
2. `AuthManager` initializes `SessionState` from persisted data.
3. If credentials exist, `SilentLoginInitializer` attempts to refresh user info.
4. If successful, session remains `LoggedIn`. If it fails (e.g., token expired), it may transition to `Guest`.

### Manual Login
1. User navigates to `LoginScreen`.
2. `AuthViewModel` calls `AuthRepository.login()`.
3. Upon success, `AuthManager` updates `SessionState` to `LoggedIn`.
4. Reactive UI components (like `ProfileScreen`) automatically update to show authenticated content.

### Token Expiration
1. A network request returns a 401 Unauthorized error.
2. `AuthInterceptor` catches the error and calls `AuthManager.clearAuth()`.
3. `SessionState` transitions to `Guest`.
4. UI components react to the state change.

## Usage in UI
To observe the session state in a Compose screen:
```kotlin
val sessionState by viewModel.sessionState.collectAsState()
when (sessionState) {
    is SessionState.LoggedIn -> // Show user content
    is SessionState.Guest -> // Show login prompt
    else -> // Show loading/error
}
```
