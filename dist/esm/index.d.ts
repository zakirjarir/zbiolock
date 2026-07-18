/**
 * ZBioLock - Plugin entry point.
 *
 * Registers the plugin with Capacitor and re-exports the public API surface
 * so consumers can import everything from 'zbiolock'.
 *
 * @author Zakir Jarir
 * @license MIT
 */
import type { ZBioLockPlugin } from './definitions';
export type { ZBioLockPlugin, BiometricType, ZBioLockErrorCode, AuthenticateOptions, AuthenticateResult, SaveTokenOptions, GetTokenOptions, GetTokenResult, DeleteTokenOptions, GetBiometricTypeResult, IsBiometricEnrolledResult, IsAvailableResult, } from './definitions';
declare const ZBioLock: ZBioLockPlugin;
export { ZBioLock };
