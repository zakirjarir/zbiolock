# 🔐 ZBioLock

**Enterprise-grade biometric authentication & secure token storage for Capacitor apps.**

[![npm version](https://img.shields.io/npm/v/zbiolock.svg)](https://www.npmjs.com/package/zbiolock)
[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![Capacitor 7+](https://img.shields.io/badge/Capacitor-7%2B-3880ff)](https://capacitorjs.com)
[![Platform: Android iOS Web](https://img.shields.io/badge/Platform-Android%20%7C%20iOS%20%7C%20Web-green)](https://capacitorjs.com)
[![CI](https://github.com/zakirjarir/zbiolock/actions/workflows/ci.yml/badge.svg)](https://github.com/zakirjarir/zbiolock/actions)
[![Status: Development](https://img.shields.io/badge/Status-In%20Development-orange)](https://github.com/zakirjarir/zbiolock)

---

> [!WARNING]
> **🚧 This plugin is currently in active development and is NOT yet production-ready.**
>
> - The API may change without notice between versions.
> - It has not yet been tested on physical devices across all Android/iOS versions.
> - It is **not recommended** for use in live/production applications at this stage.
> - Breaking changes may occur before the `v1.0.0` stable release.
>
> **Use in production at your own risk.** Watch the repository for stable release announcements.

---


ZBioLock provides **biometric authentication** (fingerprint, Face ID, iris, device PIN) and **hardware-backed secure token storage** via **Android Keystore + EncryptedSharedPreferences** and **iOS Keychain Services**.

Designed for **banking**, **healthcare**, and **government** applications where security is non-negotiable.

---

## Features

| Feature | Android | iOS | Web |
|---|---|---|---|
| Fingerprint | ✅ | ✅ | — |
| Face Unlock / Face ID | ✅ | ✅ | — |
| Iris Authentication | ✅ (Android 10+) | ✅ (Apple Vision Pro) | — |
| Device PIN / Pattern / Password | ✅ | ✅ | — |
| Android Keystore token storage | ✅ | — | — |
| iOS Keychain token storage | — | ✅ | — |
| Secure fallback behavior | ✅ | ✅ | ✅ |

---

## Table of Contents

- [Installation](#installation)
- [Capacitor Sync](#capacitor-sync)
- [Android Setup](#android-setup)
- [iOS Setup](#ios-setup)
- [API Reference](#api-reference)
- [Usage Examples](#usage-examples)
  - [Ionic / Angular](#ionic--angular)
  - [Ionic / Vue](#ionic--vue)
  - [Ionic / React](#ionic--react)
  - [Vanilla Capacitor](#vanilla-capacitor)
- [Biometric Types](#biometric-types)
- [Error Codes](#error-codes)
- [Security Notes](#security-notes)
- [Troubleshooting](#troubleshooting)
- [FAQ](#faq)
- [Publishing](#publishing)
- [License](#license)

---

## Installation

```bash
npm install zbiolock
npx cap sync
```

---

## Capacitor Sync

After installing, always run:

```bash
npx cap sync
```

This copies the native plugin code into your Android and iOS projects.

---

## Android Setup

### 1. Register the Plugin

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

---

### 2. AndroidManifest.xml — Required Changes

> [!IMPORTANT]
> You **must** add the `USE_BIOMETRIC` permission to your **app's** `AndroidManifest.xml`.
> Although the plugin's own AAR manifest declares this permission, Android Gradle sometimes
> does not auto-merge it reliably in all project configurations. Adding it explicitly is
> always safe and is the recommended practice.

Open `android/app/src/main/AndroidManifest.xml` in your Capacitor/Ionic project and add
the following entries **inside the `<manifest>` tag, outside the `<application>` block**:

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <application ...>
        <!-- your existing application content -->
    </application>

    <!-- ─────────────────────────────────────────────────────────── -->
    <!-- ZBioLock — Biometric Permissions                            -->
    <!-- ─────────────────────────────────────────────────────────── -->

    <!--
        REQUIRED: Grants access to the BiometricPrompt API (Android 9+, API 28+).
        On devices running Android 8 or below, the system automatically
        down-grades this to USE_FINGERPRINT — no extra work needed.
    -->
    <uses-permission android:name="android.permission.USE_BIOMETRIC" />

    <!--
        OPTIONAL — Hardware feature hints.
        These tell the Play Store which sensor types your app can use.
        android:required="false" ensures the app is still installable on devices
        that lack a specific sensor — ZBioLock gracefully reports them as
        unavailable at runtime rather than blocking installation.
    -->
    <uses-feature
        android:name="android.hardware.fingerprint"
        android:required="false" />

    <uses-feature
        android:name="android.hardware.biometrics.face"
        android:required="false" />

    <uses-feature
        android:name="android.hardware.biometrics.iris"
        android:required="false" />

</manifest>
```

#### What each entry does

| Entry | Required? | Purpose |
|---|---|---|
| `USE_BIOMETRIC` | **Yes** | Grants runtime access to `BiometricPrompt` |
| `android.hardware.fingerprint` | No | Hints fingerprint usage to Play Store |
| `android.hardware.biometrics.face` | No | Hints Face Unlock usage to Play Store |
| `android.hardware.biometrics.iris` | No | Hints iris scanner usage to Play Store |

> [!NOTE]
> The `USE_BIOMETRIC` permission is a **normal** permission — it does not require a
> runtime permission dialog shown to the user. The OS controls access entirely through
> the biometric prompt itself.

---

### 3. Minimum SDK

ZBioLock requires `minSdkVersion 24` (Android 7.0). Verify in
`android/variables.gradle` or `android/app/build.gradle`:

```gradle
minSdkVersion = 24
```

Biometric features behave optimally on **Android 9+ (API 28+)**.

---

### 4. Troubleshooting — Manifest Merge Conflicts

If you see a build error like:

```
Manifest merger failed: Attribute ... from AndroidManifest.xml is also present...
```

Add a `tools:replace` attribute to resolve the conflict:

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
          xmlns:tools="http://schemas.android.com/tools">

    <uses-permission
        android:name="android.permission.USE_BIOMETRIC"
        tools:replace="android:required" />

</manifest>
```



## iOS Setup

### 1. Face ID Usage Description (Required)

Add to your app's `ios/App/App/Info.plist`:

```xml
<key>NSFaceIDUsageDescription</key>
<string>We use Face ID to securely authenticate you and protect your account.</string>
```

> ⚠️ **Without this key, the app will crash on Face ID devices.**

### 2. Minimum Deployment Target

ZBioLock targets **iOS 15+**. Verify your Podfile:

```ruby
platform :ios, '15.0'
```

---

## API Reference

### `isAvailable()`

Check whether biometric or device-credential authentication is available.

```typescript
const result = await ZBioLock.isAvailable();
// { isAvailable: true, biometricType: 'face' }
```

### `authenticate(options?)`

Show the OS biometric prompt.

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
|---|---|---|---|
| `title` | `string` | `"Biometric Authentication"` | Prompt title |
| `subtitle` | `string` | — | Subtitle text |
| `description` | `string` | — | Body description |
| `allowDeviceCredential` | `boolean` | `false` | Allow PIN/pattern/password |
| `cancelText` | `string` | `"Cancel"` | Negative button text (Android) |

### `saveToken(options)`

Securely store a token string.

```typescript
await ZBioLock.saveToken({ key: 'access_token', token: myJWT });
```

### `getToken(options)`

Retrieve a stored token. Returns `null` if not found.

```typescript
const { token } = await ZBioLock.getToken({ key: 'access_token' });
```

### `deleteToken(options)`

Delete a single stored token.

```typescript
await ZBioLock.deleteToken({ key: 'access_token' });
```

### `clear()`

Delete **all** tokens stored by this plugin. Call on user logout.

```typescript
await ZBioLock.clear();
```

### `getBiometricType()`

Return the primary biometric type available.

```typescript
const { biometricType } = await ZBioLock.getBiometricType();
```

### `isBiometricEnrolled()`

Return whether the user has enrolled at least one biometric credential.

```typescript
const { enrolled } = await ZBioLock.isBiometricEnrolled();
```

---

## Usage Examples

### Ionic / Angular

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

### Ionic / Vue

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

### Ionic / React

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

### Vanilla Capacitor

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

---

## Biometric Types

| Value | Description |
|---|---|
| `fingerprint` | Fingerprint / Touch ID |
| `face` | Android Face Unlock / Apple Face ID |
| `iris` | Iris scanner |
| `device_credential` | PIN, pattern, or password |
| `unknown` | Hardware detected but type unrecognized |
| `none` | No authentication available |

---

## Error Codes

| Code | Meaning |
|---|---|
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

---

## Security Notes

> [!IMPORTANT]
> ZBioLock **never** stores tokens in `localStorage`, `sessionStorage`, `SharedPreferences`, or `UserDefaults`. All storage is hardware-backed and encrypted.

| Platform | Storage Mechanism | Encryption | Backup Protected |
|---|---|---|---|
| Android | `EncryptedSharedPreferences` | AES-256-GCM (Android Keystore) | ✅ Yes |
| iOS | Keychain Services | Hardware-backed AES (Secure Enclave) | ✅ `ThisDeviceOnly` |
| Web | ❌ Not available | ❌ | — |

### Security Best Practices

- ✅ Always call `ZBioLock.clear()` on **logout** — this wipes all stored tokens from hardware storage.
- ✅ Tokens are **device-bound** — they cannot be extracted, copied, or migrated to another device.
- ✅ On iOS, `kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly` means tokens are inaccessible before the first device unlock after reboot (cold-boot protection).
- ✅ On Android, the Keystore master key is tied to the **app signing certificate** — repackaged APKs cannot access your tokens.
- ✅ The plugin does **not** transmit any data over the network. Everything stays on-device.
- ⚠️ Do **not** log or print token values in production code — treat them as secrets.
- ⚠️ Do **not** store tokens in React/Vue/Angular component state beyond what is immediately needed.

---

## Troubleshooting

### Android: `BIOMETRIC_NOT_AVAILABLE` — hardware error

**Symptoms:** `isAvailable()` returns `false`, or `authenticate()` throws `BIOMETRIC_NOT_AVAILABLE`.

**Causes & Fixes:**
- The biometric sensor is temporarily unavailable (overheated, covered, reboot needed). Ask the user to retry.
- No biometric hardware is present on the device — expected on low-end devices. Use `allowDeviceCredential: true` as a fallback.
- The device is in a locked-down state (e.g., enterprise MDM policy). No workaround.

---

### Android: `AUTHENTICATION_LOCKED`

**Symptoms:** `authenticate()` throws `AUTHENTICATION_LOCKED`.

**Fix:** The user made too many failed attempts. The prompt is locked for a period (usually 30 seconds for soft lock, or until reboot for permanent lock). Show a user-friendly message and retry after the timeout.

---

### Android: Build fails — `Could not find org.jetbrains.kotlin:kotlin-gradle-plugin`

**Fix:** Add the Kotlin classpath to your **root-level** `build.gradle` (not `android/app/build.gradle`):

```gradle
buildscript {
    dependencies {
        classpath "org.jetbrains.kotlin:kotlin-gradle-plugin:2.0.20"
    }
}
```

---

### Android: `Manifest merger failed`

**Symptoms:**

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

---

### Android: `EncryptedSharedPreferences` crashes after app reinstall / key change

**Symptoms:** `UNKNOWN_ERROR` on `saveToken()` or `getToken()` after reinstalling the app or clearing app data.

**Cause:** The Android Keystore key was invalidated (e.g., new fingerprint enrolled, device reset).

**Fix:** ZBioLock automatically recovers by deleting and recreating the encrypted prefs file. If it still fails, call `ZBioLock.clear()` to wipe corrupted state, then re-save tokens.

---

### iOS: App crashes immediately on Face ID device

**Fix:** Add `NSFaceIDUsageDescription` to `ios/App/App/Info.plist`:

```xml
<key>NSFaceIDUsageDescription</key>
<string>We use Face ID to securely verify your identity.</string>
```

See the full [iOS Setup](#ios-setup) section.

---

### iOS: `biometryNotAvailable` on Simulator

**Fix:** In Xcode Simulator, go to:
- **Face ID**: `Features → Face ID → Enrolled`
- **Touch ID**: `Features → Touch ID → Enrolled`

Then trigger a matching biometric: `Features → Face ID → Matching Face`.

---

### iOS: `AUTHENTICATION_CANCELED` immediately (no prompt shown)

**Cause:** The `LAContext` was invalidated before `evaluatePolicy` ran — usually caused by calling `authenticate()` from a background thread.

**Fix:** This is handled internally by ZBioLock — `evaluatePolicy` always runs on `DispatchQueue.main`. If you are wrapping the call in a custom background task, ensure you `await` it on the main thread in your app code.

---

### Web: `DEVICE_NOT_SUPPORTED` on every call

**Expected behaviour.** The web platform has no biometric hardware or secure storage APIs. ZBioLock returns safe fallbacks:

```typescript
const { isAvailable } = await ZBioLock.isAvailable();
// isAvailable === false on web — no error thrown

await ZBioLock.authenticate(); // throws DEVICE_NOT_SUPPORTED — handle gracefully
```

Always check `isAvailable` before calling `authenticate()` in cross-platform code.

---

## FAQ

**Q: Can I use ZBioLock in a banking or fintech app?**
Yes. Token storage uses Android Keystore (AES-256-GCM) and iOS Secure Enclave / Keychain, satisfying:
- OWASP MASVS L2 (credential storage requirements)
- PCI-DSS requirement 8 (strong authentication)
- FIDO2 / biometric authentication guidelines

---

**Q: Does it support Capacitor 7?**
Yes. The peer dependency is `>=7.0.0`. Both Capacitor 7 and Capacitor 8 are supported.

---

**Q: The user has no biometrics enrolled — what happens?**
- `isAvailable()` returns `{ isAvailable: false, biometricType: 'none' }` when no biometrics AND no device credential are available.
- If you pass `allowDeviceCredential: true`, `isAvailable()` returns `true` as long as the device has a PIN/pattern/password set.
- `isBiometricEnrolled()` always accurately reflects whether a biometric (fingerprint/face/iris) is enrolled — independently of device credential.

---

**Q: Can another app on the device read my stored tokens?**
No. On Android, `EncryptedSharedPreferences` is private to your app's UID and the Keystore key is bound to your app's signing certificate. On iOS, Keychain items are sandboxed to your app's bundle ID by default (no `kSecAttrAccessGroup` shared group is set). Neither platform exposes tokens to other apps.

---

**Q: What happens to tokens when the user adds a new fingerprint?**
On Android, adding a new biometric **invalidates** existing Keystore keys if they were created with `setInvalidatedByBiometricEnrollment(true)`. ZBioLock uses `EncryptedSharedPreferences` with a standard `MasterKey` which is **not** invalidated on new enrollment by default — so existing tokens remain accessible.

On iOS, adding a new Touch ID / Face ID does **not** invalidate existing Keychain items.

---

**Q: Can I use multiple token keys simultaneously?**
Yes. `saveToken` / `getToken` / `deleteToken` all accept a `key` parameter. Use descriptive keys:

```typescript
await ZBioLock.saveToken({ key: 'access_token',  token: accessJWT });
await ZBioLock.saveToken({ key: 'refresh_token', token: refreshJWT });
await ZBioLock.saveToken({ key: 'session_id',    token: sessionId });

// On logout — wipe them all at once:
await ZBioLock.clear();
```

---

**Q: Can I migrate tokens to a new phone?**
No — by design. Tokens are bound to the device's hardware security module. Users must log in again on a new device and re-save their tokens. This is a **security feature**, not a limitation.

---

**Q: Does ZBioLock work with Ionic's `@capacitor-community/biometric-auth`?**
Yes — ZBioLock is a fully independent plugin and does not conflict with other biometric plugins. You can use them side-by-side if needed, though ZBioLock covers all features of that plugin and adds secure token storage.

---

**Q: Is there a React Native version?**
Not at this time. ZBioLock is a Capacitor-native plugin. A React Native port is not planned, but community contributions are welcome.

---

## Changelog

### v0.0.1 — Initial Release

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

---

## Contributing

Contributions are welcome! Please read [CONTRIBUTING.md](CONTRIBUTING.md) before submitting a pull request.

For security vulnerabilities, please use [GitHub Security Advisories](https://github.com/zakirjarir/zbiolock/security/advisories) instead of opening a public issue.

---

## Publishing

```bash
# 1. Bump version in package.json first
# 2. Build the plugin (runs automatically via prepublishOnly)
npm publish --access public
```

See [PUBLISHING.md](PUBLISHING.md) for the full release checklist.

---

## License

MIT © [Zakir Jarir](https://github.com/zakirjarir/zbiolock)

---

<p align="center">
  Built with ❤️ for the Capacitor community.<br/>
  Secure by design. Enterprise by standard.
</p>
