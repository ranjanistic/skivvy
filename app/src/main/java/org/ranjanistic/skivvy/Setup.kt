package org.ranjanistic.skivvy

import android.R.id.message
import android.app.Activity
import android.app.admin.DevicePolicyManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.view.LayoutInflater
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.delay
import org.ranjanistic.skivvy.databinding.ActivityMainBinding.inflate
import java.util.*
import java.util.concurrent.Executor
import kotlin.concurrent.schedule


@ExperimentalStdlibApi
class Setup : AppCompatActivity() {
    lateinit var skivvy: Skivvy
    private lateinit var vocalLayout: LinearLayout
    private lateinit var settingIcon: ImageView
    private lateinit var training: Switch
    private lateinit var mute: Switch
    private lateinit var theme: Switch
    private lateinit var response: Switch
    private lateinit var biometrics: Switch
    private lateinit var voiceAuth: Switch
    private lateinit var deleteVoiceSetup: TextView
    private lateinit var permissions: TextView
    private lateinit var deviceAdmin: TextView
    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo
    private lateinit var context: Context
    private var temp = Temporary()
    private lateinit var recognitionIntent:Intent
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
        setViewAndInitials()
        setListeners()
        recognitionIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            .putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            .putExtra(RecognizerIntent.EXTRA_LANGUAGE, skivvy.locale)
    }

    private fun setViewAndInitials(){
        settingIcon = findViewById(R.id.settingIcon)
        training = findViewById(R.id.trainingModeBtn)
        mute = findViewById(R.id.muteUnmuteBtn)
        theme = findViewById(R.id.themeSwitch)
        response = findViewById(R.id.parallelResponseBtn)
        biometrics = findViewById(R.id.biometricsBtn)
        vocalLayout = findViewById(R.id.voice_setup)
        voiceAuth = findViewById(R.id.voice_auth_switch)
        deleteVoiceSetup = findViewById(R.id.delete_voice_key)
        permissions = findViewById(R.id.permissionBtn)
        permissions.text = getString(R.string.grant_permissions)
        permissions.setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.ic_keyfillteal,0)
        deviceAdmin = findViewById(R.id.deviceAdminBtn)
        deviceAdmin.text = getString(R.string.make_device_admin)
        deviceAdmin.setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.ic_locknkey,0)
        findViewById<TextView>(R.id.version).text = BuildConfig.VERSION_NAME
        setThumbAttrs(training,skivvy.getTrainingStatus(),getString(R.string.deactivate_training_text),getString(R.string.activate_training_text))
        setThumbAttrs(mute,skivvy.getMuteStatus(),getString(R.string.unmute_text), getString(R.string.mute_text))
        setThumbAttrs(theme,skivvy.getThemeState() == R.style.LightTheme,getString(R.string.switch_to_dark),getString(R.string.switch_to_light))
        setThumbAttrs(response,skivvy.getParallelResponseStatus(),getString(R.string.set_queued_receive),getString(R.string.set_parallel_receive))
        if (skivvy.checkBioMetrics()) {
            biometrics.visibility = View.VISIBLE
            setThumbAttrs(biometrics,skivvy.getBiometricStatus(),getString(R.string.disable_fingerprint),getString(R.string.enable_fingerprint))
        } else biometrics.visibility = View.GONE
        if (skivvy.getPhraseKeyStatus()) {
            setThumbAttrs(voiceAuth,true, onText = getString(R.string.disable_vocal_authentication))
            deleteVoiceSetup.visibility = View.VISIBLE
        } else defaultVoiceAuthUIState()
        when {
            skivvy.hasPermissions(context) -> permissions.visibility = View.GONE
            else -> permissions.visibility = View.VISIBLE
        }
        when {
            skivvy.deviceManager.isAdminActive(skivvy.compName) -> deviceAdmin.visibility =
                View.GONE
            else -> deviceAdmin.visibility = View.VISIBLE
        }
    }
    override fun onStart() {
        super.onStart()
        for(k in arrayOf(training,mute,theme,response,biometrics,vocalLayout,permissions,deviceAdmin))
            bubbleThis(k)
    }
    private fun bubbleThis(view:View){
        view.startAnimation(AnimationUtils.loadAnimation(context, R.anim.reveal_delay))
    }
    private fun setListeners() {
        settingIcon.setOnClickListener {
            finish()
            overridePendingTransition(R.anim.fade_on, R.anim.fade_off)
        }
        training.setOnCheckedChangeListener {view, isChecked ->
            skivvy.setTrainingStatus(isChecked)
            setThumbAttrs(view as Switch,isChecked,getString(R.string.deactivate_training_text),getString(R.string.activate_training_text))
            if(isChecked){
                speakOut(getString(R.string.activate_training_text))
                finish()
            }
            else speakOut(getString(R.string.deactivate_training_text))
        }
        mute.setOnCheckedChangeListener { view, isChecked ->
            skivvy.saveMuteStatus(isChecked)
            setThumbAttrs(view as Switch,isChecked,getString(R.string.unmute_text), getString(R.string.mute_text))
            if(isChecked) speakOut(getString(R.string.muted))
            else speakOut(getString(R.string.speaking))
        }
        theme.setOnCheckedChangeListener { view, isChecked ->
            if (isChecked) {
                speakOut(getString(R.string.light_theme_set))
                view.text = getString(R.string.switch_to_dark)
                skivvy.setThemeState(R.style.LightTheme)
                startActivity(
                    Intent(
                        context,
                        MainActivity::class.java
                    ).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                )
            } else {
                speakOut(getString(R.string.dark_theme_set))
                view.text = getString(R.string.switch_to_light)
                skivvy.setThemeState(R.style.DarkTheme)
                startActivity(
                    Intent(
                        context,
                        MainActivity::class.java
                    ).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                )
            }
            finish()
        }
        response.setOnCheckedChangeListener{view,isChecked->
            skivvy.setParallelResponseStatus(isChecked)
            setThumbAttrs(view as Switch,isChecked,getString(R.string.set_queued_receive),getString(R.string.set_parallel_receive))
            if(isChecked) speakOut(getString(R.string.parallel_receive_text))
            else speakOut(getString(R.string.queued_receive_text))
        }
        biometrics.setOnCheckedChangeListener {view, isChecked ->
            if (isChecked) {
                skivvy.setBiometricsStatus(true)
                setThumbAttrs(view as Switch, true, onText = getString(R.string.disable_fingerprint))
                speakOut(getString(R.string.fingerprint_is_on))
            } else {
                if (skivvy.getBiometricStatus()) {
                    if (skivvy.checkBioMetrics()) {
                        initAndShowBioAuth(skivvy.CODE_BIOMETRIC_CONFIRM)
                    }
                }
            }
        }
        voiceAuth.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                if (skivvy.getVoiceKeyPhrase() == null) {
                    speakOut(
                        getString(R.string.tell_new_secret_phrase),
                        skivvy.CODE_VOICE_AUTH_INIT
                    )
                } else {
                    speakOut(getString(R.string.voice_auth_enabled))
                    skivvy.setPhraseKeyStatus(true)
                    setThumbAttrs(voiceAuth,true, onText = getString(R.string.disable_vocal_authentication))
                    deleteVoiceSetup.visibility = View.VISIBLE
                    if (!skivvy.getBiometricStatus()) {
                        showBiometricRecommendation()
                    }
                }
            } else {
                speakOut(getString(R.string.voice_auth_disabled))
                defaultVoiceAuthUIState()
            }
        }
        deleteVoiceSetup.setOnClickListener {
            if (skivvy.checkBioMetrics()) {
                initAndShowBioAuth(skivvy.CODE_VOICE_AUTH_CONFIRM)
            } else {
                speakOut(getString(R.string.reset_voice_auth_confirm))
                Snackbar.make(
                    findViewById(R.id.setup_layout),
                    getString(R.string.reset_voice_auth_confirm),
                    5000
                )
                    .setTextColor(ContextCompat.getColor(context, R.color.dull_white))
                    .setBackgroundTint(ContextCompat.getColor(context, R.color.dark_red))
                    .setAction(getString(R.string.reset)) {
                        skivvy.setVoiceKeyPhrase(null)
                        defaultVoiceAuthUIState()
                    }
                    .setActionTextColor(ContextCompat.getColor(context, R.color.colorPrimary))
                    .show()
            }
        }
        deleteVoiceSetup.setOnLongClickListener{view->
            view.startAnimation(AnimationUtils.loadAnimation(context, R.anim.bubble_wave))
            speakOut("This will delete your existing passphrase.")
            true
        }
        permissions.setOnClickListener {
            if (!skivvy.hasPermissions(context)) {
                speakOut("I need these permissions to work perfectly.")
                ActivityCompat.requestPermissions(
                    this, skivvy.permissions,
                    skivvy.CODE_ALL_PERMISSIONS
                )
            }
        }

        permissions.setOnLongClickListener { view ->
            view.startAnimation(AnimationUtils.loadAnimation(context, R.anim.bubble_wave))
            speakOut("Grant me some permissions for later use")
            true
        }

        deviceAdmin.setOnClickListener {
            if (!skivvy.deviceManager.isAdminActive(skivvy.compName)) {
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
        deviceAdmin.setOnLongClickListener{view->
            view.startAnimation(AnimationUtils.loadAnimation(context, R.anim.bubble_wave))
            speakOut("Make me your device admin for better performance")
            true
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            skivvy.CODE_ALL_PERMISSIONS -> {
                if (skivvy.hasPermissions(context)) {
                    showSnackBar(getString(R.string.have_all_permits))
                    this.permissions.visibility = View.GONE
                } else {
                    showSnackBar(
                        "I still don't have all the permissions.",
                        R.color.dark_red,
                        R.color.pitch_white
                    )
                    this.permissions.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun showSnackBar(
        message: String,
        bgColorID: Int = R.color.colorPrimaryDark,
        textColorID: Int = R.color.pitch_white,
        duration: Int = 5000
    ) {
        Snackbar.make(findViewById(R.id.setup_layout), message, duration)
            .setBackgroundTint(ContextCompat.getColor(context, bgColorID))
            .setTextColor(ContextCompat.getColor(context, textColorID))
            .show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        var text = String()
        if (!skivvy.nonVocalRequestCodes.contains(resultCode)) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.let{
                    text = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)!![0].toString()
                        .toLowerCase(skivvy.locale)
                }
            } else {
                if(resultCode == skivvy.CODE_VOICE_AUTH_INIT || resultCode == skivvy.CODE_VOICE_AUTH_CONFIRM) {
                    speakOut(getString(R.string.no_input))
                    skivvy.setVoiceKeyPhrase(null)
                    defaultVoiceAuthUIState()
                    return
                }
            }
        }
        when (requestCode) {
            skivvy.CODE_VOICE_AUTH_INIT -> {
                defaultVoiceAuthUIState()
                if(text!="") {
                    if (text.length < 5) {
                        speakOut("Try something longer", skivvy.CODE_VOICE_AUTH_INIT)
                    } else {
                        skivvy.setVoiceKeyPhrase(text)
                        speakOut("Say that again to confirm", skivvy.CODE_VOICE_AUTH_CONFIRM)
                    }
                } else {
                    skivvy.setVoiceKeyPhrase(null)
                    defaultVoiceAuthUIState()
                    speakOut(getString(R.string.no_input))
                    return
                }
            }
            skivvy.CODE_VOICE_AUTH_CONFIRM -> {
                if (text == skivvy.getVoiceKeyPhrase()) {
                    skivvy.setVoiceKeyPhrase(text)
                    voiceAuth.isChecked = true
                    skivvy.setPhraseKeyStatus(true)
                    deleteVoiceSetup.visibility = View.VISIBLE
                    
                    
                    speakOut("'${skivvy.getVoiceKeyPhrase()}' is the phrase.")
                    if (!skivvy.getBiometricStatus() && skivvy.checkBioMetrics()) {
                        showBiometricRecommendation()
                    }
                } else {
                    skivvy.setVoiceKeyPhrase(null)
                    defaultVoiceAuthUIState()
                    speakOut(getString(R.string.confirmation_phrase_didnt_match))
                    Snackbar.make(
                        findViewById(R.id.setup_layout),
                        getString(R.string.confirmation_phrase_didnt_match),
                        10000
                    )
                        .setTextColor(ContextCompat.getColor(context, R.color.pitch_white))
                        .setBackgroundTint(ContextCompat.getColor(context, R.color.dark_red))
                        .setAction("Try again") {
                            startVoiceRecIntent(
                                skivvy.CODE_VOICE_AUTH_INIT,
                                getString(R.string.tell_new_secret_phrase)
                            )
                        }
                        .setActionTextColor(ContextCompat.getColor(context, R.color.light_green))
                        .show()
                }
            }
            skivvy.CODE_DEVICE_ADMIN -> {
                if (resultCode == Activity.RESULT_OK) {
                    speakOut(getString(R.string.device_admin_success))
                    showSnackBar(getString(R.string.device_admin_success))
                    deviceAdmin.visibility = View.GONE
                } else {
                    showSnackBar(getString(R.string.device_admin_failure), R.color.dark_red)
                    speakOut(getString(R.string.device_admin_failure))
                }
            }
        }
    }

    private fun showBiometricRecommendation() {
        Snackbar.make(
            findViewById(R.id.setup_layout),
            getString(R.string.biometric_recommendation_passphrase_enabling),
            25000
        )
            .setTextColor(ContextCompat.getColor(context, R.color.pitch_white))
            .setBackgroundTint(ContextCompat.getColor(context, R.color.charcoal))
            .setAction("Enable") {
                skivvy.setBiometricsStatus(true)
                setThumbAttrs(biometrics,true, onText = getString(R.string.disable_fingerprint))
            }
            .setActionTextColor(ContextCompat.getColor(context, R.color.dull_white))
            .show()
    }

    private fun initAndShowBioAuth(code: Int) {
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
                    when (code) {
                        skivvy.CODE_VOICE_AUTH_CONFIRM -> {
                            speakOut("Your secret phrase was deleted")
                            defaultVoiceAuthUIState()
                            skivvy.setVoiceKeyPhrase(null)
                        }
                        skivvy.CODE_BIOMETRIC_CONFIRM -> {
                            speakOut("Fingerprint disabled")
                            skivvy.setBiometricsStatus(false)
                            setThumbAttrs(biometrics,false, offText = getString(R.string.enable_fingerprint))
                        }
                    }
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    when(code){
                        skivvy.CODE_BIOMETRIC_CONFIRM-> {
                            skivvy.setBiometricsStatus(true)
                            setThumbAttrs(biometrics,true, onText = getString(R.string.disable_fingerprint))
                        }
                    }
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    when(code){
                        skivvy.CODE_BIOMETRIC_CONFIRM ->{
                            skivvy.setBiometricsStatus(true)
                            setThumbAttrs(biometrics,true, onText = getString(R.string.disable_fingerprint))
                        }
                        skivvy.CODE_VOICE_AUTH_CONFIRM ->{
                            temp.setAuthAttemptCount(temp.getAuthAttemptCount()-1)
                            if(temp.getAuthAttemptCount() == 0){
                                //TODO: Set countdown to delete
                            }
                        }
                    }
                }
            })
        biometricPrompt.authenticate(promptInfo)
    }

    private fun defaultVoiceAuthUIState() {
        skivvy.setPhraseKeyStatus(false)
        setThumbAttrs(voiceAuth,false, offText = getString(R.string.enable_vocal_authentication))
        deleteVoiceSetup.visibility = View.GONE
    }

    private fun setThumbAttrs(switch:Switch,isOn:Boolean,onText:String? = null,offText:String? = null){
        switch.isChecked = isOn
        if(isOn){
            switch.thumbTintList = ContextCompat.getColorStateList(context, R.color.colorPrimary)
            onText?.let{switch.text = onText}
        } else{
            switch.thumbTintList = ContextCompat.getColorStateList(context, R.color.light_spruce)
            offText?.let{switch.text = offText}
        }
    }

    private fun speakOut(text: String, code: Int? = null, isParallel:Boolean = skivvy.getParallelResponseStatus()) {
        skivvy.tts!!.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onDone(utteranceId: String) {
                if(!isParallel)
                    code?.let{startVoiceRecIntent(code, text)}
            }
            override fun onError(utteranceId: String) {}
            override fun onStart(utteranceId: String) {
                if(isParallel)
                    code?.let{startVoiceRecIntent(code, text)}
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
            code?.let{startVoiceRecIntent(code, text)}
        }
        Toast.makeText(this, text, Toast.LENGTH_LONG).show()
    }

    private fun startVoiceRecIntent(code: Int, msg: String = getString(R.string.generic_voice_rec_text)) {
        recognitionIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, msg)
        if (recognitionIntent.resolveActivity(packageManager) != null)
            startActivityForResult(recognitionIntent, code)
        else
            showSnackBar(getString(R.string.recognition_service_error),R.color.dark_red,R.color.dull_white)
    }
}
