package com.zakirjarir.zbiolock

import android.content.Context
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Unit tests for ZBioLock core Kotlin implementation.
 *
 * These tests run on the JVM (Robolectric or host) and validate:
 * - getBiometricType returns a known valid string
 * - Token CRUD operations using EncryptedSharedPreferences
 * - clear() wipes all tokens
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.R], manifest = Config.NONE)
class ZBioLockTest {

    private lateinit var context: Context
    private lateinit var zbiolock: ZBioLock

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        val plainPrefs = context.getSharedPreferences("test_secure_prefs", Context.MODE_PRIVATE)
        zbiolock = ZBioLock(context, plainPrefs)
        // Start with clean slate
        zbiolock.clear()
    }

    @Test
    fun `getBiometricType returns valid type`() {
        val validTypes = setOf("fingerprint", "face", "iris", "device_credential", "unknown", "none")
        val type = zbiolock.getBiometricType()
        assertTrue("Unknown biometricType value: $type", validTypes.contains(type))
    }

    @Test
    fun `saveToken and getToken round-trip`() {
        val key = "test_access_token"
        val token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test"

        zbiolock.saveToken(key, token)
        val retrieved = zbiolock.getToken(key)

        assertEquals("Retrieved token must match saved token", token, retrieved)
    }

    @Test
    fun `getToken returns null for missing key`() {
        val result = zbiolock.getToken("non_existent_key_xyz")
        assertNull("Expected null for missing key", result)
    }

    @Test
    fun `deleteToken removes a stored token`() {
        val key = "token_to_delete"
        zbiolock.saveToken(key, "some_value")
        zbiolock.deleteToken(key)
        val result = zbiolock.getToken(key)
        assertNull("Token should be null after deletion", result)
    }

    @Test
    fun `clear removes all tokens`() {
        zbiolock.saveToken("key1", "value1")
        zbiolock.saveToken("key2", "value2")
        zbiolock.saveToken("key3", "value3")

        zbiolock.clear()

        assertNull(zbiolock.getToken("key1"))
        assertNull(zbiolock.getToken("key2"))
        assertNull(zbiolock.getToken("key3"))
    }

    @Test
    fun `overwrite token with same key`() {
        val key = "overwrite_key"
        zbiolock.saveToken(key, "original_token")
        zbiolock.saveToken(key, "updated_token")

        val result = zbiolock.getToken(key)
        assertEquals("Token should be updated to latest value", "updated_token", result)
    }

    @Test
    fun `save empty string token`() {
        val key = "empty_token_key"
        zbiolock.saveToken(key, "")
        val result = zbiolock.getToken(key)
        // Empty string is a valid stored value
        assertEquals("", result)
    }

    @Test
    fun `save and retrieve long JWT token`() {
        val key = "long_jwt"
        val jwt = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9." +
                "eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IlpBS0lSIEpBUklSIiwiaWF0IjoxNTE2MjM5MDIyfQ." +
                "SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c"
        zbiolock.saveToken(key, jwt)
        val retrieved = zbiolock.getToken(key)
        assertEquals(jwt, retrieved)
    }
}
