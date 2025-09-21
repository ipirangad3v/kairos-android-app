## Kairós - Calendar Alarms for Android
Kairós is a modern Android application designed to ensure you never miss a calendar event again. It intelligently syncs with your device's calendar and turns your appointments into unmissable, full-screen alarms, similar to a native alarm clock.

This project showcases a modern Android architecture, focusing on performance, battery efficiency, and a clean user experience.

## Key Features
- <b>Full-Screen Alarms</b>: Triggers a full-screen, audible alarm for calendar events, waking the device even when locked.

- <b>Smart Scheduling</b>: Utilizes WorkManager to efficiently schedule alarms in the background, checking for upcoming events periodically without draining the battery.

- <b>Proactive Scheduling</b>: A "safety net" feature in the ViewModel ensures that newly created, imminent events are scheduled immediately when the app is opened.

- <b>Seamless Calendar Integration</b>: Reads events from any calendar account set up on the device (e.g., Google Calendar).

  [![](https://mermaid.ink/img/pako:eNqFVV1z4jgQ_CsqP1xeIBsuMQS26q4ILAm7IV9AdhOTohR7YquwZZ8kk81Sud9-I8lgm8vV8QRmume6pyVvHD8NwOk5oaBZRGbDBSf4kfmzfdDPsuWYM8VozH5RxVJuC_Sn780lCHKdAZe68Ik0m3-Qs80gAn9FbkAkTEpEyPfPJegMi0g_jsm5oFxBYDADbxqlr2RCGSfzMWmSL2vgauoLAP60D55oVh4a4NACy17kDv7KQSqkqQKHdrTiCfBgwfeEnlF_FYo058Fy6kcQ5DH2WH5PxWpCOQ1BlGRfPBRLRkxgm0uacz-yykdegQQ9EEsD5hNNAKI6ykhrQH3ijfzdIlGaCwM-9_oxFUnZ2yKJyLmsws9N9cXmgkoyoDFKoaKi_8-q1xe61QNIAxl7t7luugO9pIKwJGEcrSagDa81GhvUV2_EYoVz2ALyTCXuDH3O9eozAS8ggPtQg3410G-lHVRLk2TNqN1sVWrdnW8GemnNKJxHk-JAElnUBwXd077Sq9SAJ15hnVRptqv6aOfzMWYbxVFfB3t5z-B1gqchLnkHhvFqY4LO6ZqFVIH8hLIFyAhqyb4ytdfejoa8gMKZ5dY7bXiSchVVJ782qBtv4RxMKQLeyBWoA2LP0AcrWjhV9I11-vP-xLd2YpWGYYwDUG49q457a3LI6TPuZ5_E_DdkcvfnnTeguOXY0vzfIo3X-sv-ZaKrlzPBQtwqRrxseFkcCpQ5YwngKaaa1PSe1sOgLFxatjvwga3rGZoa2Gxjh1O4MXKZ-qseGZdOEL3zNdSOy6wSormnIRghjf6NIItQtuNU3xFTEGvmQ7Xr3ODu7YU0yuO4ae8v8olcAA1kc54huWIvzDf36NN-5-1B_e6NQ54K2CptmCAT-Al-XgV-lOgpVmb64lIRLM24ZZN73aSYSYsr7P1h7e1rPxjGj-FJQwm1-arQOY9L8INXlWSB5JWpiBzoUQ7Ic65UXesPA3zEwJuI-jHzV7JeXs_4g27-n7WGrd8vRaR8mwktpfAQgirjo30bnHma6N9bxX1bkaRMUBXe7xf4-iKcBr5EWeD0lMih4SR4J1P909nosoWDK0lg4fTwK16_q4Wz4O-IySh_TNNkC8NXUBg5vRcaS_yVZwE2HzKK6012T_HGDUAMcGTl9FrHhsPpbZyfTq9zetg6OWp1W27Lbbd-b7UbzpvTO3EPj7qdY9dtt133tNN13feG88t0bR26R93jo5NOu3V62na77c77P3mAfq8?type=png)](https://mermaid.live/edit#pako:eNqFVV1z4jgQ_CsqP1xeIBsuMQS26q4ILAm7IV9AdhOTohR7YquwZZ8kk81Sud9-I8lgm8vV8QRmume6pyVvHD8NwOk5oaBZRGbDBSf4kfmzfdDPsuWYM8VozH5RxVJuC_Sn780lCHKdAZe68Ik0m3-Qs80gAn9FbkAkTEpEyPfPJegMi0g_jsm5oFxBYDADbxqlr2RCGSfzMWmSL2vgauoLAP60D55oVh4a4NACy17kDv7KQSqkqQKHdrTiCfBgwfeEnlF_FYo058Fy6kcQ5DH2WH5PxWpCOQ1BlGRfPBRLRkxgm0uacz-yykdegQQ9EEsD5hNNAKI6ykhrQH3ijfzdIlGaCwM-9_oxFUnZ2yKJyLmsws9N9cXmgkoyoDFKoaKi_8-q1xe61QNIAxl7t7luugO9pIKwJGEcrSagDa81GhvUV2_EYoVz2ALyTCXuDH3O9eozAS8ggPtQg3410G-lHVRLk2TNqN1sVWrdnW8GemnNKJxHk-JAElnUBwXd077Sq9SAJ15hnVRptqv6aOfzMWYbxVFfB3t5z-B1gqchLnkHhvFqY4LO6ZqFVIH8hLIFyAhqyb4ytdfejoa8gMKZ5dY7bXiSchVVJ782qBtv4RxMKQLeyBWoA2LP0AcrWjhV9I11-vP-xLd2YpWGYYwDUG49q457a3LI6TPuZ5_E_DdkcvfnnTeguOXY0vzfIo3X-sv-ZaKrlzPBQtwqRrxseFkcCpQ5YwngKaaa1PSe1sOgLFxatjvwga3rGZoa2Gxjh1O4MXKZ-qseGZdOEL3zNdSOy6wSormnIRghjf6NIItQtuNU3xFTEGvmQ7Xr3ODu7YU0yuO4ae8v8olcAA1kc54huWIvzDf36NN-5-1B_e6NQ54K2CptmCAT-Al-XgV-lOgpVmb64lIRLM24ZZN73aSYSYsr7P1h7e1rPxjGj-FJQwm1-arQOY9L8INXlWSB5JWpiBzoUQ7Ic65UXesPA3zEwJuI-jHzV7JeXs_4g27-n7WGrd8vRaR8mwktpfAQgirjo30bnHma6N9bxX1bkaRMUBXe7xf4-iKcBr5EWeD0lMih4SR4J1P909nosoWDK0lg4fTwK16_q4Wz4O-IySh_TNNkC8NXUBg5vRcaS_yVZwE2HzKK6012T_HGDUAMcGTl9FrHhsPpbZyfTq9zetg6OWp1W27Lbbd-b7UbzpvTO3EPj7qdY9dtt133tNN13feG88t0bR26R93jo5NOu3V62na77c77P3mAfq8)

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
