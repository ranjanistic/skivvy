package org.ranjanistic.skivvy

import android.app.Activity
import android.app.admin.DevicePolicyManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.opengl.Visibility
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.view.View
import android.view.animation.Animation
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import java.util.concurrent.Executor

@ExperimentalStdlibApi
class Setup : AppCompatActivity() {
    lateinit var skivvy: Skivvy
    private lateinit var vocalLayout:LinearLayout
    private lateinit var settingIcon: ImageView
    private lateinit var training: Switch
    private lateinit var mute: Switch
    private lateinit var theme:Switch
    private lateinit var biometrics: Switch
    private lateinit var voiceAuth: Switch
    private lateinit var deleteVoiceSetup: TextView
    private lateinit var permissions:TextView
    private lateinit var deviceAdmin:TextView
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
        setTheme(skivvy.getThemeState())
        setContentView(R.layout.activity_setup)
        settingIcon = findViewById(R.id.settingIcon)
        training = findViewById(R.id.trainingModeBtn)
        mute = findViewById(R.id.muteUnmuteBtn)
        theme = findViewById(R.id.themeSwitch)
        biometrics = findViewById(R.id.biometricsBtn)
        vocalLayout = findViewById(R.id.voice_setup)
        voiceAuth = findViewById(R.id.voice_auth_switch)
        deleteVoiceSetup = findViewById(R.id.delete_voice_key)
        permissions = findViewById(R.id.permissionBtn)
        deviceAdmin = findViewById(R.id.deviceAdminBtn)
        findViewById<TextView>(R.id.version).text = BuildConfig.VERSION_NAME
        setTrainingMode(skivvy.getTrainingStatus())
        setMuteStatus(skivvy.getMuteStatus())
        setThemeStatus(skivvy.getThemeState())
        when {
            skivvy.hasPermissions(context) -> permissions.visibility = View.GONE
            else -> permissions.visibility = View.VISIBLE
        }
        when {
            skivvy.deviceManager.isAdminActive(skivvy.compName) ->deviceAdmin.visibility = View.GONE
            else -> deviceAdmin.visibility = View.VISIBLE
        }
        if (skivvy.getPhraseKeyStatus()){
            voiceAuth.isChecked = true
            voiceAuth.text= getString(R.string.disable_vocal_authentication)
            voiceAuth.thumbTintList = ContextCompat.getColorStateList(this,R.color.colorPrimary)
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
        theme.setOnCheckedChangeListener{view,isChecked->
            if(isChecked){
                view.text = getString(R.string.switch_to_dark)
                skivvy.setThemeState(R.style.LightTheme)
                startActivity(Intent(this, MainActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
                this.finish()
            } else {
                view.text = getString(R.string.switch_to_light)
                skivvy.setThemeState(R.style.DarkTheme)
                startActivity(Intent(this, MainActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
                this.finish()
            }
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
        voiceAuth.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                voiceAuth.thumbTintList = ContextCompat.getColorStateList(this,R.color.colorPrimary)
                voiceAuth.text= getString(R.string.disable_vocal_authentication)
                if (skivvy.getVoiceKeyPhrase() == null) {
                    speakOut(getString(R.string.tell_new_secret_phrase), skivvy.CODE_VOICE_AUTH_INIT)
                } else {
                    skivvy.setPhraseKeyStatus(true)
                    deleteVoiceSetup.visibility = View.VISIBLE
                    deleteVoiceSetup.alpha = 1F
                    deleteVoiceSetup.isClickable = true
                    if(!skivvy.getBiometricStatus()){
                        showBiometricRecommendation()
                    }
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
        permissions.setOnClickListener{
            if(!skivvy.hasPermissions(context)){
                ActivityCompat.requestPermissions(
                    this, skivvy.permissions,
                    skivvy.CODE_ALL_PERMISSIONS
                )
            }
        }
        deviceAdmin.setOnClickListener{
            if(!skivvy.deviceManager.isAdminActive(skivvy.compName)){
                startActivityForResult(
                    Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
                        .putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, skivvy.compName)
                        .putExtra(
                            DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                            getString(R.string.device_admin_persuation)
                        ), skivvy.CODE_DEVICE_ADMIN
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            skivvy.CODE_ALL_PERMISSIONS->{
                if(skivvy.hasPermissions(context)){
                    showSnackBar(getString(R.string.have_all_permits))
                    this.permissions.visibility = View.GONE
                } else {
                    showSnackBar("I still don't have all the permissions.",R.color.dark_red,R.color.pitch_white)
                    this.permissions.visibility = View.VISIBLE
                }
            }
        }
    }
    private fun showSnackBar(message:String,bgColorID:Int = R.color.colorPrimaryDark,textColorID:Int = R.color.pitch_white,duration:Int= 5000){
        Snackbar.make(findViewById(R.id.setup_layout),message,duration)
            .setBackgroundTint(ContextCompat.getColor(context, bgColorID))
            .setTextColor(ContextCompat.getColor(context, textColorID))
            .show()
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
                        voiceAuth.isChecked = true
                        skivvy.setPhraseKeyStatus(true)
                        deleteVoiceSetup.visibility = View.VISIBLE
                        deleteVoiceSetup.alpha = 1F
                        deleteVoiceSetup.isClickable = true
                        speakOut("'$text' is the phrase.")
                        if(!skivvy.getBiometricStatus() && skivvy.checkBioMetrics()){
                            showBiometricRecommendation()
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
                skivvy.CODE_DEVICE_ADMIN->{
                    if(skivvy.deviceManager.isAdminActive(skivvy.compName)){
                        showSnackBar(getString(R.string.device_admin_success))
                        deviceAdmin.visibility = View.GONE
                    } else showSnackBar(getString(R.string.device_admin_failure),R.color.dark_red)
                }
            }
        } else {
            skivvy.setVoiceKeyPhrase(null)
            speakOut(getString(R.string.no_input))
            defaultVoiceAuthUIState()
        }
    }

    private fun showBiometricRecommendation(){
        Snackbar.make(findViewById(R.id.setup_layout),getString(R.string.biometric_recommendation_passphrase_enabling),25000)
            .setTextColor(ContextCompat.getColor(context,R.color.pitch_white))
            .setBackgroundTint(ContextCompat.getColor(context,R.color.charcoal))
            .setAction("Enable") {
                setBiometricsStatus(true)
            }
            .setActionTextColor(ContextCompat.getColor(context,R.color.colorPrimaryDark))
            .show()
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
        if(isEnabled){
            biometrics.thumbTintList = ContextCompat.getColorStateList(this,R.color.colorPrimary)
            biometrics.text = getString(R.string.disable_fingerprint)
        } else {
            biometrics.thumbTintList = ContextCompat.getColorStateList(this,R.color.light_spruce)
            biometrics.text = getString(R.string.enable_fingerprint)
        }
    }

    private fun setMuteStatus(isMuted: Boolean) {
        getSharedPreferences(skivvy.PREF_HEAD_VOICE, MODE_PRIVATE).edit()
            .putBoolean(skivvy.PREF_KEY_MUTE_UNMUTE, isMuted).apply()
        mute.isChecked = isMuted
        if(isMuted){
            mute.thumbTintList = ContextCompat.getColorStateList(this,R.color.colorPrimary)
            mute.text = getString(R.string.unmute_text)
        } else{
            mute.thumbTintList = ContextCompat.getColorStateList(this,R.color.light_spruce)
            mute.text = getString(R.string.mute_text)
        }
    }

    private fun setThemeStatus(themeCode:Int){
        getSharedPreferences(skivvy.PREF_HEAD_APP_MODE, MODE_PRIVATE).edit()
            .putInt(skivvy.PREF_KEY_THEME, themeCode).apply()
        theme.isChecked = themeCode == R.style.LightTheme
        if(theme.isChecked){
            theme.thumbTintList = ContextCompat.getColorStateList(this,R.color.colorPrimary)
            theme.text = getString(R.string.switch_to_dark)
        }
        else{
            theme.thumbTintList = ContextCompat.getColorStateList(this,R.color.light_spruce)
            theme.text = getString(R.string.switch_to_light)
        }
    }
    private fun setTrainingMode(isTraining: Boolean) {
        getSharedPreferences(skivvy.PREF_HEAD_APP_MODE, MODE_PRIVATE).edit()
            .putBoolean(skivvy.PREF_KEY_TRAINING, isTraining).apply()
        training.isChecked = isTraining
        if (isTraining) {
            training.thumbTintList = ContextCompat.getColorStateList(this,R.color.colorPrimary)
            training.text = getString(R.string.deactivate_training_text)
        } else {
            training.thumbTintList = ContextCompat.getColorStateList(this,R.color.light_spruce)
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
        voiceAuth.isChecked = false
        voiceAuth.text= getString(R.string.enable_vocal_authentication)
        voiceAuth.thumbTintList = ContextCompat.getColorStateList(this,R.color.light_spruce)
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
