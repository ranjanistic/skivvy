package org.ranjanistic.skivvy

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import java.util.concurrent.Executor

@ExperimentalStdlibApi
class Splash : AppCompatActivity() {
    lateinit var skivvy: Skivvy
    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo
    private lateinit var mainIntent:Intent
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        skivvy = this.application as Skivvy
        mainIntent = Intent(this, MainActivity::class.java)
        initializeBiometric()
        if(!skivvy.getBiometricStatus()&&!skivvy.getPhraseKeyStatus()) {
            startActivity(mainIntent)
            finish()
        }
        if(skivvy.getPhraseKeyStatus()){
            startVoiceRecIntent(skivvy.CODE_VOICE_AUTH_CONFIRM,"Passphrase")
        } else if(skivvy.getBiometricStatus()){
            biometricPrompt.authenticate(promptInfo)
        }
    }
    private fun initializeBiometric(){
        executor = ContextCompat.getMainExecutor(this)
        biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    finish()
                }
                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    --authAttemptCount
                    if(authAttemptCount==0){
                        finish()
                    }
                }
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    startActivity(mainIntent)
                    finish()
                }
            })
        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(getString(R.string.auth_demand_title))
            .setSubtitle(getString(R.string.auth_demand_subtitle))
            .setNegativeButtonText(getString(R.string.other_auth_ops))
            .build()

    }
    var authAttemptCount = 3
    @Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK && data!=null){
            val text = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)[0].toString()
                .toLowerCase(skivvy.locale)
            when(requestCode){
                skivvy.CODE_VOICE_AUTH_CONFIRM-> {
                    if(text == "fingerprint"){
                        if(skivvy.getBiometricStatus()) {
                            biometricPrompt.authenticate(promptInfo)
                        } else {
                            if(authAttemptCount>0) {
                                speakOut(
                                    getString(R.string.biometric_is_off),
                                    skivvy.CODE_VOICE_AUTH_CONFIRM
                                )
                            } else {
                                speakOut(
                                    getString(R.string.biometric_is_off)
                                )
                            }
                        }
                    }
                    if (text == skivvy.getVoiceKeyPhrase()) {
                        startActivity(mainIntent)
                        finish()
                    } else {
                        --authAttemptCount
                        if (authAttemptCount > 0) {
                            if(authAttemptCount == 1){
                                speakOut(
                                    "$authAttemptCount chance left",
                                    skivvy.CODE_VOICE_AUTH_CONFIRM
                                )
                            } else {
                                speakOut(
                                    "$authAttemptCount chances left",
                                    skivvy.CODE_VOICE_AUTH_CONFIRM
                                )
                            }
                        } else {
                            if(skivvy.getBiometricStatus()) {
                                authAttemptCount = 3
                                biometricPrompt.authenticate(promptInfo)
                                speakOut("Vocal authentication failed. I need your physical authentication.")
                            } else {
                                speakOut("Vocal authentication failed")
                                finish()
                            }
                        }
                    }
                }
            }
        } else {
            if(skivvy.getBiometricStatus()) {
                authAttemptCount=3
                biometricPrompt.authenticate(promptInfo)
            } else {
                finish()
            }
        }
    }
    private fun startVoiceRecIntent(code: Int, msg:String) {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            .putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            .putExtra(RecognizerIntent.EXTRA_LANGUAGE, skivvy.locale)
            .putExtra(RecognizerIntent.EXTRA_PROMPT, msg)
        if (intent.resolveActivity(packageManager) != null)
            startActivityForResult(intent, code)
    }

    private fun speakOut(text: String, code: Int) {
        skivvy.tts!!.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onDone(utteranceId: String) {
                startVoiceRecIntent(code,text)
            }
            override fun onError(utteranceId: String) {}
            override fun onStart(utteranceId: String) {}
        })
        if(!skivvy.getMuteStatus()) {
            skivvy.tts!!.speak(
                text,
                TextToSpeech.QUEUE_FLUSH,
                null,
                ""
            )
        } else {
            startVoiceRecIntent(code,text)
        }
        Toast.makeText(this, text, Toast.LENGTH_LONG).show()
    }
    private fun speakOut(text: String) {
        skivvy.tts!!.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onDone(utteranceId: String) {}
            override fun onError(utteranceId: String) {}
            override fun onStart(utteranceId: String) {}
        })
        skivvy.tts!!.speak(
            text,
            TextToSpeech.QUEUE_FLUSH,
            null,
            ""
        )
    }
}

