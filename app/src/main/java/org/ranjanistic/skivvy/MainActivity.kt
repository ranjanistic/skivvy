package org.ranjanistic.skivvy

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.admin.DevicePolicyManager
import android.bluetooth.BluetoothAdapter
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import android.location.LocationManager
import android.media.AudioManager
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.ContactsContract
import android.provider.MediaStore
import android.provider.Settings
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.telephony.PhoneStateListener
import android.telephony.SmsManager
import android.telephony.TelephonyManager
import android.text.format.DateFormat
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.RoundedBitmapDrawable
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import org.ranjanistic.skivvy.R.drawable.*
import org.ranjanistic.skivvy.R.string.*
import org.ranjanistic.skivvy.manager.CalculationManager
import org.ranjanistic.skivvy.manager.InputSpeechManager
import org.ranjanistic.skivvy.manager.TempDataManager
import java.io.File
import java.io.FileOutputStream
import java.net.URLEncoder
import java.util.*
import java.util.concurrent.Executor
import kotlin.collections.ArrayList

@ExperimentalStdlibApi
open class MainActivity : AppCompatActivity() {

    lateinit var skivvy: Skivvy

    //TODO: add up with last command and retry
    //TODO: widget for actions (calculations first, or a calculator widget)
    private lateinit var outPut: TextView
    private lateinit var input: TextView
    private lateinit var greet: TextView
    private lateinit var feedback: TextView

    private lateinit var focusDeFocusRotate: Animation
    private lateinit var zoomInOutRotate: Animation
    private lateinit var focusRotate: Animation
    private lateinit var zoomInRotate: Animation
    private lateinit var fadeOnFadeOff: Animation
    private lateinit var waveDamped: Animation
    private lateinit var fallDown: Animation
    private lateinit var riseUp: Animation
    private lateinit var extendDown: Animation
    private lateinit var extendDownStartSetup: Animation
    private lateinit var rotateRevolveToRight: Animation
    private lateinit var revolveRotateToLeft: Animation
    private lateinit var fadeOff: Animation
    private lateinit var fadeOn: Animation

    private lateinit var receiver: ImageButton
    private lateinit var setting: ImageButton
    private lateinit var settingBack: ImageView
    private lateinit var loading: ImageView
    private lateinit var backfall: ImageView
    private lateinit var icon: ImageView

    private var txt: String? = null

    private lateinit var context: Context
    private lateinit var calculationManager: CalculationManager
    private lateinit var inputSpeechManager: InputSpeechManager
    private lateinit var audioManager: AudioManager

    private var contact: ContactModel = ContactModel()
    private var temp: TempDataManager =
        TempDataManager()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        skivvy = this.application as Skivvy
        context = this
        setTheme(skivvy.getThemeState())
        setContentView(R.layout.activity_main)
        setViewAndDefaults()
        loadDefaultAnimations()
        normalView()
        setListeners()
        outPut.text = getString(im_ready)
        input.text = getString(tap_the_button)
    }

    private fun setViewAndDefaults() {
        setting = findViewById(R.id.setting)
        settingBack = findViewById(R.id.setting_icon_back)
        outPut = findViewById(R.id.textOutput)
        input = findViewById(R.id.textInput)
        feedback = findViewById(R.id.feedbackOutput)
        loading = findViewById(R.id.loader)
        icon = findViewById(R.id.actionIcon)
        receiver = findViewById(R.id.receiverBtn)
        greet = findViewById(R.id.greeting)
        backfall = findViewById(R.id.backdrop)
        audioManager = applicationContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        calculationManager = CalculationManager(skivvy)
        inputSpeechManager = InputSpeechManager(resources,skivvy)
    }

    private fun loadDefaultAnimations() {
        fallDown = AnimationUtils.loadAnimation(context, R.anim.fall_back)
        backfall.startAnimation(fallDown)
        riseUp = AnimationUtils.loadAnimation(context, R.anim.rise_back)
        waveDamped = AnimationUtils.loadAnimation(context, R.anim.bubble_wave)
        receiver.startAnimation(waveDamped)
        greet.startAnimation(waveDamped)
        zoomInOutRotate = AnimationUtils.loadAnimation(context, R.anim.rotate_emerge_demerge)
        focusDeFocusRotate = AnimationUtils.loadAnimation(context, R.anim.rotate_focus)
        focusRotate = AnimationUtils.loadAnimation(context, R.anim.rotate_slow)
        fadeOnFadeOff = AnimationUtils.loadAnimation(context, R.anim.fade)
        zoomInRotate = AnimationUtils.loadAnimation(context, R.anim.rotate_exit)
        fadeOff = AnimationUtils.loadAnimation(context, R.anim.fade_off)
        fadeOn = AnimationUtils.loadAnimation(context, R.anim.fade_on)
        revolveRotateToLeft = AnimationUtils.loadAnimation(context, R.anim.pill_slide_left)
        setting.startAnimation(revolveRotateToLeft)
        rotateRevolveToRight = AnimationUtils.loadAnimation(context, R.anim.pill_slide_right)
        extendDown = AnimationUtils.loadAnimation(context, R.anim.extend_back)
        extendDownStartSetup = AnimationUtils.loadAnimation(context, R.anim.extend_back)
    }

    private fun startSettingAnimate() {
        setting.startAnimation(rotateRevolveToRight)
        backfall.startAnimation(extendDownStartSetup)
        greet.startAnimation(fadeOff)
        outPut.startAnimation(fadeOff)
        receiver.startAnimation(fadeOff)
        input.startAnimation(fadeOff)
        settingBack.startAnimation(fadeOff)
    }

    private fun startResumeAnimate() {
        greet.text = getString(app_name)
        greet.startAnimation(fadeOn)
        receiver.startAnimation(fadeOn)
        receiver.visibility = View.VISIBLE
        greet.startAnimation(waveDamped)
        receiver.startAnimation(waveDamped)
        setting.startAnimation(revolveRotateToLeft)
        backfall.startAnimation(riseUp)
        outPut.startAnimation(fadeOn)
        input.startAnimation(fadeOn)
        settingBack.startAnimation(fadeOn)
        loading.setImageDrawable(getDrawable(ic_dotsincircle))
        loading.startAnimation(zoomInOutRotate)
    }

    private fun setListeners() {
        callStateListener()
        setting.setOnClickListener {
            startSettingAnimate()
        }
        receiver.setOnClickListener {
            speakOut("")
            normalView()
            startVoiceRecIntent(skivvy.CODE_SPEECH_RECORD)
        }
        extendDownStartSetup.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationEnd(p0: Animation?) {
                startActivity(Intent(context, Setup::class.java))
                overridePendingTransition(R.anim.fade_on, R.anim.fade_off)
            }

            override fun onAnimationRepeat(p0: Animation?) {}
            override fun onAnimationStart(p0: Animation?) {}
        })
    }

    override fun onStart() {
        super.onStart()
        skivvy.tts = TextToSpeech(applicationContext, TextToSpeech.OnInitListener {
            when (it) {
                TextToSpeech.SUCCESS -> {
                    when (skivvy.tts!!.setLanguage(skivvy.locale)) {
                        TextToSpeech.LANG_MISSING_DATA,
                        TextToSpeech.LANG_NOT_SUPPORTED -> outPut.text = getString(
                            language_not_supported
                        )
                    }
                }
                else -> outPut.text = getString(output_error)
            }
        })
    }

    override fun onRestart() {
        super.onRestart()
        if (!skivvy.getTrainingStatus()) {
            startResumeAnimate()
        } else {
            trainingView()
        }
    }

    override fun onStop() {
        super.onStop()
        skivvy.setTrainingStatus(false)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            skivvy.CODE_ALL_PERMISSIONS -> {
            }
            skivvy.CODE_CALL_REQUEST -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    when {
                        contact.phoneList != null -> {
                            speakOut(
                                getString(should_i_call) + "${contact.displayName}?",
                                skivvy.CODE_CALL_CONF
                            )
                        }
                        temp.getPhone() != null -> {
                            speakOut(
                                getString(should_i_call) + "${temp.getPhone()}?",
                                skivvy.CODE_CALL_CONF
                            )
                        }
                        else -> {
                            speakOut(getString(null_variable_error))
                        }
                    }
                } else {
                    temp.setPhone(null)
                    errorView()
                    speakOut(getString(call_permit_denied))
                }
            }

            skivvy.CODE_SMS_REQUEST -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    when {
                        contact.phoneList != null -> {
                            speakOut(
                                getString(should_i_text) + "${contact.displayName}?",
                                skivvy.CODE_SMS_CONF
                            )
                        }
                        temp.getPhone() != null -> {
                            speakOut(
                                getString(should_i_text) + "${temp.getPhone()}?",
                                skivvy.CODE_SMS_CONF
                            )
                        }
                        else -> {
                            speakOut(getString(null_variable_error))
                        }
                    }
                } else {
                    temp.setPhone(null)
                    errorView()
                    speakOut(getString(sms_permit_denied))
                }
            }

            skivvy.CODE_STORAGE_REQUEST -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    takeScreenshot()
                } else {
                    errorView()
                    speakOut(getString(storage_permission_denied))
                }
            }
            skivvy.CODE_CONTACTS_REQUEST -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (temp.getContactReceived() != null && temp.getContactCode() != null) {
                        contactOps(temp.getContactReceived()!!, temp.getContactCode()!!)
                    } else {
                        speakOut(getString(null_variable_error))
                    }
                } else {
                    errorView()
                    speakOut(getString(contact_permission_denied))
                }
            }
        }
    }

    private fun setFeedback(text: String?) {
        feedback.text = text
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_BACK -> {
                startSettingAnimate()
                this.finishAffinity()
            }
            KeyEvent.KEYCODE_HEADSETHOOK -> {
                speakOut("")
                normalView()
                startVoiceRecIntent(skivvy.CODE_SPEECH_RECORD)
            }
            KeyEvent.KEYCODE_VOLUME_UP -> {
                setFeedback(
                    "Volume raised to ${(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) * 100)
                            / audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)}%"
                )
            }
            KeyEvent.KEYCODE_VOLUME_DOWN -> {
                setFeedback(
                    "Volume lowered to ${(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) * 100)
                            / audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)}%"
                )
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    private fun handleTrainingInput(input: String?) {
        speakOut("Okay")
        Log.d("training", "handled $input")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        var result: ArrayList<String> = ArrayList(1)
        if (!skivvy.getTrainingStatus()) {
            if (!skivvy.nonVocalRequestCodes.contains(requestCode)) {
                if (data == null || data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                        ?.get(0)
                        .toString()
                        .toLowerCase(skivvy.locale) == ""
                ) {
                    normalView()
                    speakOut(getString(no_input))
                    return
                } else {
                    result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)!!
                    input.text = result[0]
                }
            }
        }
        when (requestCode) {
            skivvy.CODE_TRAINING_MODE -> {
                if (data != null) {
                    result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)!!
                    input.text = result[0]
                    Log.d("training", result[0])
                    handleTrainingInput(result[0])
                } else {
                    Log.d("training", "null input")
                    handleTrainingInput(null)
                }
            }
            skivvy.CODE_SPEECH_RECORD -> {
                val temp = result[0].toLowerCase(skivvy.locale)
                if (txt == null || txt == "") {
                    txt = temp
                } else {
                    when (isCooperative(temp)) {
                        false -> {
                            speakOut(getString(okay))
                            normalView()
                        }
                        else -> {
                            if (temp.contains(txt!!)) {
                                txt = temp
                            } else {
                                txt += " $temp"
                            }
                        }
                    }
                }
                if (txt != null) {
                    input.text = txt
                    if (!respondToCommand(txt!!)) {
                        if (!directActions(txt!!)) {
                            if (!computerOps(txt!!)) {
                                if (!appOptions(txt)) {
                                    errorView()
                                    speakOut(getString(recognize_error))
                                }
                            }
                        }
                    }
                }
            }
            skivvy.CODE_VOICE_AUTH_CONFIRM -> {
                txt = result[0]
                    .toLowerCase(skivvy.locale)
                if (txt != skivvy.getVoiceKeyPhrase()) {
                    if (skivvy.getBiometricStatus()) {
                        speakOut("Vocal authentication failed. I need your physical verification")
                        authStateAction(skivvy.CODE_VOICE_AUTH_CONFIRM)
                        biometricPrompt.authenticate(promptInfo)
                    } else {
                        speakOut("Vocal authentication failed")
                    }
                } else {
                    skivvy.setPhraseKeyStatus(false)
                    speakOut("Vocal authentication disabled")
                }
            }
            skivvy.CODE_BIOMETRIC_CONFIRM -> {
                txt = result[0]
                    .toLowerCase(skivvy.locale)
                if (txt != skivvy.getVoiceKeyPhrase()) {
                    if (skivvy.getBiometricStatus()) {
                        speakOut("Vocal authentication failed. I need your physical verification")
                        authStateAction(skivvy.CODE_BIOMETRIC_CONFIRM)
                        biometricPrompt.authenticate(promptInfo)
                    } else {
                        speakOut("Vocal authentication failed")
                    }
                } else {
                    skivvy.setBiometricsStatus(false)
                    speakOut(getString(biometric_is_off))
                }
            }
            skivvy.CODE_VOLUME_CONFIRM -> {
                txt = result[0].toLowerCase(skivvy.locale)
                if (txt != null) {
                    when (isCooperative(txt!!)) {
                        null -> speakOut(
                            "Are you sure about the harmful ${temp.getVolumePercent()}% volume?",
                            skivvy.CODE_VOLUME_CONFIRM
                        )
                        true -> {
                            setVolume(temp.getVolumePercent())
                            speakOut("Volume at ${temp.getVolumePercent().toInt()}%")
                        }
                        false -> {
                            outPut.text = getString(okay)
                            volumeOps(getString(optimal_volume))
                        }
                    }
                }
            }
            skivvy.CODE_APP_CONF -> {
                txt = result[0]
                    .toLowerCase(skivvy.locale)
                val pData = skivvy.packageDataManager
                when (isCooperative(txt!!)) {
                    true -> {
                        successView(pData.getPackageIcon(temp.getPackageIndex()))
                        speakOut(
                            getString(opening) + pData.getPackageAppName(temp.getPackageIndex())!!
                                .capitalize(skivvy.locale)
                        )
                        startActivity(Intent(pData.getPackageIntent(temp.getPackageIndex())))
                    }
                    false -> {
                        normalView()
                        speakOut(getString(okay))
                    }
                    else -> {
                        speakOut(
                            getString(recognize_error) + "\n" + getString(do_u_want_open) + "${pData.getPackageAppName(
                                temp.getPackageIndex()
                            )!!.capitalize(skivvy.locale)}?",
                            skivvy.CODE_APP_CONF
                        )
                    }
                }
            }
            skivvy.CODE_LOCATION_SERVICE -> {
                val locationManager =
                    applicationContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    successView(getDrawable(ic_location_pointer))
                    speakOut(getString(gps_enabled))
                } else {
                    errorView()
                    speakOut(getString(gps_is_off))
                }
            }
            skivvy.CODE_DEVICE_ADMIN -> {
                if (resultCode == Activity.RESULT_OK) {
                    deviceLockOps()
                } else {
                    errorView()
                    speakOut(getString(device_admin_failure))
                }
            }
            skivvy.CODE_CALL_CONF -> {
                //val cdata = skivvy.contactDataManager
                txt = result[0]
                    .toLowerCase(skivvy.locale)
                when (isCooperative(txt!!)) {
                    true -> {
                        if (ActivityCompat.checkSelfPermission(
                                context,
                                Manifest.permission.CALL_PHONE
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            speakOut(getString(require_physical_permission))
                            ActivityCompat.requestPermissions(
                                this,
                                arrayOf(Manifest.permission.CALL_PHONE),
                                skivvy.CODE_CALL_REQUEST
                            )
                        } else {
                            if (temp.getContactPresence()) {
                                successView(null)
                                callingOps(
                                    contact.phoneList!![temp.getPhoneIndex()],
//                                        cdata.getContactNames()[temp.getContactIndex()]!!
                                    contact.displayName!!
                                )
                            } else {
                                successView(getDrawable(ic_glossyphone))
                                callingOps(temp.getPhone())
                                temp.setPhone(null)
                            }
                        }
                    }
                    false -> {
                        temp.setPhoneIndex(temp.getPhoneIndex() + 1)
                        if (temp.getContactPresence() && temp.getPhoneIndex() < contact.phoneList!!.size && !resources.getStringArray(
                                R.array.disruptions
                            ).contains(txt)
                        ) {
                            speakOut(
                                "At ${contact.phoneList!![temp.getPhoneIndex()]}?",
                                skivvy.CODE_CALL_CONF
                            )
                        } else {
                            normalView()
                            speakOut(getString(okay))
                        }
                    }
                    else -> {
                        if (temp.getContactPresence()) {
                            speakOut(
                                getString(recognize_error) + "\n" + getString(should_i_call) + "${contact.displayName} at ${contact.phoneList!![temp.getPhoneIndex()]}?",
                                skivvy.CODE_CALL_CONF
                            )
                        } else {
                            speakOut(
                                getString(recognize_error) + "\n" + getString(should_i_call) + "${temp.getPhone()}?",
                                skivvy.CODE_CALL_CONF
                            )
                        }
                    }
                }
            }

            skivvy.CODE_EMAIL_CONTENT -> {
                //val cdata = skivvy.contactDataManager
                txt = result[0]
                    .toLowerCase(skivvy.locale)
                when (isCooperative(txt!!)) {
                    false -> {
                        normalView()
                        speakOut(getString(okay))
                    }
                    else -> {
                        if (txt == null) {
                            if (temp.getEmailSubject() == null) {
                                speakOut(
                                    getString(recognize_error) + "\n" + getString(what_is_subject),
                                    skivvy.CODE_EMAIL_CONTENT
                                )
                            } else if (temp.getEmailBody() == null) {
                                speakOut(
                                    getString(recognize_error) + "\n" + getString(what_is_body),
                                    skivvy.CODE_EMAIL_CONTENT
                                )
                            }
                        } else {
                            if (temp.getEmailSubject() == null) {
                                temp.setEmailSubject(txt)
                                speakOut(
                                    getString(subject_added) + "\n" + getString(what_is_body),
                                    skivvy.CODE_EMAIL_CONTENT
                                )
                            } else if (temp.getEmailBody() == null) {
                                temp.setEmailBody(txt)
                                if (temp.getContactPresence()) {
                                    if (contact.emailList!!.size == 1) {
                                        speakOut(
                                            getString(body_added) + "\n" +
//                                                    getString(should_i_email) + "${cdata.getContactNames()[temp.getContactIndex()]} at\n${cdata.getContactEmails()[temp.getContactIndex()]!![temp.getEmailIndex()]}?",
                                                    getString(should_i_email) + "${contact.displayName} at\n${contact.emailList!![temp.getEmailIndex()]}?",
                                            skivvy.CODE_EMAIL_CONF
                                        )
                                    } else {
                                        speakOut(
//                                            getString(body_added) + "I've got ${cdata.getContactEmails()[temp.getContactIndex()]!!.size} addresses of ${cdata.getContactNames()[temp.getContactIndex()]}.\n" +
//                                                    getString(should_i_email) + "them at\n${cdata.getContactEmails()[temp.getContactIndex()]!![temp.getEmailIndex()]}?",
                                            getString(body_added) + "I've got ${contact.emailList!!.size} addresses of ${contact.displayName}.\n" +
                                                    getString(should_i_email) + "them at\n${contact.emailList!![temp.getEmailIndex()]}?",
                                            skivvy.CODE_EMAIL_CONF
                                        )
                                    }
                                } else {
                                    speakOut(
                                        getString(body_added) + "\n" +
                                                getString(should_i_email) + "${temp.getEmail()}?",
                                        skivvy.CODE_EMAIL_CONF
                                    )
                                }
                            }
                        }
                    }
                }
            }

            skivvy.CODE_EMAIL_CONF -> {
                //val cdata = skivvy.contactDataManager
                txt = result[0]
                    .toLowerCase(skivvy.locale)
                when (isCooperative(txt!!)) {
                    true -> {
                        successView(null)
                        speakOut(getString(preparing_email))
                        if (temp.getContactPresence()) {
                            emailingOps(
//                                    cdata.getContactEmails()[temp.getContactIndex()]!![temp.getEmailIndex()],
                                contact.emailList!![temp.getEmailIndex()],
                                temp.getEmailSubject(),
                                temp.getEmailBody()
                            )
                        } else {
                            emailingOps(
                                temp.getEmail(),
                                temp.getEmailSubject(),
                                temp.getEmailBody()
                            )
                        }
                    }
                    false -> {
                        temp.setEmailIndex(temp.getEmailIndex() + 1)
                        if (temp.getContactPresence() && temp.getEmailIndex() < contact.emailList!!.size && !resources.getStringArray(
                                R.array.disruptions
                            ).contains(txt)
                        ) {
                            speakOut(
                                "At ${contact.emailList!![temp.getEmailIndex()]}?",
                                skivvy.CODE_EMAIL_CONF
                            )
                        } else {
                            normalView()
                            speakOut(getString(okay))
                        }
                    }
                    else -> {
                        if (temp.getContactPresence()) {
                            speakOut(
                                getString(recognize_error) + "\n" +
//                                            getString(should_i_email) + "${cdata.getContactNames()[temp.getContactIndex()]} at\n${cdata.getContactEmails()[temp.getContactIndex()]!![temp.getEmailIndex()]}?",
                                        getString(should_i_email) + "${contact.displayName} at\n${contact.emailList!![temp.getEmailIndex()]}?",
                                skivvy.CODE_EMAIL_CONF
                            )
                        } else {
                            speakOut(
                                getString(recognize_error) + "\n" +
                                        getString(should_i_email) + "${temp.getEmail()}?",
                                skivvy.CODE_EMAIL_CONF
                            )
                        }
                    }
                }
            }

            skivvy.CODE_TEXT_MESSAGE_BODY -> {
                //val cdata = skivvy.contactDataManager
                txt = result[0]
                    .toLowerCase(skivvy.locale)
                when (isCooperative(txt!!)) {
                    false -> {
                        normalView()
                        speakOut(getString(okay))
                    }
                    else -> {
                        if (txt != null) {
                            waitingView(null)
                            temp.setTextBody(txt)
                            if (temp.getContactPresence()) {
                                speakOut(
//                                  getString(should_i_text) + "${cdata.getContactNames()[temp.getContactIndex()]} at ${cdata.getContactPhones()[temp.getContactIndex()]!![temp.getPhoneIndex()]}" + getString( via_sms),
                                    getString(should_i_text) + "${contact.displayName} at ${contact.phoneList!![temp.getPhoneIndex()]}" + getString(
                                        via_sms
                                    ),
                                    skivvy.CODE_SMS_CONF
                                )
                            } else {
                                speakOut(
                                    getString(should_i_text) + "${temp.getPhone()}" + getString(
                                        via_sms
                                    ),
                                    skivvy.CODE_SMS_CONF
                                )
                            }
                        } else {
                            speakOut(
                                getString(recognize_error) + getString(what_is_message),
                                skivvy.CODE_TEXT_MESSAGE_BODY
                            )
                        }
                    }
                }
            }

            skivvy.CODE_SMS_CONF -> {
                //val cdata = skivvy.contactDataManager
                txt = result[0]
                    .toLowerCase(skivvy.locale)
                when (isCooperative(txt!!)) {
                    true -> {
                        if (ActivityCompat.checkSelfPermission(
                                this,
                                Manifest.permission.SEND_SMS
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            speakOut(getString(require_physical_permission))
                            ActivityCompat.requestPermissions(
                                this,
                                arrayOf(Manifest.permission.SEND_SMS),
                                skivvy.CODE_SMS_REQUEST
                            )
                        } else {
                            if (temp.getContactPresence()) {
                                speakOut(getString(sending_sms_at) + "${contact.phoneList!![temp.getPhoneIndex()]}")
                                textMessageOps(
                                    contact.phoneList!![temp.getPhoneIndex()]!!,
                                    temp.getTextBody()!!,
                                    skivvy.CODE_SMS_CONF
                                )
                            } else if (temp.getPhone() != null) {
                                speakOut(getString(sending_sms_at) + "${temp.getPhone()}")
                                textMessageOps(
                                    temp.getPhone()!!,
                                    temp.getTextBody()!!,
                                    skivvy.CODE_SMS_CONF
                                )
                            }
                        }
                    }
                    false -> {
                        temp.setPhoneIndex(temp.getPhoneIndex() + 1)
                        // if (temp.getContactPresence() && temp.getPhoneIndex() < cdata.getContactPhones()[temp.getContactIndex()]!!.size && !resources.getStringArray(
                        if (temp.getContactPresence() && temp.getPhoneIndex() < contact.phoneList!!.size && !resources.getStringArray(
                                R.array.disruptions
                            ).contains(txt)
                        ) {
                            speakOut(
                                //                                 "At ${cdata.getContactPhones()[temp.getContactIndex()]!![temp.getPhoneIndex()]}?",
                                "At ${contact.phoneList!![temp.getPhoneIndex()]}?",
                                skivvy.CODE_SMS_CONF
                            )
                        } else {
                            normalView()
                            speakOut(getString(okay))
                        }
                    }
                    else -> {
                        if (temp.getContactPresence()) {
                            speakOut(
//                                getString(should_i_text) + "${cdata.getContactNames()[temp.getContactIndex()]} at ${cdata.getContactPhones()[temp.getContactIndex()]!![temp.getPhoneIndex()]}" + getString( via_sms),
                                getString(should_i_text) + "${contact.displayName} at ${contact.phoneList!![temp.getPhoneIndex()]}" + getString(
                                    via_sms
                                ),
                                skivvy.CODE_SMS_CONF
                            )
                        } else {
                            speakOut(
                                getString(should_i_text) + "${temp.getPhone()}" + getString(
                                    via_sms
                                ),
                                skivvy.CODE_SMS_CONF
                            )
                        }
                    }
                }
            }
            skivvy.CODE_WHATSAPP_ACTION -> {
                val pData = skivvy.packageDataManager
                txt = result[0]
                    .toLowerCase(skivvy.locale)
                if (txt != "") {
                    try {
                        if (Intent(Intent.ACTION_VIEW).setPackage("com.whatsapp")
                                .resolveActivity(context.packageManager) != null
                        ) {
                            speakOut("Sending '$txt' on whatsapp to ${contact.displayName}")
                            context.startActivity(
                                Intent(
                                    Intent.ACTION_VIEW, Uri.parse(
                                        "https://api.whatsapp.com/send?phone="
                                                + contact.phoneList!![temp.getPhoneIndex()] + "&text=" + URLEncoder.encode(
                                            txt,
                                            "UTF-8"
                                        )
                                    )
                                )
                            )
                        }
                    } catch (e: java.lang.Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    //TODO: extend this to other types of responses too.
    private fun isCooperative(response: String): Boolean? {
        return when {
            inputSpeechManager.containsDisagreement(inputSpeechManager.removeBeforePositive(response)) ->
                false
            inputSpeechManager.containsAgreement(inputSpeechManager.removeBeforeNegative(response)) ->
                true
            inputSpeechManager.containsDisagreement(response) ->
                false
            inputSpeechManager.containsAgreement(response) ->
                true
            else -> null
        }
    }

    //actions invoking quick commands
    @ExperimentalStdlibApi
    private fun respondToCommand(text: String): Boolean {
        val array = arrayOf(
            R.array.setup_list,
            R.array.bt_list,
            R.array.wifi_list,
            R.array.gps_list,
            R.array.lock_list,
            R.array.snap_list
        )
        when {
            text.contains("training mode") -> {
                if (text == "activate training mode") {
                    if (!skivvy.getTrainingStatus()) {
                        skivvy.setTrainingStatus(true)
                        speakOut("Activating training mode", skivvy.CODE_TRAINING_MODE)
                        //trainingView()
                        //recreate()
                    } else {
                        speakOut("Already learning")
                    }
                } else {
                    if (text.contains("deactivate")) {
                        if (skivvy.getTrainingStatus()) {
                            skivvy.setTrainingStatus(false)
                            speakOut("Deactivating training mode")
                            normalView(true)
                            //startResumeAnimate()
                            //recreate()
                        } else {
                            speakOut("Already inactive")
                        }
                    }
                }
            }

            text.contains("setup") || text.contains("set up") -> {
                startActivity(Intent(context, Setup::class.java))
            }
            text.contains("bluetooth") -> {
                bluetoothOps(text)
            }
            text.contains("wi-fi") || text.contains("wifi") -> {
                waitingView(getDrawable(ic_wifi_connected))
                wifiOps(text)
            }
            resources.getStringArray(array[3]).contains(text) -> {
                waitingView(getDrawable(ic_location_pointer))
                startActivityForResult(
                    Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS),
                    skivvy.CODE_LOCATION_SERVICE
                )
            }
            text.contains("lock") || text.contains("loc") -> {
                if (text.contains("screen") || text.contains("device") || text.contains("phone")) {
                    deviceLockOps()
                } else {
                    if (text.replace("lock", "").trim() == "") {
                        txt = text
                        speakOut("Lock what?", skivvy.CODE_SPEECH_RECORD)
                    } else return false
                }
            }
            text.contains("screenshot") || text.contains("snapshot") || text.contains("take ss") -> {
                if (ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    speakOut(getString(require_physical_permission))
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        skivvy.CODE_STORAGE_REQUEST
                    )
                } else {
                    takeScreenshot()
                }
            }
            text.contains("airplane") -> {
                if (isAirplaneModeEnabled()) {
                    if (text.contains("on") || text.contains("enable")) {
                        speakOut("Already on airplane mode")
                    } else {
                        speakOut("Airplane mode on")
                        setAirplaneMode(false)
                    }
                } else {
                    if (text.contains("off") || text.contains("disable")) {
                        speakOut("Airplane mode already off")
                    } else {
                        speakOut("Airplane mode off")
                        setAirplaneMode(true)
                    }
                }
            }
            text.contains("flash") -> {
                if (isFlashAvailable()) {
                    if (text.contains("off")) {
                        speakOut("Flashlight is off")
                        setFlashLight(false)
                    } else {
                        speakOut("Flashlight is on")
                        setFlashLight(true)
                    }
                } else {
                    speakOut("Flashlight not available")
                }
            }
            text.contains("search") -> {
                var query = text.replace("search for", "").trim()
                query = query.replace("search", "").trim()
                if (query != "") {
                    speakOut("Searching for $query via Google")
                    startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://google.com/#q=$query")
                        )
                    )
                } else {
                    if (text.replace("search for", "").trim() == "" || text.replace(
                            "search",
                            ""
                        )
                            .trim() == ""
                    ) {
                        txt = text
                        speakOut("Search for what?", skivvy.CODE_SPEECH_RECORD)
                    } else return false
                }
            }
            text.contains("volume") -> {
                volumeOps(text.replace("volume", "").trim())
            }
            text == "mute" -> {
                skivvy.saveMuteStatus(true)
                speakOut("Muted")
            }
            text == "speak" || text == "unmute" -> {
                if (skivvy.getMuteStatus()) {
                    skivvy.saveMuteStatus(false)
                    speakOut(getString(okay))
                } else {
                    speakOut(getString(voice_output_on))
                }
            }
            text == getString(exit) -> {
                finish()
            }
            text.contains("voice authentication") -> {
                return when {
                    text.contains("enable") && text.contains("disable") -> {
                        txt = text.replace("enable", "")
                        txt = txt!!.replace("disable", "")
                        speakOut(getString(enable_or_disable), skivvy.CODE_SPEECH_RECORD)
                        true
                    }
                    text.contains("enable") -> {
                        if (!skivvy.getPhraseKeyStatus()) {
                            if (skivvy.getVoiceKeyPhrase() != null) {
                                skivvy.setPhraseKeyStatus(true)
                                speakOut("Vocal authentication enabled")
                            } else {
                                speakOut("You need to physically set up vocal authentication")
                                startActivity(Intent(context, Setup::class.java))
                            }
                        } else {
                            speakOut("Vocal authentication already enabled")
                        }
                        true
                    }
                    text.contains("disable") -> {
                        if (!skivvy.getPhraseKeyStatus()) {
                            speakOut("Vocal authentication already disabled")
                        } else {
                            speakOut(
                                "Tell me your secret passphrase",
                                skivvy.CODE_VOICE_AUTH_CONFIRM
                            )
                        }
                        true
                    }
                    else -> {
                        if (text.replace("voice authentication", "").trim() == "") {
                            txt = text
                            speakOut("Vocal authentication what?", skivvy.CODE_SPEECH_RECORD)
                            true
                        } else false
                    }
                }
            }
            text.contains("biometric") -> {
                if (!skivvy.checkBioMetrics()) {
                    speakOut("Your device doesn't support biometric authentication.")
                } else {
                    return when {
                        text.contains("enable") && text.contains("disable") -> {
                            txt = text.replace("enable", "")
                            txt = txt!!.replace("disable", "")
                            speakOut(getString(enable_or_disable), skivvy.CODE_SPEECH_RECORD)
                            true
                        }
                        text.contains("enable") -> {
                            if (skivvy.getBiometricStatus()) {
                                speakOut(getString(biometric_already_on))
                            } else {
                                skivvy.setBiometricsStatus(true)
                                if (skivvy.getBiometricStatus()) speakOut(getString(biometric_on))
                                else speakOut(getString(biometric_enable_error))
                            }
                            true
                        }
                        text.contains("disable") -> {
                            if (!skivvy.getBiometricStatus()) {
                                speakOut(getString(biometric_already_off))
                            } else {
                                if (skivvy.getPhraseKeyStatus()) {
                                    speakOut(
                                        "Tell me your secret passphrase",
                                        skivvy.CODE_BIOMETRIC_CONFIRM
                                    )
                                } else {
                                    speakOut(getString(physical_auth_request))
                                    authStateAction(skivvy.CODE_BIOMETRIC_CONFIRM)
                                    biometricPrompt.authenticate(promptInfo)
                                }
                            }
                            true
                        }
                        else -> {
                            if (text.replace("biometric", "").trim() == "") {
                                txt = text
                                speakOut("Biometric what?", skivvy.CODE_SPEECH_RECORD)
                                true
                            } else false
                        }
                    }
                }
            }
            /*
            text.contains("uninstall") -> {
                if (text.replace("uninstall", "").trim().isNullOrBlank()) {
                    txt = text
                    speakOut("Uninstall what?", skivvy.CODE_SPEECH_RECORD)
                    return true
                }
                deviceManager =
                    applicationContext.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
                compName = ComponentName(context, Administrator::class.java)
                if (!deviceManager!!.isAdminActive(compName!!)) {
                    waitingView(null)
                    speakOut(getString(device_admin_request))
                    startActivityForResult(
                        Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
                            .putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, compName)
                            .putExtra(
                                DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                                getString(device_admin_persuation)
                            ), skivvy.CODE_DEVICE_ADMIN
                    )
                } else {
                    val pData = skivvy.packageDataManager
                    var i = 0
                    while (i < pData.getTotalPackages()) {
                        if (pData.getPackageAppName(i)!!
                                .toLowerCase(skivvy.locale) == text.replace("uninstall", "").trim()
                        ) {
                            waitingView(pData.getPackageIcon(i))
                            val intentSender = PendingIntent.getBroadcast(
                                this,
                                101,
                                Intent(ACTION_UNINSTALL_RESULT),
                                0
                            ).intentSender
                            val pi = packageManager.packageInstaller
                            pi.uninstall(pData.getPackageName(i)!!, intentSender)
                            return true
                        }
                        ++i
                    }
                }
                return false
            }
             */
            text == "get permission" -> {
                if (!skivvy.hasPermissions(context)) {
                    ActivityCompat.requestPermissions(
                        this, skivvy.permissions,
                        skivvy.CODE_ALL_PERMISSIONS
                    )
                } else {
                    speakOut(getString(have_all_permits))
                }
            }
            else -> {
                return false
            }
        }
        return true
    }

    /*
    private val ACTION_UNINSTALL_RESULT = "uninstall"
    private val receiverBroad = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, intent: Intent?) {
            if (intent?.action == ACTION_UNINSTALL_RESULT) {
                Log.d(
                    "NIGGA",
                    "requested to uninstall " + intent.getStringExtra(PackageInstaller.EXTRA_PACKAGE_NAME)
                            + "(result=" + intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE) + ")"
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        registerReceiver(receiverBroad, IntentFilter(ACTION_UNINSTALL_RESULT))
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(receiverBroad)
    }
     */
    private fun computerOps(rawExpression: String): Boolean {
        val expression = calculationManager.expressionize(rawExpression)

        /**
         * Storing availability of all operators and functions in given expression,
         * to arrays of booleans as true.
         */
        val operatorsAndFunctionsArray = arrayOf(skivvy.operators, skivvy.mathFunctions)
        val operatorsAndFunctionsBoolean = arrayOf(
            arrayOfNulls<Boolean>(operatorsAndFunctionsArray[0].size),
            arrayOfNulls(operatorsAndFunctionsArray[1].size)
        )
        var of = 0
        while (of < operatorsAndFunctionsBoolean.size) {
            var f = 0
            while (f < operatorsAndFunctionsBoolean[of].size) {
                operatorsAndFunctionsBoolean[of][f] =
                    expression.contains(operatorsAndFunctionsArray[of][f])
                ++f
            }
            ++of
        }
        if (!operatorsAndFunctionsBoolean[0].contains(true)) {     //if no operators
            if (operatorsAndFunctionsBoolean[1].contains(true)) {       //has a mathematical function
                if (expression.contains(skivvy.numberPattern)) {
                    setFeedback(expression)
                    saveCalculationResult(calculationManager.functionOperate(expression)!!)
                    speakOut(calculationManager.formatToProperValue(getLastCalculationResult()!!))
                    return true
                }
            }
            return false
        }

        val totalOps = calculationManager.totalOperatorsInExpression(expression)
        if (totalOps == 0 || !calculationManager.isExpressionOperatable(expression) || calculationManager.segmentizeExpression(
                expression,
                2 * totalOps + 1
            ) == null
        )
            return false

        var arrayOfExpression =
            calculationManager.segmentizeExpression(expression, 2 * totalOps + 1)!!

        var l = 0
        var k = 0
        while (l < arrayOfExpression.size && k < arrayOfExpression.size) {  //operator in place validity check
            if (arrayOfExpression[l] != null && arrayOfExpression[k] != null) {
                if (arrayOfExpression[k]!!.contains(skivvy.nonNumeralPattern)
                    && !arrayOfExpression[k]!!.contains(".")        //if decimal
                    && !operatorsAndFunctionsBoolean[1].contains(true)
                ) {
                    return false
                }
            } else return false
            ++l
            k += 2
        }

        var midOutput = String()
        var bb = 0
        while (bb < arrayOfExpression.size) {
            midOutput += arrayOfExpression[bb]
            ++bb
        }
        setFeedback(midOutput)      //segmentized expression to user

        if (operatorsAndFunctionsBoolean[1].contains(true)) {      //If expression has mathematical functions
            val temp =
                calculationManager.evaluateFunctionsInSegmentedArrayOfExpression(arrayOfExpression)
            if (temp == null) {
                return false
            } else {
                arrayOfExpression = temp
            }
        }

        if (!calculationManager.isExpressionArrayOnlyNumbersAndOperators(arrayOfExpression))     //if array contains invalid values
            return false
        else {
            saveCalculationResult(calculationManager.expressionCalculation(arrayOfExpression))
            speakOut(getLastCalculationResult()!!)
        }
        return true
    }

    private fun bluetoothOps(text: String) {
        val mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (text.contains("on")) {
            if (mBluetoothAdapter.isEnabled) {
                successView(getDrawable(ic_bluetooth))
                speakOut(getString(bt_on))
            } else {
                mBluetoothAdapter.enable()
                successView(getDrawable(ic_bluetooth))
                speakOut(getString(bt_on))
            }
        } else {
            if (mBluetoothAdapter.isEnabled) {
                mBluetoothAdapter.disable()
                speakOut(getString(bt_off))
            } else {
                mBluetoothAdapter.enable()
                successView(getDrawable(ic_bluetooth))
                speakOut(getString(bt_on))
            }
        }
    }

    private fun wifiOps(text: String) {
        val wifiManager: WifiManager =
            applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        if (text.contains("on") || text.contains("enable")) {
            if (!wifiManager.isWifiEnabled) {
                wifiManager.isWifiEnabled = true
                successView(getDrawable(ic_wifi_connected))
                speakOut(getString(wifi_on))
            } else {
                successView(getDrawable(ic_wifi_connected))
                speakOut(getString(wifi_on))
            }
        } else {
            if (wifiManager.isWifiEnabled) {
                wifiManager.isWifiEnabled = false
                speakOut(getString(wifi_off))
            } else {
                successView(getDrawable(ic_wifi_connected))
                wifiManager.isWifiEnabled = true
                speakOut(getString(wifi_on))
            }
        }
    }

    private fun deviceLockOps() {
        if (skivvy.deviceManager.isAdminActive(skivvy.compName)) {
            successView(getDrawable(ic_glossylock))
            speakOut(getString(screen_locked))
            skivvy.deviceManager.lockNow()
        } else {
            waitingView(getDrawable(ic_glossylock))
            speakOut(getString(device_admin_request))
            startActivityForResult(
                Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
                    .putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, skivvy.compName)
                    .putExtra(
                        DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                        getString(device_admin_persuation)
                    ), skivvy.CODE_DEVICE_ADMIN
            )
        }
    }

    private fun setVolume(percent: Float, feedback: Boolean = false) {
        if (feedback) setFeedback("Volume at ${percent.toInt()}%")
        audioManager.setStreamVolume(
            AudioManager.STREAM_MUSIC,
            (audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) * (percent / 100)).toInt(),
            AudioManager.FLAG_SHOW_UI
        )
    }

    private fun volumeOps(action: String) {
        when {
            action.contains(skivvy.numberPattern) -> {
                val percent = action.replace(skivvy.nonNumeralPattern, "").toFloat()
                if (percent > 100F) {
                    speakOut("Invalid volume level.")
                    return
                }
                if (percent > 75F) {
                    setVolume(40F, feedback = true)
                    temp.setVolumePercent(percent)
                    speakOut(
                        "Volume at this level might cause harm to you. Are you sure about ${percent.toInt()}%?",
                        skivvy.CODE_VOLUME_CONFIRM,
                        true
                    )
                } else {
                    setVolume(percent, feedback = true)
                    speakOut(
                        "Volume at ${percent.toInt()}%", null,
                        parallelReceiver = false,
                        isFeedback = true
                    )
                }
            }
            action.contains("up") || action.contains("increase") ||
                    action.contains("raise") -> {
                if (audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) ==
                    audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                ) {
                    speakOut("Maximum volume achieved")
                } else {
                    audioManager.adjustStreamVolume(
                        AudioManager.STREAM_MUSIC,
                        AudioManager.ADJUST_RAISE,
                        AudioManager.FLAG_SHOW_UI
                    )
                    speakOut(
                        "Volume increased",
                        null,
                        parallelReceiver = false,
                        isFeedback = true
                    )
                }
            }
            action.contains("down") || action.contains("decrease") -> {
                if (
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) ==
                                audioManager.getStreamMinVolume(AudioManager.STREAM_MUSIC)
                    } else {
                        audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) == 0
                    }
                ) {
                    speakOut("Minimum volume achieved")
                } else {
                    audioManager.adjustStreamVolume(
                        AudioManager.STREAM_MUSIC,
                        AudioManager.ADJUST_LOWER,
                        AudioManager.FLAG_SHOW_UI
                    )
                    speakOut(
                        "Volume decreased",
                        null,
                        isFeedback = true
                    )
                }
            }
            action.contains("max") ||
                    action.contains("full") || action.contains("highest") -> {
                audioManager.setStreamVolume(
                    AudioManager.STREAM_MUSIC,
                    audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC),
                    AudioManager.FLAG_SHOW_UI
                )
                speakOut("Maximum volume achieved")
            }
            action.contains("min") || action.contains("lowest") -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    audioManager.setStreamVolume(
                        AudioManager.STREAM_MUSIC,
                        audioManager.getStreamMinVolume(AudioManager.STREAM_MUSIC),
                        AudioManager.FLAG_SHOW_UI
                    )
                    audioManager.adjustStreamVolume(
                        AudioManager.STREAM_MUSIC,
                        AudioManager.ADJUST_RAISE,
                        AudioManager.FLAG_SHOW_UI
                    )
                    speakOut("Minimum audible volume achieved")
                } else {
                    volumeOps("10%")
                }
            }
            action.contains("silence") || action.contains("zero") -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    audioManager.setStreamVolume(
                        AudioManager.STREAM_MUSIC,
                        audioManager.getStreamMinVolume(AudioManager.STREAM_MUSIC),
                        AudioManager.FLAG_SHOW_UI
                    )
                    speakOut("Volume silenced")
                } else {
                    volumeOps("0%")
                }
            }
            else -> {
                audioManager.adjustStreamVolume(
                    AudioManager.STREAM_MUSIC,
                    AudioManager.ADJUST_SAME,
                    AudioManager.FLAG_SHOW_UI
                )
                speakOut("I'm showing you the volume controls.")
            }
        }
    }

    private fun isThereAnyAppNamed(name: String, startFrom: Int = 0): Boolean {
        val pData = skivvy.packageDataManager
        var index = startFrom
        while (index < pData.getTotalPackages()) {
            if (name == pData.getPackageAppName(index)) {
                temp.setPackageIndex(index)
                return true
            }
            ++index
        }
        return false
    }

    //TODO: specific app actions
    //actions invoking other applications
    private fun appOptions(text: String?): Boolean {
        var localText: String
        val pData = skivvy.packageDataManager
        if (text != null) {
            localText = text.replace("open", "").trim()
            if (localText == "") {
                speakOut("Open what?", skivvy.CODE_SPEECH_RECORD)
                return true
            }
            localText = localText.replace("start", "").trim()
            if (localText == "") {
                speakOut("Start what?", skivvy.CODE_SPEECH_RECORD)
                return true
            }
            if (pData.getTotalPackages() > 0) {
                var i = 0
                while (i < pData.getTotalPackages()) {
                    when {
                        localText == getString(app_name).toLowerCase(skivvy.locale) -> {
                            normalView()
                            speakOut(getString(i_am) + getString(app_name))
                            return true
                        }
                        localText == pData.getPackageAppName(i) -> {
                            temp.setPackageIndex(i)
                            successView(pData.getPackageIcon(i))
                            speakOut(
                                getString(opening) + pData.getPackageAppName(i)!!
                                    .capitalize(skivvy.locale)
                            )
                            startActivity(Intent(pData.getPackageIntent(i)))
                            return true
                        }
                        pData.getPackageName(i)!!.contains(localText) -> {
                            temp.setPackageIndex(i)
                            waitingView(pData.getPackageIcon(i))
                            speakOut(
                                getString(do_u_want_open) + "${pData.getPackageAppName(i)!!
                                    .capitalize(skivvy.locale)}?",
                                skivvy.CODE_APP_CONF
                            )
                            return true
                        }
                        else -> ++i
                    }
                }
            } else {
                speakOut("No applications installed on your device")
            }
        } else speakOut(getString(null_variable_error))
        return false
    }

    //action invoking direct intents
    private fun directActions(text: String): Boolean {
        var localTxt: String
        val continuationOperators: Array<String> =
            arrayOf("add", "plus", "+", "-", "minus", "subtract", "divide", "multiply", "/", "x")
        when {
            continuationOperators.contains(
                text.substringBefore(" ").toLowerCase(skivvy.locale)
            ) -> {
                if (text.contains(skivvy.numberPattern)) {
                    computerOps(getLastCalculationResult() + text)
                    //TODO: continued calculation
                    //        startVoiceRecIntent(skivvy.CODE_SPEECH_RECORD,"Continue calculation")
                } else return false
            }
            text.contains(getString(call)) -> {
                waitingView(getDrawable(ic_glossyphone))
                localTxt = text.replace(getString(call), "", true).trim()
                temp.setPhone(text.replace(skivvy.nonNumeralPattern, ""))
                if (temp.getPhone() != null) {
                    when {
                        temp.getPhone()!!.contains(skivvy.numberPattern) -> {
                            if (temp.getPhone()!!.length == 10) {
                                val local = temp.getPhone()
                                temp.setPhone("")
                                var k = 0
                                while (k < 10) {
                                    temp.setPhone(temp.getPhone() + local!![k])
                                    if (k == 4) {
                                        temp.setPhone(temp.getPhone() + " ")
                                    }
                                    ++k
                                }
                            }
                            if (ActivityCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.CALL_PHONE
                                ) != PackageManager.PERMISSION_GRANTED
                            ) {
                                speakOut(getString(require_physical_permission))
                                ActivityCompat.requestPermissions(
                                    this,
                                    arrayOf(Manifest.permission.CALL_PHONE),
                                    skivvy.CODE_CALL_REQUEST
                                )
                            } else {
                                speakOut(
                                    getString(should_i_call) + "${temp.getPhone()}?",
                                    skivvy.CODE_CALL_CONF
                                )
                            }
                        }
                        else -> {
                            if (localTxt.length > 1) {
                                contactOps(localTxt, skivvy.CODE_CALL_CONF)
                            } else {
                                txt = "call "
                                speakOut("Call who?", skivvy.CODE_SPEECH_RECORD)
                            }
                        }
                    }
                } else {
                    txt = "call "
                    speakOut("Call who?", skivvy.CODE_SPEECH_RECORD)
                }
            }
            text.contains(getString(email)) -> {
                waitingView(getDrawable(ic_email_envelope))
                localTxt = text.replace(getString(email), "", true).trim()
                temp.setEmail(localTxt.replace(" ", "").trim())
                when {
                    temp.getEmail()!!.matches(skivvy.emailPattern) -> {
                        input.text = temp.getEmail()
                        speakOut(getString(what_is_subject), skivvy.CODE_EMAIL_CONTENT)
                    }
                    localTxt.length > 1 -> {
                        contactOps(localTxt, skivvy.CODE_EMAIL_CONF)
                    }
                    else -> {
                        txt = "email "
                        speakOut("Email who?", skivvy.CODE_SPEECH_RECORD)
                    }
                }
            }
            text.contains(getString(R.string.text)) -> {
                waitingView(getDrawable(ic_messageicon))
                localTxt = text.replace(getString(R.string.text), "", false)
                localTxt = localTxt.trim()
                temp.setPhone(localTxt.replace(skivvy.nonNumeralPattern, ""))
                when {
                    temp.getPhone()!!.contains(skivvy.numberPattern) -> {
                        speakOut(getString(what_is_message), skivvy.CODE_TEXT_MESSAGE_BODY)
                    }
                    localTxt.contains("whatsapp") -> {
                        localTxt = localTxt.replace("whatsapp", "")
                        localTxt = localTxt.replace("via", "")
                        localTxt = localTxt.replace("on", "")
                        localTxt.replace(" ", "")
                        contactOps(localTxt, skivvy.CODE_WHATSAPP_ACTION)
                    }
                    localTxt.length > 1 -> {
                        contactOps(localTxt, skivvy.CODE_SMS_CONF)
                    }
                    else -> {
                        txt = text
                        speakOut("Text who?", skivvy.CODE_SPEECH_RECORD)
                    }
                }
            }
            else -> return false
        }
        return true
    }

    private fun textMessageOps(target: String, payLoad: String, code: Int) {
        if (code == skivvy.CODE_SMS_CONF) {
            try {
                successView(null)
                val sms: SmsManager = SmsManager.getDefault()
                sms.sendTextMessage(target, null, payLoad, null, null)
            } catch (e: Exception) {
                Log.d("BITCH", e.toString())
                speakOut("Failed to send SMS")
            }
        } else {
            speakOut("Not yet supported")
        }
    }

    @SuppressLint("MissingPermission")
    private fun callingOps(number: String?, name: String? = null) {
        if (number != null) {
            name?.let { speakOut(getString(calling) + name) }
            startActivity(Intent(Intent.ACTION_CALL, Uri.parse("tel:$number")))
        } else {
            errorView()
            speakOut(getString(null_variable_error))
        }
    }

    private fun emailingOps(address: String?, subject: String?, body: String?) {
        startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("mailto:$address?subject=$subject&body=$body")
            )
        )
    }

    //TODO: Either create contact list data class and use that here, or do direct lookup faster in background.
    private fun contactOps(name: String, code: Int) {
        temp.setContactCode(code)
        temp.setPhoneIndex(0)
        temp.setEmailIndex(0)
        temp.setContactReceived(name.trim())
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_CONTACTS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            speakOut(getString(require_physical_permission))
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_CONTACTS),
                skivvy.CODE_CONTACTS_REQUEST
            )
        } else {
            /*
            val cd = skivvy.contactDataManager
            if (cd.getTotalContacts() == 0) {
                errorView()
                speakOut(getString(no_contacts_available))
            } else {
                var contactIndex = 0
                while (contactIndex < cd.getTotalContacts()) {
                    if(cd.getContactIDs()[contactIndex].isNullOrEmpty()){
                        speakOut("Please wait",skivvy.CODE_SPEECH_RECORD)
                        normalView()
                    } else if (temp.getContactReceived() == cd.getContactNames()[contactIndex]!!.toLowerCase(skivvy.locale) ||
                        temp.getContactReceived() == cd.getContactNames()[contactIndex]!!.substringBefore(" ").toLowerCase(skivvy.locale)||
                        !cd.getContactNicknames()[contactIndex].isNullOrEmpty()&&cd.getContactNicknames()[contactIndex]!!.contains(temp.getContactReceived())
                    ) {
                        temp.setContactIndex(contactIndex)
                        temp.setContactPresence(true)
                        if (cd.getContactPhotoUris()[contactIndex] != null) {
                            val rb: RoundedBitmapDrawable =
                                RoundedBitmapDrawableFactory.create(resources, MediaStore.Images.Media.getBitmap(
                                    context.contentResolver,
                                    Uri.parse(cd.getContactPhotoUris()[contactIndex])
                                ))
                            rb.isCircular = true
                            rb.setAntiAlias(true)
                            waitingView(rb)
                        } else waitingView(null)
                        if (temp.getContactCode() == skivvy.CODE_EMAIL_CONF) {
                            if (!cd.getContactEmails()[contactIndex].isNullOrEmpty()) {
                                speakOut(getString(what_is_subject), skivvy.CODE_EMAIL_CONTENT)
                            } else {
                                errorView()
                                speakOut(
                                    getString(you_dont_seem_having) + cd.getContactNames()[contactIndex] + getString(
                                        someone_email_address
                                    )
                                )
                            }
                        } else if (!cd.getContactPhones()[contactIndex].isNullOrEmpty()) {
                            when (code) {
                                skivvy.CODE_CALL_CONF -> {
                                    if (cd.getContactPhones()[contactIndex]!!.size == 1) {
                                        speakOut(
                                            getString(should_i_call) + "${cd.getContactNames()[contactIndex]}?",
                                            skivvy.CODE_CALL_CONF
                                        )
                                    } else {
                                        speakOut(
                                            "I've got ${cd.getContactPhones()[contactIndex]!!.size} phone numbers of ${cd.getContactNames()[contactIndex]}.\nShould I call them at " +
                                                    "${cd.getContactPhones()[contactIndex]?.get(
                                                        temp.getPhoneIndex()
                                                    )}?",
                                            skivvy.CODE_CALL_CONF
                                        )
                                    }
                                }
                                skivvy.CODE_SMS_CONF -> {
                                    speakOut(
                                        getString(what_is_message),
                                        skivvy.CODE_TEXT_MESSAGE_BODY
                                    )
                                }
                            }
                        } else if (cd.getContactPhones()[contactIndex].isNullOrEmpty()) {
                            errorView()
                            speakOut(
                                getString(you_dont_seem_having) + cd.getContactNames()[contactIndex] + getString(
                                    someones_phone_number
                                )
                            )
                        }
                    }
                    ++contactIndex
                }
                if (!temp.getContactPresence()) {
                    errorView()
                    speakOut(getString(contact_not_found))
                }
            }
        }
        */
            val cr: ContentResolver = contentResolver
            val cur: Cursor? =
                cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null)
            if (cur?.count!! > 0) {
                while (cur.moveToNext()) {
                    contact.contactID =
                        cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID))
                    contact.displayName =
                        cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
                    val nickCur: Cursor? = cr.query(        //for nicknames
                        ContactsContract.Data.CONTENT_URI,
                        null,
                        ContactsContract.Data.CONTACT_ID + " = ? AND " + ContactsContract.Data.MIMETYPE + " = ?",
                        arrayOf(
                            contact.contactID,
                            ContactsContract.CommonDataKinds.Nickname.CONTENT_ITEM_TYPE
                        ),
                        null
                    )
                    if (nickCur!!.count > 0) {
                        var nc = 0
                        contact.nickName = arrayOfNulls(nickCur.count)
                        while (nickCur.moveToNext()) {
                            contact.nickName!![nc] =
                                nickCur.getString(nickCur.getColumnIndex(ContactsContract.CommonDataKinds.Nickname.NAME))
                                    ?.toLowerCase(skivvy.locale)
                            ++nc
                        }
                    }
                    nickCur.close()
                    val fName = contact.displayName?.substringBefore(" ")
                    if (temp.getContactReceived() == contact.displayName?.toLowerCase(skivvy.locale) || temp.getContactReceived() == fName?.toLowerCase(
                            skivvy.locale
                        ) ||
                        !contact.nickName.isNullOrEmpty() && contact.nickName!!.contains(temp.getContactReceived())
                    ) {
                        temp.setContactPresence(true)
                        val dpUri =
                            cur.getString(cur.getColumnIndex(ContactsContract.Contacts.PHOTO_URI))
                        if (dpUri != null) {
                            contact.photoID = dpUri
                            val b = MediaStore.Images.Media.getBitmap(
                                context.contentResolver,
                                Uri.parse(contact.photoID)
                            )
                            val rb: RoundedBitmapDrawable =
                                RoundedBitmapDrawableFactory.create(resources, b)
                            rb.isCircular = true
                            rb.setAntiAlias(true)
                            waitingView(rb)
                        }
                        val eCur: Cursor? = cr.query(
                            ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?",
                            arrayOf(contact.contactID),
                            null
                        )
                        var size = 0
                        while (eCur!!.moveToNext()) {
                            ++size
                        }
                        eCur.moveToFirst()
                        if (size > 0) {
                            contact.emailList = arrayOfNulls(size)
                            var k = 0
                            while (k < size) {
                                contact.emailList!![k] =
                                    eCur.getString(eCur.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA))
                                eCur.moveToNext()
                                ++k
                            }
                            if (temp.getContactCode() == skivvy.CODE_EMAIL_CONF) {
                                speakOut(getString(what_is_subject), skivvy.CODE_EMAIL_CONTENT)
                            }
                            eCur.close()
                        } else {
                            if (temp.getContactCode() == skivvy.CODE_EMAIL_CONF) {
                                errorView()
                                speakOut(
                                    getString(you_dont_seem_having) + contact.displayName + getString(
                                        someone_email_address
                                    )
                                )
                            }
                        }
                        if (temp.getContactCode() == skivvy.CODE_CALL_CONF || temp.getContactCode() == skivvy.CODE_SMS_CONF || temp.getContactCode() == skivvy.CODE_WHATSAPP_ACTION) {
                            if (cur.getString(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))
                                    .toInt() > 0
                            ) {
                                val pCur: Cursor? = cr.query(
                                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                    null,
                                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                                    arrayOf(contact.contactID),
                                    null
                                )
                                pCur!!.moveToFirst()
                                size = 0
                                while (pCur.moveToNext()) {
                                    ++size
                                }
                                contact.phoneList = arrayOfNulls(size)
                                pCur.moveToFirst()
                                var k = 0
                                while (k < size) {
                                    contact.phoneList!![k] =
                                        pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                                    pCur.moveToNext()
                                    ++k
                                }
                                if (size == 1) {
                                    when (temp.getContactCode()) {
                                        skivvy.CODE_CALL_CONF -> {
                                            speakOut(
                                                getString(should_i_call) + "${contact.displayName}?",
                                                skivvy.CODE_CALL_CONF
                                            )
                                        }
                                        skivvy.CODE_SMS_CONF -> {
                                            speakOut(
                                                getString(what_is_message),
                                                skivvy.CODE_TEXT_MESSAGE_BODY
                                            )
                                        }
                                        skivvy.CODE_WHATSAPP_ACTION -> {
                                            speakOut(
                                                "What is your message to ${contact.displayName} via whatsapp?",
                                                skivvy.CODE_WHATSAPP_ACTION
                                            )
                                        }
                                    }
                                } else {
                                    if (temp.getContactCode() == skivvy.CODE_CALL_CONF) {
                                        speakOut(
                                            "I've got $size phone numbers of ${contact.displayName}.\nShould I call them at " +
                                                    "${contact.phoneList!![temp.getPhoneIndex()]}?",
                                            skivvy.CODE_CALL_CONF
                                        )
                                    } else if (temp.getContactCode() == skivvy.CODE_SMS_CONF) {
                                        speakOut(
                                            getString(what_is_message),
                                            skivvy.CODE_TEXT_MESSAGE_BODY
                                        )
                                    } else if (temp.getContactCode() == skivvy.CODE_WHATSAPP_ACTION) {
                                        speakOut(
                                            "What is your message to ${contact.displayName} via whatsapp?",
                                            skivvy.CODE_WHATSAPP_ACTION
                                        )
                                    }
                                }
                                pCur.close()
                            } else {
                                errorView()
                                speakOut(
                                    getString(you_dont_seem_having) + contact.displayName + getString(
                                        someones_phone_number
                                    )
                                )
                            }
                        }
                        eCur.close()
                        break
                    } else temp.setContactPresence(false)
                }
            } else {
                errorView()
                speakOut(getString(no_contacts_available))
            }
            cur.close()
            if (!temp.getContactPresence()) {
                errorView()
                speakOut(getString(contact_not_found))
            }
        }
    }

    private fun takeScreenshot() {
        val now = Date()
        DateFormat.format("yyyy-MM-dd_hh:mm:ss", now)
        try {
            val mPath: String =
                Environment.getExternalStorageDirectory().toString() + "/" + now + ".jpg"
            val v1 = window.decorView.rootView
            v1.isDrawingCacheEnabled = true
            val bitmap = Bitmap.createBitmap(v1.drawingCache)
            v1.isDrawingCacheEnabled = false
            val imageFile = File(mPath)
            val outputStream = FileOutputStream(imageFile)
            val quality = 100
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            outputStream.flush()
            outputStream.close()
            speakOut(getString(snap_success))
        } catch (e: Throwable) {
            errorView()
            speakOut(getString(snap_failed))
            e.printStackTrace()
        }
    }

    //TODO:  airplane mode, power off, restart phone,brightness,auto rotation,hotspot, specific settings

    private fun isAirplaneModeEnabled(): Boolean {
        return Settings.System.getInt(contentResolver, Settings.System.AIRPLANE_MODE_ON, 0) == 1
    }

    private fun setAirplaneMode(status: Boolean) {
        Settings.System.putInt(
            contentResolver, Settings.System.AIRPLANE_MODE_ON,
            if (status) 1
            else 0
        )
        try {
            sendBroadcast(
                Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED).putExtra(
                    "state", status
                )
            )
        } catch (e: SecurityException) {
            speakOut("I'm not allowed to do that")
        }
    }

    private fun setFlashLight(status: Boolean) {
        try {
            val mCameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
            val mCameraId = mCameraManager.cameraIdList[0]
            Log.d("Flash", mCameraId)
            mCameraManager.setTorchMode(mCameraId, status)
        } catch (e: CameraAccessException) {
            speakOut("Can't access your flashlight")
            e.printStackTrace()
        }
    }

    private fun isFlashAvailable(): Boolean {
        return applicationContext.packageManager
            .hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT)
    }

    private fun saveCalculationResult(result: String) {
        getSharedPreferences(skivvy.PREF_HEAD_CALC, Context.MODE_PRIVATE).edit()
            .putString(skivvy.PREF_KEY_LAST_CALC, result).apply()
    }

    private fun getLastCalculationResult(): String? {
        return getSharedPreferences(skivvy.PREF_HEAD_CALC, Context.MODE_PRIVATE)
            .getString(skivvy.PREF_KEY_LAST_CALC, "0")
    }

    //Handle incoming phone calls
    private var phoneStateListener: PhoneStateListener? = null
    private var telephonyManager: TelephonyManager? = null
    private var lastState: Int? = null

    private fun callStateListener() {
        telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        phoneStateListener = object : PhoneStateListener() {
            override fun onCallStateChanged(state: Int, number: String) {
                when (state) {
                    lastState -> return
                    TelephonyManager.CALL_STATE_RINGING -> {
                        successView(getDrawable(ic_glossyphone))
                        setVolume(50F)
                        speakOut("Incoming call from $number")
                    }
                    TelephonyManager.CALL_STATE_OFFHOOK -> {
                        if (lastState == TelephonyManager.CALL_STATE_RINGING) {
                            waitingView(getDrawable(ic_glossyphone))
                            speakOut("Speaking to $number")
                        } else {
                            if (temp.getContactPresence()) {
                                speakOut("Calling ${contact.displayName}")
                            } else {
                                speakOut("Calling $number")
                            }
                        }
                    }
                    TelephonyManager.CALL_STATE_IDLE -> {
                        if (lastState == TelephonyManager.CALL_STATE_RINGING) {
                            speakOut("You missed a call from $number")
                            errorView()
                        } else if (lastState == TelephonyManager.CALL_STATE_OFFHOOK) {
                            if (temp.getContactPresence()) {
                                speakOut("Call ended with ${contact.displayName}")
                            } else {
                                speakOut("Call ended with\n$number")
                            }
                        }
                    }
                }
                lastState = state
            }
        }
        // Register the listener with the telephony manager
        // Listen for changes to the device call state.
        telephonyManager!!.listen(
            phoneStateListener,
            PhoneStateListener.LISTEN_CALL_STATE
        )
    }

    override fun onDestroy() {
        loading.startAnimation(zoomInRotate)
        speakOut(getString(exit_msg))
        if (skivvy.tts != null) {
            skivvy.tts!!.stop()
            skivvy.tts!!.shutdown()
        }
        super.onDestroy()
    }

    override fun onBackPressed() {
        speakOut(getString(exit_msg))
        loading.startAnimation(zoomInRotate)
        if (skivvy.tts != null) {
            skivvy.tts!!.stop()
            skivvy.tts!!.shutdown()
        }
        super.onBackPressed()
    }

    private fun normalView(fromTraining: Boolean = false) {
        loading.setImageDrawable(getDrawable(ic_dotsincircle))
        loading.startAnimation(zoomInOutRotate)
        input.text = null
        outPut.text = null
        feedback.text = null
        icon.setImageDrawable(null)
        contact = ContactModel()
        temp = TempDataManager()
        txt = null
        if (fromTraining) {
            startResumeAnimate()
            receiver.isClickable = true
            setting.isClickable = true
            receiver.visibility = View.VISIBLE
            greet.visibility = View.VISIBLE
            setting.visibility = View.VISIBLE
            settingBack.visibility = View.VISIBLE
            outPut.text = getString(im_ready)
            input.text = getString(tap_the_button)
        }
    }

    private fun trainingView() {
        skivvy.saveMuteStatus(false)
        backfall.startAnimation(AnimationUtils.loadAnimation(context, R.anim.extend_back))
        receiver.startAnimation(AnimationUtils.loadAnimation(context, R.anim.fade_off))
        loading.startAnimation(AnimationUtils.loadAnimation(context, R.anim.rotate_slow))
        loading.setImageDrawable(getDrawable(ic_yellow_dotsincircle))
        receiver.isClickable = false
        setting.isClickable = false
        greet.text = getString(traning_title)
        receiver.visibility = View.GONE
        setting.visibility = View.GONE
        settingBack.visibility = View.GONE
    }

    fun waitingView(image: Drawable?) {
        loading.startAnimation(focusRotate)
        loading.setImageDrawable(getDrawable(ic_yellow_dotsincircle))
        if (image != null) {
            icon.setImageDrawable(image)
        }
    }

    fun errorView(): Boolean {
        loading.startAnimation(fadeOnFadeOff)
        loading.setImageDrawable(getDrawable(ic_red_dotsincircle))
        return false
    }

    fun successView(image: Drawable?) {
        loading.startAnimation(focusDeFocusRotate)
        loading.setImageDrawable(getDrawable(ic_green_dotsincircle))
        if (image != null) {
            icon.setImageDrawable(image)
        }
    }

    private fun speakOut(
        text: String,
        taskCode: Int? = null,
        parallelReceiver: Boolean = skivvy.getParallelResponseStatus(),
        isFeedback: Boolean = false
    ) {
        if (isFeedback) setFeedback(text)
        else outPut.text = text
        skivvy.tts!!.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onDone(utteranceId: String) {
                if (!parallelReceiver)
                    taskCode?.let { startVoiceRecIntent(it, text) }
            }

            override fun onError(utteranceId: String) {}
            override fun onStart(utteranceId: String) {
                if (parallelReceiver)
                    taskCode?.let { startVoiceRecIntent(it, text) }
            }
        })
        if (!skivvy.getMuteStatus()) skivvy.tts!!.speak(
            text,
            TextToSpeech.QUEUE_FLUSH,
            null,
            "$taskCode"
        )
        else
            taskCode?.let { startVoiceRecIntent(it, text) }
    }

    //intent voice recognition, code according to action command, serving activity result
    private fun startVoiceRecIntent(
        code: Int,
        message: String = getString(generic_voice_rec_text)
    ) {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            .putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            .putExtra(RecognizerIntent.EXTRA_LANGUAGE, skivvy.locale)
            .putExtra(RecognizerIntent.EXTRA_PROMPT, message)
        if (intent.resolveActivity(packageManager) != null)
            startActivityForResult(intent, code)
        else {
            errorView()
            speakOut(getString(internal_error))
        }
    }

    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    private fun authStateAction(code: Int) {
        executor = ContextCompat.getMainExecutor(context)
        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(getString(auth_demand_title))
            .setSubtitle(getString(auth_demand_subtitle))
            .setDescription(getString(biometric_auth_explanation))
            .setNegativeButtonText(getString(discard))
            .build()
        biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult
                ) {
                    super.onAuthenticationSucceeded(result)
                    when (code) {
                        skivvy.CODE_BIOMETRIC_CONFIRM -> {
                            skivvy.setBiometricsStatus(false)
                            speakOut(getString(biometric_is_off))
                        }
                        skivvy.CODE_VOICE_AUTH_CONFIRM -> {
                            skivvy.setPhraseKeyStatus(false)
                            speakOut("Vocal authentication disabled")
                        }
                    }
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    when (code) {
                        skivvy.CODE_BIOMETRIC_CONFIRM ->
                            speakOut(getString(biometric_off_error))
                        skivvy.CODE_VOICE_AUTH_CONFIRM ->
                            speakOut(getString(vocal_auth_off_error))
                    }

                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    speakOut(getString(verification_unsuccessfull))
                }
            })
    }
}
