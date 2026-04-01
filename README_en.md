# JiZhi - Today's Poetry

<div align="center">

![Kotlin](https://img.shields.io/badge/Kotlin-2.2.20-blue?logo=kotlin)
![Jetpack Compose](https://img.shields.io/badge/Compose-BOM%202024.02.00-blue)
![License](https://img.shields.io/badge/License-Apache%202.0-green)
![Min SDK](https://img.shields.io/badge/Min%20SDK-24-orange)

</div>

JiZhi (几枝) is an elegant Android poetry app that delivers a classic Chinese poem to you every day. Built with pure Kotlin and Jetpack Compose, it offers a smooth and natural reading experience.

## Features

### Core Features
- **Daily Poetry**: Auto-fetch and display精选 poetry each day
- **History**: Automatically save viewed poems with browse and delete support
- **Poem Details**: Show complete poem content, author, and dynasty information
- **Translation**: Provide modern Chinese translations
- **Favorites**: One-tap收藏 favorite poems

### Smart Detection
- **Ci Detection**: Intelligently detect词牌名 and switch layout styles (Poem vs Ci)
- **800+ 词牌名 Database**: Built-in complete词牌名 database

### Home Screen Widget
- **Multiple Sizes**: Support 1x1, 2x2, 2x4, 3x4 widget sizes
- **Scheduled Updates**: Customizable update frequency from 15 minutes to 1 day
- **Background Color**: 12 preset colors + custom color support
- **Text Color**: 8 preset text colors
- **Custom Fonts**: System font switching + custom font file support

### Personalization
- **Multi-language**: Support Chinese, English, and System Default
- **Line Break Modes**: Default, Smart Punctuation, Force Punctuation
- **Theme Support**: Dark/Light theme auto-adaptation

## Tech Stack

| Category | Technology |
|----------|------------|
| **Language** | Kotlin 2.2.20 |
| **UI Framework** | Jetpack Compose (BOM 2024.02.00) |
| **Dependency Injection** | Hilt 2.50 |
| **Local Storage** | Room + DataStore |
| **Networking** | Retrofit + OkHttp |
| **Background Tasks** | WorkManager |
| **Architecture** | MVVM + Clean Architecture |

## Project Structure

```
com.jizhi/
├── ui/                          # UI Layer
│   ├── main/                   # Main Screen (MainActivity, MainScreen)
│   ├── history/                # History Screen
│   ├── detail/                 # Poem Detail Screen
│   ├── setting/                # Settings Screen
│   └── theme/                  # Compose Theme
├── data/                       # Data Layer
│   ├── local/                  # Local Data (Room, DataStore)
│   ├── remote/                 # Remote API
│   ├── PoemType.kt             # 词牌名 Detection
│   └── PoemFormatter.kt        # Content Formatting
├── di/                         # Hilt Modules
├── repository/                 # Repository
├── worker/                     # WorkManager Tasks
└── widget/                     # Home Screen Widget
```

## Getting Started

### Requirements

- Android Studio Arctic Fox or higher
- JDK 17+
- Android SDK 36

### Build Project

```bash
# Debug build
./gradlew assembleDebug

# Debug build and install to device
./gradlew installDebug

# Release build
./gradlew assembleRelease
```

### Import Project

1. Clone the project locally
2. Open the project with Android Studio
3. Wait for Gradle sync to complete
4. Run or build

## API

App data is provided by [今日诗词 API](https://www.jinrishici.com/), thanks for the free open-source poetry data API.

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/token` | GET | Get user token |
| `/sentence` | GET | Get daily sentence |

## Contributing

Issues and Pull Requests are welcome!

1. Fork the project
2. Create feature branch (`git checkout -b feature/xxx`)
3. Commit your changes (`git commit -m 'Add xxx'`)
4. Push to the branch (`git push origin feature/xxx`)
5. Create a Pull Request

## License

This project is licensed under the Apache License 2.0.

---

<div align="center">

**Every time you open your phone, meet a poem**

</div>
