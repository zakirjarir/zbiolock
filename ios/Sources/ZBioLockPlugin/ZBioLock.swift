import Foundation
import LocalAuthentication
import Security

@objc public class ZBioLock: NSObject {
    private let service = "com.zakirjarir.zbiolock"

    @objc public func isAvailable(_ allowDeviceCredential: Bool) -> Bool {
        let context = LAContext()
        var error: NSError?
        
        let policy: LAPolicy = allowDeviceCredential ? .deviceOwnerAuthentication : .deviceOwnerAuthenticationWithBiometrics
        let result = context.canEvaluatePolicy(policy, error: &error)
        
        if result {
            return true
        }
        
        if let err = error as? LAError {
            if err.code == .biometryNotEnrolled {
                return true
            }
        }
        
        return false
    }

    @objc public func isBiometricEnrolled() -> Bool {
        let context = LAContext()
        var error: NSError?
        return context.canEvaluatePolicy(.deviceOwnerAuthenticationWithBiometrics, error: &error)
    }

    @objc public func getBiometricType() -> String {
        let context = LAContext()
        var error: NSError?
        
        _ = context.canEvaluatePolicy(.deviceOwnerAuthenticationWithBiometrics, error: &error)
        
        if #available(iOS 11.0, *) {
            switch context.biometryType {
            case .none:
                let hasPasscode = context.canEvaluatePolicy(.deviceOwnerAuthentication, error: &error)
                return hasPasscode ? "device_credential" : "none"
            case .touchID:
                return "fingerprint"
            case .faceID:
                return "face"
            case .opticID:
                return "iris"
            @unknown default:
                return "unknown"
            }
        } else {
            let hasBiometrics = context.canEvaluatePolicy(.deviceOwnerAuthenticationWithBiometrics, error: &error)
            if hasBiometrics {
                return "fingerprint"
            }
            let hasPasscode = context.canEvaluatePolicy(.deviceOwnerAuthentication, error: &error)
            return hasPasscode ? "device_credential" : "none"
        }
    }

    @objc public func saveToken(_ key: String, token: String) -> Bool {
        guard let data = token.data(using: .utf8) else { return false }
        
        let query: [String: Any] = [
            kSecClass as String: kSecClassGenericPassword,
            kSecAttrService as String: service,
            kSecAttrAccount as String: key,
            kSecValueData as String: data,
            kSecAttrAccessible as String: kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly
        ]
        
        let deleteQuery: [String: Any] = [
            kSecClass as String: kSecClassGenericPassword,
            kSecAttrService as String: service,
            kSecAttrAccount as String: key
        ]
        SecItemDelete(deleteQuery as CFDictionary)
        
        let status = SecItemAdd(query as CFDictionary, nil)
        return status == errSecSuccess
    }

    @objc public func getToken(_ key: String) -> String? {
        let query: [String: Any] = [
            kSecClass as String: kSecClassGenericPassword,
            kSecAttrService as String: service,
            kSecAttrAccount as String: key,
            kSecReturnData as String: true,
            kSecMatchLimit as String: kSecMatchLimitOne
        ]
        
        var dataTypeRef: AnyObject?
        let status = SecItemCopyMatching(query as CFDictionary, &dataTypeRef)
        
        if status == errSecSuccess, let data = dataTypeRef as? Data, let token = String(data: data, encoding: .utf8) {
            return token
        }
        return nil
    }

    @objc public func deleteToken(_ key: String) -> Bool {
        let query: [String: Any] = [
            kSecClass as String: kSecClassGenericPassword,
            kSecAttrService as String: service,
            kSecAttrAccount as String: key
        ]
        
        let status = SecItemDelete(query as CFDictionary)
        return status == errSecSuccess || status == errSecItemNotFound
    }

    @objc public func clear() -> Bool {
        let query: [String: Any] = [
            kSecClass as String: kSecClassGenericPassword,
            kSecAttrService as String: service
        ]
        
        let status = SecItemDelete(query as CFDictionary)
        return status == errSecSuccess || status == errSecItemNotFound
    }
}
