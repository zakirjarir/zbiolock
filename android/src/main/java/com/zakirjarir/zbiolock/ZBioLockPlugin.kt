package com.zakirjarir.zbiolock

import androidx.biometric.BiometricPrompt
import androidx.biometric.BiometricManager.Authenticators
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.getcapacitor.JSObject
import com.getcapacitor.Plugin
import com.getcapacitor.PluginCall
import com.getcapacitor.PluginMethod
import com.getcapacitor.annotation.CapacitorPlugin

@CapacitorPlugin(name = "ZBioLock")
class ZBioLockPlugin : Plugin() {

    private lateinit var implementation: ZBioLock

    override fun load() {
        super.load()
        implementation = ZBioLock(context)
    }

    @PluginMethod
    fun isAvailable(call: PluginCall) {
        val allowDeviceCredential = call.getBoolean("allowDeviceCredential", false) ?: false
        val available = implementation.isAvailable(allowDeviceCredential)
        val biometricType = implementation.getBiometricType()
        
        val ret = JSObject()
        ret.put("isAvailable", available)
        ret.put("biometricType", biometricType)
        call.resolve(ret)
    }

    @PluginMethod
    fun getBiometricType(call: PluginCall) {
        val biometricType = implementation.getBiometricType()
        val ret = JSObject()
        ret.put("biometricType", biometricType)
        call.resolve(ret)
    }

    @PluginMethod
    fun isBiometricEnrolled(call: PluginCall) {
        val enrolled = implementation.isBiometricEnrolled()
        val ret = JSObject()
        ret.put("enrolled", enrolled)
        call.resolve(ret)
    }

    @PluginMethod
    fun saveToken(call: PluginCall) {
        val key = call.getString("key")
        val token = call.getString("token")

        if (key == null) {
            call.reject("Key must be provided", "INVALID_ARGUMENT")
            return
        }
        if (token == null) {
            call.reject("Token must be provided", "INVALID_ARGUMENT")
            return
        }

        try {
            implementation.saveToken(key, token)
            call.resolve()
        } catch (e: Exception) {
            call.reject(e.localizedMessage, "UNKNOWN_ERROR", e)
        }
    }

    @PluginMethod
    fun getToken(call: PluginCall) {
        val key = call.getString("key")

        if (key == null) {
            call.reject("Key must be provided", "INVALID_ARGUMENT")
            return
        }

        try {
            val token = implementation.getToken(key)
            val ret = JSObject()
            ret.put("token", token)
            call.resolve(ret)
        } catch (e: Exception) {
            call.reject(e.localizedMessage, "UNKNOWN_ERROR", e)
        }
    }

    @PluginMethod
    fun deleteToken(call: PluginCall) {
        val key = call.getString("key")

        if (key == null) {
            call.reject("Key must be provided", "INVALID_ARGUMENT")
            return
        }

        try {
            implementation.deleteToken(key)
            call.resolve()
        } catch (e: Exception) {
            call.reject(e.localizedMessage, "UNKNOWN_ERROR", e)
        }
    }

    @PluginMethod
    fun clear(call: PluginCall) {
        try {
            implementation.clear()
            call.resolve()
        } catch (e: Exception) {
            call.reject(e.localizedMessage, "UNKNOWN_ERROR", e)
        }
    }

    @PluginMethod
    fun authenticate(call: PluginCall) {
        val title = call.getString("title") ?: "Biometric Authentication"
        val subtitle = call.getString("subtitle")
        val description = call.getString("description")
        val allowDeviceCredential = call.getBoolean("allowDeviceCredential", false) ?: false

        val biometricType = implementation.getBiometricType()

        // Check availability first
        if (!implementation.isAvailable(allowDeviceCredential)) {
            call.reject("Biometrics or device passcode not available", "BIOMETRIC_NOT_AVAILABLE")
            return
        }

        val activity = activity as? FragmentActivity
        if (activity == null) {
            call.reject("Activity is not a FragmentActivity", "DEVICE_NOT_SUPPORTED")
            return
        }

        // Run UI prompt on UI Thread
        activity.runOnUiThread {
            try {
                val executor = ContextCompat.getMainExecutor(activity)
                
                val promptInfoBuilder = BiometricPrompt.PromptInfo.Builder()
                    .setTitle(title)

                if (subtitle != null) {
                    promptInfoBuilder.setSubtitle(subtitle)
                }
                if (description != null) {
                    promptInfoBuilder.setDescription(description)
                }

                if (allowDeviceCredential && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                    promptInfoBuilder.setAllowedAuthenticators(
                        Authenticators.BIOMETRIC_STRONG or Authenticators.BIOMETRIC_WEAK or Authenticators.DEVICE_CREDENTIAL
                    )
                } else if (allowDeviceCredential && android.os.Build.VERSION.SDK_INT == android.os.Build.VERSION_CODES.Q) {
                    val isBiometricEnrolled = implementation.isBiometricEnrolled()
                    if (isBiometricEnrolled) {
                        promptInfoBuilder.setAllowedAuthenticators(
                            Authenticators.BIOMETRIC_STRONG or Authenticators.BIOMETRIC_WEAK
                        )
                        promptInfoBuilder.setNegativeButtonText(call.getString("cancelText") ?: "Cancel")
                    } else {
                        promptInfoBuilder.setAllowedAuthenticators(Authenticators.DEVICE_CREDENTIAL)
                    }
                } else {
                    promptInfoBuilder.setAllowedAuthenticators(
                        Authenticators.BIOMETRIC_STRONG or Authenticators.BIOMETRIC_WEAK
                    )
                    promptInfoBuilder.setNegativeButtonText(call.getString("cancelText") ?: "Cancel")
                }

                val promptInfo = promptInfoBuilder.build()

                val biometricPrompt = BiometricPrompt(activity, executor, object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        super.onAuthenticationError(errorCode, errString)
                        val code = mapAndroidError(errorCode)
                        call.reject(errString.toString(), code)
                    }

                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        super.onAuthenticationSucceeded(result)
                        val ret = JSObject()
                        ret.put("success", true)
                        ret.put("biometricType", biometricType)
                        call.resolve(ret)
                    }

                    override fun onAuthenticationFailed() {
                        super.onAuthenticationFailed()
                        // This callback only notifies that fingerprint was scanned but failed verification.
                        // The prompt UI remains visible. We do not reject yet.
                    }
                })

                biometricPrompt.authenticate(promptInfo)
            } catch (e: Exception) {
                call.reject(e.localizedMessage, "UNKNOWN_ERROR", e)
            }
        }
    }

    private fun mapAndroidError(errorCode: Int): String {
        return when (errorCode) {
            BiometricPrompt.ERROR_USER_CANCELED,
            BiometricPrompt.ERROR_NEGATIVE_BUTTON -> "AUTHENTICATION_CANCELED"
            
            BiometricPrompt.ERROR_LOCKOUT,
            BiometricPrompt.ERROR_LOCKOUT_PERMANENT -> "AUTHENTICATION_LOCKED"
            
            BiometricPrompt.ERROR_HW_NOT_PRESENT,
            BiometricPrompt.ERROR_HW_UNAVAILABLE -> "BIOMETRIC_NOT_AVAILABLE"
            
            BiometricPrompt.ERROR_NO_BIOMETRICS -> "BIOMETRIC_NOT_ENROLLED"
            
            BiometricPrompt.ERROR_NO_DEVICE_CREDENTIAL -> "BIOMETRIC_NOT_AVAILABLE"
            
            BiometricPrompt.ERROR_TIMEOUT -> "AUTHENTICATION_FAILED"
            
            else -> "UNKNOWN_ERROR"
        }
    }
}
