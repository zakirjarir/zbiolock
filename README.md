<div align="center">

# 🔐 ZBioLock

### Enterprise-grade biometric authentication & secure token storage for Capacitor apps

<p>
  <img src="https://img.shields.io/npm/v/zbiolock.svg?style=for-the-badge&color=CB3837&logo=npm&logoColor=white" alt="npm version" />
  <img src="https://img.shields.io/badge/License-MIT-blue.svg?style=for-the-badge" alt="MIT License" />
  <img src="https://img.shields.io/badge/Capacitor-7%2B-3880ff.svg?style=for-the-badge&logo=capacitor&logoColor=white" alt="Capacitor 7+" />
</p>
<p>
  <img src="https://img.shields.io/badge/Android-3DDC84.svg?style=flat-square&logo=android&logoColor=white" alt="Android" />
  <img src="https://img.shields.io/badge/iOS-000000.svg?style=flat-square&logo=apple&logoColor=white" alt="iOS" />
  <img src="https://img.shields.io/badge/Web-safe%20fallback-6c757d.svg?style=flat-square" alt="Web" />
  <img src="https://github.com/zakirjarir/zbiolock/actions/workflows/ci.yml/badge.svg" alt="CI" />
  <img src="https://img.shields.io/badge/Status-In%20Development-orange.svg?style=flat-square" alt="Status" />
</p>

**Hardware-backed biometric auth & token storage — built for banking, healthcare & government apps.**

</div>

<br/>

> [!WARNING]
> ### 🚧 Active Development — Not Yet Production-Ready
> - The API may change without notice between versions
> - Not yet verified on physical devices across all Android/iOS versions
> - **Not recommended** for live/production applications at this stage
> - Breaking changes may occur before the `v1.0.0` stable release
>
> **Use in production at your own risk.** ⭐ Watch this repo for stable release announcements.

<br/>

## ✨ Overview

ZBioLock provides **biometric authentication** (fingerprint, Face ID, iris, device PIN) and **hardware-backed secure token storage**, powered by:

| Platform | Storage Backend |
|:--|:--|
| 🤖 **Android** | Android Keystore + `EncryptedSharedPreferences` |
| 🍎 **iOS** | Keychain Services (Secure Enclave) |

Designed from the ground up for applications where security is **non-negotiable**.

<br/>

## 🧩 Feature Matrix

| Feature | 🤖 Android | 🍎 iOS | 🌐 Web |
|:--|:--:|:--:|:--:|
| Fingerprint | ✅ | ✅ | — |
| Face Unlock / Face ID | ✅ | ✅ | — |
| Iris Authentication | ✅ `Android 10+` | ✅ `Vision Pro` | — |
| Device PIN / Pattern / Password | ✅ | ✅ | — |
| Hardware-backed token storage | ✅ Keystore | ✅ Keychain | — |
| Secure fallback behavior | ✅ | ✅ | ✅ |

<br/>

## 📖 Table of Contents

<table>
<tr>
<td valign="top" width="33%">

**Getting Started**
- [Installation](#-installation)
- [Capacitor Sync](#-capacitor-sync)
- [Android Setup](#-android-setup)
- [iOS Setup](#-ios-setup)

</td>
<td valign="top" width="33%">

**Using the Plugin**
- [API Reference](#-api-reference)
- [Usage Examples](#-usage-examples)
- [Biometric Types](#-biometric-types)
- [Error Codes](#-error-codes)

</td>
<td valign="top" width="33%">

**Reference**
- [Security Notes](#-security-notes)
- [Troubleshooting](#-troubleshooting)
- [FAQ](#-faq)
- [Changelog](#-changelog)

</td>
</tr>
</table>

<br/>

## 📦 Installation

```bash
npm install zakirjarir/zbiolock
npx cap sync
```

<br/>

## 🔄 Capacitor Sync

After installing, always run:

```bash
npx cap sync
```

This copies the native plugin code into your Android and iOS projects.

<br/>

## 🤖 Android Setup

### 1️⃣ Register the Plugin

In your app's `MainActivity.kt`:

```kotlin
import com.zakirjarir.zbiolock.ZBioLockPlugin

class MainActivity : BridgeActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        registerPlugin(ZBioLockPlugin::class.java)
        super.onCreate(savedInstanceState)
    }
}
```

### 2️⃣ AndroidManifest.xml — Required Changes

> [!IMPORTANT]
> You **must** add the `USE_BIOMETRIC` permission to your **app's** `AndroidManifest.xml`. Although the plugin's own AAR manifest declares this permission, Android Gradle sometimes does not auto-merge it reliably across all project configurations. Adding it explicitly is always safe and is the recommended practice.

Open `android/app/src/main/AndroidManifest.xml` and add the following **inside the `<manifest>` tag, outside the `<application>` block**:

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <application ...>
        <!-- your existing application content -->
    </application>

    <!-- ─────────────────────────────────────────────── -->
    <!--  ZBioLock — Biometric Permissions                -->
    <!-- ─────────────────────────────────────────────── -->

    <!--
        REQUIRED: Grants access to the BiometricPrompt API (Android 9+, API 28+).
        On Android 8 or below, the system automatically down-grades this
        to USE_FINGERPRINT — no extra work needed.
    -->
    <uses-permission android:name="android.permission.USE_BIOMETRIC" />

    <!--
        OPTIONAL — Hardware feature hints for the Play Store.
        android:required="false" keeps the app installable on devices
        without a given sensor; ZBioLock reports it as unavailable at runtime.
    -->
    <uses-feature android:name="android.hardware.fingerprint" android:required="false" />
    <uses-feature android:name="android.hardware.biometrics.face" android:required="false" />
    <uses-feature android:name="android.hardware.biometrics.iris" android:required="false" />

</manifest>
```

#### What each entry does

| Entry | Required? | Purpose |
|:--|:--:|:--|
| `USE_BIOMETRIC` | ✅ **Yes** | Grants runtime access to `BiometricPrompt` |
| `android.hardware.fingerprint` | No | Hints fingerprint usage to Play Store |
| `android.hardware.biometrics.face` | No | Hints Face Unlock usage to Play Store |
| `android.hardware.biometrics.iris` | No | Hints iris scanner usage to Play Store |

> [!NOTE]
> `USE_BIOMETRIC` is a **normal** permission — it never triggers a runtime permission dialog. Access is controlled entirely through the biometric prompt itself.

### 3️⃣ Minimum SDK

ZBioLock requires `minSdkVersion 24` (Android 7.0). Verify in `android/variables.gradle` or `android/app/build.gradle`:

```gradle
minSdkVersion = 24
```

> Biometric features behave optimally on **Android 9+ (API 28+)**.

### 4️⃣ Manifest Merge Conflicts

If you hit a build error like:

```
Manifest merger failed: Attribute ... from AndroidManifest.xml is also present...
```

Add a `tools:replace` attribute:

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
          xmlns:tools="http://schemas.android.com/tools">

    <uses-permission
        android:name="android.permission.USE_BIOMETRIC"
        tools:replace="android:required" />

</manifest>
```

<br/>

## 🍎 iOS Setup

### 1️⃣ Face ID Usage Description — Required

Add to `ios/App/App/Info.plist`:

```xml
<key>NSFaceIDUsageDescription</key>
<string>We use Face ID to securely authenticate you and protect your account.</string>
```

> [!CAUTION]
> **Without this key, the app will crash on Face ID devices.**

### 2️⃣ Minimum Deployment Target

ZBioLock targets **iOS 15+**. Verify your Podfile:

```ruby
platform :ios, '15.0'
```

<br/>

## 🛠 API Reference

<details open>
<summary><code>isAvailable()</code> — check biometric / device-credential availability</summary>

```typescript
const result = await ZBioLock.isAvailable();
// { isAvailable: true, biometricType: 'face' }
```
</details>

<details open>
<summary><code>authenticate(options?)</code> — show the OS biometric prompt</summary>

```typescript
const result = await ZBioLock.authenticate({
  title: 'Verify Identity',
  subtitle: 'Use biometrics to access your account',
  description: 'Place your finger or look at the camera',
  allowDeviceCredential: true, // Allow PIN/pattern fallback
  cancelText: 'Cancel',        // Android only
});
// { success: true, biometricType: 'fingerprint' }
```

| Option | Type | Default | Description |
|:--|:--|:--|:--|
| `title` | `string` | `"Biometric Authentication"` | Prompt title |
| `subtitle` | `string` | — | Subtitle text |
| `description` | `string` | — | Body description |
| `allowDeviceCredential` | `boolean` | `false` | Allow PIN/pattern/password |
| `cancelText` | `string` | `"Cancel"` | Negative button text (Android) |
</details>

<details>
<summary><code>saveToken(options)</code> — securely store a token string</summary>

```typescript
await ZBioLock.saveToken({ key: 'access_token', token: myJWT });
```
</details>

<details>
<summary><code>getToken(options)</code> — retrieve a stored token (returns <code>null</code> if not found)</summary>

```typescript
const { token } = await ZBioLock.getToken({ key: 'access_token' });
```
</details>

<details>
<summary><code>deleteToken(options)</code> — delete a single stored token</summary>

```typescript
await ZBioLock.deleteToken({ key: 'access_token' });
```
</details>

<details>
<summary><code>clear()</code> — delete <b>all</b> stored tokens (call on logout)</summary>

```typescript
await ZBioLock.clear();
```
</details>

<details>
<summary><code>getBiometricType()</code> — return the primary biometric type available</summary>

```typescript
const { biometricType } = await ZBioLock.getBiometricType();
```
</details>

<details>
<summary><code>isBiometricEnrolled()</code> — whether the user has enrolled a biometric credential</summary>

```typescript
const { enrolled } = await ZBioLock.isBiometricEnrolled();
```
</details>

<br/>

## 💡 Usage Examples

<details open>
<summary><b>Ionic / Angular</b></summary>

```typescript
// auth.service.ts
import { Injectable } from '@angular/core';
import { ZBioLock } from 'zbiolock';

@Injectable({ providedIn: 'root' })
export class AuthService {
  async loginWithBiometrics(): Promise<void> {
    const { isAvailable } = await ZBioLock.isAvailable();
    if (!isAvailable) throw new Error('Biometrics not available');

    const { success } = await ZBioLock.authenticate({
      title: 'Sign In',
      allowDeviceCredential: true,
    });

    if (success) {
      const { token } = await ZBioLock.getToken({ key: 'access_token' });
      // token is ready to use
    }
  }

  async logout(): Promise<void> {
    await ZBioLock.clear(); // wipe all stored tokens
  }
}
```
</details>

<details>
<summary><b>Ionic / Vue</b></summary>

```typescript
// useAuth.ts
import { ZBioLock } from 'zbiolock';

export function useAuth() {
  const authenticate = async () => {
    const result = await ZBioLock.authenticate({ title: 'Confirm Identity' });
    return result.success;
  };

  const storeToken = (token: string) =>
    ZBioLock.saveToken({ key: 'jwt', token });

  const readToken = () =>
    ZBioLock.getToken({ key: 'jwt' });

  const logout = () => ZBioLock.clear();

  return { authenticate, storeToken, readToken, logout };
}
```
</details>

<details>
<summary><b>Ionic / React</b></summary>

```typescript
// useBiometric.ts
import { ZBioLock } from 'zbiolock';
import { useCallback } from 'react';

export function useBiometric() {
  const login = useCallback(async () => {
    const { success } = await ZBioLock.authenticate({
      title: 'Biometric Login',
      allowDeviceCredential: true,
    });
    if (!success) throw new Error('Authentication failed');
    return ZBioLock.getToken({ key: 'session_token' });
  }, []);

  return { login };
}
```
</details>

<details>
<summary><b>Vanilla Capacitor</b></summary>

```typescript
import { ZBioLock } from 'zbiolock';

async function run() {
  const { isAvailable, biometricType } = await ZBioLock.isAvailable();
  console.log(`Available: ${isAvailable}, Type: ${biometricType}`);

  if (isAvailable) {
    try {
      await ZBioLock.authenticate({ title: 'Unlock', allowDeviceCredential: true });
      await ZBioLock.saveToken({ key: 'my_token', token: 'Bearer abc123' });
      const { token } = await ZBioLock.getToken({ key: 'my_token' });
      console.log('Token:', token);
    } catch (err: unknown) {
      const e = err as { code: string; message: string };
      console.error(`[${e.code}] ${e.message}`);
    }
  }
}
```
</details>

<br/>

## 🧬 Biometric Types

| Value | Description |
|:--|:--|
| `fingerprint` | Fingerprint / Touch ID |
| `face` | Android Face Unlock / Apple Face ID |
| `iris` | Iris scanner |
| `device_credential` | PIN, pattern, or password |
| `unknown` | Hardware detected but type unrecognized |
| `none` | No authentication available |

<br/>

## ⚠️ Error Codes

| Code | Meaning |
|:--|:--|
| `BIOMETRIC_NOT_AVAILABLE` | Hardware unavailable or disabled |
| `BIOMETRIC_NOT_ENROLLED` | No biometrics enrolled on device |
| `AUTHENTICATION_FAILED` | Attempt failed (wrong finger, etc.) |
| `AUTHENTICATION_CANCELED` | User dismissed the prompt |
| `AUTHENTICATION_LOCKED` | Too many failed attempts — locked out |
| `DEVICE_NOT_SUPPORTED` | Platform does not support this feature |
| `TOKEN_NOT_FOUND` | Requested key does not exist in storage |
| `INVALID_ARGUMENT` | Missing or malformed parameter |
| `UNKNOWN_ERROR` | An unexpected internal error occurred |

Handle errors with a typed catch:

```typescript
try {
  await ZBioLock.authenticate();
} catch (err: unknown) {
  const e = err as { code: string; message: string };
  if (e.code === 'AUTHENTICATION_CANCELED') {
    // User tapped cancel — no action needed
  } else if (e.code === 'AUTHENTICATION_LOCKED') {
    // Show lockout message
  }
}
```

<br/>

## 🔒 Security Notes

> [!IMPORTANT]
> ZBioLock **never** stores tokens in `localStorage`, `sessionStorage`, `SharedPreferences`, or `UserDefaults`. All storage is hardware-backed and encrypted.

| Platform | Storage Mechanism | Encryption | Backup Protected |
|:--|:--|:--|:--:|
| 🤖 Android | `EncryptedSharedPreferences` | AES-256-GCM (Android Keystore) | ✅ Yes |
| 🍎 iOS | Keychain Services | Hardware-backed AES (Secure Enclave) | ✅ `ThisDeviceOnly` |
| 🌐 Web | ❌ Not available | ❌ | — |

### ✅ Best Practices

- Always call `ZBioLock.clear()` on **logout** — wipes all stored tokens from hardware storage
- Tokens are **device-bound** — they cannot be extracted, copied, or migrated to another device
- iOS: `kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly` keeps tokens inaccessible before the first unlock after reboot (cold-boot protection)
- Android: the Keystore master key is tied to the **app signing certificate** — repackaged APKs cannot access your tokens
- The plugin **never transmits data over the network** — everything stays on-device

### 🚫 Avoid

- Logging or printing token values in production code — treat them as secrets
- Storing tokens in React/Vue/Angular component state beyond what's immediately needed

<br/>

## 🩺 Troubleshooting

<details>
<summary><b>Android — <code>BIOMETRIC_NOT_AVAILABLE</code></b> hardware error</summary>

**Symptoms:** `isAvailable()` returns `false`, or `authenticate()` throws `BIOMETRIC_NOT_AVAILABLE`.

**Causes & Fixes:**
- Sensor temporarily unavailable (overheated, covered, reboot needed) — ask the user to retry
- No biometric hardware present — expected on low-end devices; use `allowDeviceCredential: true` as a fallback
- Device is in a locked-down state (e.g., enterprise MDM policy) — no workaround
</details>

<details>
<summary><b>Android — <code>AUTHENTICATION_LOCKED</code></b></summary>

**Symptoms:** `authenticate()` throws `AUTHENTICATION_LOCKED`.

**Fix:** Too many failed attempts. The prompt locks for a period (usually 30s for soft lock, or until reboot for permanent lock). Show a friendly message and retry after the timeout.
</details>

<details>
<summary><b>Android — build fails:</b> <code>Could not find org.jetbrains.kotlin:kotlin-gradle-plugin</code></summary>

**Fix:** Add the Kotlin classpath to your **root-level** `build.gradle` (not `android/app/build.gradle`):

```gradle
buildscript {
    dependencies {
        classpath "org.jetbrains.kotlin:kotlin-gradle-plugin:2.0.20"
    }
}
```
</details>

<details>
<summary><b>Android — <code>Manifest merger failed</code></b></summary>

```
Manifest merger failed: Attribute uses-permission#android.permission.USE_BIOMETRIC
```

**Fix:** Add `xmlns:tools` and `tools:replace` to your app manifest:

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
          xmlns:tools="http://schemas.android.com/tools">

    <uses-permission
        android:name="android.permission.USE_BIOMETRIC"
        tools:replace="android:required" />

</manifest>
```
</details>

<details>
<summary><b>Android — <code>EncryptedSharedPreferences</code> crashes after reinstall / key change</b></summary>

**Symptoms:** `UNKNOWN_ERROR` on `saveToken()` or `getToken()` after reinstalling the app or clearing app data.

**Cause:** The Android Keystore key was invalidated (e.g., new fingerprint enrolled, device reset).

**Fix:** ZBioLock automatically recovers by deleting and recreating the encrypted prefs file. If it still fails, call `ZBioLock.clear()` to wipe corrupted state, then re-save tokens.
</details>

<details>
<summary><b>iOS — app crashes immediately on Face ID device</b></summary>

**Fix:** Add `NSFaceIDUsageDescription` to `ios/App/App/Info.plist`:

```xml
<key>NSFaceIDUsageDescription</key>
<string>We use Face ID to securely verify your identity.</string>
```

See the full [iOS Setup](#-ios-setup) section.
</details>

<details>
<summary><b>iOS — <code>biometryNotAvailable</code> on Simulator</b></summary>

**Fix:** In Xcode Simulator, go to:
- **Face ID:** `Features → Face ID → Enrolled`
- **Touch ID:** `Features → Touch ID → Enrolled`

Then trigger a matching biometric: `Features → Face ID → Matching Face`.
</details>

<details>
<summary><b>iOS — <code>AUTHENTICATION_CANCELED</code> immediately (no prompt shown)</b></summary>

**Cause:** The `LAContext` was invalidated before `evaluatePolicy` ran — usually caused by calling `authenticate()` from a background thread.

**Fix:** Handled internally by ZBioLock — `evaluatePolicy` always runs on `DispatchQueue.main`. If wrapping the call in a custom background task, ensure you `await` it on the main thread in your app code.
</details>

<details>
<summary><b>Web — <code>DEVICE_NOT_SUPPORTED</code> on every call</b></summary>

**Expected behavior.** The web platform has no biometric hardware or secure storage APIs. ZBioLock returns safe fallbacks:

```typescript
const { isAvailable } = await ZBioLock.isAvailable();
// isAvailable === false on web — no error thrown

await ZBioLock.authenticate(); // throws DEVICE_NOT_SUPPORTED — handle gracefully
```

Always check `isAvailable` before calling `authenticate()` in cross-platform code.
</details>

<br/>

## ❓ FAQ

<details>
<summary><b>Can I use ZBioLock in a banking or fintech app?</b></summary>
<br/>

Yes. Token storage uses Android Keystore (AES-256-GCM) and iOS Secure Enclave / Keychain, satisfying:
- OWASP MASVS L2 (credential storage requirements)
- PCI-DSS requirement 8 (strong authentication)
- FIDO2 / biometric authentication guidelines
</details>

<details>
<summary><b>Does it support Capacitor 7?</b></summary>
<br/>

Yes. The peer dependency is `>=7.0.0`. Both Capacitor 7 and Capacitor 8 are supported.
</details>

<details>
<summary><b>The user has no biometrics enrolled — what happens?</b></summary>
<br/>

- `isAvailable()` returns `{ isAvailable: false, biometricType: 'none' }` when no biometrics **and** no device credential are available
- If you pass `allowDeviceCredential: true`, `isAvailable()` returns `true` as long as the device has a PIN/pattern/password set
- `isBiometricEnrolled()` always accurately reflects whether a biometric is enrolled — independently of device credential
</details>

<details>
<summary><b>Can another app on the device read my stored tokens?</b></summary>
<br/>

No. On Android, `EncryptedSharedPreferences` is private to your app's UID and the Keystore key is bound to your app's signing certificate. On iOS, Keychain items are sandboxed to your app's bundle ID by default (no shared `kSecAttrAccessGroup` is set). Neither platform exposes tokens to other apps.
</details>

<details>
<summary><b>What happens to tokens when the user adds a new fingerprint?</b></summary>
<br/>

**Android:** adding a new biometric invalidates existing Keystore keys only if created with `setInvalidatedByBiometricEnrollment(true)`. ZBioLock uses `EncryptedSharedPreferences` with a standard `MasterKey`, which is **not** invalidated on new enrollment by default — existing tokens remain accessible.

**iOS:** adding a new Touch ID / Face ID does **not** invalidate existing Keychain items.
</details>

<details>
<summary><b>Can I use multiple token keys simultaneously?</b></summary>
<br/>

Yes. `saveToken` / `getToken` / `deleteToken` all accept a `key` parameter:

```typescript
await ZBioLock.saveToken({ key: 'access_token',  token: accessJWT });
await ZBioLock.saveToken({ key: 'refresh_token', token: refreshJWT });
await ZBioLock.saveToken({ key: 'session_id',    token: sessionId });

// On logout — wipe them all at once:
await ZBioLock.clear();
```
</details>

<details>
<summary><b>Can I migrate tokens to a new phone?</b></summary>
<br/>

No — by design. Tokens are bound to the device's hardware security module. Users must log in again on a new device and re-save their tokens. This is a **security feature**, not a limitation.
</details>

<details>
<summary><b>Does ZBioLock work with Ionic's <code>@capacitor-community/biometric-auth</code>?</b></summary>
<br/>

Yes — ZBioLock is fully independent and does not conflict with other biometric plugins. You can use them side-by-side, though ZBioLock covers all features of that plugin plus secure token storage.
</details>

<details>
<summary><b>Is there a React Native version?</b></summary>
<br/>

Not at this time. ZBioLock is a Capacitor-native plugin. A React Native port is not planned, but community contributions are welcome.
</details>

<br/>

## 📝 Changelog

### `v0.0.1` — Initial Release

- ✅ `isAvailable()` — checks biometric + device credential availability
- ✅ `authenticate()` — BiometricPrompt (Android) / LAContext (iOS) with full option support
- ✅ `saveToken()` / `getToken()` / `deleteToken()` / `clear()` — hardware-backed secure storage
- ✅ `getBiometricType()` / `isBiometricEnrolled()` — device capability introspection
- ✅ Android Keystore + `EncryptedSharedPreferences` (AES-256-GCM)
- ✅ iOS Keychain Services (`kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly`)
- ✅ Web safe fallback — no crashes, meaningful errors
- ✅ Strict TypeScript — no `any` types
- ✅ Capacitor 7 + 8 compatible
- ✅ GitHub Actions CI (TypeScript + Android + iOS)

<br/>

## 🤝 Contributing

Contributions are welcome! Please read [CONTRIBUTING.md](CONTRIBUTING.md) before submitting a pull request.

For security vulnerabilities, please use [GitHub Security Advisories](https://github.com/zakirjarir/zbiolock/security/advisories) instead of opening a public issue.

<br/>

## 🚀 Publishing

```bash
# 1. Bump version in package.json first
# 2. Build the plugin (runs automatically via prepublishOnly)
npm publish --access public
```

See [PUBLISHING.md](PUBLISHING.md) for the full release checklist.

<br/>

## 📄 License

MIT © [Zakir Jarir](https://github.com/zakirjarir/zbiolock)

<br/>

<div align="center">

Built with ❤️ for the Capacitor community.
**Secure by design. Enterprise by standard.**

</div>