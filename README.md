<img src="/images/kairos-banner.png" width="1024" height="500">

# Kairós - Calendar Alarms for Android & Wear OS

Kairós is a modern application designed to ensure you never miss a calendar event again, **whether on your smartphone or directly from your wrist with Wear OS**. It intelligently syncs with your device's calendar and turns your appointments into unmissable, full-screen alarms, similar to a native alarm clock.

This project showcases a modern Android architecture, focusing on performance, battery efficiency, and a clean user experience across devices.

## Download
<a href='https://play.google.com/store/apps/details?id=digital.tonima.kairos' target="_blank" rel="noopener noreferrer"><img alt='Get it on Google Play' src='https://play.google.com/intl/en_us/badges/static/images/badges/en_badge_web_generic.png' width='200'/></a>

## Wear OS:
<img src="/images/wear.png" width="454" height="454">

## Key Features

- **Full-Screen Alarms**: Triggers a full-screen, audible alarm for calendar events, waking the device even when locked. **On Wear OS, alerts are displayed prominently on the watch face.**

- **Smart Scheduling**: Utilizes WorkManager to efficiently schedule alarms in the background, checking for upcoming events periodically without draining the battery.

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
  - **Global Alarm Toggle**: Enable or disable event alarms directly from your watch.

- **Robust Permission Handling**: Guides the user through granting all necessary permissions, including special ones like `SCHEDULE_EXACT_ALARM` and `USE_FULL_SCREEN_INTENT`.

- **Manufacturer-Specific Helpers**: Includes a proactive suggestion for users to enable "Autostart" on devices with aggressive battery optimization, improving background reliability.

- **Localization**: Supports multiple languages for UI text and Play Store listings.

## Architecture & Tech Stack
This application is built following modern Android development principles and a clear MVVM (Model-View-ViewModel) architecture. It's designed for multi-module and multi-platform (Android Phone and Wear OS) scalability.

## UI Layer:

- **Jetpack Compose**: The entire UI is built with Jetpack Compose for a declarative, modern, and reactive user interface, providing a native experience on both phone and watch.

- **ViewModel**: Manages UI state and business logic, exposing a single `UiState` object to the screen.

## Data Layer:

- **Repository Pattern**: Centralizes data operations, abstracting the `ContentResolver` for fetching calendar events.

## Domain/Service Layer:

- **WorkManager**: Handles reliable, battery-efficient background tasks for periodic alarm scheduling.

- **AlarmManager**: Used to set the precise, full-screen alarms.

- **BroadcastReceivers & Services**: Manages alarm triggers, sound playback, and notification actions.

**Language**: 100% Kotlin.

## Dependency:

- **Accompanist**: For streamlined permission handling in Compose.

- **Kizitonwose Calendar**: For the highly customizable calendar view (primarily for phone UI).

## Flow:
<img src="/images/flow.png" width="3840" height="3405">
