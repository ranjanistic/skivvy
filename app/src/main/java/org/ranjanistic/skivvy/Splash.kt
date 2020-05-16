package org.ranjanistic.skivvy

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import org.ranjanistic.skivvy.manager.TempDataManager
import java.util.concurrent.Executor

@ExperimentalStdlibApi
class Splash : AppCompatActivity() {

    lateinit var skivvy: Skivvy
    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo
    private lateinit var mainIntent: Intent
    private lateinit var recognizeIntent: Intent
    private var temp: TempDataManager = TempDataManager()
    private lateinit var context: Context

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        skivvy = this.application as Skivvy
        when {
            skivvy.getThemeState() == R.style.LightTheme -> {
                setTheme(R.style.SplashLight)
            }
            skivvy.getThemeState() == R.style.BlackTheme -> {
                setTheme(R.style.SplashBlack)
            }
            skivvy.getThemeState() == R.style.BlueTheme -> {
                setTheme(R.style.SplashBlue)
            }
            else -> {
                setTheme(R.style.Splash)
            }
        }
        context = this
        mainIntent = Intent(context, MainActivity::class.java)
        recognizeIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            .putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            .putExtra(RecognizerIntent.EXTRA_LANGUAGE, skivvy.locale)
        skivvy.tts = TextToSpeech(skivvy, TextToSpeech.OnInitListener {
            if (it == TextToSpeech.SUCCESS) {
                skivvy.tts!!.language = skivvy.locale
            } else
                Toast.makeText(skivvy, getString(R.string.output_error), Toast.LENGTH_SHORT).show()
        })
        if (!skivvy.getBiometricStatus() && !skivvy.getPhraseKeyStatus()) {
            startActivity(mainIntent)
            finish()
            overridePendingTransition(R.anim.fade_on, R.anim.fade_off)
        } else {
            executor = ContextCompat.getMainExecutor(context)
            promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle(getString(R.string.auth_demand_title))
                .setSubtitle(getString(R.string.auth_demand_subtitle))
                .setNegativeButtonText(getString(R.string.other_auth_ops))
                .build()
            initializeBiometric()
        }
        if (skivvy.getPhraseKeyStatus()) {
            startVoiceRecIntent(skivvy.CODE_VOICE_AUTH_CONFIRM, getString(R.string.passphrase_text))
        } else if (skivvy.getBiometricStatus()) {
            biometricPrompt.authenticate(promptInfo)
        }
    }

    private fun initializeBiometric() {
        biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    finish()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    temp.setAuthAttemptCount(temp.getAuthAttemptCount() - 1)
                    if (temp.getAuthAttemptCount() == 0) {
                        Toast.makeText(
                            applicationContext,
                            getString(R.string.auth_failed),
                            Toast.LENGTH_LONG
                        ).show()
                        finish()
                    }
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    speakOut(getString(R.string.welcome))
                    startActivity(mainIntent)
                    finish()
                    overridePendingTransition(R.anim.fade_on, R.anim.fade_off)
                }
            })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && data != null) {
            when (requestCode) {
                skivvy.CODE_VOICE_AUTH_CONFIRM -> {
                    when (data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)!![0].toString()
                        .toLowerCase(skivvy.locale)) {
                        skivvy.getVoiceKeyPhrase() -> {
                            speakOut(getString(R.string.welcome))
                            startActivity(mainIntent)
                            finish()
                            overridePendingTransition(R.anim.fade_on, R.anim.fade_off)
                        }
                        "fingerprint" -> {
                            if (skivvy.getBiometricStatus()) {
                                temp.setAuthAttemptCount(3)
                                biometricPrompt.authenticate(promptInfo)
                            } else {
                                if (temp.getAuthAttemptCount() > 0)
                                    speakOut(
                                        getString(R.string.biometric_is_off),
                                        skivvy.CODE_VOICE_AUTH_CONFIRM
                                    )
                                else
                                    speakOut(getString(R.string.biometric_is_off))
                            }
                        }
                        else -> {
                            temp.setAuthAttemptCount(temp.getAuthAttemptCount() - 1)
                            if (temp.getAuthAttemptCount() > 0) {
                                if (temp.getAuthAttemptCount() == 1) {
                                    speakOut(
                                        getString(R.string.last_chance),
                                        skivvy.CODE_VOICE_AUTH_CONFIRM
                                    )
                                } else {
                                    speakOut(
                                        "${temp.getAuthAttemptCount()}" + getString(R.string.chances_left),
                                        skivvy.CODE_VOICE_AUTH_CONFIRM
                                    )
                                }
                            } else {
                                if (skivvy.getBiometricStatus()) {
                                    speakOut(getString(R.string.vocal_auth_failed) + getString(R.string.need_physical_verification))
                                    temp.setAuthAttemptCount(3)
                                    biometricPrompt.authenticate(promptInfo)
                                } else {
                                    speakOut(getString(R.string.auth_failed))
                                    finish()
                                }
                            }
                        }
                    }
                }
            }
        } else {
            if (skivvy.getBiometricStatus()) {
                temp.setAuthAttemptCount(3)
                biometricPrompt.authenticate(promptInfo)
            } else {
                finish()
            }
        }
    }

    private fun startVoiceRecIntent(code: Int, msg: String) {
        recognizeIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, msg)
        recognizeIntent.resolveActivity(packageManager)
            ?.let { startActivityForResult(recognizeIntent, code) }
    }

    private fun speakOut(
        text: String,
        code: Int? = null,
        parallelReceiver: Boolean = skivvy.getParallelResponseStatus()
    ) {
        skivvy.tts!!.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onDone(utteranceId: String) {
                if (!parallelReceiver)
                    code?.let { startVoiceRecIntent(code, text) }
            }

            override fun onError(utteranceId: String) {}
            override fun onStart(utteranceId: String) {
                if (parallelReceiver)
                    code?.let { startVoiceRecIntent(code, text) }
            }
        })
        if (!skivvy.getMuteStatus()) {
            skivvy.tts!!.speak(
                text,
                TextToSpeech.QUEUE_FLUSH,
                null,
                "$code"
            )
        } else {
            code?.let { startVoiceRecIntent(code, text) }
        }
    }
}

