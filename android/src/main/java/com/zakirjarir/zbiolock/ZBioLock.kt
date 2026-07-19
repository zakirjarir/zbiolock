package com.zakirjarir.zbiolock

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import android.app.KeyguardManager

class ZBioLock @JvmOverloads constructor(
    private val context: Context,
    private val prefs: android.content.SharedPreferences? = null
) {

    private val securePrefsFile = "zbiolock_secure_prefs"

    private val sharedPreferences by lazy {
        if (prefs != null) {
            prefs
        } else {
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
    }

    fun isAvailable(allowDeviceCredential: Boolean): Boolean {
        val biometricManager = BiometricManager.from(context)
        
        if (allowDeviceCredential && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11+ supports strong/weak biometrics + device credential
            val authenticators = Authenticators.BIOMETRIC_STRONG or Authenticators.BIOMETRIC_WEAK or Authenticators.DEVICE_CREDENTIAL
            val canAuth = biometricManager.canAuthenticate(authenticators)
            return canAuth == BiometricManager.BIOMETRIC_SUCCESS || 
                   canAuth == BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED
        } else if (allowDeviceCredential && Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) {
            // Android 10 (API 29): Query biometrics and check Keyguard separately to avoid combination crashes
            val canBio = biometricManager.canAuthenticate(Authenticators.BIOMETRIC_STRONG or Authenticators.BIOMETRIC_WEAK)
            val keyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
            val isSecure = keyguardManager?.isDeviceSecure == true
            return canBio == BiometricManager.BIOMETRIC_SUCCESS || 
                   canBio == BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED || 
                   isSecure
        } else {
            // Android 9 and below: DEVICE_CREDENTIAL is not supported by BiometricManager/BiometricPrompt.
            // Check only biometric hardware.
            val authenticators = Authenticators.BIOMETRIC_STRONG or Authenticators.BIOMETRIC_WEAK
            val canAuth = biometricManager.canAuthenticate(authenticators)
            return canAuth == BiometricManager.BIOMETRIC_SUCCESS || 
                   canAuth == BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED
        }
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
