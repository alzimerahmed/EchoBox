# EchoBox — a cross-platform music streaming app backed by YouTube Music

<div align="center">

[![Platform](https://img.shields.io/badge/platform-Android%20%7C%20Windows%20%7C%20macOS%20%7C%20Linux%20%7C%20iOS-blue?logo=android)](https://github.com/alzimerahmed84/EchoBox/releases)
[![Language](https://img.shields.io/badge/language-Kotlin-7F52FF?logo=kotlin)](https://kotlinlang.org)
[![UI](https://img.shields.io/badge/UI-Jetpack%20Compose%20%26%20Compose%20Multiplatform-4285F4?logo=jetpackcompose)](https://www.jetbrains.com/lifecycle/compose/)
[![License](https://img.shields.io/badge/license-GPL--3.0-green)](LICENSE)

*Stream music from YouTube Music with synced lyrics, offline playback and zero ads — on Android, desktop and iOS.*

[Quick Start](#quick-start--building) • [Features](#features) • [Building](#quick-start--building)

</div>

---

## Features

- Streaming from YouTube and YouTube Music — up to 256 kbps Opus/AAC (higher quality for Premium accounts)
- Three Now Playing styles: Classic, Material 3 Expressive, Apple Music
- Word-by-word synced lyrics, romanization for 12 languages, AI lyrics translation (bring your own OpenAI/Gemini key)
- Ten-band equalizer with presets and AutoEq headphone profiles, plus delay/reverb effects
- Offline playback with caching; playlist import from Spotify and other apps
- Crossfade, sleep timer, Android Auto, Discord Rich Presence, Last.fm scrobbling
- SponsorBlock and Return YouTube Dislike support
- Listen Together: shared rooms that play in sync with friends
- EchoBox Wrapped: yearly and monthly listening recaps, on-device listening analytics
- Home screen widgets: turntable, playlists, listening insights
- Light/dark/dynamic theming, multi-account YouTube support

## Screenshots

Screenshots live in [`asset/screenshot/`](asset/screenshot/) (16 device frames covering phone and desktop layouts).

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Kotlin (Kotlin Multiplatform) |
| UI | Jetpack Compose + Compose Multiplatform |
| Architecture | MVVM + repository pattern, Hilt DI |
| Media | Media3 / ExoPlayer, mpv (desktop), libmpv bundle |
| Data | Room, DataStore, Retrofit |
| Backend data | YouTube Music (via kotlinYtmusicScraper), NewPipe extractors |
| Targets | Android, Windows/macOS/Linux (JVM), iOS |

## Project Structure

```
├── composeApp/        # Shared Compose Multiplatform app (UI, viewmodels, DI)
├── androidApp/        # Android entry point (manifest, widgets, Wear/Auto glue)
├── desktopApp/        # Desktop JVM entry point
├── core/              # Vendored multiplatform libraries
│   ├── common/ data/ domain/   # Models, repositories, use cases
│   ├── media/ service/         # Player adapters, YouTube Music scraper, lyrics
├── lastfm/ cast/ crashlytics/  # Optional platform feature modules
├── fastlane/          # Store metadata
└── .github/           # CI workflows
```

## Quick Start / Building

```bash
# 1. Requirements: JDK 17+, Android SDK (for Android targets)
# 2. Build the Android debug APK
./gradlew assembleDebug
# 3. Run the desktop app
./gradlew :composeApp:run
```

<details>
<summary>Advanced build targets</summary>

- `./gradlew :composeApp:packageReleaseDistributionForCurrentOS` — native desktop packages (Conveyor)
- `./gradlew :composeApp:mpvSetupAll` — fetch pinned mpv native bundles before desktop release builds
- Release signing uses `keystore.properties` at the repo root (not committed)

</details>

## Usage

Install the app, grant the notification permission, and search or paste a YouTube Music link. Playlists, liked songs and albums sync with your YouTube Music account when you sign in from Settings.

## FAQ / Troubleshooting

- **Playback fails on some songs** — the app depends on YouTube Music's streaming endpoints; player errors on individual tracks are expected and usually upstream.
- **Desktop build fails on mpv natives** — run `./gradlew :composeApp:mpvSetupAll` first; it downloads and verifies the pinned native bundles.

## Contributing

Fork the repo, create a feature branch, and open a pull request. Keep commits focused; CI runs build checks on every PR.

## Roadmap

- [ ] Package namespace rebrand (`com.alzimer.echobox.*` → new namespace)
- [ ] Self-hosted update channel
- [ ] iOS TestFlight distribution

## Changelog

See [GitHub Releases](https://github.com/alzimerahmed84/EchoBox/releases).

## License

GPL-3.0 — see [LICENSE](LICENSE). Maintained by Alzimer Ahmed (alzimerahmed84@gmail.com).
