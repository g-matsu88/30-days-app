# 30-Day Challenge (Jetpack Compose)

A small Android app built with Kotlin + Jetpack Compose, structured the same way as
Google's ["Courses" sample app](https://github.com/google-developer-training/basic-android-kotlin-compose-training-courses)
from the *Android Basics with Compose* course, but adapted into a 30-day habit/fitness
challenge tracker.

## What it does
- Shows a scrollable 2-column grid of all 30 days (`DayGrid` / `DayCard`, mirroring the
  course's `TopicGrid` / `TopicCard`).
- Tapping a day toggles it complete/incomplete.
- A progress bar and "`X of 30 days completed`" label update live at the top.
- Content for each day lives in one place, `DataSource.kt`, so it's easy to swap in
  your own 30-day plan (reading habit, coding challenge, water intake, etc.).

## Project structure
```
app/src/main/java/com/example/thirtydays/
├── MainActivity.kt        # Activity + all composables (ChallengeApp, DayGrid, DayCard...)
├── model/Day.kt            # Data class for a single day
├── data/DataSource.kt      # The list of 30 Day objects
└── ui/theme/                # Standard Material3 theme (Color.kt, Type.kt, Theme.kt)
```

## Opening the project
1. Unzip this project.
2. Open **Android Studio** (Koala/2024.1 or newer recommended).
3. **File > Open** and select the unzipped `ThirtyDaysApp` folder.
4. Let Gradle sync (Android Studio will download the Gradle 8.7 distribution
   referenced in `gradle/wrapper/gradle-wrapper.properties` the first time).
5. Click **Run ▶** on the `app` configuration with an emulator or device (minSdk 24).

## Customizing
- Edit the `days` list in `DataSource.kt` to change the 30 daily tasks/titles.
- Change `Green40` / `Green80` etc. in `ui/theme/Color.kt` to re-theme the app.
- To persist completion state across app restarts, swap the in-memory
  `mutableStateList` in `ChallengeApp()` for a `ViewModel` backed by
  `DataStore` — the same pattern taught later in the Android Basics course.
