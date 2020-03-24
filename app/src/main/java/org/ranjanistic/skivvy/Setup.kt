package org.ranjanistic.skivvy

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.view.View
import android.widget.TextView
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import java.util.concurrent.Executor

class Setup : AppCompatActivity() {
    private lateinit var training:TextView
    private lateinit var mute:TextView
    private lateinit var biometrics:TextView
    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setup)
        training = findViewById(R.id.trainingModeBtn)
        mute = findViewById(R.id.muteUnmuteBtn)
        biometrics = findViewById(R.id.biometricsBtn)
        setTrainingMode(getTrainingStatus())
        saveMuteStatus(getMuteStatus())
        setBiometricsStatus(getBiometricStatus())
        if(getBiometricStatus()) {authSetup()}
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
    private fun authSetup(){
        executor = ContextCompat.getMainExecutor(this)
        biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int,
                                                   errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                }

                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    setBiometricsStatus(!getBiometricStatus())
                }
            })
        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(getString(R.string.auth_demand_title))
            .setSubtitle(getString(R.string.auth_demand_subtitle))
            .setNegativeButtonText(getString(R.string.other_auth_ops))
            .build()
    }
    private fun setBiometricsStatus(isEnabled:Boolean){
        getSharedPreferences("appLock", MODE_PRIVATE).edit()
            .putBoolean("fingerprint",isEnabled).apply()
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
        return getSharedPreferences("appLock", MODE_PRIVATE)
            .getBoolean("fingerprint", false)
    }
    private fun saveMuteStatus(isMuted:Boolean){
        getSharedPreferences("voicePreference", MODE_PRIVATE).edit()
            .putBoolean("muted",isMuted ).apply()
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
        getSharedPreferences("training", MODE_PRIVATE).edit()
            .putBoolean("status",isTraining ).apply()
        if(isTraining){
            training.text = getString(R.string.deactivate_training_text)
        } else{
            training.text = getString(R.string.activate_training_text)
        }
    }
    private fun getTrainingStatus():Boolean{
        return getSharedPreferences("training", MODE_PRIVATE)
            .getBoolean("status", false)
    }
    private fun getMuteStatus():Boolean{
        return getSharedPreferences("voicePreference", MODE_PRIVATE)
            .getBoolean("muted", false)
    }
}
