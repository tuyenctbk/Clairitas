# Sift Architecture & Technical Documentation

## 🏗️ Architectural Overview

Sift follows modern Android development guidelines and clean MVVM architecture principles:

```
com.example/
├── data/
│   ├── local/          # Room database, Entities, DAOs (AppDatabase)
│   ├── model/          # Data models (Article, TimeBudget, ProcessingMode, etc.)
│   ├── remote/         # Network clients & API specs
│   └── repository/     # NewsRepository combining local and remote data sources
├── service/
│   ├── AudioDigestManager.kt       # TTS & audio playback management
│   ├── BackgroundSyncManager.kt    # Sync controls and work scheduling
│   ├── FirebaseService.kt          # Gemini API integration & AI prompts
│   ├── NewsSyncWorker.kt           # Background WorkManager task for news sync
│   └── RadarNotificationManager.kt # Notification dispatcher for noise radar
├── ui/
│   ├── components/     # Reusable Material 3 composables
│   ├── screens/        # Feature screens (Home, Radar, Audio, Bookmarks, Settings, etc.)
│   ├── theme/          # M3 ColorScheme, Typography, Shapes, ThemeMode
│   └── NewsViewModel.kt# Central ViewModel managing app state & actions
└── MainActivity.kt     # App entry point, navigation graph, and edge-to-edge setup
```

---

## 🔒 Data Persistence & Room

- **Articles Table**: Stores article metadata, full content, translation, SNR score, and clickbait metrics.
- **Bookmarks Table**: Tracks user-saved articles for offline reading.
- **Keyword Traps Table**: Stores detected trap keywords and sensational patterns for the Noise & Trap Radar.

---

## 🤖 Gemini API Integration

`FirebaseService` coordinates interactions with Gemini models (`gemini-2.5-flash` / `gemini-1.5-flash`):
- **Summarization**: Generates bullet-point intelligence briefings.
- **SNR Calculation**: Evaluates information density versus emotional clickbait.
- **Translation**: Translates articles into selected target languages on-the-fly.
