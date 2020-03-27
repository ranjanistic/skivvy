package org.ranjanistic.skivvy

import android.media.Image
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import java.util.concurrent.Executor

class Setup : AppCompatActivity() {
    lateinit var skivvy:Skivvy
    private var scaleAnimation: Animation? = null
    private lateinit var settingIcon: ImageView
    private lateinit var training:TextView
    private lateinit var mute:TextView
    private lateinit var biometrics:TextView
    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        skivvy = this.application as Skivvy
        setContentView(R.layout.activity_setup)
        settingIcon = findViewById(R.id.settingIcon)
        training = findViewById(R.id.trainingModeBtn)
        mute = findViewById(R.id.muteUnmuteBtn)
        biometrics = findViewById(R.id.biometricsBtn)
        //scaleAnimation = AnimationUtils.loadAnimation(this,R.anim.scale_translate_setting_reverse)
        setTrainingMode(getTrainingStatus())
        saveMuteStatus(getMuteStatus())

        settingIcon.setOnClickListener{
            finish()
            //startFinishAnimation()
        }
        /*
        scaleAnimation!!.setAnimationListener(object : Animation.AnimationListener{
            override fun onAnimationEnd(p0: Animation?) {
                finish()
                overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out)
            }
            override fun onAnimationStart(p0: Animation?) {}
            override fun onAnimationRepeat(p0: Animation?) {}
        })
         */
        training.setOnClickListener{
            setTrainingMode(!getTrainingStatus())
        }
        mute.setOnClickListener {
            saveMuteStatus(!getMuteStatus())
        }
        biometrics.setOnClickListener{
            if(getBiometricStatus()){
                biometricPrompt.authenticate(promptInfo)
            } else {
                setBiometricsStatus(!getBiometricStatus())
            }
        }

    }

    override fun onStart() {
        if(checkBioMetrics()) {
            biometrics.visibility = View.VISIBLE
            setBiometricsStatus(getBiometricStatus())
            authSetup()
        } else {
            biometrics.visibility = View.GONE
            setBiometricsStatus(false)
        }
        super.onStart()
    }

    private fun checkBioMetrics():Boolean{
        val biometricManager = BiometricManager.from(this)
        return when (biometricManager.canAuthenticate()) {
            BiometricManager.BIOMETRIC_SUCCESS -> true
            else -> false
        }
    }
    private fun startFinishAnimation(){
        settingIcon.startAnimation(scaleAnimation)
    }
    private fun authSetup(){
        executor = ContextCompat.getMainExecutor(this)
        biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    setBiometricsStatus(!getBiometricStatus())
                }
            })
        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(getString(R.string.auth_demand_title))
            .setSubtitle(getString(R.string.auth_demand_subtitle))
            .setDescription(getString(R.string.biometric_auth_explanation))
            .setNegativeButtonText("Discard")
            .build()
    }
    private fun setBiometricsStatus(isEnabled:Boolean){
        getSharedPreferences(skivvy.PREF_HEAD_SECURITY, MODE_PRIVATE).edit()
            .putBoolean(skivvy.PREF_KEY_BIOMETRIC,isEnabled).apply()
        if(isEnabled) {
            biometrics.text = getString(R.string.disable_fingerprint)
            biometrics.setBackgroundResource(R.drawable.red_square_round_button)
        }
        else{
            biometrics.text = getString(R.string.enable_fingerprint)
            biometrics.setBackgroundResource(R.drawable.spruce_square_round_button)
        }
    }
    private fun getBiometricStatus():Boolean{
        return getSharedPreferences(skivvy.PREF_HEAD_SECURITY, MODE_PRIVATE)
            .getBoolean(skivvy.PREF_KEY_BIOMETRIC, false)
    }
    private fun saveMuteStatus(isMuted:Boolean){
        getSharedPreferences(skivvy.PREF_HEAD_VOICE, MODE_PRIVATE).edit()
            .putBoolean(skivvy.PREF_KEY_MUTE_UNMUTE,isMuted ).apply()
        if(isMuted) {
            mute.text = getString(R.string.unmute_text)
            mute.setBackgroundResource(R.drawable.red_square_round_button)
        }
        else{
            mute.text = getString(R.string.mute_text)
            mute.setBackgroundResource(R.drawable.spruce_square_round_button)
        }

    }
    private fun setTrainingMode(isTraining:Boolean){
        getSharedPreferences(skivvy.PREF_HEAD_APP_MODE, MODE_PRIVATE).edit()
            .putBoolean(skivvy.PREF_KEY_TRAINING,isTraining ).apply()
        if(isTraining){
            training.text = getString(R.string.deactivate_training_text)
        } else{
            training.text = getString(R.string.activate_training_text)
        }
    }
    private fun getTrainingStatus():Boolean{
        return getSharedPreferences(skivvy.PREF_HEAD_APP_MODE, MODE_PRIVATE)
            .getBoolean(skivvy.PREF_KEY_TRAINING, false)
    }
    private fun getMuteStatus():Boolean{
        return getSharedPreferences(skivvy.PREF_HEAD_VOICE, MODE_PRIVATE)
            .getBoolean(skivvy.PREF_KEY_MUTE_UNMUTE, false)
    }
}
