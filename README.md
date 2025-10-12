<img src="/images/kairos-banner.png" width="1024" height="500">

[![Android CI - Kairos Multi-Module](https://github.com/ipirangad3v/kairos-android-app/actions/workflows/android-ci.yaml/badge.svg)](https://github.com/ipirangad3v/kairos-android-app/actions/workflows/android-ci.yaml) [![codecov](https://codecov.io/gh/ipirangad3v/kairos-android-app/graph/badge.svg?token=TKC92HM5VY)](https://codecov.io/gh/ipirangad3v/kairos-android-app)

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
