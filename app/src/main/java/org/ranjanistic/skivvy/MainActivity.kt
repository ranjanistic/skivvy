package org.ranjanistic.skivvy

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.PendingIntent
import android.app.admin.DevicePolicyManager
import android.bluetooth.BluetoothAdapter
import android.content.*
import android.content.pm.PackageInstaller
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
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
import java.io.File
import java.io.FileOutputStream
import java.net.URLEncoder
import java.util.*
import java.util.concurrent.Executor

@ExperimentalStdlibApi
open class MainActivity : AppCompatActivity() {
    lateinit var skivvy: Skivvy
    private var outPut: TextView? = null
    fun setOutput(text: String) {
        this.outPut!!.text = text
    }

    private var input: TextView? = null
    fun setInput(text: String) {
        this.input!!.text = text
    }

    private var feedback: TextView? = null
    private var focusRotate: Animation? = null
    private var normalRotate: Animation? = null
    private var rotateSlow: Animation? = null
    private var exitAnimation: Animation? = null
    private var fadeAnimation: Animation? = null
    private var bubbleAnimation: Animation? = null
    private var fallAnimation: Animation? = null
    private var riseAnimation: Animation? = null
    private var extendAnimation: Animation? = null
    private var pillExitAnim: Animation? = null
    private var pillEnterAnim: Animation? = null
    private var fadeOffAnimation: Animation? = null
    private var fadeOnAnimation: Animation? = null
    private var translateAnimation: Animation? = null
    private var receiver: ImageButton? = null
    private lateinit var setting: ImageButton
    private lateinit var settingBack: ImageView
    private var greet: TextView? = null
    private var tempPackageIndex: Int = 0
    private var tempPhoneNumberIndex: Int = 0
    private var tempEmailIndex: Int = 0
    private var loading: ImageView? = null
    private var outputStat: ImageView? = null
    private var icon: ImageView? = null
    private var txt: String? = null
    private var tempPhone: String? = null
    private var tempMail: String? = null
    private var tempMailSubject: String? = null
    private var tempMailBody: String? = null
    private var tempTextBody: String? = null
    private var tempContact: String? = null
    private var tempContactIndex: Int = 0
    private var tempContactCode: Int? = null
    private lateinit var backfall: ImageView
    private lateinit var context: Context
    private var deviceManger: DevicePolicyManager? = null
    private var compName: ComponentName? = null
    private var contact: ContactModel = ContactModel()
    private lateinit var calculator: Calculator
    private lateinit var audioManager: AudioManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        context = this
        skivvy = this.application as Skivvy
        setViewAndDefaults()
        loadDefaultAnimations()
        normalView()
        setListeners()
        outPut?.text = getString(im_ready)
        input?.text = getString(tap_the_button)
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
        outputStat = findViewById(R.id.outputStatusView)
        outputStat!!.visibility = View.INVISIBLE
        audioManager = applicationContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        calculator = Calculator(skivvy)
    }

    private fun loadDefaultAnimations() {
        fallAnimation = AnimationUtils.loadAnimation(context, R.anim.fall_back)
        backfall.startAnimation(fallAnimation)
        riseAnimation = AnimationUtils.loadAnimation(context, R.anim.rise_back)
        bubbleAnimation = AnimationUtils.loadAnimation(context, R.anim.bubble_wave)
        receiver!!.startAnimation(bubbleAnimation)
        greet!!.startAnimation(bubbleAnimation)
        normalRotate = AnimationUtils.loadAnimation(context, R.anim.rotate_emerge_demerge)
        focusRotate = AnimationUtils.loadAnimation(context, R.anim.rotate_focus)
        rotateSlow = AnimationUtils.loadAnimation(context, R.anim.rotate_slow)
        fadeAnimation = AnimationUtils.loadAnimation(context, R.anim.fade)
        exitAnimation = AnimationUtils.loadAnimation(context, R.anim.rotate_exit)
        fadeOffAnimation = AnimationUtils.loadAnimation(context, R.anim.fade_off)
        fadeOnAnimation = AnimationUtils.loadAnimation(context, R.anim.fade_on)
        pillEnterAnim = AnimationUtils.loadAnimation(context, R.anim.pill_slide_left)
        setting.startAnimation(pillEnterAnim)
        pillExitAnim = AnimationUtils.loadAnimation(context, R.anim.pill_slide_right)
        extendAnimation = AnimationUtils.loadAnimation(context, R.anim.extend_back)
    }

    private fun startSettingAnimate() {
        setting.startAnimation(pillExitAnim)
        backfall.startAnimation(extendAnimation)
        greet!!.startAnimation(fadeOffAnimation)
        outPut!!.startAnimation(fadeOffAnimation)
        receiver!!.startAnimation(fadeOffAnimation)
        input!!.startAnimation(fadeOffAnimation)
        settingBack.startAnimation(fadeOffAnimation)
    }

    private fun startResumeAnimate() {
        setting.startAnimation(pillEnterAnim)
        backfall.startAnimation(riseAnimation)
        greet!!.startAnimation(fadeOnAnimation)
        outPut!!.startAnimation(fadeOnAnimation)
        receiver!!.startAnimation(fadeOnAnimation)
        input!!.startAnimation(fadeOnAnimation)
        settingBack.startAnimation(fadeOnAnimation)
    }

    private fun setListeners() {
        callStateListener()
        setting.setOnClickListener {
            startSettingAnimate()
        }
        receiver?.setOnClickListener {
            speakOut("")
            normalView()
            startVoiceRecIntent(skivvy.CODE_SPEECH_RECORD)
        }
        pillExitAnim!!.setAnimationListener(object : Animation.AnimationListener {
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
                        TextToSpeech.LANG_NOT_SUPPORTED -> outPut!!.text = getString(
                            language_not_supported
                        )
                    }
                }
                else -> outPut!!.text = getString(output_error)
            }
        })
    }

    override fun onRestart() {
        super.onRestart()
        startResumeAnimate()
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
                        tempPhone != null -> {
                            speakOut(
                                getString(should_i_call) + "$tempPhone?",
                                skivvy.CODE_CALL_CONF
                            )
                        }
                        else -> {
                            speakOut(getString(null_variable_error))
                        }
                    }
                } else {
                    tempPhone = null
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
                        tempPhone != null -> {
                            speakOut(
                                getString(should_i_text) + "$tempPhone?",
                                skivvy.CODE_SMS_CONF
                            )
                        }
                        else -> {
                            speakOut(getString(null_variable_error))
                        }
                    }
                } else {
                    tempPhone = null
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
                    if (tempContact != null && tempContactCode != null) {
                        contactOps(tempContact!!, tempContactCode!!)
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
        feedback!!.text = text
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
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
            KeyEvent.KEYCODE_CAPS_LOCK -> {
                setFeedback("Caps lock toggled")
            }
            KeyEvent.KEYCODE_HOME -> {
                setFeedback("I'm still working")
            }
            KeyEvent.KEYCODE_MEDIA_PLAY -> {
                setFeedback("Your music must be playing now")
            }
            KeyEvent.KEYCODE_MEDIA_PAUSE -> {
                setFeedback("Music paused")
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    @Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (!skivvy.nonVocalRequestCodes.contains(requestCode)) {
            if (data == null || data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)[0].toString()
                    .toLowerCase(skivvy.locale) == ""
            ) {
                normalView()
                speakOut(getString(no_input))
                return
            }
        }
        when (requestCode) {
            skivvy.CODE_SPEECH_RECORD -> {
                val temp =
                    data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)[0].toString()
                        .toLowerCase(skivvy.locale)
                if (txt == null || txt == "") {
                    txt = temp
                } else {
                    if (resources.getStringArray(R.array.disruptions).contains(temp)) {
                        speakOut(getString(okay))
                        normalView()
                    } else {
                        if (temp.contains(txt!!)) {
                            txt = temp
                        } else {
                            txt += " $temp"
                        }
                    }
                }
                if (txt != null) {
                    input?.text = txt
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
                txt = data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)[0].toString()
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
                txt = data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)[0].toString()
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
            skivvy.CODE_APP_CONF -> {
                txt = data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)[0].toString()
                    .toLowerCase(skivvy.locale)
                val pData = skivvy.packageDataManager
                if (resources.getStringArray(R.array.acceptances).contains(txt)) {
                    successView(pData.getPackageIcon(tempPackageIndex))
                    speakOut(
                        getString(opening) + pData.getPackageAppName(tempPackageIndex)!!
                            .capitalize(skivvy.locale)
                    )
                    startActivity(Intent(pData.getPackageIntent(tempPackageIndex)))
                } else if (resources.getStringArray(R.array.denials).contains(txt) ||
                    resources.getStringArray(R.array.disruptions).contains(txt)
                ) {
                    normalView()
                    speakOut(getString(okay))
                } else {
                    speakOut(
                        getString(recognize_error) + "\n" + getString(do_u_want_open) + "${pData.getPackageAppName(
                            tempPackageIndex
                        )!!.capitalize(skivvy.locale)}?",
                        skivvy.CODE_APP_CONF
                    )
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
                    respondToCommand(txt!!)
                } else {
                    errorView()
                    speakOut(getString(device_admin_failure))
                }
            }
            skivvy.CODE_CALL_CONF -> {
                //val cdata = skivvy.contactData
                txt = data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)[0].toString()
                    .toLowerCase(skivvy.locale)
                when {
                    resources.getStringArray(R.array.acceptances).contains(txt) -> {
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
                            if (this.isContactPresent) {
                                successView(null)
                                callingOps(
                                    contact.phoneList!![tempPhoneNumberIndex],
//                                        cdata.getContactNames()[tempContactIndex]!!
                                    contact.displayName!!
                                )
                            } else {
                                successView(getDrawable(ic_glossyphone))
                                callingOps(tempPhone)
                                tempPhone = null
                            }
                        }
                    }
                    resources.getStringArray(R.array.denials).contains(txt) ||
                            resources.getStringArray(R.array.disruptions).contains(txt) -> {
                        tempPhoneNumberIndex += 1
                        if (this.isContactPresent && tempPhoneNumberIndex < contact.phoneList!!.size && !resources.getStringArray(
                                R.array.disruptions
                            ).contains(txt)
                        ) {
                            speakOut(
                                "At ${contact.phoneList!![tempPhoneNumberIndex]}?",
                                skivvy.CODE_CALL_CONF
                            )
                        } else {
                            normalView()
                            speakOut(getString(okay))
                        }
                    }
                    else -> {
                        if (this.isContactPresent) {
                            speakOut(
                                getString(recognize_error) + "\n" + getString(should_i_call) + "${contact.displayName} at ${contact.phoneList!![tempPhoneNumberIndex]}?",
                                skivvy.CODE_CALL_CONF
                            )
                        } else {
                            speakOut(
                                getString(recognize_error) + "\n" + getString(should_i_call) + "$tempPhone?",
                                skivvy.CODE_CALL_CONF
                            )
                        }
                    }
                }
            }

            skivvy.CODE_EMAIL_CONTENT -> {
                //val cdata = skivvy.contactData
                txt = data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)[0].toString()
                    .toLowerCase(skivvy.locale)
                when {
                    resources.getStringArray(R.array.disruptions).contains(txt) -> {
                        normalView()
                        speakOut(getString(okay))
                    }
                    txt != null -> {
                        if (tempMailSubject == null) {
                            tempMailSubject = txt
                            speakOut(
                                getString(subject_added) + "\n" + getString(what_is_body),
                                skivvy.CODE_EMAIL_CONTENT
                            )
                        } else if (tempMailBody == null) {
                            tempMailBody = txt
                            if (this.isContactPresent) {
                                if (contact.emailList!!.size == 1) {
                                    speakOut(
                                        getString(body_added) + "\n" +
//                                                    getString(should_i_email) + "${cdata.getContactNames()[tempContactIndex]} at\n${cdata.getContactEmails()[tempContactIndex]!![tempEmailIndex]}?",
                                                getString(should_i_email) + "${contact.displayName} at\n${contact.emailList!![tempEmailIndex]}?",
                                        skivvy.CODE_EMAIL_CONF
                                    )
                                } else {
                                    speakOut(
//                                            getString(body_added) + "I've got ${cdata.getContactEmails()[tempContactIndex]!!.size} addresses of ${cdata.getContactNames()[tempContactIndex]}.\n" +
//                                                    getString(should_i_email) + "them at\n${cdata.getContactEmails()[tempContactIndex]!![tempEmailIndex]}?",
                                        getString(body_added) + "I've got ${contact.emailList!!.size} addresses of ${contact.displayName}.\n" +
                                                getString(should_i_email) + "them at\n${contact.emailList!![tempEmailIndex]}?",
                                        skivvy.CODE_EMAIL_CONF
                                    )
                                }
                            } else {
                                speakOut(
                                    getString(body_added) + "\n" +
                                            getString(should_i_email) + "$tempMail?",
                                    skivvy.CODE_EMAIL_CONF
                                )
                            }
                        }
                    }
                    else -> {
                        if (tempMailSubject == null) {
                            speakOut(
                                getString(recognize_error) + "\n" + getString(what_is_subject),
                                skivvy.CODE_EMAIL_CONTENT
                            )
                        } else if (tempMailBody == null) {
                            speakOut(
                                getString(recognize_error) + "\n" + getString(what_is_body),
                                skivvy.CODE_EMAIL_CONTENT
                            )
                        }
                    }
                }
            }

            skivvy.CODE_EMAIL_CONF -> {
                //val cdata = skivvy.contactData
                txt = data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)[0].toString()
                    .toLowerCase(skivvy.locale)
                when {
                    resources.getStringArray(R.array.acceptances).contains(txt) -> {
                        successView(null)
                        speakOut(getString(preparing_email))
                        if (this.isContactPresent) {
                            emailingOps(
//                                    cdata.getContactEmails()[tempContactIndex]!![tempEmailIndex],
                                contact.emailList!![tempEmailIndex],
                                tempMailSubject,
                                tempMailBody
                            )
                        } else {
                            emailingOps(tempMail, tempMailSubject, tempMailBody)
                        }
                    }
                    resources.getStringArray(R.array.denials).contains(txt) ||
                            resources.getStringArray(R.array.disruptions).contains(txt) -> {
                        tempEmailIndex += 1
                        if (this.isContactPresent && tempEmailIndex < contact.emailList!!.size && !resources.getStringArray(
                                R.array.disruptions
                            ).contains(txt)
                        ) {
                            speakOut(
                                "At ${contact.emailList!![tempEmailIndex]}?",
                                skivvy.CODE_EMAIL_CONF
                            )
                        } else {
                            normalView()
                            speakOut(getString(okay))
                        }
                    }
                    else -> {
                        if (contact.emailList != null) {
                            speakOut(
                                getString(recognize_error) + "\n" +
//                                            getString(should_i_email) + "${cdata.getContactNames()[tempContactIndex]} at\n${cdata.getContactEmails()[tempContactIndex]!![tempEmailIndex]}?",
                                        getString(should_i_email) + "${contact.displayName} at\n${contact.emailList!![tempEmailIndex]}?",
                                skivvy.CODE_EMAIL_CONF
                            )
                        } else {
                            speakOut(
                                getString(recognize_error) + "\n" +
                                        getString(should_i_email) + "$tempMail?",
                                skivvy.CODE_EMAIL_CONF
                            )
                        }
                    }
                }
            }

            skivvy.CODE_TEXT_MESSAGE_BODY -> {
                //val cdata = skivvy.contactData
                txt = data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)[0].toString()
                    .toLowerCase(skivvy.locale)
                when {
                    resources.getStringArray(R.array.disruptions).contains(txt)
                    -> {
                        normalView()
                        speakOut(getString(okay))
                    }
                    txt != null -> {
                        waitingView(null)
                        tempTextBody = txt
                        if (this.isContactPresent) {
                            speakOut(
                                //                                  getString(should_i_text) + "${cdata.getContactNames()[tempContactIndex]} at ${cdata.getContactPhones()[tempContactIndex]!![tempPhoneNumberIndex]}" + getString( via_sms),
                                getString(should_i_text) + "${contact.displayName} at ${contact.phoneList!![tempPhoneNumberIndex]}" + getString(
                                    via_sms
                                ),
                                skivvy.CODE_SMS_CONF
                            )
                        } else {
                            speakOut(
                                getString(should_i_text) + "$tempPhone" + getString(via_sms),
                                skivvy.CODE_SMS_CONF
                            )
                        }
                    }
                    else -> {
                        speakOut(
                            getString(recognize_error) + getString(what_is_message),
                            skivvy.CODE_TEXT_MESSAGE_BODY
                        )
                    }
                }
            }

            skivvy.CODE_SMS_CONF -> {
                //val cdata = skivvy.contactData
                txt = data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)[0].toString()
                    .toLowerCase(skivvy.locale)
                when {
                    resources.getStringArray(R.array.acceptances).contains(txt) -> {
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
                            if (this.isContactPresent) {
                                speakOut(getString(sending_sms_at) + "${contact.phoneList!![tempPhoneNumberIndex]}")
                                textMessageOps(
                                    contact.phoneList!![tempPhoneNumberIndex]!!,
                                    tempTextBody!!,
                                    skivvy.CODE_SMS_CONF
                                )
                            } else if (tempPhone != null) {
                                speakOut(getString(sending_sms_at) + "$tempPhone")
                                textMessageOps(
                                    tempPhone!!,
                                    tempTextBody!!,
                                    skivvy.CODE_SMS_CONF
                                )
                            }
                        }
                    }
                    resources.getStringArray(R.array.denials)
                        .contains(txt) || resources.getStringArray(R.array.disruptions)
                        .contains(txt) -> {
                        tempPhoneNumberIndex += 1
                        // if (this.isContactPresent && tempPhoneNumberIndex < cdata.getContactPhones()[tempContactIndex]!!.size && !resources.getStringArray(
                        if (this.isContactPresent && tempPhoneNumberIndex < contact.phoneList!!.size && !resources.getStringArray(
                                R.array.disruptions
                            ).contains(txt)
                        ) {
                            speakOut(
                                //                                 "At ${cdata.getContactPhones()[tempContactIndex]!![tempPhoneNumberIndex]}?",
                                "At ${contact.phoneList!![tempPhoneNumberIndex]}?",
                                skivvy.CODE_SMS_CONF
                            )
                        } else {
                            normalView()
                            speakOut(getString(okay))
                        }
                    }
                    else -> {
                        if (this.isContactPresent) {
                            speakOut(
//                                getString(should_i_text) + "${cdata.getContactNames()[tempContactIndex]} at ${cdata.getContactPhones()[tempContactIndex]!![tempPhoneNumberIndex]}" + getString( via_sms),
                                getString(should_i_text) + "${contact.displayName} at ${contact.phoneList!![tempPhoneNumberIndex]}" + getString(
                                    via_sms
                                ),
                                skivvy.CODE_SMS_CONF
                            )
                        } else {
                            speakOut(
                                getString(should_i_text) + "$tempPhone" + getString(via_sms),
                                skivvy.CODE_SMS_CONF
                            )
                        }
                    }
                }
            }
            skivvy.CODE_WHATSAPP_ACTION -> {
                txt = data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)[0].toString()
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
                                                + contact.phoneList!![tempPhoneNumberIndex] + "&text=" + URLEncoder.encode(
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
                    if (text.replace("search for", "").trim() == "" || text.replace("search", "")
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
            text.contains("uninstall") -> {
                if (text.replace("uninstall", "").trim().isNullOrBlank()) {
                    txt = text
                    speakOut("Uninstall what?", skivvy.CODE_SPEECH_RECORD)
                    return true
                }
                deviceManger =
                    applicationContext.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
                compName = ComponentName(context, Administrator::class.java)
                if (!deviceManger!!.isAdminActive(compName!!)) {
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

    private fun computerOps(rawExpression: String): Boolean {
        val expression = calculator.expressionize(rawExpression)

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
                    saveCalculationResult(calculator.functionOperate(expression)!!)
                    speakOut(calculator.formatToProperValue(getLastCalculationResult()!!))
                    return true
                }
            }
            return false
        }

        val totalOps = calculator.totalOperatorsInExpression(expression)
        if (totalOps == 0 || !calculator.isExpressionOperatable(expression) || calculator.segmentizeExpression(
                expression,
                2 * totalOps + 1
            ) == null
        )
            return false

        var arrayOfExpression = calculator.segmentizeExpression(expression, 2 * totalOps + 1)!!

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
            val temp = calculator.evaluateFunctionsInSegmentedArrayOfExpression(arrayOfExpression)
            if (temp == null) {
                return false
            } else {
                arrayOfExpression = temp
            }
        }

        if (!calculator.isExpressionArrayOnlyNumbersAndOperators(arrayOfExpression))     //if array contains invalid values
            return false
        else {
            saveCalculationResult(calculator.expressionCalculation(arrayOfExpression))
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
        deviceManger =
            applicationContext.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        compName = ComponentName(context, Administrator::class.java)
        if (deviceManger!!.isAdminActive(compName!!)) {
            successView(getDrawable(ic_glossylock))
            speakOut(getString(screen_locked))
            deviceManger!!.lockNow()
        } else {
            waitingView(getDrawable(ic_glossylock))
            speakOut(getString(device_admin_request))
            startActivityForResult(
                Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
                    .putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, compName)
                    .putExtra(
                        DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                        getString(device_admin_persuation)
                    ), skivvy.CODE_DEVICE_ADMIN
            )
        }
    }

    private fun volumeOps(action: String) {
        when {
            action.contains(skivvy.numberPattern) -> {
                val percent = action.replace(skivvy.nonNumeralPattern, "").toFloat() / 100
                val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                val volumeLevel = (maxVolume * percent).toInt()
                audioManager.setStreamVolume(
                    AudioManager.STREAM_MUSIC,
                    volumeLevel,
                    AudioManager.FLAG_SHOW_UI
                )
                speakOut("Volume at ${(percent * 100).toInt()}%")
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
                    speakOut("Volume increased")
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
                    speakOut("Volume decreased")
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
                            tempPackageIndex = i
                            successView(pData.getPackageIcon(i))
                            speakOut(
                                getString(opening) + pData.getPackageAppName(i)!!
                                    .capitalize(skivvy.locale)
                            )
                            startActivity(Intent(pData.getPackageIntent(i)))
                            return true
                        }
                        pData.getPackageName(i)!!.contains(localText) -> {
                            tempPackageIndex = i
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
        when {
            text.contains("add") || text.contains("plus") || text.contains("subtract")//||text.contains("multiply")||text.contains("divide" )
            -> {
                if (getLastCalculationResult() != "0") {
                    localTxt = getLastCalculationResult() + text
                    computerOps(calculator.expressionize(localTxt))
                } else return false
//                startVoiceRecIntent(skivvy.CODE_SPEECH_RECORD,"Continue calculation")
            }
            text.contains(getString(call)) -> {
                waitingView(getDrawable(ic_glossyphone))
                localTxt = text.replace(getString(call), "", true).trim()
                tempPhone = text.replace(skivvy.nonNumeralPattern, "")
                if (tempPhone != null) {
                    when {
                        tempPhone!!.contains(skivvy.numberPattern) -> {
                            if (tempPhone!!.length == 10) {
                                val local = tempPhone
                                tempPhone = ""
                                var k = 0
                                while (k < 10) {
                                    tempPhone += local!![k]
                                    if (k == 4) {
                                        tempPhone += " "
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
                                    getString(should_i_call) + "$tempPhone?",
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
                tempMail = localTxt.replace(" ", "").trim()
                when {
                    tempMail!!.matches(skivvy.emailPattern) -> {
                        input!!.text = tempMail
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
            text.contains("text") -> {
                waitingView(getDrawable(ic_messageicon))
                localTxt = text.replace("text", "", false)
                localTxt = localTxt.trim()
                tempPhone = localTxt.replace(skivvy.nonNumeralPattern, "")
                when {
                    tempPhone!!.contains(skivvy.numberPattern) -> {
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
                        txt = "text "
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
    private fun callingOps(number: String?) {
        if (number != null) {
            speakOut(getString(calling) + "$number")
            startActivity(Intent(Intent.ACTION_CALL, Uri.parse("tel:$number")))
        } else {
            errorView()
            speakOut(getString(null_variable_error))
        }
    }

    @SuppressLint("MissingPermission")
    private fun callingOps(number: String?, name: String) {
        this.isContactPresent = false
        if (number != null) {
            speakOut(getString(calling) + name)
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
    private var isContactPresent = false
    private fun contactOps(name: String, code: Int) {
        tempContactCode = code
        tempPhoneNumberIndex = 0
        tempEmailIndex = 0
        tempContact = name.trim()
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
            val cd = skivvy.contactData
            if (cd.getTotalContacts() == 0) {
                errorView()
                speakOut(getString(no_contacts_available))
            } else {
                var contactIndex = 0
                while (contactIndex < cd.getTotalContacts()) {
                    if(cd.getContactIDs()[contactIndex].isNullOrEmpty()){
                        speakOut("Please wait",skivvy.CODE_SPEECH_RECORD)
                        normalView()
                    } else if (tempContact == cd.getContactNames()[contactIndex]!!.toLowerCase(skivvy.locale) ||
                        tempContact == cd.getContactNames()[contactIndex]!!.substringBefore(" ").toLowerCase(skivvy.locale)||
                        !cd.getContactNicknames()[contactIndex].isNullOrEmpty()&&cd.getContactNicknames()[contactIndex]!!.contains(tempContact)
                    ) {
                        tempContactIndex = contactIndex
                        this.isContactPresent = true
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
                        if (tempContactCode == skivvy.CODE_EMAIL_CONF) {
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
                                                        tempPhoneNumberIndex
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
                if (!this.isContactPresent) {
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
                    if (tempContact == contact.displayName?.toLowerCase(skivvy.locale) || tempContact == fName?.toLowerCase(
                            skivvy.locale
                        ) ||
                        !contact.nickName.isNullOrEmpty() && contact.nickName!!.contains(tempContact)
                    ) {
                        this.isContactPresent = true
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
                            if (tempContactCode == skivvy.CODE_EMAIL_CONF) {
                                speakOut(getString(what_is_subject), skivvy.CODE_EMAIL_CONTENT)
                            }
                            eCur.close()
                        } else {
                            if (tempContactCode == skivvy.CODE_EMAIL_CONF) {
                                errorView()
                                speakOut(
                                    getString(you_dont_seem_having) + contact.displayName + getString(
                                        someone_email_address
                                    )
                                )
                            }
                        }
                        if (tempContactCode == skivvy.CODE_CALL_CONF || tempContactCode == skivvy.CODE_SMS_CONF || tempContactCode == skivvy.CODE_WHATSAPP_ACTION) {
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
                                    when (tempContactCode) {
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
                                    if (tempContactCode == skivvy.CODE_CALL_CONF) {
                                        speakOut(
                                            "I've got $size phone numbers of ${contact.displayName}.\nShould I call them at " +
                                                    "${contact.phoneList!![tempPhoneNumberIndex]}?",
                                            skivvy.CODE_CALL_CONF
                                        )
                                    } else if (tempContactCode == skivvy.CODE_SMS_CONF) {
                                        speakOut(
                                            getString(what_is_message),
                                            skivvy.CODE_TEXT_MESSAGE_BODY
                                        )
                                    } else if (tempContactCode == skivvy.CODE_WHATSAPP_ACTION) {
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
                    } else this.isContactPresent = false
                }
            } else {
                errorView()
                speakOut(getString(no_contacts_available))
            }
            cur.close()
            if (!this.isContactPresent) {
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
                        speakOut("Incoming $number")
                        successView(null)
                    }
                    TelephonyManager.CALL_STATE_OFFHOOK -> {
                        waitingView(null)
                        if (lastState == TelephonyManager.CALL_STATE_RINGING) {
                            speakOut("Speaking to $number")
                        } else {
                            speakOut("Calling $number")
                        }
                    }
                    TelephonyManager.CALL_STATE_IDLE -> {
                        if (lastState == TelephonyManager.CALL_STATE_RINGING) {
                            speakOut("You missed a call from $number")
                            errorView()
                        } else if (lastState == TelephonyManager.CALL_STATE_OFFHOOK) {
                            speakOut("Call ended $number")
                            successView(null)
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
        loading?.startAnimation(exitAnimation)
        speakOut(getString(exit_msg))
        if (skivvy.tts != null) {
            skivvy.tts!!.stop()
            skivvy.tts!!.shutdown()
        }
        super.onDestroy()
    }

    override fun onBackPressed() {
        speakOut(getString(exit_msg))
        loading?.startAnimation(exitAnimation)
        super.onBackPressed()
    }

    private fun normalView() {
        loading?.setImageDrawable(getDrawable(ic_dotsincircle))
        loading?.startAnimation(normalRotate)
        input?.text = null
        outPut?.text = null
        feedback?.text = null
        icon?.setImageDrawable(null)
        contact = ContactModel()
        txt = null
        tempMail = null
        tempMailSubject = null
        tempMailBody = null
        tempPackageIndex = 0
        tempPhoneNumberIndex = 0
        tempContactIndex = 0
        tempEmailIndex = 0
        tempPhone = null
        tempTextBody = null
        tempContactCode = null
        tempContact = null
        isContactPresent = false
    }

    fun waitingView(image: Drawable?) {
        loading?.startAnimation(rotateSlow)
        loading?.setImageDrawable(getDrawable(ic_yellow_dotsincircle))
        if (image != null) {
            icon?.setImageDrawable(image)
        }
    }

    fun errorView(): Boolean {
        loading?.startAnimation(fadeAnimation)
        loading?.setImageDrawable(getDrawable(ic_red_dotsincircle))
        return false
    }

    fun successView(image: Drawable?) {
        loading?.startAnimation(focusRotate)
        loading?.setImageDrawable(getDrawable(ic_green_dotsincircle))
        if (image != null) {
            icon?.setImageDrawable(image)
        }
    }

    private fun speakOut(text: String) {
        outPut?.text = text
        skivvy.tts!!.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onDone(utteranceId: String) {}
            override fun onError(utteranceId: String) {}
            override fun onStart(utteranceId: String) {}
        })
        if (!skivvy.getMuteStatus()) skivvy.tts!!.speak(
            text,
            TextToSpeech.QUEUE_FLUSH,
            null,
            text
        )
        else
            outPut?.text = text
    }

    private fun speakOut(text: String, taskCode: Int) {
        outPut?.text = text
        skivvy.tts!!.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onDone(utteranceId: String) {
                startVoiceRecIntent(taskCode)
            }

            override fun onError(utteranceId: String) {}
            override fun onStart(utteranceId: String) {}
        })
        if (!skivvy.getMuteStatus()) skivvy.tts!!.speak(
            text,
            TextToSpeech.QUEUE_FLUSH,
            null,
            ""
        )
        else
            startVoiceRecIntent(taskCode, text)
    }

    //intent voice recognition, code according to action command, serving activity result
    private fun startVoiceRecIntent(code: Int) {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            .putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            .putExtra(RecognizerIntent.EXTRA_LANGUAGE, skivvy.locale)
            .putExtra(RecognizerIntent.EXTRA_PROMPT, "I'm listening")
        if (intent.resolveActivity(packageManager) != null)
            startActivityForResult(intent, code)
        else {
            errorView()
            speakOut(getString(internal_error))
        }
    }

    //intent voice recognition, code according to action command, serving activity result
    private fun startVoiceRecIntent(code: Int, message: String) {
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
                    speakOut(getString(biometric_off_error))
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    speakOut(getString(verification_unsuccessfull))
                }
            })
    }
}
