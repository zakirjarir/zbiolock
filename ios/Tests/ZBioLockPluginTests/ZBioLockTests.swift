import XCTest
@testable import ZBioLockPlugin

/**
 * Unit tests for ZBioLock iOS implementation.
 *
 * Tests validate:
 * - getBiometricType() returns a recognized value
 * - Keychain save / get / delete round-trip
 * - clear() removes all items stored under the service name
 * - Overwriting an existing key
 * - Deleting a non-existent key (must not crash)
 */
class ZBioLockTests: XCTestCase {

    var implementation: ZBioLock!

    override func setUpWithError() throws {
        implementation = ZBioLock()
        // Start each test with a clean Keychain namespace
        _ = implementation.clear()
    }

    override func tearDownWithError() throws {
        _ = implementation.clear()
    }

    // MARK: - Biometric Type

    func testGetBiometricTypeReturnsValidValue() {
        let validTypes: Set<String> = ["fingerprint", "face", "iris", "device_credential", "unknown", "none"]
        let type = implementation.getBiometricType()
        XCTAssertTrue(validTypes.contains(type), "Unexpected biometricType value: \(type)")
    }

    // MARK: - Save / Get

    func testSaveAndGetTokenRoundTrip() {
        let key = "access_token"
        let token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test"

        let saved = implementation.saveToken(key, token: token)
        XCTAssertTrue(saved, "saveToken should return true on success")

        let retrieved = implementation.getToken(key)
        XCTAssertEqual(retrieved, token, "Retrieved token must match saved token")
    }

    func testGetTokenReturnsNilForMissingKey() {
        let result = implementation.getToken("non_existent_key_xyz")
        XCTAssertNil(result, "Expected nil for a key that was never saved")
    }

    // MARK: - Delete

    func testDeleteTokenRemovesEntry() {
        let key = "token_to_delete"
        _ = implementation.saveToken(key, token: "some_value")
        _ = implementation.deleteToken(key)
        let result = implementation.getToken(key)
        XCTAssertNil(result, "Token should be nil after deletion")
    }

    func testDeleteNonExistentKeyDoesNotCrash() {
        // Should not throw or return false in a way that causes issues
        let result = implementation.deleteToken("key_that_does_not_exist")
        XCTAssertTrue(result, "Deleting a non-existent key should succeed (errSecItemNotFound is OK)")
    }

    // MARK: - Overwrite

    func testOverwriteExistingToken() {
        let key = "overwrite_key"
        _ = implementation.saveToken(key, token: "original_value")
        _ = implementation.saveToken(key, token: "updated_value")
        let result = implementation.getToken(key)
        XCTAssertEqual(result, "updated_value", "Token should reflect the most recently saved value")
    }

    // MARK: - Clear

    func testClearRemovesAllTokens() {
        _ = implementation.saveToken("k1", token: "v1")
        _ = implementation.saveToken("k2", token: "v2")
        _ = implementation.saveToken("k3", token: "v3")

        _ = implementation.clear()

        XCTAssertNil(implementation.getToken("k1"))
        XCTAssertNil(implementation.getToken("k2"))
        XCTAssertNil(implementation.getToken("k3"))
    }

    // MARK: - Edge Cases

    func testSaveEmptyTokenString() {
        let key = "empty_token"
        let saved = implementation.saveToken(key, token: "")
        XCTAssertTrue(saved)
        let result = implementation.getToken(key)
        XCTAssertEqual(result, "", "Empty string should be a valid storable value")
    }

    func testSaveLongJWTToken() {
        let key = "long_jwt"
        let jwt = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9." +
                  "eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IlpBS0lSIEpBUklSIiwiaWF0IjoxNTE2MjM5MDIyfQ." +
                  "SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c"
        _ = implementation.saveToken(key, token: jwt)
        let result = implementation.getToken(key)
        XCTAssertEqual(result, jwt)
    }

    func testIsAvailableDoesNotCrash() {
        // Should always return without throwing
        let result = implementation.isAvailable(false)
        XCTAssertNotNil(result)
    }
}
