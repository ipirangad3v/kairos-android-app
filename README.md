<img src="/images/kairos-banner.png" width="1024" height="500">

[![Android CI - Kairos Multi-Module](https://github.com/ipirangad3v/kairos-android-app/actions/workflows/android-ci.yaml/badge.svg)](https://github.com/ipirangad3v/kairos-android-app/actions/workflows/android-ci.yaml) [![codecov](https://codecov.io/gh/ipirangad3v/kairos-android-app/graph/badge.svg?token=TKC92HM5VY)](https://codecov.io/gh/ipirangad3v/kairos-android-app)

<picture>
  <source media="(prefers-color-scheme: dark)" srcset="https://playbadges.pavi2410.com/badge/full?id=digital.tonima.kairos&theme=dark">
  <img alt="PlayBadges Card Folo" src="https://playbadges.pavi2410.com/badge/full?id=digital.tonima.kairos">
</picture>

# Kairós - Calendar Alarms for Android & Wear OS

Kairós is a modern application designed to ensure you never miss a calendar event again, **whether on your smartphone or directly from your wrist with Wear OS**. It intelligently syncs with your device's calendar and turns your appointments into unmissable, full-screen alarms, similar to a native alarm clock.

This project showcases a modern Android architecture, focusing on performance, battery efficiency, and a clean user experience across devices.

## Download
<a href='https://play.google.com/store/apps/details?id=digital.tonima.kairos' target="_blank" rel="noopener noreferrer"><img alt='Get it on Google Play' src='https://play.google.com/intl/en_us/badges/static/images/badges/en_badge_web_generic.png' width='200'/></a>

## Wear OS:
Experience seamless calendar integration and proactive event management directly on your smartwatch.

<table>
  <tr>
    <td style="text-align: center;">
      <img src="/images/watch1.png" width="400" height="auto"> </td>
    <td style="text-align: center;">
      <img src="/images/watch2.png" width="400" height="auto"> </td>
  </tr>
</table>

### Complications in Action:
Visualize your upcoming events at a glance with Kairós complications.

<img src="/images/complications.gif" alt="Kairós Complications in Action" width="600" height="auto" style="border-radius: 8px; box-shadow: 0 4px 8px rgba(0,0,0,0.2);">

### Tiles for Instant Access:
Quickly view your next upcoming event directly from your watch face with interactive Kairós Tiles. Tap on the Tile to open the event or the app for more details.

<img src="/images/tile.gif" alt="Kairós Tile in Action" width="600" height="auto" style="border-radius: 8px; box-shadow: 0 4px 8px rgba(0,0,0,0.2);">

## Key Features

- **Full-Screen Alarms**: Triggers a full-screen, audible alarm for calendar events, waking the device even when locked. **On Wear OS, alerts are displayed prominently on the watch face.**

- **Complication Support (Wear OS)**: Get a quick glance at your upcoming events directly on your watch face with customizable complication widgets. Supports `SHORT_TEXT`, `LONG_TEXT`, and `RANGED_VALUE` types to display event times, titles, and time until the next event.

- **Tile Support (Wear OS)**: Access your next event instantly with a dedicated Wear OS Tile, providing crucial information at a glance and quick access to the app.

- **Smart Scheduling**: Utilizes [WorkManager](https://developer.android.com/guide/background/persistent/getting-started) to efficiently schedule alarms in the background, checking for upcoming events periodically without draining the battery.

- **Proactive Scheduling**: A "safety net" feature in the ViewModel ensures that newly created, imminent events are scheduled immediately when the app is opened.

- **Seamless Calendar Integration**: Reads events from any calendar account set up on the device (e.g., Google Calendar).

## Interactive UI:

- **Smartphone**:
  - A clean, interactive monthly calendar view to navigate through events.
  - Clickable events that open directly in the native calendar app.
  - A FAB for quick access to the calendar app.
  - Total Control: Users can globally enable/disable all alarms or toggle alarms for individual events.

- **Wear OS**:
  - **Optimized for Wearable Devices**: A dedicated interface to view upcoming events directly on your smartwatch.
  - **Quick Glance**: Get event notifications and details at a glance, right on your wrist.
  - **Watch Face Complications**: Add Kairós complications to your favorite watch face for immediate access to upcoming event information.
  - **Wear OS Tiles**: Easily add a Kairós Tile to your watch for rapid access to your next event.
  - **Global Alarm Toggle**: Enable or disable event alarms directly from your watch.

- **Robust Permission Handling**: Guides the user through granting all necessary permissions, including special ones like `SCHEDULE_EXACT_ALARM` and `USE_FULL_SCREEN_INTENT`.

- **Manufacturer-Specific Helpers**: Includes a proactive suggestion for users to enable "Autostart" on devices with aggressive battery optimization, improving background reliability.

- **Localization**: Supports multiple languages for UI text and Play Store listings.

## Architecture & Tech Stack
This application is built following modern Android development principles and a clear MVVM (Model-View-ViewModel) architecture. It's designed for multi-module and multi-platform (Android Phone and Wear OS) scalability.

## UI Layer:

- **[Jetpack Compose](https://developer.android.com/jetpack/compose)**: The entire UI is built with Jetpack Compose for a declarative, modern, and reactive user interface, providing a native experience on both phone and watch.

- **ViewModel**: Manages UI state and business logic, exposing a single `UiState` object to the screen.

- **ComplicationDataSourceService**: Handles the logic for providing real-time calendar event data to watch face complications.

- **TileService**: Provides the data and layout for Wear OS Tiles, displaying key information at a glance.

## Data Layer:

- **Repository Pattern**: Centralizes data operations, abstracting the `ContentResolver` for fetching calendar events.

## Domain/Service Layer:

- **[WorkManager](https://developer.android.com/guide/background/persistent/getting-started)**: Handles reliable, battery-efficient background tasks for periodic alarm scheduling.

- **[AlarmManager](https://developer.android.com/reference/android/app/AlarmManager)**: Used to set the precise, full-screen alarms.

- **BroadcastReceivers & Services**: Manages alarm triggers, sound playback, and notification actions.

**Language**: 100% Kotlin.

## Dependency:

- **[Accompanist](https://google.github.io/accompanist/)**: For streamlined permission handling in Compose.

- **[Kizitonwose Calendar](https://github.com/kizitonwose/Calendar)**: For the highly customizable calendar view (primarily for phone UI).

## Flow:
<img src="/images/flow.png" width="3840" height="3405">

### Event Sync Phone → Wear OS (Data Layer)
To ensure reliability on the watch, Kairos syncs upcoming events from the phone to Wear OS using the Google Play Services Wearable Data Layer. The flow is as follows:

- Phone (app): `PhoneEventSyncWorker` collects events from the next 24 hours and sends them as a DataItem to the `/kairos/events24h` path.
- Watch (wear): `WearEventListenerService` receives the DataItem, parses it, and saves it locally in the `WearEventCache`.
- Watch (wear): `CachedEventSchedulingWorker` reads the cache and schedules alarms locally (respecting the global toggle and event/series deactivations).
- Tile/Complication use the same cache to display the next event.

Technical Details
- Shared schema (paths and keys): `core/src/main/java/digital/tonima/core/sync/WearSyncSchema.kt`
- `PATH_EVENTS_24H = "/kairos/events24h"`
- `KEY_EVENTS`, `KEY_ID`, `KEY_TITLE`, `KEY_START`, `KEY_RECUR`, `KEY_GENERATED_AT`
- Phone side (sending): `app/src/main/java/digital/tonima/kairos/service/PhoneEventSyncWorker.kt`
- Filters the next 24 hours, sorts by time, and sends the list (DataMapArrayList) to `PATH_EVENTS_24H`.
- WorkManager: 15-minute frequency (+ one initial trigger). `UNIQUE_WORK_NAME = "phone-event-sync"`. - Watch side (receive): `wear/src/main/java/digital/tonima/kairos/wear/sync/WearEventListenerService.kt`
- Declared in the Manifest with `<action name="com.google.android.gms.wearable.DATA_CHANGED"/>` and `pathPrefix="/kairos"`.
- Converts the DataItem to an `Event` list and saves it via `WearEventCache`.
- Emits a local broadcast `SyncActions.ACTION_EVENTS_UPDATED` and triggers an immediate schedule with `WorkManager` (unique work `WorkNames.UNIQUE_SCHEDULE_NOW`).
- Scheduler on Wear: `wear/src/main/java/digital/tonima/kairos/wear/sync/CachedEventSchedulingWorker.kt`
- Scheduling window: now...+75 min; ignores past events. - Respects user preferences via DataStore (`AppPreferencesRepository`):
- Global ON/OFF, disabled instance IDs (`uniqueIntentId`) and series IDs (`id`).
- Executed periodically every 15 minutes by `KairosWearApplication.setupRecurringWork()` using `WorkNames.UNIQUE_PERIODIC_SCHEDULER`.
- Local cache on Wear: `wear/src/main/java/digital/tonima/kairos/wear/sync/WearEventCache.kt` (SharedPreferences + plain JSON).

Permissions and Dependencies
- Phone: `READ_CALENDAR` (read events) and notification permissions as needed. - Wearable: `POST_NOTIFICATIONS`, `SCHEDULE_EXACT_ALARM`, `VIBRATE`, and `FOREGROUND_SERVICE_MEDIA_PLAYBACK` (for the alarm service).
- Dependency: `com.google.android.gms:play-services-wearable` in the app and wearable (via the Version Catalog).

How to test manually
1. Install the app on your phone and watch; ensure they are paired and have Google Play Services enabled.
2. Open the app on your phone to grant permissions and generate a first push.
3. On your watch, open Kairos: the list should show upcoming events (future events only).
4. Create an event in the next few minutes and wait: the watch should receive it, and the worker will schedule the alarm.

Debugging Tips (Logcat)
- Phone: Look for `Phone→Wear sync: sending ... events.`
- Wear (listener): `Wear received N events from phone.`
- Wear (worker): `Wear: Evaluating ... cached events for scheduling window ...` and `Wear: Scheduling '...' at ...`.

---

## Requirements

- Android Studio (Giraffe or newer recommended; Arctic Fox may not support Kotlin 2.2+ features). As of 2025-10-12, Android Studio Jellyfish/Koala+ should work well.
- JDK 21 (the Gradle configs use JVM toolchain 21)
- Android SDK:
  - Compile SDK: 36
  - Target SDK: 36
  - Min SDK: 30
- Gradle Wrapper: use the provided ./gradlew
- Ruby (optional, for Fastlane) if you plan to use release lanes

## Project Structure

Multi-module Android project targeting Phone and Wear OS:

- app/ — Android app (phone)
- wear/ — Wear OS companion app
- core/ — Shared logic, domain, data, scheduling, repositories
- build-logic/ — Convention plugins (e.g., Jacoco)
- fastlane/ — Release automation (Fastlane lanes)
- images/ — Media used by README and Play assets

## Stack

- Language: Kotlin
- UI: Jetpack Compose (phone and Wear)
- DI: Hilt
- Background: WorkManager, AlarmManager, BroadcastReceivers/Services
- Data: AndroidX DataStore Preferences
- Wear OS: Tiles, Complications, ProtoLayout, Watchface APIs
- Testing: JUnit4, Robolectric, MockK, Turbine, Coroutines Test, AndroidX Core Testing
- Build: Gradle (Kotlin DSL), AGP 8.13.0, Kotlin 2.2.20
- Code style: Spotless
- Analytics/Crash: Firebase Analytics, Crashlytics (app)
- Ads: Google Mobile Ads (AdMob)
- Coverage: Jacoco, custom merged report task
- Distribution: Fastlane (Android Supply)

## Getting Started

1) Clone the repository

- git clone https://github.com/ipirangad3v/kairos-android-app.git
- cd kairos-android-app

2) Open in Android Studio

- Use the provided Gradle wrapper; Android Studio will sync automatically.

3) Configure local.properties (optional for release)

- Some release values can be injected from local.properties (see env vars below). For normal debug development, defaults and test ad units are used.

4) Run the app(s)

- Phone (app module):
  - From Android Studio: select app run configuration and Run.
  - CLI: ./gradlew :app:installDebug and then launch on device/emulator.
- Wear (wear module):
  - From Android Studio: select wear run configuration and Run (with a Wear emulator/device).
  - CLI: ./gradlew :wear:installDebug


## Tests

- Run unit tests (all modules): ./gradlew testDebugUnitTest
- Run unit tests (per module):
  - Phone: ./gradlew :app:testDebugUnitTest
  - Core: ./gradlew :core:testDebugUnitTest
  - Wear: ./gradlew :wear:testDebugUnitTest
- Coverage (merged Jacoco for all modules): ./gradlew createJacocoMergedCoverageReport
  - Reports output (HTML): build/reports/jacoco/createJacocoMergedCoverageReport/html/index.html

## Environment Variables and Configuration

Debug builds:
- Use test AdMob IDs automatically; no special setup required.

Release builds may use the following environment variables (or fallback to local.properties where supported):
- ANDROID_SIGNING_KEY_PASSWORD — Keystore password (app and wear)
- ANDROID_SIGNING_KEY_ALIAS — Key alias
- ANDROID_SIGNING_KEY_ALIAS_PASSWORD — Key password
- ADMOB_APP_ID — AdMob App ID (app, only used during release tasks)
- ADMOB_BANNER_AD_UNIT_HOME — Ad unit id for home banner (app, only used during release tasks)

Fastlane (distribution):
- REPO_ROOT — Absolute path to the repo root (used by Fastlane to locate artifacts)
- PLAY_STORE_SERVICE_ACCOUNT_JSON_PLAINTEXT — The JSON key content (string) for your Google Play service account; Fastlane assigns it to GOOGLE_PLAY_SERVICE_ACCOUNT_JSON_KEY

Optional local.properties keys (release convenience):
- admob.app.id=<your-admob-app-id>
- admob.banner.ad.unit.home=<your-banner-ad-unit>

Signing files expected for release:
- app/release-key.jks — Phone app keystore
- wear/release-key.jks — Wear app keystore

Google services configuration (Firebase / google-services.json):

If Firebase Analytics or Crashlytics are enabled, the app expects a valid google-services.json. Without it, the app can crash at startup. Provide this file for BOTH modules: app/ and wear/.

How to generate google-services.json (Android):
- 1) Go to Firebase Console: https://console.firebase.google.com/ and click Add project (or select an existing one).
- 2) In Project settings > Your apps, click Add app and choose Android.
- 3) Enter the Android package name: digital.tonima.kairos (same package is used for phone and wear here).
- 4) App nickname: optional (e.g., Kairos Phone or Kairos Wear).
- 5) SHA-1/SHA-256: optional but recommended if you will use features like Crashlytics, Dynamic Links, or Google Sign-In. You can obtain SHA-1 with: keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android
- 6) Click Register app, then Download google-services.json.
- 7) Place a copy of the downloaded file at:
  - app/google-services.json
  - wear/google-services.json
- 8) Sync Gradle and rebuild the project (Android Studio will pick up the files automatically).

Notes:
- Do NOT commit google-services.json to public repos. Treat it as a secret.
- You can keep separate Firebase projects for debug and release as needed. Make sure the package name matches and that you add the proper SHA certificates for release builds.
- If you see a crash at startup related to Firebase initialization, verify that each module (app and wear) has its own google-services.json and that the Gradle sync completed successfully.

## CI

- GitHub Actions workflow badge is present and points to .github/workflows/android-ci.yaml
- Codecov badge indicates coverage upload to Codecov

## Permissions and Special Capabilities

- Calendar read permissions
- Exact alarms (SCHEDULE_EXACT_ALARM)
- Full screen intents (USE_FULL_SCREEN_INTENT)
- Autostart/manufacturer-specific optimizations guidance in-app

## Contributing

- Run ./gradlew spotlessApply before pushing
- Ensure unit tests pass: ./gradlew testDebugUnitTest

