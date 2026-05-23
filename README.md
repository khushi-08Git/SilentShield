# SilentShield 🛡️

> Real-time Android security app that protects users from scam calls, phishing SMS, and malicious links.

![Android](https://img.shields.io/badge/Platform-Android-green?logo=android)
![Kotlin](https://img.shields.io/badge/Language-Kotlin-blue?logo=kotlin)
![Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-orange)
![Firebase](https://img.shields.io/badge/Backend-Firebase-yellow?logo=firebase)
![MinSDK](https://img.shields.io/badge/Min%20SDK-26-red)
![TargetSDK](https://img.shields.io/badge/Target%20SDK-36-red)

---

## 📖 Description

SilentShield is an Android security application designed to protect everyday smartphone users from digital fraud. Millions of users — especially in India — receive fake KYC calls, OTP scam messages, and phishing links daily with zero built-in protection. SilentShield runs silently in the background, monitoring incoming calls and SMS in real time, scanning URLs for threats, and enabling users to crowdsource scam reports — all powered by a clean MVVM architecture and reactive Kotlin Flows.

---

## ✨ Features

### 🔴 Scam Call Detection
- Monitors incoming calls via Android Foreground Service running 24/7
- Two-layer number analysis — local pattern matching + NumLookup API
- Detects known scam prefixes, VOIP numbers, repeated digit patterns, and hidden numbers
- Instant notification on detection with risk level — HIGH, MEDIUM, or LOW
- Auto-restarts after device reboot via BootReceiver

### 💬 SMS Phishing Detection
- BroadcastReceiver with priority 999 intercepts messages before other apps
- Pattern engine with 20+ phishing templates covering OTP fraud, fake KYC, loan scams, bank spoofing, and prize fraud
- Detects URL shorteners, spoofed sender IDs, and suspicious keyword combinations
- Works completely offline — no internet required

### 🔗 Link Scanner
- Two-layer URL threat detection
- **Layer 1** — Instant offline heuristics: URL shorteners, suspicious keywords, IP-based domains, fake bank login patterns
- **Layer 2** — Google Safe Browsing API v4: checks against Google's live threat database covering malware, social engineering, and unwanted software
- Graceful offline fallback if API is unavailable
- Results shown as SAFE / SUSPICIOUS / DANGEROUS with exact reasons

### 🌐 Community Reports
- Crowdsourced scam intelligence feed shared across all users
- Real-time updates via Firebase Firestore snapshot listeners — no manual refresh
- Submit reports for scam calls, SMS, or links
- Upvote system to surface the most reported threats
- Reports appear instantly for all users worldwide

### 📊 Protection Dashboard
- Live stats: calls blocked, SMS scanned, links checked
- Persisted locally via DataStore — survives app restarts
- Recent alerts list populated from real detections
- Protection toggle to pause/resume monitoring

### ⚙️ Settings
- Toggle call scanning, SMS scanning, notifications independently
- Auto-block high risk callers
- View real-time permission status
- Community report feed toggle

---

## 🏗️ Architecture

SilentShield follows **MVVM (Model-View-ViewModel)** with a clean **Repository pattern**.

```
┌─────────────────────────────────────────┐
│           UI Layer (Compose)            │
│   Screens observe StateFlow from VM     │
├─────────────────────────────────────────┤
│         ViewModel Layer                 │
│   Holds UI state, handles user events   │
├─────────────────────────────────────────┤
│         Repository Layer                │
│   Single source of truth for data      │
├────────────┬────────────┬───────────────┤
│    Room    │  DataStore │   Firebase    │
│  (Alerts) │  (Stats)   │  (Reports)    │
└────────────┴────────────┴───────────────┘

Background Layer (independent of UI)
┌─────────────────────────────────────────┐
│  CallDetectionService (ForegroundSvc)  │
│  SmsReceiver (BroadcastReceiver)        │
│  CallStateReceiver (BroadcastReceiver)  │
│  BootReceiver (BroadcastReceiver)       │
└─────────────────────────────────────────┘
```

---

## 🛠️ Tech Stack

| Category | Technology |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Architecture | MVVM + Repository Pattern |
| Navigation | Jetpack Navigation 3 |
| Local Database | Room + KSP |
| Preferences | DataStore Preferences |
| Networking | Retrofit + OkHttp + Gson |
| Cloud Database | Firebase Firestore |
| Link Scanning | Google Safe Browsing API v4 |
| Background | Foreground Service + BroadcastReceivers |
| Async | Kotlin Coroutines + Flow |
| Permissions | Accompanist Permissions |
| Build | Gradle Version Catalogs |
| Min SDK | 26 (Android 8.0) |
| Target SDK | 36 (Android 16) |

---

## 📱 Screens

| Screen | Description |
|---|---|
| Splash | Animated launch screen |
| Permissions | Runtime permission requests for Phone, SMS, Notifications |
| Onboarding | 3-slide feature introduction |
| Home | Dashboard with shield status, stats, and recent alerts |
| Scan Link | URL scanner with real-time threat analysis |
| Reports | Community scam intelligence feed |
| Alert Detail | Full threat detail with recommended actions |
| Settings | App preferences and permission status |

---

## 🔄 Data Flow

```
Incoming Call
    → CallStateReceiver catches PHONE_STATE_CHANGED broadcast
    → Starts CallDetectionService with phone number
    → ScamNumberChecker runs local patterns + API check
    → If scam: Room INSERT + DataStore increment + Notification
    → HomeScreen updates live via Flow

Incoming SMS
    → SmsReceiver intercepts SMS_RECEIVED broadcast
    → SmsAnalyzer runs 20+ phishing pattern checks
    → If phishing: Room INSERT + DataStore increment + Notification
    → HomeScreen updates live via Flow

Link Scan
    → Local heuristics run instantly (offline)
    → Google Safe Browsing API called
    → Results combined → SAFE / SUSPICIOUS / DANGEROUS
    → DataStore links counter incremented
```

---

## 🚀 Setup

### Prerequisites
- Android Studio Hedgehog or later
- Android device or emulator with API 26+
- Google account for Firebase and Safe Browsing API

### Steps

**1. Clone the repository**
```bash
git clone https://github.com/khushi-08Git/SilentShield.git
```

**2. Firebase setup**
- Go to [console.firebase.google.com](https://console.firebase.google.com)
- Create a new project → Add Android app with your package name
- Download `google-services.json` → place in `app/` folder
- Enable Firestore Database in test mode

**3. Google Safe Browsing API**
- Go to [console.cloud.google.com](https://console.cloud.google.com)
- Enable Safe Browsing API → Create API Key
- Open `data/remote/ApiKeys.kt` → paste your key:
```kotlin
const val SAFE_BROWSING = "YOUR_API_KEY_HERE"
```

**4. Build and run**
```bash
./gradlew assembleDebug
```

---

## 📋 Permissions Required

| Permission | Purpose |
|---|---|
| `READ_PHONE_STATE` | Detect incoming call state |
| `READ_CALL_LOG` | Access call information |
| `RECEIVE_SMS` | Intercept incoming SMS |
| `READ_SMS` | Read message content for analysis |
| `POST_NOTIFICATIONS` | Show scam alert notifications |
| `FOREGROUND_SERVICE` | Keep detection service running |
| `RECEIVE_BOOT_COMPLETED` | Auto-restart after reboot |
| `INTERNET` | Safe Browsing API + Firebase |

---

## 🔮 Roadmap

- [ ] Firebase Authentication for real user identity
- [ ] Firestore security rules for production
- [ ] Crowdsourced scam number database
- [ ] WorkManager for scheduled database cleanup
- [ ] Block call functionality
- [ ] Export alert history as PDF
- [ ] Widget for quick link scanning

---

## 👨‍💻 Author

Built with ❤️ as a portfolio project demonstrating real-world Android development with background services, multiple data sources, third-party API integration, and reactive architecture.

---

