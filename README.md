# 📞 Auto Dialer - Android App

Auto Dialer is a powerful Android application that automates phone calls to a list of numbers uploaded via a text file. It's useful for scenarios like sales follow-ups, appointment confirmations, or bulk voice messages.

## 📲 Features

- 📁 Upload a file containing phone numbers
- ☎️ Automatically call numbers sequentially
- 🔴 Call state tracking (Idle, Connecting, Disconnected)
- 📅 Save call outcomes with selected date
- ✅ Mark numbers with status: Saved, Follow-up, Not Interested
- 🧾 Overlay support while calling
- 📊 Count of remaining numbers displayed

## 🛠️ Tech Stack

- Kotlin
- Android SDK
- Android TelephonyManager
- Custom ListView Adapter
- XML UI with CardView and ListView
- Optional: Google Mobile Ads SDK (for monetization)

## 🛡 Permissions

The app requires the following permissions:
- `CALL_PHONE` - to initiate phone calls
- `SYSTEM_ALERT_WINDOW` - to show overlay on screen during a call
- File access - to read uploaded contact files

## ⚠ Disclaimer

This app **makes real phone calls** from your device. Ensure you have enough credit or call plan if using on a live network. Use responsibly and ethically.



## 🚀 Getting Started

1. Clone the repo:
git clone https://github.com/yourusername/auto-dialer.git
