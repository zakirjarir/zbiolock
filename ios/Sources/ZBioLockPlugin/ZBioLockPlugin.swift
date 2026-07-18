import Foundation
import Capacitor
import LocalAuthentication

/**
 * ZBioLockPlugin: Capacitor plugin wrapper for ZBioLock.
 *
 * Bridges JavaScript calls from Capacitor to the native ZBioLock Swift implementation.
 * Handles biometric authentication via LocalAuthentication and secure token storage via Keychain.
 */
@objc(ZBioLockPlugin)
public class ZBioLockPlugin: CAPPlugin, CAPBridgedPlugin {
    public let identifier = "ZBioLockPlugin"
    public let jsName = "ZBioLock"
    public let pluginMethods: [CAPPluginMethod] = [
        CAPPluginMethod(name: "isAvailable",       returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "authenticate",      returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "saveToken",         returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "getToken",          returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "deleteToken",       returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "clear",             returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "getBiometricType",  returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "isBiometricEnrolled", returnType: CAPPluginReturnPromise),
    ]

    private let implementation = ZBioLock()

    // MARK: - isAvailable

    @objc func isAvailable(_ call: CAPPluginCall) {
        let allowDeviceCredential = call.getBool("allowDeviceCredential") ?? false
        let available = implementation.isAvailable(allowDeviceCredential)
        let biometricType = implementation.getBiometricType()
        call.resolve([
            "isAvailable": available,
            "biometricType": biometricType
        ])
    }

    // MARK: - getBiometricType

    @objc func getBiometricType(_ call: CAPPluginCall) {
        let biometricType = implementation.getBiometricType()
        call.resolve(["biometricType": biometricType])
    }

    // MARK: - isBiometricEnrolled

    @objc func isBiometricEnrolled(_ call: CAPPluginCall) {
        let enrolled = implementation.isBiometricEnrolled()
        call.resolve(["enrolled": enrolled])
    }

    // MARK: - authenticate

    @objc func authenticate(_ call: CAPPluginCall) {
        let title             = call.getString("title")       ?? "Biometric Authentication"
        let subtitle          = call.getString("subtitle")
        let description       = call.getString("description")
        let allowDeviceCredential = call.getBool("allowDeviceCredential") ?? false
        let biometricType     = implementation.getBiometricType()

        guard implementation.isAvailable(allowDeviceCredential) else {
            call.reject("Biometric authentication is not available on this device.", "BIOMETRIC_NOT_AVAILABLE")
            return
        }

        let context = LAContext()
        context.localizedCancelTitle = call.getString("cancelText") ?? "Cancel"
        if let subtitle = subtitle {
            context.localizedFallbackTitle = subtitle
        }

        let policy: LAPolicy = allowDeviceCredential
            ? .deviceOwnerAuthentication
            : .deviceOwnerAuthenticationWithBiometrics

        let reason = description ?? subtitle ?? title

        DispatchQueue.main.async {
            context.evaluatePolicy(policy, localizedReason: reason) { [weak self] success, error in
                guard let self = self else { return }

                if success {
                    call.resolve([
                        "success": true,
                        "biometricType": biometricType
                    ])
                } else if let error = error as? LAError {
                    let code = self.mapLAError(error)
                    call.reject(error.localizedDescription, code)
                } else {
                    call.reject("Authentication failed.", "AUTHENTICATION_FAILED")
                }
            }
        }
    }

    // MARK: - Token Storage

    @objc func saveToken(_ call: CAPPluginCall) {
        guard let key = call.getString("key"), !key.isEmpty else {
            call.reject("Key must be provided.", "INVALID_ARGUMENT"); return
        }
        guard let token = call.getString("token") else {
            call.reject("Token must be provided.", "INVALID_ARGUMENT"); return
        }

        let success = implementation.saveToken(key, token: token)
        if success {
            call.resolve()
        } else {
            call.reject("Failed to save token to Keychain.", "UNKNOWN_ERROR")
        }
    }

    @objc func getToken(_ call: CAPPluginCall) {
        guard let key = call.getString("key"), !key.isEmpty else {
            call.reject("Key must be provided.", "INVALID_ARGUMENT"); return
        }

        let token = implementation.getToken(key)
        call.resolve(["token": token as Any])
    }

    @objc func deleteToken(_ call: CAPPluginCall) {
        guard let key = call.getString("key"), !key.isEmpty else {
            call.reject("Key must be provided.", "INVALID_ARGUMENT"); return
        }

        _ = implementation.deleteToken(key)
        call.resolve()
    }

    @objc func clear(_ call: CAPPluginCall) {
        _ = implementation.clear()
        call.resolve()
    }

    // MARK: - Error Mapping

    private func mapLAError(_ error: LAError) -> String {
        switch error.code {
        case .userCancel, .appCancel, .systemCancel:
            return "AUTHENTICATION_CANCELED"
        case .authenticationFailed:
            return "AUTHENTICATION_FAILED"
        case .biometryLockout:
            return "AUTHENTICATION_LOCKED"
        case .biometryNotAvailable:
            return "BIOMETRIC_NOT_AVAILABLE"
        case .biometryNotEnrolled:
            return "BIOMETRIC_NOT_ENROLLED"
        case .passcodeNotSet:
            return "DEVICE_NOT_SUPPORTED"
        case .userFallback:
            return "AUTHENTICATION_CANCELED"
        case .notInteractive:
            return "UNKNOWN_ERROR"
        case .invalidContext:
            return "UNKNOWN_ERROR"
        default:
            return "UNKNOWN_ERROR"
        }
    }
}
