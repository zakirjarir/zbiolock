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
import type { ZBioLockPlugin, IsAvailableResult, AuthenticateOptions, AuthenticateResult, SaveTokenOptions, GetTokenOptions, GetTokenResult, DeleteTokenOptions, GetBiometricTypeResult, IsBiometricEnrolledResult } from './definitions';
export declare class ZBioLockWeb extends WebPlugin implements ZBioLockPlugin {
    /**
     * On the web, biometrics are never available.
     * Returns isAvailable: false without throwing.
     */
    isAvailable(): Promise<IsAvailableResult>;
    /**
     * Authentication is not possible on the web platform.
     * Throws a meaningful DEVICE_NOT_SUPPORTED error.
     */
    authenticate(_options?: AuthenticateOptions): Promise<AuthenticateResult>;
    /**
     * Secure hardware-backed storage is not available on the web.
     * Throws DEVICE_NOT_SUPPORTED to prevent accidental insecure storage.
     */
    saveToken(_options: SaveTokenOptions): Promise<void>;
    /**
     * Secure storage retrieval not available on the web.
     */
    getToken(_options: GetTokenOptions): Promise<GetTokenResult>;
    /**
     * Secure storage deletion not available on the web.
     */
    deleteToken(_options: DeleteTokenOptions): Promise<void>;
    /**
     * Secure storage clear not available on the web.
     */
    clear(): Promise<void>;
    /**
     * Always returns 'none' on the web.
     */
    getBiometricType(): Promise<GetBiometricTypeResult>;
    /**
     * Always returns enrolled: false on the web.
     */
    isBiometricEnrolled(): Promise<IsBiometricEnrolledResult>;
}
