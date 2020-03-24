package org.ranjanistic.skivvy

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import java.util.concurrent.Executor

class Splash : AppCompatActivity() {
    lateinit var skivvy: Skivvy
    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        skivvy = this.application as Skivvy
        val intent= Intent(this, MainActivity::class.java)
        if(getBiometricStatus()) {
            executor = ContextCompat.getMainExecutor(this)
            biometricPrompt = BiometricPrompt(this, executor,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        super.onAuthenticationError(errorCode, errString)
                        finish()
                    }
                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        super.onAuthenticationSucceeded(result)
                        startActivity(intent)
                        finish()
                    }
                })
            promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle(getString(R.string.auth_demand_title))
                .setSubtitle(getString(R.string.auth_demand_subtitle))
                .setNegativeButtonText(getString(R.string.other_auth_ops))
                .build()
            biometricPrompt.authenticate(promptInfo)
        } else{
            startActivity(intent)
            finish()
        }
    }
    private fun getBiometricStatus():Boolean{
        return getSharedPreferences(skivvy.PREF_HEAD_SECURITY, MODE_PRIVATE)
            .getBoolean(skivvy.PREF_KEY_BIOMETRIC, false)
    }
}
