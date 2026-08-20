# SmartDialer

SmartDialer is a Kotlin Android dialer replacement scaffold built with Material 3 and official Android Telecom, Contacts, and Call Log APIs. It avoids root, hidden APIs, Accessibility abuse, notification interception, and fake call state.

## Implemented phase
- Android Studio/Gradle project using Kotlin and Material 3.
- Default Phone role onboarding via `RoleManager.ROLE_DIALER`.
- Dial pad with tap, long-press zero for plus, haptics, delete, long-delete, T9 matching, and Telecom call initiation.
- `InCallService` and `CallScreeningService` declarations with conservative supported API behavior.
- Domain models and unit-tested utilities for call timers, T9 matching, and number normalization.
- CI workflow for tests, lint, debug APK, and optional release artifacts.

## Build and run
1. Open this repository in Android Studio.
2. Let Gradle sync.
3. Run `./gradlew assembleDebug` or `gradle assembleDebug`.
4. Install on a physical Android phone for Telecom behavior.
5. Tap **Open default Phone app settings** and select SmartDialer.

## Permissions and roles
SmartDialer requests only permissions needed by implemented features: calling, contacts, call log, phone state, and supported phone-call foreground service capability. Android still requires the user to grant roles; declaring permissions does not make an app the default dialer.

## Known limitations
This is an initial production-oriented scaffold, not a completed full dialer. Contacts, recents, private contacts, analytics, biometric lock, advanced settings, and complete in-call UI should be expanded in later phases. Call screening behavior depends on Android version, manufacturer, carrier, default Phone role, and screening role.

## GitHub Actions
`.github/workflows/build.yml` runs unit tests, lint, debug build, and uploads APK/AAB artifacts when produced. Release signing must use GitHub Secrets; no keystore or password is committed.
