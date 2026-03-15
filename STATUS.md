# FlowStable UPI — Project Status & War Room Doc
**Last Updated:** Session 1 — Handoff from friend's abandoned code
**Owner:** You (took over from original dev "meet1")
**Device Used in Demo:** Xiaomi — API 35

---

## 🧭 Where We Started

Your friend built this app, shipped some code, then abandoned it.
You downloaded the zip. No fork. No GitHub of your own yet.
The code was sitting on your Mac doing nothing.

### What the original repo had:
- Full Android app in Kotlin
- USSD automation via AccessibilityService
- QR code scanner (ML Kit + CameraX)
- CI/CD pipeline (GitHub Actions)
- Hardcoded Telegram bot token pointing to your FRIEND's channel
- `local.properties` hardcoded to your friend's Windows PC path (`C:\Users\meet1\...`)
- Zero `.gitignore` — build folders, APKs, IDE files all going to git
- No security config for a payment app

---

## 📱 What the App Actually Does

It's an **offline UPI payment app** for India.
Instead of internet, it uses the `*99#` USSD GSM gateway.

```
User scans QR or types UPI ID + amount
        ↓
App dials *99# via CALL_PHONE permission
        ↓
AccessibilityService (USSDService.kt) watches the USSD dialog pop up
        ↓
Reads the text → matches keywords → auto-fills inputs → clicks Send
        ↓
Walks through: Main Menu → Enter UPI ID → Enter Amount
        ↓
STOPS at PIN — user types PIN manually (intentional, zero-trust design)
        ↓
Shows Processing screen with live state updates (polls every 500ms)
        ↓
Shows Result screen — Success or Failed
```

### Works on:
- Real Android phone ✅ (API 26+ / Android 8.0+)
- Emulator ❌ (no SIM, no USSD, no cellular radio)

---

## ✅ What We've Done This Session

### 1. Understood the full codebase
- Read every Kotlin file, every XML, every config
- Mapped the entire flow from button tap to payment result

### 2. Created branch: `fix/security-vulnerabilities`
All work lives here. `main` is untouched (your friend's original).

### 3. Fixed all security vulnerabilities

| # | What | File |
|---|---|---|
| 1 | Added `.gitignore` — stops local.properties, build/, APKs, keystores going to GitHub | `.gitignore` |
| 2 | `allowBackup="false"` — blocks ADB backup data theft on payment app | `AndroidManifest.xml` |
| 3 | Added `network_security_config.xml` — forces HTTPS only, blocks HTTP | `res/xml/network_security_config.xml` |
| 4 | Turned on minification + resource shrinking for release builds | `app/build.gradle.kts` |
| 5 | Fixed `accessibility_service_config.xml` — was missing `typeWindowContentChanged` event (USSD updates were being silently dropped) | `res/xml/accessibility_service_config.xml` |
| 6 | Added `@Volatile` to all shared state in `USSDController` singleton — thread safety between UI thread and Accessibility thread | `USSDController.kt` |
| 7 | Added real UPI ID regex validation + amount cap (max ₹1,00,000) + decimal check | `PaymentActivity.kt` |
| 8 | Blocked QR data injection — validates UPI scheme + ID format before filling fields | `PaymentActivity.kt` |
| 9 | `backup_rules.xml` — changed from "include everything" to "exclude everything" | `res/xml/backup_rules.xml` |

### 4. Fixed Telegram CI/CD
- Removed friend's hardcoded bot token + chat ID
- Wired to GitHub Secrets (`TELEGRAM_BOT_TOKEN`, `TELEGRAM_CHAT_ID`)
- APK also uploads as GitHub Artifact (downloadable from Actions page)
- You add YOUR OWN bot token → APK comes to YOUR Telegram on every push

### 5. Fixed two bugs visible in the demo video

**Bug 1 — ₹0 on Payment Success screen**
- Root cause: `ResultActivity` was reading `payment?.amount` but in some flows `currentPayment` was already null or amount wasn't passed
- Fix: Snapshot the payment object the moment `ResultActivity` opens, before anything can reset it. Hide amount if genuinely unavailable instead of showing ₹0

**Bug 2 — Balance result showing as "Payment Successful ₹0"**
- Root cause: Balance flow and Payment flow both hit the same `ResultActivity` code path with no differentiation
- Fix: Added `result_type` intent extra (`"payment"` or `"balance"`). Each has its own display branch now:
  - `balance` → "Balance Retrieved" title, shows balance text, hides amount field
  - `payment` → "Payment Successful" title, shows ₹amount + recipient
  - `failed` → "Payment Failed" title, hides amount

---

## 🗂️ Current Git State

```
Branch:  fix/security-vulnerabilities  ← active, all work here
main:    original friend's code        ← untouched

Commits on fix branch:
  1cfe355  fix: ₹0 bug + balance/payment screen separation
  8cf7a98  feat: wire Telegram delivery to GitHub Secrets
  81fd0d5  fix: resolve all security vulnerabilities
```

### Files changed from original:
```
.gitignore                                        ← NEW
.github/workflows/android.yml                     ← Telegram secrets fix
app/build.gradle.kts                              ← minification on
app/src/main/AndroidManifest.xml                  ← allowBackup off, network config
app/src/main/java/.../PaymentActivity.kt          ← UPI validation
app/src/main/java/.../ProcessingActivity.kt       ← result_type fix
app/src/main/java/.../ResultActivity.kt           ← ₹0 bug + screen separation
app/src/main/java/.../ussd/USSDController.kt      ← @Volatile thread safety
app/src/main/res/xml/accessibility_service_config ← typeWindowContentChanged added
app/src/main/res/xml/backup_rules.xml             ← exclude all backup
app/src/main/res/xml/network_security_config.xml  ← NEW, HTTPS only
```

---

## 🎯 Goal Right Now

**MVP demo video only. Not a product launch.**

You want to record a video showing:
1. App opens with SERVICE ACTIVE
2. Scan a QR or enter UPI ID + amount manually
3. USSD session kicks off, automation navigates the menu
4. PIN screen appears — user enters PIN
5. Payment Successful screen with correct amount + recipient name

No real money needs to move for the video if you don't want it to.

---

## ⏭️ What's Next (TODO)

- [ ] Push branch to YOUR GitHub repo (not friend's)
- [ ] Set up your own Telegram bot + add secrets to GitHub
- [ ] Test build on your Android phone with your SIM
- [ ] Enable AccessibilityService on phone before recording
- [ ] Record the demo video
- [ ] (Later) Merge `fix/security-vulnerabilities` → `main`
- [ ] (Later) Add "To Mobile" and "To Bank" flows — currently only "To UPI ID" works
- [ ] (Later) Add People section (currently UI exists, no functionality)

---

## ⚠️ Things to Know

1. **Emulator is useless for this app** — only real phone with real SIM works
2. **AccessibilityService must be manually enabled** on the phone every time you reinstall
3. **`local.properties` must never be committed** — `.gitignore` now handles this
4. **The Telegram token in the original code is your friend's** — do not use it, use your own
5. **Code rule:** Never delete existing code — comment it out if replacing. Original logic stays.
6. **Edit code anywhere** — Zed, Claude Code, VS Code. Android Studio is only needed to BUILD and push to phone.

---

*Doc maintained by: Antigravity (AI pair programmer)*
*Project owner: You*