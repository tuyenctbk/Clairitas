# Sift: News Intelligence & Anti-Clickbait Tool

**Sift** is an ultra-clean, anti-clickbait, privacy-first news intelligence Android application powered by Jetpack Compose, Room Database, WorkManager, and the Google Gemini API.

---

## 🌟 Core Features

1. **High-SNR Curation**: Automatically filters out sensationalism, outrage bait, and clickbait headlines, surfacing signal-heavy intelligence.
2. **Gemini-Powered Summaries**: Choose between **Flash Mode** (quick 1-minute briefs) and **Deep Dive Mode** (comprehensive analysis with key takeaways, sentiment, and fact-checking).
3. **Noise & Trap Radar**: Identifies and flags common clickbait patterns, emotional manipulation, and keyword traps in news feeds.
4. **Audio Briefing Digest**: Built-in Text-to-Speech audio queue allowing users to listen to daily briefings and summaries hands-free.
5. **Offline-First & Local Storage**: Utilizes Room Database for lightning-fast local persistence, bookmarks, search history, and automated cleanup retention policies.
6. **Background Intelligence Worker**: Periodic background sync via WorkManager to fetch, pre-cache, and compile daily top-priority intelligence briefings.
7. **Customizable Region & Language**: Supports multi-language translation and region-specific news feeds.

---

## 📚 Documentation

- [Architecture & Tech Stack](./docs/ARCHITECTURE.md)
- [User Guide](./docs/USER_GUIDE.md)

---

## 🚀 Getting Started

1. Clone or open the project in Android Studio or AI Studio Build.
2. Provide your **Gemini API Key** in the **Settings** screen (or configure via Secrets panel).
3. Build and run using Gradle (`compile_applet`).
