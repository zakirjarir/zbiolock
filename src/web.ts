/**
 * ZBioLock - Web (Browser) fallback implementation.
 *
 * The Web platform does not support biometric hardware or hardware-backed
 * secure storage. This implementation returns safe, meaningful responses
 * rather than throwing unhandled errors or crashing.
 *
 * @author Zakir Jarir
 * @license MIT
 */

import { WebPlugin } from '@capacitor/core';
import type {
  ZBioLockPlugin,
  IsAvailableResult,
  AuthenticateOptions,
  AuthenticateResult,
  SaveTokenOptions,
  GetTokenOptions,
  GetTokenResult,
  DeleteTokenOptions,
  GetBiometricTypeResult,
  IsBiometricEnrolledResult,
} from './definitions';

export class ZBioLockWeb extends WebPlugin implements ZBioLockPlugin {
  /**
   * On the web, biometrics are never available.
   * Returns isAvailable: false without throwing.
   */
  async isAvailable(_options?: { allowDeviceCredential?: boolean }): Promise<IsAvailableResult> {
    return { isAvailable: false, biometricType: 'none' };
  }

  /**
   * Authentication is not possible on the web platform.
   * Throws a meaningful DEVICE_NOT_SUPPORTED error.
   */
  async authenticate(_options?: AuthenticateOptions): Promise<AuthenticateResult> {
    throw this.unavailable(
      'Biometric authentication is not available on the web platform.',
    );
  }

  /**
   * Secure hardware-backed storage is not available on the web.
   * Throws DEVICE_NOT_SUPPORTED to prevent accidental insecure storage.
   */
  async saveToken(_options: SaveTokenOptions): Promise<void> {
    throw this.unavailable(
      'Secure token storage is not available on the web platform. ' +
        'Never store tokens in localStorage or sessionStorage.',
    );
  }

  /**
   * Secure storage retrieval not available on the web.
   */
  async getToken(_options: GetTokenOptions): Promise<GetTokenResult> {
    throw this.unavailable(
      'Secure token retrieval is not available on the web platform.',
    );
  }

  /**
   * Secure storage deletion not available on the web.
   */
  async deleteToken(_options: DeleteTokenOptions): Promise<void> {
    throw this.unavailable(
      'Secure token deletion is not available on the web platform.',
    );
  }

  /**
   * Secure storage clear not available on the web.
   */
  async clear(): Promise<void> {
    throw this.unavailable(
      'Secure storage clear is not available on the web platform.',
    );
  }

  /**
   * Always returns 'none' on the web.
   */
  async getBiometricType(): Promise<GetBiometricTypeResult> {
    return { biometricType: 'none' };
  }

  /**
   * Always returns enrolled: false on the web.
   */
  async isBiometricEnrolled(): Promise<IsBiometricEnrolledResult> {
    return { enrolled: false };
  }
}
