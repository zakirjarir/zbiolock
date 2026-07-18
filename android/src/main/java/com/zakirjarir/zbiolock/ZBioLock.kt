package com.zakirjarir.zbiolock

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import android.app.KeyguardManager

class ZBioLock(private val context: Context) {

    private val securePrefsFile = "zbiolock_secure_prefs"

    private val sharedPreferences by lazy {
        try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            EncryptedSharedPreferences.create(
                context,
                securePrefsFile,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            // Attempt recovery if SharedPreferences/Keystore is corrupted
            try {
                context.deleteSharedPreferences(securePrefsFile)
            } catch (ex: Exception) {
                // Ignore failure to delete
            }
            
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            EncryptedSharedPreferences.create(
                context,
                securePrefsFile,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        }
    }

    fun isAvailable(allowDeviceCredential: Boolean): Boolean {
        val biometricManager = BiometricManager.from(context)
        val authenticators = if (allowDeviceCredential) {
            Authenticators.BIOMETRIC_STRONG or Authenticators.BIOMETRIC_WEAK or Authenticators.DEVICE_CREDENTIAL
        } else {
            Authenticators.BIOMETRIC_STRONG or Authenticators.BIOMETRIC_WEAK
        }
        
        val canAuth = biometricManager.canAuthenticate(authenticators)
        return canAuth == BiometricManager.BIOMETRIC_SUCCESS || 
               canAuth == BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED
    }

    fun isBiometricEnrolled(): Boolean {
        val biometricManager = BiometricManager.from(context)
        val canAuth = biometricManager.canAuthenticate(Authenticators.BIOMETRIC_STRONG or Authenticators.BIOMETRIC_WEAK)
        return canAuth == BiometricManager.BIOMETRIC_SUCCESS
    }

    fun getBiometricType(): String {
        val packageManager = context.packageManager
        val biometricManager = BiometricManager.from(context)

        val canAuthBiometrics = biometricManager.canAuthenticate(Authenticators.BIOMETRIC_STRONG or Authenticators.BIOMETRIC_WEAK)
        if (canAuthBiometrics != BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE && 
            canAuthBiometrics != BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE) {
            
            if (packageManager.hasSystemFeature(PackageManager.FEATURE_FACE)) {
                return "face"
            }
            if (packageManager.hasSystemFeature(PackageManager.FEATURE_FINGERPRINT)) {
                return "fingerprint"
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && packageManager.hasSystemFeature(PackageManager.FEATURE_IRIS)) {
                return "iris"
            }
            
            if (canAuthBiometrics == BiometricManager.BIOMETRIC_SUCCESS || 
                canAuthBiometrics == BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED) {
                return "fingerprint"
            }
        }

        val keyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
        if (keyguardManager?.isDeviceSecure == true) {
            return "device_credential"
        }

        return "none"
    }

    fun saveToken(key: String, token: String) {
        sharedPreferences.edit().putString(key, token).apply()
    }

    fun getToken(key: String): String? {
        return sharedPreferences.getString(key, null)
    }

    fun deleteToken(key: String) {
        sharedPreferences.edit().remove(key).apply()
    }

    fun clear() {
        sharedPreferences.edit().clear().apply()
    }
}
