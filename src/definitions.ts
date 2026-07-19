/**
 * ZBioLock - Capacitor Biometric Authentication Plugin
 *
 * TypeScript type definitions, interfaces, and enums.
 * Strict, no-any, production-ready.
 *
 * @author Zakir Jarir
 * @license MIT
 */

// ---------------------------------------------------------------------------
// Biometric Type
// ---------------------------------------------------------------------------

/** Standardized biometric hardware type identifier. */
export type BiometricType = 'fingerprint' | 'face' | 'iris' | 'device_credential' | 'unknown' | 'none';

// ---------------------------------------------------------------------------
// Error Codes
// ---------------------------------------------------------------------------

/** All possible error codes returned by the plugin. */
export type ZBioLockErrorCode =
  | 'BIOMETRIC_NOT_AVAILABLE'
  | 'BIOMETRIC_NOT_ENROLLED'
  | 'AUTHENTICATION_FAILED'
  | 'AUTHENTICATION_CANCELED'
  | 'AUTHENTICATION_LOCKED'
  | 'DEVICE_NOT_SUPPORTED'
  | 'TOKEN_NOT_FOUND'
  | 'INVALID_ARGUMENT'
  | 'UNKNOWN_ERROR';

// ---------------------------------------------------------------------------
// Method Options
// ---------------------------------------------------------------------------

/** Options for the authenticate() call. */
export interface AuthenticateOptions {
  /** Dialog title shown to the user. Defaults to "Biometric Authentication". */
  title?: string;
  /** Optional subtitle shown beneath the title. */
  subtitle?: string;
  /** Optional descriptive text shown in the dialog body. */
  description?: string;
  /** Allow device PIN / pattern / password as fallback. Defaults to false. */
  allowDeviceCredential?: boolean;
  /** Text shown on the cancel / negative button (Android only, ignored when allowDeviceCredential is true). */
  cancelText?: string;
}

/** Options for the saveToken() call. */
export interface SaveTokenOptions {
  /** Unique key to store the token under. */
  key: string;
  /** The token value to store securely. */
  token: string;
}

/** Options for the getToken() call. */
export interface GetTokenOptions {
  /** Key used when the token was saved. */
  key: string;
}

/** Options for the deleteToken() call. */
export interface DeleteTokenOptions {
  /** Key of the token to remove. */
  key: string;
}

// ---------------------------------------------------------------------------
// Method Results
// ---------------------------------------------------------------------------

/** Result of isAvailable(). */
export interface IsAvailableResult {
  /** Whether biometric or device credential authentication is available. */
  isAvailable: boolean;
  /** The primary biometric type available on this device. */
  biometricType: BiometricType;
}

/** Result of authenticate(). */
export interface AuthenticateResult {
  /** Whether authentication succeeded. */
  success: boolean;
  /** The biometric type that was used. */
  biometricType: BiometricType;
}

/** Result of getToken(). */
export interface GetTokenResult {
  /** The stored token, or null if not found. */
  token: string | null;
}

/** Result of getBiometricType(). */
export interface GetBiometricTypeResult {
  /** The primary biometric type available on this device. */
  biometricType: BiometricType;
}

/** Result of isBiometricEnrolled(). */
export interface IsBiometricEnrolledResult {
  /** Whether the user has enrolled at least one biometric credential. */
  enrolled: boolean;
}

// ---------------------------------------------------------------------------
// Plugin Interface
// ---------------------------------------------------------------------------

/**
 * ZBioLockPlugin defines the public API surface of the ZBioLock Capacitor plugin.
 *
 * All methods return Promises and use hardware-backed secure storage.
 * On Web, safe fallback behaviour is provided (no crashes, meaningful errors).
 */
export interface ZBioLockPlugin {
  /**
   * Check whether biometric (or device credential) authentication is supported
   * and available on this device.
   */
  isAvailable(options?: { allowDeviceCredential?: boolean }): Promise<IsAvailableResult>;

  /**
   * Prompt the user to authenticate using their enrolled biometric or
   * device credential (if allowDeviceCredential is true).
   *
   * @throws {ZBioLockErrorCode} BIOMETRIC_NOT_AVAILABLE, BIOMETRIC_NOT_ENROLLED,
   *   AUTHENTICATION_CANCELED, AUTHENTICATION_LOCKED, AUTHENTICATION_FAILED, UNKNOWN_ERROR
   */
  authenticate(options?: AuthenticateOptions): Promise<AuthenticateResult>;

  /**
   * Securely store a token string under the given key.
   * Uses Android Keystore + EncryptedSharedPreferences / iOS Keychain.
   *
   * @throws {ZBioLockErrorCode} INVALID_ARGUMENT, UNKNOWN_ERROR
   */
  saveToken(options: SaveTokenOptions): Promise<void>;

  /**
   * Retrieve a previously stored token by key.
   * Returns null in the token field if the key was not found.
   *
   * @throws {ZBioLockErrorCode} INVALID_ARGUMENT, UNKNOWN_ERROR
   */
  getToken(options: GetTokenOptions): Promise<GetTokenResult>;

  /**
   * Delete a specific stored token by key.
   *
   * @throws {ZBioLockErrorCode} INVALID_ARGUMENT, UNKNOWN_ERROR
   */
  deleteToken(options: DeleteTokenOptions): Promise<void>;

  /**
   * Delete ALL tokens stored by this plugin.
   * Call on logout to perform secure cleanup.
   *
   * @throws {ZBioLockErrorCode} UNKNOWN_ERROR
   */
  clear(): Promise<void>;

  /**
   * Return the primary biometric type available on this device.
   */
  getBiometricType(): Promise<GetBiometricTypeResult>;

  /**
   * Return whether the user has enrolled at least one biometric credential.
   */
  isBiometricEnrolled(): Promise<IsBiometricEnrolledResult>;
}
