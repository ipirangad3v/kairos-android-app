<img src="/images/kairos-banner.png" width="1024" height="500">

## Kairós - Calendar Alarms for Android
Kairós is a modern Android application designed to ensure you never miss a calendar event again. It intelligently syncs with your device's calendar and turns your appointments into unmissable, full-screen alarms, similar to a native alarm clock.

This project showcases a modern Android architecture, focusing on performance, battery efficiency, and a clean user experience.

## Download
<a href='https://play.google.com/store/apps/details?id=digital.tonima.kairos' target="_blank" rel="noopener noreferrer"><img alt='Get it on Google Play' src='https://play.google.com/intl/en_us/badges/static/images/badges/en_badge_web_generic.png' width='200'/></a>

## Key Features
- <b>Full-Screen Alarms</b>: Triggers a full-screen, audible alarm for calendar events, waking the device even when locked.

- <b>Smart Scheduling</b>: Utilizes WorkManager to efficiently schedule alarms in the background, checking for upcoming events periodically without draining the battery.

- <b>Proactive Scheduling</b>: A "safety net" feature in the ViewModel ensures that newly created, imminent events are scheduled immediately when the app is opened.

- <b>Seamless Calendar Integration</b>: Reads events from any calendar account set up on the device (e.g., Google Calendar).

## Interactive UI:

- A clean, interactive monthly calendar view to navigate through events.

- Clickable events that open directly in the native calendar app.

- A FAB for quick access to the calendar app.

- Total Control: Users can globally enable/disable all alarms or toggle alarms for individual events.

- Robust Permission Handling: Guides the user through granting all necessary permissions, including special ones like SCHEDULE_EXACT_ALARM and USE_FULL_SCREEN_INTENT.

- Manufacturer-Specific Helpers: Includes a proactive suggestion for users to enable "Autostart" on devices with aggressive battery optimization, improving background reliability.

 - Localization: Supports multiple languages for UI text and Play Store listings.

## Architecture & Tech Stack
This application is built following modern Android development principles and a clear MVVM (Model-View-ViewModel) architecture.

## UI Layer:

 - <b>Jetpack Compose</b>: The entire UI is built with Jetpack Compose for a declarative, modern, and reactive user interface.

- <b>ViewModel</b>: Manages UI state and business logic, exposing a single UiState object to the screen.

## Data Layer:

- <b>Repository Pattern</b>: Centralizes data operations, abstracting the ContentResolver for fetching calendar events.

## Domain/Service Layer:

- <b>WorkManager</b>: Handles reliable, battery-efficient background tasks for periodic alarm scheduling.

 - <b>AlarmManager</b>: Used to set the precise, full-screen alarms.

- <b>BroadcastReceivers & Services</b>: Manages alarm triggers, sound playback, and notification actions.

<b>Language</b>: 100% Kotlin.

## Dependency:

 - <b>Accompanist</b>: For streamlined permission handling in Compose.

- <b>Kizitonwose Calendar</b>: For the highly customizable calendar view.

## Flow:
<img src="/images/flow.png" width="3840" height="3405">
