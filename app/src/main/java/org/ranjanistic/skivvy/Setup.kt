package org.ranjanistic.skivvy

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.view.View
import android.view.animation.Animation
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import java.util.concurrent.Executor

class Setup : AppCompatActivity() {
    lateinit var skivvy: Skivvy
    private lateinit var vocalLayout:LinearLayout
    private lateinit var settingIcon: ImageView
    private lateinit var training: Switch
    private lateinit var mute: Switch
    private lateinit var biometrics: Switch
    private lateinit var voiceAuthSwitch: Switch
    private lateinit var deleteVoiceSetup: TextView
    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo
    private lateinit var context: Context
    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.fade_on, R.anim.fade_off)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        skivvy = this.application as Skivvy
        context = this
        setContentView(R.layout.activity_setup)
        settingIcon = findViewById(R.id.settingIcon)
        training = findViewById(R.id.trainingModeBtn)
        mute = findViewById(R.id.muteUnmuteBtn)
        biometrics = findViewById(R.id.biometricsBtn)
        vocalLayout = findViewById(R.id.voice_setup)
        voiceAuthSwitch = findViewById(R.id.voice_auth_switch)
        deleteVoiceSetup = findViewById(R.id.delete_voice_key)
        findViewById<TextView>(R.id.version).text = BuildConfig.VERSION_NAME
        setTrainingMode(skivvy.getTrainingStatus())
        setMuteStatus(skivvy.getMuteStatus())
        if (skivvy.getPhraseKeyStatus()){
            voiceAuthSwitch.isChecked = true
            voiceAuthSwitch.text= getString(R.string.disable_vocal_authentication)
            deleteVoiceSetup.visibility = View.VISIBLE
            deleteVoiceSetup.isClickable = true
            deleteVoiceSetup.alpha = 1F
        } else defaultVoiceAuthUIState()
        setListeners()
    }

    private fun setListeners() {
        settingIcon.setOnClickListener {
            finish()
            overridePendingTransition(R.anim.fade_on, R.anim.fade_off)
        }
        training.setOnCheckedChangeListener { _, isChecked ->
            setTrainingMode(isChecked)
        }
        mute.setOnCheckedChangeListener { _, isChecked ->
            setMuteStatus(isChecked)
        }
        biometrics.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                biometrics.text = getString(R.string.disable_fingerprint)
                setBiometricsStatus(true)
            } else {
                if (skivvy.getBiometricStatus()) {
                    if(skivvy.checkBioMetrics()) {
                        authSetup(skivvy.CODE_BIOMETRIC_CONFIRM)
                        biometricPrompt.authenticate(promptInfo)
                    }
                }
            }
        }
        voiceAuthSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                voiceAuthSwitch.text= getString(R.string.disable_vocal_authentication)
                if (skivvy.getVoiceKeyPhrase() == null) {
                    speakOut(getString(R.string.tell_new_secret_phrase), skivvy.CODE_VOICE_AUTH_INIT)
                } else {
                    skivvy.setPhraseKeyStatus(true)
                    deleteVoiceSetup.visibility = View.VISIBLE
                    deleteVoiceSetup.alpha = 1F
                    deleteVoiceSetup.isClickable = true
                }
            } else {
                defaultVoiceAuthUIState()
            }
        }
        deleteVoiceSetup.setOnClickListener{
            if(skivvy.checkBioMetrics()) {
                authSetup(skivvy.CODE_VOICE_AUTH_CONFIRM)
                biometricPrompt.authenticate(promptInfo)
            } else {
                Snackbar.make(findViewById(R.id.setup_layout),"Confirm to reset and disable vocal authentication?",5000)
                    .setTextColor(ContextCompat.getColor(context,R.color.dull_white))
                    .setBackgroundTint(ContextCompat.getColor(context,R.color.dark_red))
                    .setAction(getString(R.string.reset)) {
                        skivvy.setVoiceKeyPhrase(null)
                        defaultVoiceAuthUIState()
                    }
                    .setActionTextColor(ContextCompat.getColor(context,R.color.colorPrimary))
                    .show()
            }
        }
    }

    @Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && data != null) {
            val text = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)[0].toString()
                .toLowerCase(skivvy.locale)
            when (requestCode) {
                skivvy.CODE_VOICE_AUTH_INIT -> {
                    defaultVoiceAuthUIState()
                    if (text.length < 5) {
                        speakOut("Try something longer", skivvy.CODE_VOICE_AUTH_INIT)
                    } else {
                        skivvy.setVoiceKeyPhrase(text)
                        speakOut("Say that again to confirm", skivvy.CODE_VOICE_AUTH_CONFIRM)
                    }
                }
                skivvy.CODE_VOICE_AUTH_CONFIRM -> {
                    if (text == skivvy.getVoiceKeyPhrase()) {
                        skivvy.setVoiceKeyPhrase(text)
                        voiceAuthSwitch.isChecked = true
                        skivvy.setPhraseKeyStatus(true)
                        deleteVoiceSetup.visibility = View.VISIBLE
                        deleteVoiceSetup.alpha = 1F
                        deleteVoiceSetup.isClickable = true
                        speakOut("'$text' is the phrase.")
                        if(!skivvy.getBiometricStatus() && skivvy.checkBioMetrics()){
                            Snackbar.make(findViewById(R.id.setup_layout),getString(R.string.biometric_recommendation_passphrase_enabling),25000)
                                .setTextColor(ContextCompat.getColor(context,R.color.pitch_white))
                                .setBackgroundTint(ContextCompat.getColor(context,R.color.charcoal))
                                .setAction("Enable") {
                                    setBiometricsStatus(true)
                                }
                                .setActionTextColor(ContextCompat.getColor(context,R.color.colorPrimaryDark))
                                .show()
                        }
                    } else {
                        skivvy.setVoiceKeyPhrase(null)
                        defaultVoiceAuthUIState()
                        speakOut(getString(R.string.confirmation_phrase_didnt_match))
                        Snackbar.make(findViewById(R.id.setup_layout),getString(R.string.confirmation_phrase_didnt_match),10000)
                            .setTextColor(ContextCompat.getColor(context,R.color.dull_white))
                            .setBackgroundTint(ContextCompat.getColor(context,R.color.dark_red))
                            .setAction("Try again") {
                                startVoiceRecIntent(skivvy.CODE_VOICE_AUTH_INIT,getString(R.string.tell_new_secret_phrase))
                            }
                            .setActionTextColor(ContextCompat.getColor(context,R.color.colorPrimary))
                            .show()
                    }
                }
            }
        } else {
            skivvy.setVoiceKeyPhrase(null)
            speakOut(getString(R.string.no_input))
            defaultVoiceAuthUIState()
        }
    }

    override fun onStart() {
        if (skivvy.checkBioMetrics()) {
            biometrics.visibility = View.VISIBLE
            setBiometricsStatus(skivvy.getBiometricStatus())
        } else {
            biometrics.visibility = View.GONE
            setBiometricsStatus(false)
        }
        super.onStart()
    }

    private fun authSetup(code:Int) {
        executor = ContextCompat.getMainExecutor(this)
        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(getString(R.string.auth_demand_title))
            .setSubtitle(getString(R.string.auth_demand_subtitle))
            .setDescription(getString(R.string.biometric_auth_explanation))
            .setNegativeButtonText(getString(R.string.discard))
            .build()
        biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult
                ) {
                    super.onAuthenticationSucceeded(result)
                    when(code){
                        skivvy.CODE_VOICE_AUTH_CONFIRM->{
                            defaultVoiceAuthUIState()
                            skivvy.setVoiceKeyPhrase(null)
                        }
                        skivvy.CODE_BIOMETRIC_CONFIRM ->{
                            biometrics.text = getString(R.string.enable_fingerprint)
                            setBiometricsStatus(false)
                        }
                    }
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    if(code == skivvy.CODE_BIOMETRIC_CONFIRM) {
                        setBiometricsStatus(true)
                    }
                }
                override fun onAuthenticationFailed() {
                    if(code == skivvy.CODE_BIOMETRIC_CONFIRM) {
                        setBiometricsStatus(true)
                    }
                    super.onAuthenticationFailed()
                }
            })
    }

    private fun setBiometricsStatus(isEnabled: Boolean) {
        getSharedPreferences(skivvy.PREF_HEAD_SECURITY, MODE_PRIVATE).edit()
            .putBoolean(skivvy.PREF_KEY_BIOMETRIC, isEnabled).apply()
        biometrics.isChecked = isEnabled
        if (isEnabled) {
            biometrics.text = getString(R.string.disable_fingerprint)
        } else {
            biometrics.text = getString(R.string.enable_fingerprint)
        }
    }

    private fun setMuteStatus(isMuted: Boolean) {
        getSharedPreferences(skivvy.PREF_HEAD_VOICE, MODE_PRIVATE).edit()
            .putBoolean(skivvy.PREF_KEY_MUTE_UNMUTE, isMuted).apply()
        mute.isChecked = isMuted
        if (isMuted) {
            mute.text = getString(R.string.unmute_text)
        } else {
            mute.text = getString(R.string.mute_text)
        }
    }

    private fun setTrainingMode(isTraining: Boolean) {
        getSharedPreferences(skivvy.PREF_HEAD_APP_MODE, MODE_PRIVATE).edit()
            .putBoolean(skivvy.PREF_KEY_TRAINING, isTraining).apply()
        training.isChecked = isTraining
        if (isTraining) {
            training.text = getString(R.string.deactivate_training_text)
        } else {
            training.text = getString(R.string.activate_training_text)
        }
    }

    private fun speakOut(text: String, code: Int) {
        skivvy.tts!!.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onDone(utteranceId: String) {
                startVoiceRecIntent(code,text)
            }

            override fun onError(utteranceId: String) {}
            override fun onStart(utteranceId: String) {}
        })
        if (!skivvy.getMuteStatus()) {
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

    private fun defaultVoiceAuthUIState() {
        voiceAuthSwitch.isChecked = false
        voiceAuthSwitch.text= getString(R.string.enable_vocal_authentication)
        skivvy.setPhraseKeyStatus(false)
        deleteVoiceSetup.visibility = View.GONE
    }

    private fun startVoiceRecIntent(code: Int,msg:String) {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            .putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            .putExtra(RecognizerIntent.EXTRA_LANGUAGE, skivvy.locale)
            .putExtra(RecognizerIntent.EXTRA_PROMPT, msg)
        if (intent.resolveActivity(packageManager) != null)
            startActivityForResult(intent, code)
        else {
            Snackbar.make(
                findViewById(R.id.setup_layout),
                "Error in speech recognition.",
                Snackbar.LENGTH_SHORT
            )
                .setBackgroundTint(ContextCompat.getColor(context,R.color.dark_red))
                .setTextColor(ContextCompat.getColor(context,R.color.dull_white))
        }
    }
    private fun speakOut(text: String) {
        skivvy.tts!!.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onDone(utteranceId: String) {}
            override fun onError(utteranceId: String) {}
            override fun onStart(utteranceId: String) {}
        })
        if (!skivvy.getMuteStatus()) {
            skivvy.tts!!.speak(
                text,
                TextToSpeech.QUEUE_FLUSH,
                null,
                ""
            )
        }

        Toast.makeText(this, text, Toast.LENGTH_LONG).show()
    }
}
