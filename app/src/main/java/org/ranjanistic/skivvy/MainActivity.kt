package org.ranjanistic.skivvy

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.admin.DevicePolicyManager
import android.bluetooth.BluetoothAdapter
import android.content.ComponentName
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
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
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.telephony.SmsManager
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
import java.util.*
import java.util.concurrent.Executor
import kotlin.math.*

@ExperimentalStdlibApi
class MainActivity : AppCompatActivity(), RecognitionListener {
    lateinit var skivvy: Skivvy
    private var outPut: TextView? = null
    private var input: TextView? = null
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
    private var tempPackageIndex: Int? = null
    private var tempPhoneNumberIndex: Int? = 0
    private var tempEmailIndex: Int? = 0
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
    private var tempContactCode: Int? = null
    private lateinit var backfall: ImageView
    private lateinit var context: Context
    private var deviceManger: DevicePolicyManager? = null
    private var compName: ComponentName? = null
    private var contact: ContactModel = ContactModel()
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
        loading = findViewById(R.id.loader)
        icon = findViewById(R.id.actionIcon)
        receiver = findViewById(R.id.receiverBtn)
        greet = findViewById(R.id.greeting)
        backfall = findViewById(R.id.backdrop)
        outputStat = findViewById(R.id.outputStatusView)
        outputStat!!.visibility = View.INVISIBLE
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
        setting.setOnClickListener {
            setButtonsClickable(false)
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
        setButtonsClickable(true)
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
                                getString(should_i_call) + "${contact.displayName} at ${contact.phoneList!![tempPhoneNumberIndex!!]}?",
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
                                getString(should_i_text) + "${contact.displayName} at ${contact.phoneList!![tempPhoneNumberIndex!!]}?",
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

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_HEADSETHOOK) {
            setButtonsClickable(false)
            speakOut("")
            normalView()
            startVoiceRecIntent(skivvy.CODE_SPEECH_RECORD)
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            val audio =
                applicationContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            speakOut(
                "Volume at ${(audio.getStreamVolume(AudioManager.STREAM_MUSIC) * 100)
                        / audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC)}%"
            )
        }
        return super.onKeyDown(keyCode, event)
    }

    @Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        setButtonsClickable(true)
        skivvy.tts!!.language = skivvy.locale
        when (requestCode) {
            skivvy.CODE_SPEECH_RECORD -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    val temp =
                        data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)[0].toString()
                            .toLowerCase(skivvy.locale)
                    if (txt == null || txt == "") {
                        txt = temp
                    } else {
                        if (resources.getStringArray(R.array.disruptions).contains(temp)) {
                            txt = null
                            normalView()
                            speakOut(getString(okay))
                        } else {
                            if (temp.contains(txt!!)) {
                                txt = temp
                            } else {
                                txt += temp
                            }
                        }
                    }
                    if (txt != null) {
                        input?.text = txt
                        if (!respondToCommand(txt!!)) {
                            if (!directActions(txt!!)) {
                                if (!appOptions(txt)) {
                                    if (!computerOps(txt!!)) {
                                        errorView()
                                        speakOut(getString(recognize_error))
                                    }
                                }
                            }
                        }
                    }
                } else {
                    normalView()
                    speakOut(getString(no_input))
                }
            }
            skivvy.CODE_APP_CONF -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    txt = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)[0].toString()
                        .toLowerCase(skivvy.locale)
                    if (txt != null) {
                        if (resources.getStringArray(R.array.acceptances).contains(txt)) {
                            if (tempPackageIndex != null) {
                                successView(skivvy.packagesIcon[tempPackageIndex!!])
                                speakOut(getString(opening) + skivvy.packagesAppName[tempPackageIndex!!])
                                startActivity(Intent(skivvy.packagesMain[tempPackageIndex!!]))
                                tempPackageIndex = null
                            } else {
                                errorView()
                                speakOut(getString(null_variable_error))
                            }
                        } else if (resources.getStringArray(R.array.denials).contains(txt) ||
                            resources.getStringArray(R.array.disruptions).contains(txt)
                        ) {
                            normalView()
                            speakOut(getString(okay))
                        } else {
                            speakOut(
                                getString(recognize_error) + "\n" + getString(do_u_want_open) + skivvy.packagesAppName[tempPackageIndex!!] + "?",
                                skivvy.CODE_APP_CONF
                            )
                        }
                    }
                } else {
                    normalView()
                    speakOut(getString(no_input))
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
            skivvy.CODE_LOCK_SCREEN -> {
                if (resultCode == Activity.RESULT_OK) {
                    deviceLockOps()
                } else {
                    errorView()
                    speakOut(getString(device_admin_failure))
                }
            }
            skivvy.CODE_CALL_CONF -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    txt = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)[0].toString()
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
                                if (contact.phoneList != null) {
                                    successView(null)
                                    callingOps(
                                        contact.phoneList!![tempPhoneNumberIndex!!],
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
                            tempPhoneNumberIndex = tempPhoneNumberIndex!! + 1
                            if (contact.phoneList != null && tempPhoneNumberIndex!! < contact.phoneList!!.size && !resources.getStringArray(
                                    R.array.disruptions
                                ).contains(txt)
                            ) {
                                speakOut(
                                    "At ${contact.phoneList!![tempPhoneNumberIndex!!]}?",
                                    skivvy.CODE_CALL_CONF
                                )
                            } else {
                                normalView()
                                speakOut(getString(okay))
                            }
                        }
                        else -> {
                            if (contact.phoneList != null) {
                                speakOut(
                                    getString(recognize_error) + "\n" + getString(should_i_call) + "${contact.displayName} at ${contact.phoneList!![tempPhoneNumberIndex!!]}?",
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
                } else {
                    normalView()
                    speakOut(getString(no_input))
                }
            }

            skivvy.CODE_EMAIL_CONTENT -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    txt = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)[0].toString()
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
                                if (contact.emailList != null) {
                                    if (contact.emailList!!.size == 1) {
                                        speakOut(
                                            getString(body_added) + "\n" +
                                                    getString(should_i_email) + "${contact.displayName} at\n${contact.emailList!![tempEmailIndex!!]}?",
                                            skivvy.CODE_EMAIL_CONF
                                        )
                                    } else {
                                        speakOut(
                                            getString(body_added) + "I've got ${contact.emailList!!.size} addresses of ${contact.displayName}.\n" +
                                                    getString(should_i_email) + "them at\n${contact.emailList!![tempEmailIndex!!]}?",
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
                } else {
                    normalView()
                    speakOut(getString(no_input))
                }
            }

            skivvy.CODE_EMAIL_CONF -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    txt = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)[0].toString()
                        .toLowerCase(skivvy.locale)
                    when {
                        resources.getStringArray(R.array.acceptances).contains(txt) -> {
                            successView(null)
                            speakOut(getString(preparing_email))
                            if (contact.emailList != null) {
                                emailingOps(
                                    contact.emailList!![tempEmailIndex!!],
                                    tempMailSubject,
                                    tempMailBody
                                )
                            } else {
                                emailingOps(tempMail, tempMailSubject, tempMailBody)
                            }
                        }
                        resources.getStringArray(R.array.denials).contains(txt) ||
                                resources.getStringArray(R.array.disruptions).contains(txt) -> {
                            tempEmailIndex = tempEmailIndex!! + 1
                            if (contact.emailList != null && tempEmailIndex!! < contact.emailList!!.size && !resources.getStringArray(
                                    R.array.disruptions
                                ).contains(txt)
                            ) {
                                speakOut(
                                    "At ${contact.emailList!![tempEmailIndex!!]}?",
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
                                            getString(should_i_email) + "${contact.displayName} at\n${contact.emailList!![tempEmailIndex!!]}?",
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
                } else {
                    normalView()
                    speakOut(getString(no_input))
                }
            }

            skivvy.CODE_TEXT_MESSAGE_BODY -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    txt = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)[0].toString()
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
                            if (contact.phoneList != null) {
                                speakOut(
                                    getString(should_i_text) + "${contact.displayName} at ${contact.phoneList!![tempPhoneNumberIndex!!]}" + getString(
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
                } else {
                    normalView()
                    speakOut(getString(no_input))
                }
            }

            skivvy.CODE_SMS_CONF -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    txt = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)[0].toString()
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
                                if (contact.phoneList != null) {
                                    speakOut(getString(sending_sms_at) + "${contact.phoneList!![tempPhoneNumberIndex!!]}")
                                    textMessageOps(
                                        contact.phoneList!![tempPhoneNumberIndex!!]!!,
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
                            tempPhoneNumberIndex = tempPhoneNumberIndex!! + 1
                            if (contact.phoneList != null && tempPhoneNumberIndex!! < contact.phoneList!!.size && !resources.getStringArray(
                                    R.array.disruptions
                                ).contains(txt)
                            ) {
                                speakOut(
                                    "At ${contact.phoneList!![tempPhoneNumberIndex!!]}?",
                                    skivvy.CODE_SMS_CONF
                                )
                            } else {
                                normalView()
                                speakOut(getString(okay))
                            }
                        }
                        else -> {
                            if (contact.phoneList != null) {
                                speakOut(
                                    getString(should_i_text) + "${contact.displayName} at ${contact.phoneList!![tempPhoneNumberIndex!!]}" + getString(
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
                } else {
                    normalView()
                    speakOut(getString(no_input))
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
                locationOps()
            }
            text.contains("lock") -> {
                if (text.contains("screen") || text.contains("device") || text.contains("phone")) {
                    deviceLockOps()
                } else {
                    if (text.replace("lock", "").trim() == "") {
                        txt = "lock"
                        speakOut("Lock what?", skivvy.CODE_SPEECH_RECORD)
                    } else return false
                }
            }
            text.contains("screenshot") || text.contains("snapshot") || text.replace(" ", "")
                .contains("takess") -> {
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
                        txt = "search for "
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
            text.contains("biometric") -> {
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
                            speakOut(getString(physical_auth_request))
                            authStateAction()
                            biometricPrompt.authenticate(promptInfo)
                        }
                        true
                    }
                    else -> {
                        if (text.replace("biometric", "").trim() == "") {
                            txt = "biometric"
                            speakOut("Biometric what?", skivvy.CODE_SPEECH_RECORD)
                            true
                        } else false
                    }
                }
            }
            text == "get permission" -> {
                if (!hasPermissions(context, *skivvy.permissions)) {
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

    //to format the received expression
    private fun expressionize(expression: String): String {
        var finalExpression = expression
        val toBeRemoved = arrayOf(
            " ", "calculate", "compute", "solve", "whatis",
            "what's", "thevalueof", "valueof"
        )
        val toBePercented = arrayOf("%of", "percentof")
        val toBeModded = arrayOf("%", "mod")
        val toBeLogged = arrayOf("naturallogof", "naturallog")
        val toBeLog = arrayOf("logof")
        val toBeMultiplied = arrayOf("x", "multipliedby", "into", "and")
        val toBeDivided = arrayOf("dividedby", "by", "upon", "over")
        val toBeAdded = arrayOf("plus", "or")
        val toBeSubtracted = arrayOf("minus", "negative")
        val toBeNumerized = arrayOf("hundred")
        val toBePowered = arrayOf(
            "raisedtothepowerof", "raisetothepowerof", "raisedtothepower", "raisetothepower",
            "tothepowerof", "tothepower", "raisedto", "raiseto", "raised", "raise", "kipower"
        )

        val formatArrays = arrayOf(
            toBeRemoved, toBePercented, toBeModded, toBeLogged, toBeLog,
            toBeMultiplied, toBeDivided, toBeAdded, toBeSubtracted, toBeNumerized
            , toBePowered
        )
        val replacingArray = arrayOf("", "p", "m", "ln", "log", "*", "/", "+", "-", "100", "^")
        var formatIndex = 0
        while (formatIndex < formatArrays.size) {
            var formatArrayIndex = 0
            while (formatArrayIndex < formatArrays[formatIndex].size) {
                finalExpression = finalExpression.replace(
                    formatArrays[formatIndex][formatArrayIndex],
                    replacingArray[formatIndex]
                )
                ++formatArrayIndex
            }
            ++formatIndex
        }
        return finalExpression
    }

    //for expression evaluation
    private fun computerOps(expressionString: String): Boolean {
        val expression = expressionize(expressionString)
        if (!expression.contains(skivvy.numberPattern)) {
            return false
        }
        val operatorBool = arrayOfNulls<Boolean>(skivvy.operators.size)
        val functionBool = arrayOfNulls<Boolean>(skivvy.mathFunctions.size)

        var f = 0
        while (f < functionBool.size) {
            functionBool[f] = false
            ++f
        }
        f = 0
        while (f < operatorBool.size) {
            operatorBool[f] = false
            ++f
        }

        /**
         * Storing availability of all operators in given expression, to an array of booleans.
         */
        var opIndex = 0
        while (opIndex < operatorBool.size) {
            operatorBool[opIndex] = expression.contains(skivvy.operators[opIndex])
            ++opIndex
        }
        var funIndex = 0
        while (funIndex < functionBool.size) {
            functionBool[funIndex] = expression.contains(skivvy.mathFunctions[funIndex])
            ++funIndex
        }
        if (!operatorBool.contains(true)) {
            speakOut(functionOperate(expression)!!)
            return true
        }
        /**
         *  The following block stores the position of skivvy.operators in the given expression
         *  in  a new array (of Integers), which will help the further block of code to contain
         *  and create a distinction between operands (numbers) and skivvy.operators.
         */

        var expIndex = 0
        var totalOps = 0
        while (expIndex < expression.length) {
            opIndex = 0
            while (opIndex < operatorBool.size) {
                if (expression[expIndex] == skivvy.operators[opIndex]) {
                    ++totalOps              //counting total
                }
                ++opIndex
            }
            ++expIndex
        }
        if (totalOps == 0) {
            return false
        }
        expIndex = 0
        val expOperatorPos = arrayOfNulls<Int>(totalOps)
        var expOpIndex = 0
        while (expIndex < expression.length) {
            opIndex = 0
            while (opIndex < operatorBool.size) {
                if (expression[expIndex] == skivvy.operators[opIndex]) {
                    expOperatorPos[expOpIndex] = expIndex         //saving operator positions
                    ++expOpIndex
                }
                ++opIndex
            }
            ++expIndex
        }

        var localExp = expression
        var kk = 0
        while (kk < skivvy.operators.size) {
            localExp = localExp.replace(skivvy.operators[kk].toString(), "")
            ++kk
        }
        kk = 0
        while (kk < skivvy.mathFunctions.size) {
            localExp = localExp.replace(skivvy.mathFunctions[kk], "")
            ++kk
        }
        localExp = localExp.replace(skivvy.numberPattern, "")
        localExp = localExp.replace(".", "")
        if (localExp != "") {
            return false
        }
        /**
         * The following block extracts values from given expression, char by char, and stores them
         * in an array of Strings, by grouping digits in form of numbers at the same index as string,
         * and skivvy.operators in the expression at a separate index if array of Strings.
         *  For ex - Let the given expression be :   1234/556*89+4-23
         *  Starting from index = 0, the following block will store digits till '/'  at index =0 of empty array of Strings, then
         *  will store '/' itself at index =  1 of empty array of Strings. Then proceeds to store 5, 5  and 6
         *  at the same index = 2 of e.a. of strings. And stores the next operator '*' at index = 3, and so on.
         *  Thus a distinction between operands and operators is created and stored in a new array (of strings).
         */

        val arrayOfExpression = arrayOfNulls<String>(2 * totalOps + 1)
        var expArrayIndex = 0
        var positionInExpression = expArrayIndex
        var positionInOperatorPos = positionInExpression
        while (positionInOperatorPos < expOperatorPos.size && positionInExpression < expression.length) {
            while (positionInExpression < expOperatorPos[positionInOperatorPos]!!) {
                if (arrayOfExpression[expArrayIndex] == null) {
                    arrayOfExpression[expArrayIndex] = expression[positionInExpression].toString()
                } else {
                    arrayOfExpression[expArrayIndex] += expression[positionInExpression].toString()
                }
                ++positionInExpression
            }
            ++expArrayIndex
            if (positionInExpression == expOperatorPos[positionInOperatorPos]) {
                if (arrayOfExpression[expArrayIndex] == null) {
                    arrayOfExpression[expArrayIndex] = expression[positionInExpression].toString()
                } else {
                    arrayOfExpression[expArrayIndex] += expression[positionInExpression].toString()
                }
                ++expArrayIndex
            }
            ++positionInExpression
            ++positionInOperatorPos
            if (positionInOperatorPos >= expOperatorPos.size) {
                while (positionInExpression < expression.length) {
                    if (arrayOfExpression[expArrayIndex] == null) {
                        arrayOfExpression[expArrayIndex] =
                            expression[positionInExpression].toString()
                    } else {
                        arrayOfExpression[expArrayIndex] += expression[positionInExpression].toString()
                    }
                    ++positionInExpression
                }
            }
        }
        //if operator comes first, place zero at null index
        if (arrayOfExpression[0] == null) {
            arrayOfExpression[0] = "0"
        }
        if (arrayOfExpression[arrayOfExpression.size - 1] == null) return false

        //operator in place validity check
        var l = 0
        var k = 0
        while (l < arrayOfExpression.size && k < arrayOfExpression.size) {
            if (arrayOfExpression[l] != null && arrayOfExpression[k] != null) {
                if (arrayOfExpression[k]!!.contains(skivvy.nonNumeralPattern)
                    && !arrayOfExpression[k]!!.contains(".")
                    && !functionBool.contains(true)
                ) {
                    return false
                }
            } else return false
            ++l
            k += 2
        }

        //Solves predefined mathematical functions.
        if (functionBool.contains(true)) {
            var fin = 0
            while (fin < arrayOfExpression.size) {
                if (arrayOfExpression[fin]!!.contains(skivvy.textPattern)) {
                    if (!arrayOfExpression[fin]!!.contains(skivvy.numberPattern)) {
                        return false
                    }
                    arrayOfExpression[fin] = functionOperate(arrayOfExpression[fin]!!)
                    if (!arrayOfExpression[fin]!!.contains(skivvy.numberPattern)) {
                        return false
                    }
                }
                ++fin
            }
        }
        //validating final expression
        var finalCheckIndex = 0
        while (finalCheckIndex < arrayOfExpression.size) {
            if (arrayOfExpression[finalCheckIndex] != null) {
                if (arrayOfExpression[finalCheckIndex]!!.contains(skivvy.textPattern) &&
                    arrayOfExpression[finalCheckIndex]!!.length > 1
                ) {
                    return false
                }
            } else {
                return false
            }
            ++finalCheckIndex
        }

        /**
         * Now, as we have the new array of strings, having the proper
         * expression, with skivvy.operators at every even position of the array (at odd indices),
         * the following block of code will evaluate the expression according to the BODMAS rule.
         */

        var nullPosCount = 0
        opIndex = 0
        while (opIndex < operatorBool.size) {
            var opPos = 1
            while (opPos < arrayOfExpression.size - nullPosCount) {
                if (arrayOfExpression[opPos] == skivvy.operators[opIndex].toString()) {
                    if (arrayOfExpression[opPos] == "-") {
                        arrayOfExpression[opPos + 1] =
                            (0 - arrayOfExpression[opPos + 1]!!.toFloat()).toString()
                        arrayOfExpression[opPos] = "+"
                    }
                    arrayOfExpression[opPos - 1] = operate(
                        arrayOfExpression[opPos - 1]!!.toFloat(),
                        arrayOfExpression[opPos]!!.toCharArray()[0],
                        arrayOfExpression[opPos + 1]!!.toFloat()
                    ).toString()
                    var j = opPos
                    while (j + 2 < arrayOfExpression.size) {
                        arrayOfExpression[j] = arrayOfExpression[j + 2]
                        ++j
                    }
                    nullPosCount += 2
                    if (arrayOfExpression.size > 3 &&
                        arrayOfExpression[opPos] == skivvy.operators[opIndex].toString()
                    ) {    //if replacing operator is same as the replaced one
                        opPos -= 2            //index two indices back so that it returns at same position again
                    }
                }
                opPos += 2        //next index of operator in array of expression
            }
            ++opIndex       //next operator
        }
        //final result stored at index = 0
        if (arrayOfExpression[0]!!.toFloat() - arrayOfExpression[0]!!.toFloat().toInt() == 0F) {
            speakOut(
                arrayOfExpression[0]!!.toFloat().toInt().toString()
            )
        } else {
            speakOut(arrayOfExpression[0]!!)
        }
        return true
    }

    private fun functionOperate(func: String): String? {
        return when {
            func.contains("sin") -> sin(
                func.replace(skivvy.textPattern, "").toFloat() * (PI / 180)
            ).toString()
            func.contains("cos") -> cos(
                func.replace(skivvy.textPattern, "").toFloat() * (PI / 180)
            ).toString()
            func.contains("tan") -> tan(
                func.replace(skivvy.textPattern, "").toFloat() * (PI / 180)
            ).toString()
            func.contains("cot") -> (1 / tan(
                func.replace(skivvy.textPattern, "").toFloat() * (PI / 180)
            )).toString()
            func.contains("sec") -> (1 / cos(
                func.replace(skivvy.textPattern, "").toFloat() * (PI / 180)
            )).toString()
            func.contains("cosec") -> (1 / sin(
                func.replace(skivvy.textPattern, "").toFloat() * (PI / 180)
            )).toString()
            func.contains("log") -> {
                log(func.replace(skivvy.textPattern, "").toFloat(), 10F).toString()
            }
            func.contains("ln") -> {
                ln1p(func.replace(skivvy.textPattern, "").toFloat()).toString()
            }
            else -> getString(invalid_expression)
        }
    }

    private fun operate(a: Float, operator: Char, b: Float): Float? {
        return when (operator) {
            '/' -> a / b
            '*' -> a * b
            '+' -> a + b
            '-' -> a - b
            'p' -> (a / 100) * b
            'm' -> a % b
            '^' -> a.toDouble().pow(b.toDouble()).toFloat()
            else -> null
        }
    }

    private fun hasPermissions(context: Context, vararg permissions: String): Boolean =
        permissions.all {
            ActivityCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
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

    private fun locationOps() {
        waitingView(getDrawable(ic_location_pointer))
        startActivityForResult(
            Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS),
            skivvy.CODE_LOCATION_SERVICE
        )
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
                    ), skivvy.CODE_LOCK_SCREEN
            )
        }
    }

    private fun volumeOps(action: String) {
        val audioManager: AudioManager =
            applicationContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
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
        if (text != null) {
            localText = text.replace("open", "").trim()
            localText = localText.replace("start", "").trim()
            if (skivvy.packagesTotal > 0) {
                var i = 0
                while (i < skivvy.packagesTotal) {
                    when {
                        localText == getString(app_name).toLowerCase(skivvy.locale) -> {
                            speakOut(getString(i_am) + getString(app_name))
                            return true
                        }
                        localText == skivvy.packagesAppName[i] -> {
                            successView(skivvy.packagesIcon[i])
                            speakOut(getString(opening) + skivvy.packagesAppName[i])
                            startActivity(Intent(skivvy.packagesMain[i]))
                            return true
                        }
                        skivvy.packagesName[i]!!.contains(localText) -> {
                            tempPackageIndex = i
                            waitingView(skivvy.packagesIcon[i])
                            speakOut(
                                getString(do_u_want_open) + skivvy.packagesAppName[i] + "?",
                                skivvy.CODE_APP_CONF
                            )
                            return true
                        }
                        else -> ++i
                    }
                }
            }
        }
        return false
    }

    //action invoking direct intents
    private fun directActions(text: String): Boolean {
        var localTxt: String
        if (text.contains(getString(call))) {
            waitingView(getDrawable(ic_glossyphone))
            localTxt = text.replace(getString(call), "", true).trim()
            tempPhone = text.replace(skivvy.nonNumeralPattern, "")
            if (tempPhone != null) {
                when {
                    tempPhone!!.contains(skivvy.numberPattern) -> {
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
            return true
        } else if (text.contains(getString(email))) {
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
            return true
        } else if (text.contains("text")) {
            waitingView(getDrawable(ic_messageicon))
            localTxt = text.replace("text", "", false)
            localTxt = localTxt.trim()
            tempPhone = localTxt.replace(skivvy.nonNumeralPattern, "")
            when {
                tempPhone!!.contains(skivvy.numberPattern) -> {
                    speakOut(getString(what_is_message), skivvy.CODE_TEXT_MESSAGE_BODY)
                }
                localTxt.length > 1 -> {
                    contactOps(localTxt, skivvy.CODE_SMS_CONF)
                }
                else -> {
                    txt = "text "
                    speakOut("Text who?", skivvy.CODE_SPEECH_RECORD)
                }
            }
            return true
        }
        return false
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

    private fun contactOps(name: String, code: Int) {
        var isContactPresent = false
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
            val cr: ContentResolver = contentResolver
            val cur: Cursor? =
                cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null)
            if (cur?.count!! > 0) {
                while (cur.moveToNext()) {
                    //TODO: Additional Nickname support
                    val dName =
                        cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
                            .toLowerCase(skivvy.locale)
                    val fName = dName.substringBefore(" ")
                    if (tempContact == dName || tempContact == fName) {
                        isContactPresent = true
                        contact.contactID =
                            cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID))
                        contact.displayName =
                            cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
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
                        val pCur: Cursor? = cr.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            arrayOf(contact.contactID),
                            null
                        )
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
                        if (tempContactCode == skivvy.CODE_CALL_CONF || tempContactCode == skivvy.CODE_SMS_CONF) {
                            if (cur.getString(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))
                                    .toInt() > 0
                            ) {
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
                                    if (tempContactCode == skivvy.CODE_CALL_CONF) {
                                        speakOut(
                                            getString(should_i_call) + "${contact.displayName}?",
                                            skivvy.CODE_CALL_CONF
                                        )
                                    } else if (tempContactCode == skivvy.CODE_SMS_CONF) {
                                        speakOut(
                                            getString(what_is_message),
                                            skivvy.CODE_TEXT_MESSAGE_BODY
                                        )
                                    }
                                } else {
                                    if (tempContactCode == skivvy.CODE_CALL_CONF) {
                                        speakOut(
                                            "I've got $size phone numbers of ${contact.displayName}.\nShould I call them at " +
                                                    "${contact.phoneList!![tempPhoneNumberIndex!!]}?",
                                            skivvy.CODE_CALL_CONF
                                        )
                                    } else if (tempContactCode == skivvy.CODE_SMS_CONF) {
                                        speakOut(
                                            getString(what_is_message),
                                            skivvy.CODE_TEXT_MESSAGE_BODY
                                        )
                                    }
                                }
                            } else {
                                errorView()
                                speakOut(
                                    getString(you_dont_seem_having) + contact.displayName + getString(
                                        someones_phone_number
                                    )
                                )
                            }
                        }
                        pCur!!.close()
                        break
                    } else isContactPresent = false
                }
            } else {
                errorView()
                speakOut(getString(no_contacts_available))
            }
            cur.close()
            if (!isContactPresent) {
                errorView()
                speakOut(getString(contact_not_found))
            }
        }
    }

    //intent voice recognition, code according to action command, serving activity result
    private fun startVoiceRecIntent(code: Int) {
/*
        val speech:SpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        speech.setRecognitionListener(this)
        val intentd = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intentd.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "en")
        intentd.putExtra(
            RecognizerIntent.EXTRA_CALLING_PACKAGE,
            this.packageName
        )
        speech.startListening(intentd)
*/
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            .setFlags(Intent.FLAG_ACTIVITY_LAUNCH_ADJACENT)
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

    public override fun onDestroy() {
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
        contact = ContactModel()
        txt = null
        tempMail = null
        tempMailSubject = null
        tempMailBody = null
        tempPackageIndex = null
        tempPhoneNumberIndex = 0
        tempEmailIndex = 0
        tempPhone = null
        tempTextBody = null
        tempContactCode = null
        tempContact = null
        loading?.setImageDrawable(getDrawable(ic_dotsincircle))
        loading?.startAnimation(normalRotate)
        input?.text = null
        outPut?.text = null
        icon?.setImageDrawable(null)
    }

    private fun waitingView(image: Drawable?) {
        loading?.startAnimation(rotateSlow)
        loading?.setImageDrawable(getDrawable(ic_yellow_dotsincircle))
        if (image != null) {
            icon?.setImageDrawable(image)
        }
    }

    private fun errorView(): Boolean {
        loading?.startAnimation(fadeAnimation)
        loading?.setImageDrawable(getDrawable(ic_red_dotsincircle))
        return false
    }

    private fun successView(image: Drawable?): Boolean {
        loading?.startAnimation(focusRotate)
        loading?.setImageDrawable(getDrawable(ic_green_dotsincircle))
        if (image != null) {
            icon?.setImageDrawable(image)
        }
        return true
    }

    private fun setButtonsClickable(state: Boolean) {
        setting.isClickable = state
    }

    private fun speakOut(text: String) {
        outPut?.text = text
        skivvy.tts!!.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onDone(utteranceId: String) {
//                 outputStat!!.visibility = View.INVISIBLE
            }

            override fun onError(utteranceId: String) {}
            override fun onStart(utteranceId: String) {
                //            outputStat!!.visibility = View.VISIBLE
            }
        })
        if (!skivvy.getMuteStatus()) skivvy.tts!!.speak(text, TextToSpeech.QUEUE_FLUSH, null, "")
    }

    private fun speakOut(text: String, taskCode: Int?) {
        outPut?.text = text
        skivvy.tts!!.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onDone(utteranceId: String) {
                //      outputStat!!.visibility = View.INVISIBLE
                if (taskCode != null) {
                    startVoiceRecIntent(taskCode)
                }
                setButtonsClickable(true)
            }

            override fun onError(utteranceId: String) {}
            override fun onStart(utteranceId: String) {
                setButtonsClickable(false)
//                outputStat!!.visibility = View.VISIBLE
            }
        })
        if (!skivvy.getMuteStatus()) skivvy.tts!!.speak(text, TextToSpeech.QUEUE_FLUSH, null, "")
        else {
            if (taskCode != null) startVoiceRecIntent(taskCode)
        }
    }

    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo
    private fun authStateAction() {
        executor = ContextCompat.getMainExecutor(context)
        biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult
                ) {
                    super.onAuthenticationSucceeded(result)
                    skivvy.setBiometricsStatus(false)
                    speakOut(getString(biometric_is_off))
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
        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(getString(auth_demand_title))
            .setSubtitle(getString(auth_demand_subtitle))
            .setDescription(getString(biometric_auth_explanation))
            .setNegativeButtonText(getString(discard))
            .build()
    }

    private fun getTrainingStatus(): Boolean {
        return getSharedPreferences(skivvy.PREF_HEAD_APP_MODE, MODE_PRIVATE)
            .getBoolean(skivvy.PREF_KEY_TRAINING, false)
    }

    override fun onReadyForSpeech(p0: Bundle?) {
        TODO("Not yet implemented")
    }

    override fun onRmsChanged(p0: Float) {
        TODO("Not yet implemented")
    }

    override fun onBufferReceived(p0: ByteArray?) {
        speakOut(p0.toString())
    }

    override fun onPartialResults(p0: Bundle?) {
        TODO("Not yet implemented")
    }

    override fun onEvent(p0: Int, p1: Bundle?) {
        TODO("Not yet implemented")
    }

    override fun onBeginningOfSpeech() {
        TODO("Not yet implemented")
    }

    override fun onEndOfSpeech() {
        TODO("Not yet implemented")
    }

    override fun onError(p0: Int) {
        TODO("Not yet implemented")
    }

    override fun onResults(p0: Bundle?) {
        TODO("Not yet implemented")
    }
}
