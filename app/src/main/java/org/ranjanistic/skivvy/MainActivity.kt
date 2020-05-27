@file:Suppress("PropertyName")

package org.ranjanistic.skivvy

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.admin.DevicePolicyManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.hardware.camera2.CameraManager
import android.location.LocationManager
import android.media.AudioManager
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.*
import android.provider.CallLog
import android.provider.ContactsContract
import android.provider.MediaStore
import android.provider.Settings
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.telecom.TelecomManager
import android.telephony.PhoneStateListener
import android.telephony.SmsManager
import android.telephony.TelephonyManager
import android.text.format.DateFormat
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.RoundedBitmapDrawable
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import kotlinx.coroutines.delay
import org.ranjanistic.skivvy.R.drawable.*
import org.ranjanistic.skivvy.R.string.*
import org.ranjanistic.skivvy.manager.*
import java.io.File
import java.io.FileOutputStream
import java.util.*
import java.util.concurrent.Executor
import kotlin.collections.ArrayList
import kotlin.properties.Delegates

@ExperimentalStdlibApi
open class MainActivity : AppCompatActivity() {

    lateinit var skivvy: Skivvy

    //TODO: lock screen activity, its brightness, charging status, incoming notifications on lock screen view, charging view
    //TODO: widget for actions (calculations first, or a calculator widget)
    private lateinit var outputText: TextView
    private lateinit var inputText: TextView
    private lateinit var greet: TextView
    private lateinit var feedback: TextView

    private class Animations {
        lateinit var focusDefocusRotate: Animation
        lateinit var zoomInOutRotate: Animation
        lateinit var focusRotate: Animation
        lateinit var zoomInRotate: Animation
        lateinit var fadeOnFadeOff: Animation
        lateinit var waveDamped: Animation
        lateinit var fallDown: Animation
        lateinit var riseUp: Animation
        lateinit var extendDownStartSetup: Animation
        lateinit var slideToRight: Animation
        lateinit var rotateClock: Animation
        lateinit var revolveRotateToLeft: Animation
        lateinit var fadeOff: Animation
        lateinit var fadeOn: Animation
        lateinit var fadeOnFast: Animation
    }

    private val anim = Animations()
    private val CALLTASK = 0
    private val NOTIFTASK = 1

    private var lastTxt: String? = null
    private var tasksOngoing: ArrayList<Boolean> = arrayListOf(false, false)
    private fun anyTaskRunning(): Boolean = tasksOngoing[CALLTASK] || tasksOngoing[NOTIFTASK]
    private lateinit var receiver: ImageButton
    private lateinit var setting: ImageButton
    private lateinit var loading: ImageView
    private lateinit var backfall: ImageView
    private lateinit var icon: ImageView
    private var nothing = String()
    private var space = String()
    private var txt: String? = null
    private lateinit var context: Context
    private lateinit var calculation: CalculationManager
    private lateinit var packages: PackageDataManager
    private lateinit var audioManager: AudioManager
    private lateinit var wifiManager: WifiManager
    private lateinit var recognitionIntent: Intent
    private var input = InputSpeechManager()
    private var contact = ContactModel()
    private var temp: TempDataManager = TempDataManager()
    private var feature: SystemFeatureManager = SystemFeatureManager()
    private lateinit var intentFilter: IntentFilter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        context = this
        skivvy = this.application as Skivvy
        skivvy.isHomePageRunning = true
        setTheme(skivvy.getThemeState())
        setContentView(R.layout.activity_homescreen)
        window.statusBarColor = when (skivvy.shouldFullScreen()) {
            true -> ContextCompat.getColor(context, android.R.color.transparent)
            else -> getSysBarColorByTheme(context, true)
        }
        window.navigationBarColor = when (skivvy.shouldFullScreen()) {
            true -> window.statusBarColor
            else -> getSysBarColorByTheme(context, false)
        }
        hideSysUI()
        setViewAndDefaults()
        loadDefaultAnimations()
        initialView()
        resetVariables()
        setListeners()
        initiateRecognitionIntent()
        skivvy.tts = TextToSpeech(this, TextToSpeech.OnInitListener {
            if (it == TextToSpeech.SUCCESS) {
                skivvy.tts?.language = skivvy.locale
            } else speakOut(getString(output_error))
        })
        setOutput(getString(im_ready))
        if (skivvy.shouldListenStartup())
            startVoiceRecIntent(skivvy.CODE_SPEECH_RECORD)
        else {
            inputText.text = getString(tap_the_button)
        }
    }

    private fun initiateRecognitionIntent(){
        recognitionIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            .putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            .putExtra(RecognizerIntent.EXTRA_LANGUAGE, skivvy.locale)
    }
    private fun getSysBarColorByTheme(
        context: Context,
        isStatus: Boolean
    ): Int {
        return ContextCompat.getColor(
            context,
            when (skivvy.getThemeState()) {
                R.style.LightTheme -> {
                    if (isStatus) R.color.dull_white
                    else R.color.pitch_white
                }
                R.style.BlackTheme -> {
                    if (isStatus) R.color.pitch_black
                    else R.color.dull_white
                }
                R.style.BlueTheme -> {
                    if (isStatus) R.color.blue
                    else R.color.pitch_white
                }
                else -> {
                    if (isStatus) R.color.dead_blue
                    else R.color.charcoal
                }
            }
        )
    }

    private fun setViewAndDefaults() {
        space = skivvy.space
        nothing = skivvy.nothing
        setting = findViewById(R.id.setting)
        outputText = findViewById(R.id.textOutput)
        //TODO: Handy view
        //setHandyView()
        inputText = findViewById(R.id.textInput)
        receiver = findViewById(R.id.receiverBtn)
        feedback = findViewById(R.id.feedbackOutput)
        loading = findViewById(R.id.loader)
        icon = findViewById(R.id.actionIcon)
        greet = findViewById(R.id.greeting)
        backfall = findViewById(R.id.backdrop)
        packages = skivvy.packageDataManager
        audioManager =
            applicationContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        wifiManager =
            applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        calculation = CalculationManager(skivvy)
        greet.text = getString(app_name)
        greet.setCompoundDrawablesWithIntrinsicBounds(dots_in_circle, 0, 0, 0)
        if (skivvy.isLocaleHindi()) {
            greet.typeface = Typeface.DEFAULT
            inputText.typeface = Typeface.DEFAULT
        }
    }

    private fun loadDefaultAnimations() {
        anim.fallDown = AnimationUtils.loadAnimation(context, R.anim.fall_back)
        backfall.startAnimation(anim.fallDown)
        anim.riseUp = AnimationUtils.loadAnimation(context, R.anim.rise_back)
        anim.waveDamped = AnimationUtils.loadAnimation(context, R.anim.bubble_wave)
        receiver.startAnimation(anim.waveDamped)
        greet.startAnimation(anim.waveDamped)
        anim.zoomInOutRotate = AnimationUtils.loadAnimation(context, R.anim.rotate_emerge_demerge)
        anim.focusDefocusRotate = AnimationUtils.loadAnimation(context, R.anim.rotate_focus)
        anim.focusRotate = AnimationUtils.loadAnimation(context, R.anim.rotate_slow)
        anim.fadeOnFadeOff = AnimationUtils.loadAnimation(context, R.anim.fade)
        anim.zoomInRotate = AnimationUtils.loadAnimation(context, R.anim.rotate_exit)
        anim.fadeOff = AnimationUtils.loadAnimation(context, R.anim.fade_off)
        anim.fadeOn = AnimationUtils.loadAnimation(context, R.anim.fade_on)
        anim.fadeOnFast = AnimationUtils.loadAnimation(context, R.anim.fade_on_quick)
        anim.revolveRotateToLeft = AnimationUtils.loadAnimation(context, R.anim.pill_slide_left)
        setting.startAnimation(anim.revolveRotateToLeft)
        anim.rotateClock = AnimationUtils.loadAnimation(context, R.anim.rotate_clock)
        anim.slideToRight = AnimationUtils.loadAnimation(context, R.anim.slide_right)
        anim.extendDownStartSetup =
            AnimationUtils.loadAnimation(context, R.anim.extend_back)
    }

    private fun startSettingAnimate() {
        setting.startAnimation(anim.slideToRight)
        backfall.startAnimation(anim.extendDownStartSetup)
        greet.startAnimation(anim.fadeOff)
        outputText.startAnimation(anim.fadeOff)
        receiver.startAnimation(anim.fadeOff)
        inputText.startAnimation(anim.fadeOff)
    }

    private fun finishAnimate() {
        loading.startAnimation(anim.zoomInRotate)
        setting.startAnimation(anim.slideToRight)
        backfall.startAnimation(
            AnimationUtils.loadAnimation(
                context,
                R.anim.extend_back
            )
        )
        greet.startAnimation(anim.fadeOff)
        outputText.startAnimation(anim.fadeOff)
        receiver.startAnimation(anim.fadeOff)
        inputText.startAnimation(anim.fadeOff)
    }

    private fun startResumeAnimate() {
        greet.text = getString(app_name)
        greet.startAnimation(anim.fadeOn)
        receiver.startAnimation(anim.fadeOn)
        receiver.visibility = View.VISIBLE
        greet.startAnimation(anim.waveDamped)
        receiver.startAnimation(anim.waveDamped)
        setting.startAnimation(anim.revolveRotateToLeft)
        backfall.startAnimation(anim.riseUp)
        outputText.startAnimation(anim.fadeOn)
        inputText.startAnimation(anim.fadeOn)
    }

    private fun setListeners() {
        callStateListener()
        greet.setOnClickListener{
            it.startAnimation(anim.waveDamped)
            speakOut(getString(i_am) + getString(app_name))
        }
        setting.setOnClickListener {
            startSettingAnimate()
        }
        receiver.setOnClickListener {
            initialView(anyTaskRunning())
            if (!anyTaskRunning()) {
                speakOut(nothing, skivvy.CODE_SPEECH_RECORD, parallelReceiver = true)
                setFeedback(nothing)
                setOutput(getString(im_ready))
            } else {
                startVoiceRecIntent(skivvy.CODE_SPEECH_RECORD)
            }
        }
        anim.extendDownStartSetup.setAnimationListener(object :
            Animation.AnimationListener {
            override fun onAnimationEnd(p0: Animation?) {
                startActivity(Intent(context, Setup::class.java))
                overridePendingTransition(R.anim.fade_on, R.anim.fade_off)
            }

            override fun onAnimationRepeat(p0: Animation?) {}
            override fun onAnimationStart(p0: Animation?) {}
        })
    }

    override fun onResume() {
        super.onResume()
        hideSysUI()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus)
            hideSysUI()
    }

    override fun onStart() {
        super.onStart()
        initiateRecognitionIntent()
        this.registerReceiver(this.mBatInfoReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        this.registerReceiver(this.mNotificationReceiver, IntentFilter(skivvy.actionNotification))
        if (isNotificationServiceRunning() && skivvy.showNotifications()) {
            startService(Intent(this, NotificationWatcher::class.java))
        }
        setTheme(skivvy.getThemeState())
    }

    private fun hideSysUI() {
        if (skivvy.shouldFullScreen()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                window.attributes.layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }
            window.decorView.apply {
                systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE
                        or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                        )
            }
        } else return
    }

    override fun onRestart() {
        super.onRestart()
        hideSysUI()
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
                speakOut(
                    if (skivvy.hasPermissions(context)) getString(have_all_permits)
                    else getString(all_permissions_not_granted)
                )
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
            skivvy.CODE_CALL_LOG_REQUEST -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    ArrangeViaLogs().execute(contact.displayName)
                } else {
                    contact.phoneList?.let {
                        speakOut(
                            getString(should_i_call) + contact.displayName +
                                    " at ${it[temp.getPhoneIndex()]}?",
                            skivvy.CODE_CALL_CONF
                        )
                    }
                }
            }
            skivvy.CODE_ANSWER_CALL -> {
                txt?.let { manageIncomingCall(it) }
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
                    if (msgCode.getMessage() != nothing && msgCode.getCode() != 0) {
                        SearchContact().execute(msgCode)
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

    private fun isAcceptingCall(response: String): Boolean? {
        val ignorance = arrayOf("ignore", "standby")
        val acceptance = arrayOf("pick up", "pickup", "answer", "accept")
        val abortions = arrayOf("abort", "discard", "cut the call", "call cut", "reject")
        return when {
            input.containsString(
                input.removeBeforeLastStringsIn(
                    response, arrayOf(ignorance, abortions)
                ), arrayOf(acceptance)
            ) -> {
                Log.d("isAcception", "true")
                true
            }
            input.containsString(
                input.removeBeforeLastStringsIn(
                    response, arrayOf(ignorance, acceptance)
                ), arrayOf(abortions)
            ) -> {
                Log.d("isAcception", "false")
                false
            }
            input.containsString(
                input.removeBeforeLastStringsIn(
                    response, arrayOf(abortions, acceptance)
                ), arrayOf(ignorance)
            ) -> {
                Log.d("isAcception", "null")
                null
            }
            else -> {
                Log.d("isAcception", "nullElse")
                null
            }
        }
    }

    private fun manageIncomingCall(response: String) {
        when (isAcceptingCall(response)) {
            null -> {        //ignoring
                return
            }
            else -> {
                if (!isAcceptingCall(response)?.let {
                        feature.respondToCall(
                            this.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager,
                            this.getSystemService(Context.TELECOM_SERVICE) as TelecomManager,
                            it
                        )
                    }!!) {
                    speakOut("Can't control calling on your phone.", isFeedback = true)
                }
            }
        }
    }

    private fun setFeedback(text: String?, isNotOngoing: Boolean = true) {
        if(isNotOngoing) {
            feedback.startAnimation(anim.fadeOnFast)
            feedback.text = text
        }
    }

    private fun setOutput(text: String?, isNotOngoing: Boolean = true) {
        outputText.startAnimation(anim.fadeOnFast)
        outputText.text = text
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_BACK -> {
                finishAnimate()
            }
            KeyEvent.KEYCODE_HEADSETHOOK -> {
                initialView(anyTaskRunning())
                if (!anyTaskRunning()) {
                    speakOut(nothing, skivvy.CODE_SPEECH_RECORD, parallelReceiver = true)
                    setFeedback(nothing)
                    setOutput(getString(im_ready))
                } else {
                    startVoiceRecIntent(skivvy.CODE_SPEECH_RECORD)
                }
            }
            KeyEvent.KEYCODE_VOLUME_UP -> {
                skivvy.setVoicePreference(normalizeVolume = false)
                if (!anyTaskRunning()) {
                    setFeedback(
                        getString(volume_raised_to_) + "${feature.getMediaVolume(audioManager)}" + getString(
                            percent
                        )
                    )
                }
            }
            KeyEvent.KEYCODE_VOLUME_DOWN -> {
                skivvy.setVoicePreference(normalizeVolume = false)
                if (!anyTaskRunning()) {
                    setFeedback(
                        getString(volume_low_to_) + "${feature.getMediaVolume(audioManager)}" + getString(
                            percent
                        )
                    )
                }
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    private fun inGlobalCommands(text: String?): Boolean {
        if (!respondToCommand(text!!)) {
            if (!directActions(text)) {
                if (!computerOps(text)) {
                    if (!appOptions(text)) {
                        speakOut(getString(recognize_error))
                        return false
                    }
                }
            }
        }
        return true
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        var result: ArrayList<String> = ArrayList(1)
        if (!skivvy.nonVocalRequestCodes.contains(requestCode)) {
            if (data == null || data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                    ?.get(0).toString().toLowerCase(skivvy.locale) == nothing
            ) {
                initialView(anyTaskRunning())
                if (!anyTaskRunning()) {
                    speakOut(getString(no_input))
                }
                return
            } else {
                data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.let { result = it }
                inputText.text = result[0]
                txt = result[0].toLowerCase(skivvy.locale)
            }
        }
        when (requestCode) {
            skivvy.CODE_SPEECH_RECORD -> {
                if (lastTxt != null)
                    txt = lastTxt + space + txt
                inputText.text = txt
                if (!inGlobalCommands(txt)) {
                    errorView()
                    if (skivvy.shouldRetry()) {
                        speakOut(getString(recognize_error), skivvy.CODE_SPEECH_RECORD)
                    } else
                        speakOut(getString(recognize_error))
                }
            }
            skivvy.CODE_VOICE_AUTH_CONFIRM -> {
                if (txt != skivvy.getVoiceKeyPhrase()) {
                    if (skivvy.getBiometricStatus()) {
                        speakOut(
                            getString(vocal_auth_failed) + getString(
                                need_physical_verification
                            )
                        )
                        authStateAction(skivvy.CODE_VOICE_AUTH_CONFIRM)
                        biometricPrompt.authenticate(promptInfo)
                    } else {
                        speakOut(getString(vocal_auth_failed))
                    }
                } else {
                    skivvy.setSecurityPref(vocalAuthOn = false)
                    speakOut(getString(vocal_auth_disabled))
                }
            }
            skivvy.CODE_BIOMETRIC_CONFIRM -> {
                if (txt != skivvy.getVoiceKeyPhrase()) {
                    if (skivvy.getBiometricStatus()) {
                        speakOut(
                            getString(vocal_auth_failed) + getString(
                                need_physical_verification
                            )
                        )
                        authStateAction(skivvy.CODE_BIOMETRIC_CONFIRM)
                        biometricPrompt.authenticate(promptInfo)
                    } else {
                        errorView()
                        speakOut(getString(vocal_auth_failed))
                    }
                } else {
                    skivvy.setSecurityPref(biometricOn = false)
                    speakOut(getString(biometric_is_off))
                }
            }
            skivvy.CODE_VOLUME_CONFIRM -> {
                when (isCooperative(txt!!)) {
                    true -> {
                        skivvy.setVoicePreference(normalizeVolume = false)
                        feature.setMediaVolume(
                            temp.getVolumePercent(),
                            audioManager
                        )
                        speakOut(
                            getString(volume_at_) + " ${temp.getVolumePercent()
                                .toInt()}" + getString(percent)
                        )
                    }
                    false -> {
                        setOutput(getString(okay))
                    }
                    else -> {
                        if (isDisruptive(txt!!)) {
                            initialView()
                            speakOut(getString(okay))
                        } else {
                            speakOut(
                                "Are you sure about the harmful" + "${temp.getVolumePercent()}% volume?",
                                skivvy.CODE_VOLUME_CONFIRM
                            )
                        }
                    }
                }
            }
            skivvy.CODE_APP_CONF -> {
                when (isInitiative(txt!!)) {
                    true -> {
                        successView(packages.getPackageIcon(temp.getPackageIndex()))
                        packages.getPackageAppName(temp.getPackageIndex())?.let {
                            speakOut(
                                getString(opening) + it.capitalize(skivvy.locale)
                            )
                        }
                        startActivity(Intent(packages.getPackageIntent(temp.getPackageIndex())))
                    }
                    false -> {
                        initialView()
                        speakOut(getString(okay))
                    }
                    else -> {
                        if (!inGlobalCommands(txt!!)) {
                            speakOut(
                                getString(recognize_error) + "\n" + getString(do_u_want_open) + "${packages.getPackageAppName(
                                    temp.getPackageIndex()
                                )!!.capitalize(skivvy.locale)}?",
                                skivvy.CODE_APP_CONF
                            )
                        }
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
            skivvy.CODE_SYSTEM_SETTINGS -> {
                if (!Settings.System.canWrite(context)) {
                    speakOut(getString(write_settings_permit_denied))
                    errorView()
                } else {
                    speakOut(getString(say_again), skivvy.CODE_SPEECH_RECORD)
                }
            }
            skivvy.CODE_CALL_CONF -> {

                when (isCooperative(txt!!)) {
                    true -> {
                        if (!skivvy.hasThisPermission(context, skivvy.CODE_CALL_REQUEST)) {
                            speakOut(getString(require_physical_permission))
                            requestThisPermission(skivvy.CODE_CALL_REQUEST)
                        } else {
                            if (temp.getContactPresence()) {
                                successView(null)
                                callingOps(
                                    contact.phoneList!![temp.getPhoneIndex()],
                                    contact.displayName
                                )
                            } else {
                                successView(getDrawable(ic_phone_dialer))
                                callingOps(temp.getPhone())
                                temp.setPhone(null)
                            }
                        }
                    }
                    false -> {
                        temp.setPhoneIndex(temp.getPhoneIndex() + 1)
                        if (temp.getContactPresence() && temp.getPhoneIndex() < contact.phoneList!!.size) {
                            speakOut(
                                getLocalisedString(
                                    getString(at) + "${contact.phoneList!![temp.getPhoneIndex()]}?",
                                    "${contact.phoneList!![temp.getPhoneIndex()]}" + getString(
                                        at
                                    ) + "?"
                                ),
                                skivvy.CODE_CALL_CONF
                            )
                        } else {
                            initialView()
                            speakOut(getString(okay))
                        }
                    }
                    else -> {
                        if (isDisruptive(txt!!)) {
                            initialView()
                            speakOut(getString(okay))
                        } else {
                            if (!inGlobalCommands(txt!!)) {
                                if (temp.getContactPresence()) {
                                    speakOut(
                                        getString(recognize_error) + "\n" + getString(
                                            should_i_call
                                        ) + contact.displayName + space +
                                                getLocalisedString(
                                                    "at ${contact.phoneList!![temp.getPhoneIndex()]}?",
                                                    "को ${contact.phoneList!![temp.getPhoneIndex()]}${getString(
                                                        at
                                                    )}?"
                                                ),
                                        skivvy.CODE_CALL_CONF
                                    )
                                } else {
                                    speakOut(
                                        getString(recognize_error) + "\n" + getString(
                                            should_i_call
                                        ) + "${temp.getPhone()}" + getLocalisedString(
                                            "?",
                                            "${getString(at)}?"
                                        ),
                                        skivvy.CODE_CALL_CONF
                                    )
                                }
                            }
                        }
                    }
                }
            }
            skivvy.CODE_ANSWER_CALL -> {
                if (txt != nothing && txt != null) {
                    if (!skivvy.hasThisPermission(context, skivvy.CODE_ANSWER_CALL)) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            requestThisPermission(skivvy.CODE_ANSWER_CALL)
                            //TODO: picking calls
                        } else speakOut("I can't pick calls on this device.")
                    } else {
                        manageIncomingCall(txt!!)
                    }
                }
            }
            skivvy.CODE_EMAIL_CONTENT -> {
                txt = result[0]
                    .toLowerCase(skivvy.locale)
                if (txt != null) {
                    if (input.isStringInArray(
                            txt!!,
                            arrayOf(resources.getStringArray(R.array.disruptions))
                        )
                    ) {
                        initialView()
                        speakOut(getString(okay))
                    } else {
                        if (temp.getEmailSubject() == null) {
                            temp.setEmailSubject(txt)
                            setFeedback("Subject: ${temp.getEmailSubject()}")
                            speakOut(
                                getString(subject_added) + "\n" + getString(
                                    what_is_body
                                ),
                                skivvy.CODE_EMAIL_CONTENT
                            )
                        } else if (temp.getEmailBody() == null) {
                            temp.setEmailBody(txt)
                            setFeedback("Subject: ${temp.getEmailSubject()}\nBody: ${temp.getEmailBody()}")
                            if (temp.getContactPresence()) {
                                //not displaying total emails because of email content
                                speakOut(
                                    getString(body_added) +
                                            getString(should_i_email) + "${contact.displayName}${space}" +
                                            getLocalisedString(
                                                "at ${contact.emailList!![temp.getEmailIndex()]}?",
                                                "को ${contact.emailList!![temp.getEmailIndex()]}${getString(
                                                    at
                                                )}?"
                                            ),
                                    skivvy.CODE_EMAIL_CONF
                                )
                            } else {
                                speakOut(
                                    getString(body_added) + "\n" +
                                            getString(should_i_email) + "${temp.getEmail()}" + getLocalisedString(
                                        "?",
                                        "${getString(at)}?"
                                    ),
                                    skivvy.CODE_EMAIL_CONF
                                )
                            }
                        }
                    }
                } else if (txt == null) {
                    if (temp.getEmailSubject() == null) {
                        speakOut(
                            getString(recognize_error) + "\n" + getString(
                                what_is_subject
                            ),
                            skivvy.CODE_EMAIL_CONTENT
                        )
                    } else if (temp.getEmailBody() == null) {
                        speakOut(
                            getString(recognize_error) + "\n" + getString(
                                what_is_body
                            ),
                            skivvy.CODE_EMAIL_CONTENT
                        )
                    }
                }
            }
            skivvy.CODE_EMAIL_CONF -> {
                txt = result[0]
                    .toLowerCase(skivvy.locale)
                when (isCooperative(txt!!)) {
                    true -> {
                        successView(null)
                        speakOut(getString(preparing_email))
                        if (temp.getContactPresence()) {
                            emailingOps(
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
                        if (temp.getContactPresence() && temp.getEmailIndex() < contact.emailList!!.size) {
                            speakOut(
                                getLocalisedString(
                                    "${getString(at)}${contact.emailList!![temp.getEmailIndex()]}?",
                                    "${contact.emailList!![temp.getEmailIndex()]}${getString(
                                        at
                                    )}?"
                                )
                                ,
                                skivvy.CODE_EMAIL_CONF
                            )
                        } else {
                            initialView()
                            speakOut(getString(okay))
                        }
                    }
                    else -> {
                        if (isDisruptive(txt!!)) {
                            initialView()
                            speakOut(getString(okay))
                        } else {
                            if (!inGlobalCommands(txt!!)) {
                                if (temp.getContactPresence()) {
                                    speakOut(
                                        getString(recognize_error) + "\n" +
                                                getString(should_i_email) + "${contact.displayName}${space}" +
                                                getLocalisedString(
                                                    getString(at) + "\n${contact.emailList!![temp.getEmailIndex()]}?",
                                                    "को \n${contact.emailList!![temp.getEmailIndex()]}${getString(
                                                        at
                                                    )}?"
                                                ),
                                        skivvy.CODE_EMAIL_CONF
                                    )
                                } else {
                                    speakOut(
                                        getString(recognize_error) + "\n" +
                                                getString(should_i_email) + "${temp.getEmail()}" + getLocalisedString(
                                            "?",
                                            "${getString(at)}?"
                                        ),
                                        skivvy.CODE_EMAIL_CONF
                                    )
                                }
                            }
                        }
                    }
                }
            }

            skivvy.CODE_TEXT_MESSAGE_BODY -> {
                txt = result[0]
                    .toLowerCase(skivvy.locale)
                when (isCooperative(txt!!)) {
                    false -> {
                        initialView()
                        speakOut(getString(okay))
                    }
                    else -> {
                        if (isDisruptive(txt!!)) {
                            initialView()
                            speakOut(getString(okay))
                        } else if (txt != null) {
                            waitingView()
                            temp.setTextBody(txt)
                            if (temp.getContactPresence()) {
                                speakOut(
                                    getString(should_i_text) + contact.displayName+space+
                                            getLocalisedString(
                                                "at${contact.phoneList!![temp.getPhoneIndex()]}",
                                                "को ${contact.phoneList!![temp.getPhoneIndex()]}" + getString(
                                                    at
                                                )
                                            ) + getString(via_sms) + "?",
                                    skivvy.CODE_SMS_CONF
                                )
                            } else {
                                speakOut(
                                    getString(should_i_text) + "${temp.getPhone()}" + getLocalisedString(
                                        nothing,
                                        "${getString(at)}$space"
                                    ) + getString(
                                        via_sms
                                    ) + "?",
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
                txt = result[0]
                    .toLowerCase(skivvy.locale)
                when (isCooperative(txt!!)) {
                    true -> {
                        if (!skivvy.hasThisPermission(context, skivvy.CODE_SMS_REQUEST)) {
                            speakOut(getString(require_physical_permission))
                            requestThisPermission(skivvy.CODE_SMS_REQUEST)
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
                        if (temp.getContactPresence() && temp.getPhoneIndex() < contact.phoneList!!.size && !resources.getStringArray(
                                R.array.disruptions
                            ).contains(txt)
                        ) {
                            speakOut(
                                getLocalisedString(
                                    getString(at) + "${contact.phoneList!![temp.getPhoneIndex()]}?",
                                    "${contact.phoneList!![temp.getPhoneIndex()]}" + getString(
                                        at
                                    ) + "?"
                                ),
                                skivvy.CODE_SMS_CONF
                            )
                        } else {
                            initialView()
                            speakOut(getString(okay))
                        }
                    }
                    else -> {
                        if (isDisruptive(txt!!)) {
                            initialView()
                            speakOut(getString(okay))
                        } else {
                            if (!inGlobalCommands(txt!!)) {
                                if (temp.getContactPresence()) {
                                    speakOut(
                                        getString(should_i_text) + contact.displayName + space +
                                                getLocalisedString(
                                                    getString(at) + "${contact.phoneList!![temp.getPhoneIndex()]}",
                                                    "को ${contact.phoneList!![temp.getPhoneIndex()]}" + getString(
                                                        at
                                                    )
                                                ) + getString(via_sms) + "?",
                                        skivvy.CODE_SMS_CONF
                                    )
                                } else {
                                    speakOut(
                                        getString(should_i_text) + "${temp.getPhone()}" + getLocalisedString(
                                            nothing,
                                            "${getString(at)}$space"
                                        ) + getString(
                                            via_sms
                                        ) + "?",
                                        skivvy.CODE_SMS_CONF
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun setBrightness(value: Int) {
        if (feature.getSystemBrightness(skivvy.cResolver) == null) {        //if brightness setting not found
            speakOut(getString(brightness_inaccessible))
        } else {
            Settings.System.putInt(
                skivvy.cResolver,
                Settings.System.SCREEN_BRIGHTNESS,
                value
            )
            val layoutparams: WindowManager.LayoutParams = window.attributes
            layoutparams.screenBrightness = value / 255.toFloat()
            window.attributes = layoutparams
        }
    }

    //TODO: Setup commands here
    private fun settingsCommand(text: String): Boolean {
        val setters = arrayOf("set","change","turn","enable","disable")
        if(!input.finallySaidLineFromList(text,arrayOf(setters))){
            Log.d("noooooooo","not contains")
            return false
        }else {
            Log.d("noooooooo","contains")
            when {
                text.contains("setup") || text.contains("set up") -> {
                    startActivity(Intent(context, Setup::class.java))
                    overridePendingTransition(R.anim.fade_on, R.anim.fade_off)
                }
                text.contains(skivvy.numberPattern) && text.contains("log base") -> {
                    val base = text.replace(skivvy.nonNumeralPattern, nothing)
                    skivvy.setMathsPref(logBase = base.toFloat().toInt())
                    speakOut("$base is the log base.")
                }
                text.contains("volume") -> {
                    volumeOps(text.replace("volume", nothing).trim())
                }
                text.contains("brightness") -> {
                    brightnessOps(text.replace("brightness", nothing).trim())
                }
                text == "mute" -> {
                    skivvy.setVoicePreference(voiceMute = true)
                    speakOut("Muted")
                }
                text == "speak" || text == "unmute" -> {
                    if (skivvy.getMuteStatus()) {
                        skivvy.setVoicePreference(voiceMute = false)
                        speakOut(getString(okay))
                    } else {
                        speakOut(getString(voice_output_on))
                    }
                }
                text.contains("voice authentication") -> {
                    return when {
                        text.contains("enable") && text.contains("disable") -> {
                            txt = text.replace("enable", nothing)
                            txt = txt!!.replace("disable", nothing)
                            speakOut(
                                getString(enable_or_disable),
                                skivvy.CODE_SPEECH_RECORD
                            )
                            true
                        }
                        text.contains("enable") -> {
                            if (!skivvy.getPhraseKeyStatus()) {
                                if (skivvy.getVoiceKeyPhrase() != null) {
                                    skivvy.setSecurityPref(vocalAuthOn = true)
                                    speakOut(getString(vocal_auth_enabled))
                                } else {
                                    speakOut(getString(vocal_auth_unset))
                                    startActivity(Intent(context, Setup::class.java))
                                }
                            } else {
                                speakOut(getString(vocal_auth_already_on))
                            }
                            true
                        }
                        text.contains("disable") -> {
                            if (!skivvy.getPhraseKeyStatus()) {
                                speakOut(getString(vocal_auth_already_off))
                            } else {
                                speakOut(
                                    getString(get_passphrase_text),
                                    skivvy.CODE_VOICE_AUTH_CONFIRM
                                )
                            }
                            true
                        }
                        else -> {
                            if (text.replace("voice authentication", nothing)
                                    .trim() == nothing
                            ) {
                                txt = text
                                speakOut(
                                    "Vocal authentication what?",
                                    skivvy.CODE_SPEECH_RECORD
                                )
                                true
                            } else false
                        }
                    }
                }
                text.contains("biometric") -> {
                    if (!skivvy.checkBioMetrics()) {
                        speakOut(getString(biometric_unsupported))
                    } else {
                        return when {
                            text.contains("enable") && text.contains("disable") -> {
                                txt = text.replace("enable", nothing)
                                txt = txt!!.replace("disable", nothing)
                                speakOut(
                                    getString(enable_or_disable),
                                    skivvy.CODE_SPEECH_RECORD
                                )
                                true
                            }
                            text.contains("enable") -> {
                                if (skivvy.getBiometricStatus()) {
                                    speakOut(getString(biometric_already_on))
                                } else {
                                    skivvy.setSecurityPref(biometricOn = true)
                                    if (skivvy.getBiometricStatus()) speakOut(
                                        getString(
                                            biometric_on
                                        )
                                    )
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
                                            getString(get_passphrase_text),
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
                                if (text.replace("biometric", nothing).trim() == nothing) {
                                    txt = text
                                    speakOut("Biometric what?", skivvy.CODE_SPEECH_RECORD)
                                    true
                                } else false
                            }
                        }
                    }
                }
                text.contains( "permission") -> {
                    if (!skivvy.hasPermissions(context)) {
                        speakOut(getString(need_all_permissions))
                        requestThisPermission(skivvy.CODE_ALL_PERMISSIONS)
                    } else {
                        speakOut(getString(have_all_permits))
                    }
                }
                else -> return false
            }
        }
        return true
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
        val deviceNames = arrayOf("screen", "phone", "device", "system", "yourself")
        if (!settingsCommand(text))
            when {
                text.contains("battery level") || text.contains("battery status") || text.contains("battery") -> {
                    speakOut("Battery at $batteryLevel%.")
                }
                //TODO: use this as external function, and return special codes for commands here just like isCooperative(): input.removeBeforeLastStringsIn(text, arrayOf(resources.getStringArray(R.array.bt_list)))
                text.contains("bluetooth") -> {
                    //the first function of Skivvy  ;-)
                    if (text.contains("on")) {
                        val stat = feature.bluetooth(true)
                        if (stat == null) {
                            successView(getDrawable(ic_bluetooth))
                            speakOut(getString(bt_already_on))
                        } else {
                            successView(getDrawable(ic_bluetooth))
                            speakOut(getString(bt_on))
                        }
                    } else if (text.contains("off")) {
                        val stat = feature.bluetooth(false)
                        if (stat == null) {
                            errorView(getDrawable(ic_bluetooth))
                            speakOut(getString(bt_already_off))
                        } else {
                            errorView(getDrawable(ic_bluetooth))
                            speakOut(getString(bt_off))
                        }
                    } else {
                        val stat = feature.bluetooth(null)
                        if (stat!!) {
                            successView(getDrawable(ic_bluetooth))
                            speakOut(getString(bt_on))
                        } else {
                            errorView(getDrawable(ic_bluetooth))
                            speakOut(getString(bt_off))
                        }
                    }
                }
                text.contains("wi-fi") || text.contains("wifi") -> {
                    if (text.contains("on") || text.contains("enable")) {
                        when (feature.wirelessFidelity(true, wifiManager)) {
                            null -> {
                                waitingView(getDrawable(ic_wifi_connected))
                                speakOut(getString(wifi_already_on))
                            }
                            true -> {
                                successView(getDrawable(ic_wifi_connected))
                                speakOut(getString(wifi_on))
                            }
                        }
                    } else if (text.contains("off")) {
                        when (feature.wirelessFidelity(false, wifiManager)) {
                            null -> {
                                waitingView(getDrawable(ic_wifi_disconnected))
                                speakOut(getString(wifi_already_off))
                            }
                            false -> {
                                successView(getDrawable(ic_wifi_disconnected))
                                speakOut(getString(wifi_off))
                            }
                        }
                    } else {
                        if (feature.wirelessFidelity(null, wifiManager)!!) {
                            waitingView(getDrawable(ic_wifi_connected))
                            speakOut(getString(wifi_on))
                        } else {
                            waitingView(getDrawable(ic_wifi_disconnected))
                            speakOut(getString(wifi_off))
                        }
                    }
                }
                resources.getStringArray(array[3]).contains(text) -> {
                    waitingView(getDrawable(ic_location_pointer))
                    startActivityForResult(
                        Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS),
                        skivvy.CODE_LOCATION_SERVICE
                    )
                }
                //TODO: reset txt when new input has no enablers/disablers/on/off etc.
                input.containsString(text, arrayOf(deviceNames)) -> {
                    if (text.contains("lock"))
                        deviceLockOps()
                }
                text.contains("screenshot") || text.contains("snapshot") || text.contains("take ss") -> {
                    if (!skivvy.hasThisPermission(context, skivvy.CODE_STORAGE_REQUEST)) {
                        speakOut(getString(require_physical_permission))
                        requestThisPermission(skivvy.CODE_STORAGE_REQUEST)
                    } else {
                        takeScreenshot()
                    }
                }
                text.contains("airplane") -> {
                    if (text.contains("on") || text.contains("enable")) {
                        if (isAirplaneModeEnabled()) {
                            speakOut(getString(R.string.airplane_already_on))
                        } else {
                            speakOut(getString(R.string.airplane_mode_on))
                            setAirplaneMode(true)
                        }
                    } else if (text.contains("off") || text.contains("disable")) {
                        if (!isAirplaneModeEnabled()) {
                            speakOut(getString(R.string.airplane_already_off))
                        } else {
                            speakOut(getString(R.string.airplane_mode_off))
                            setAirplaneMode(false)
                        }
                    }
                }
                text.contains("flash") -> {
                    if (skivvy.isFlashAvailable()) {
                        val mCameraManager =
                            getSystemService(Context.CAMERA_SERVICE) as CameraManager
                        if (text.contains("off")) {
                            feature.setFlashLight(mCameraManager, false)
                            speakOut(getString(flash_off))
                        } else {
                            if (!feature.setFlashLight(mCameraManager, false)) {
                                speakOut(getString(flash_access_error))
                            } else speakOut(getString(flash_on))
                        }
                    } else {
                        speakOut("Flashlight not available")
                    }
                }
                //TODO: Searching
                text == getString(exit) -> {
                    speakOut(getString(exit))
                    finishAnimate()
                    finish()
                }
                else -> return false
            }
        return true
    }

    private fun computerOps(
        rawExpression: String,
        reusing: Boolean = false
    ): Boolean {
        val expression = input.expressionize(rawExpression)
        if (!expression.contains(skivvy.numberPattern))
            return false
        if (expression.length == 3 && !reusing) {
            try {
                val res = calculation.operate(
                    expression[0].toString().toFloat(),
                    expression[1],
                    expression[2].toString().toFloat()
                )
                if (res != null) {
                    speakOut(calculation.returnValidResult(arrayOf(res.toString())))
                    return true
                } else {
                    computerOps(rawExpression, true)
                }
            } catch (e: Exception) {
                computerOps(rawExpression, true)
            }
        }
        /**
         * Storing availability of all operators and functions in given expression,
         * to arrays of booleans as true.
         */
        val operatorsAndFunctionsArray =
            arrayOf(calculation.operators, calculation.mathFunctions)
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
                    setFeedback(expression, !anyTaskRunning())
                    saveCalculationResult(calculation.operateFuncWithConstant(expression)!!)
                    if (getLastCalculationResult().let {
                            calculation.handleExponentialTerm(
                                it
                            )
                        } == nothing) {
                        errorView()
                        speakOut(getString(invalid_expression))
                    } else
                        speakOut(
                            calculation.formatToProperValue(
                                calculation.handleExponentialTerm(
                                    getLastCalculationResult()
                                )
                            )
                        )
                    return true
                } else {
                    errorView()
                    speakOut(getString(invalid_expression))
                    return true
                }
            }
            return false
        }

        val totalOps = calculation.totalOperatorsInExpression(expression)
        if (totalOps == 0 || !calculation.isExpressionOperatable(expression) || calculation.segmentizeExpression(
                expression,
                2 * totalOps + 1
            ) == null
        )
            return false

        var arrayOfExpression =
            calculation.segmentizeExpression(expression, 2 * totalOps + 1)!!

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

        val midOutput = arrayOfExpression.contentToString().replace("[", nothing)
            .replace("]", nothing).replace(",", nothing).replace(space, nothing)
        if (midOutput != rawExpression.replace(space, nothing))
            setFeedback(midOutput, !anyTaskRunning())      //segmentized expression to user

        if (operatorsAndFunctionsBoolean[1].contains(true)) {      //If expression has mathematical functions
            val temp =
                calculation.evaluateFunctionsInExpressionArray(arrayOfExpression)
            if (temp == null) {
                return false
            } else {
                arrayOfExpression = temp
            }
        }

        if (!calculation.isExpressionArrayOnlyNumbersAndOperators(arrayOfExpression))     //if array contains invalid values
            return false
        else {
            saveCalculationResult(calculation.expressionCalculation(arrayOfExpression))
            speakOut(getLastCalculationResult())
        }
        return true
    }

    private fun deviceLockOps() {
        if (skivvy.deviceManager.isAdminActive(skivvy.compName)) {
            successView(getDrawable(ic_locked))
            speakOut(getString(screen_locked))
            skivvy.deviceManager.lockNow()
        } else {
            waitingView(getDrawable(ic_locked))
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

    //TODO: Brightness specifications
    private fun brightnessOps(action: String) {
        when {
            action.contains(skivvy.numberPattern) -> {
                val percent =
                    action.replace(skivvy.nonNumeralPattern, nothing).toFloat().toInt()
                if (percent > 100) {
                    speakOut("Invalid brightness level")
                    return
                }
                setBrightness(percent)
            }
            else -> {
                if (feature.getSystemBrightness(skivvy.cResolver) != null) {
                    speakOut("${feature.getSystemBrightness(skivvy.cResolver)}%")
                } else {
                    speakOut("I couldn't access brightness")
                }
            }
        }
    }

    private fun volumeOps(action: String) {
        when {
            action.contains(skivvy.numberPattern) -> {
                val percent = action.replace(skivvy.nonNumeralPattern, nothing).toFloat()
                if (percent > 100F) {
                    speakOut(getString(invalid_volume_level))
                    return
                }
                if (percent > 75F) {
                    temp.setVolumePercent(percent)
                    speakOut(
                        getString(volume_level_can_harm) + "Are you sure about ${percent.toInt()}%?",
                        skivvy.CODE_VOLUME_CONFIRM
                    )
                } else {
                    skivvy.setVoicePreference(normalizeVolume = false)
                    feature.setMediaVolume(percent, audioManager)
                    speakOut(
                        getString(volume_at_) + "${percent.toInt()}" + getString(R.string.percent),
                        null,
                        isFeedback = !anyTaskRunning()
                    )
                }
            }
            action.contains("up") || action.contains("increase") ||
                    action.contains("raise") -> {
                skivvy.setVoicePreference(normalizeVolume = false)
                if (audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) ==
                    audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                ) {
                    speakOut(getString(volume_highest))
                } else {
                    audioManager.adjustStreamVolume(
                        AudioManager.STREAM_MUSIC,
                        AudioManager.ADJUST_RAISE,
                        AudioManager.FLAG_SHOW_UI
                    )
                    speakOut(
                        getString(R.string.volume_increased),
                        null,
                        parallelReceiver = false,
                        isFeedback = !anyTaskRunning()
                    )
                }
            }
            action.contains("down") || action.contains("decrease") -> {
                skivvy.setVoicePreference(normalizeVolume = false)
                if (
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) ==
                                audioManager.getStreamMinVolume(AudioManager.STREAM_MUSIC)
                    } else {
                        audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) == 0
                    }
                ) {
                    speakOut(getString(volume_lowest))
                } else {
                    audioManager.adjustStreamVolume(
                        AudioManager.STREAM_MUSIC,
                        AudioManager.ADJUST_LOWER,
                        AudioManager.FLAG_SHOW_UI
                    )
                    speakOut(
                        getString(volume_decreased),
                        null,
                        isFeedback = !anyTaskRunning()
                    )
                }
            }
            action.contains("max") ||
                    action.contains("full") || action.contains("highest") -> {
                skivvy.setVoicePreference(normalizeVolume = false)
                audioManager.setStreamVolume(
                    AudioManager.STREAM_MUSIC,
                    audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC),
                    AudioManager.FLAG_SHOW_UI
                )
                speakOut(getString(volume_highest))
            }
            action.contains("min") || action.contains("lowest") -> {
                skivvy.setVoicePreference(normalizeVolume = false)
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
                    speakOut(getString(min_audible_volume))
                } else {
                    volumeOps(getString(ten_percent))
                }
            }
            action.contains("silence") || action.contains("zero") -> {
                skivvy.setVoicePreference(normalizeVolume = false)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    audioManager.setStreamVolume(
                        AudioManager.STREAM_MUSIC,
                        audioManager.getStreamMinVolume(AudioManager.STREAM_MUSIC),
                        AudioManager.FLAG_SHOW_UI
                    )
                    speakOut(getString(volume_off))
                } else {
                    volumeOps(getString(zero_percent))
                }
            }
            else -> {
                audioManager.adjustStreamVolume(
                    AudioManager.STREAM_MUSIC,
                    AudioManager.ADJUST_SAME,
                    AudioManager.FLAG_SHOW_UI
                )
                speakOut(
                    getString(volume_at_) + "${feature.getMediaVolume(audioManager)}" + getString(
                        percent
                    )
                )
            }
        }
    }

    //TODO: specific app actions
//actions invoking other applications
    private fun appOptions(text: String?): Boolean {
        var localText: String
        if (text != null) {
            localText = input.removeBeforeLastStringsIn(
                text,
                arrayOf(resources.getStringArray(R.array.initiators))
            )
            if (localText == nothing) {
                localText = input.removeStringsIn(
                    text,
                    arrayOf(resources.getStringArray(R.array.initiators))
                )
                if (localText == nothing) {
                    speakOut("Open what?", skivvy.CODE_SPEECH_RECORD)
                    return true
                }
            }
            if (packages.getTotalPackages() > 0) {
                var i = 0
                while (i < packages.getTotalPackages()) {
                    if (packages.packageAppName() == null || packages.packagesName() == null || packages.packagesMain() == null) {
                        speakOut(
                            "What?",
                            skivvy.CODE_SPEECH_RECORD,
                            parallelReceiver = true
                        )
                        return true
                    }
                    //TODO: Retry on function end successful preference
                    when {
                        localText == getString(app_name).toLowerCase(skivvy.locale) -> {
                            initialView()
                            speakOut(getString(i_am) + getString(app_name))
                            return true
                        }
                        localText == packages.getPackageAppName(i) -> {
                            temp.setPackageIndex(i)
                            successView(packages.getPackageIcon(i))
                            speakOut(
                                getString(opening) + packages.getPackageAppName(i)!!
                                    .capitalize(skivvy.locale)
                            )
                            startActivity(Intent(packages.getPackageIntent(i)))
                            return true
                        }
                        packages.getPackageName(i)!!.contains(localText) -> {
                            temp.setPackageIndex(i)
                            waitingView(packages.getPackageIcon(i))
                            if (packages.getPackageAppName(i)!!.contains(localText)) {
                                speakOut(
                                    "Did you mean ${packages.getPackageAppName(i)!!
                                        .capitalize(skivvy.locale)}?",
                                    skivvy.CODE_APP_CONF
                                )
                            } else {
                                speakOut(
                                    getString(do_u_want_open) + "${packages.getPackageAppName(
                                        i
                                    )!!
                                        .capitalize(skivvy.locale)}?",
                                    skivvy.CODE_APP_CONF
                                )
                            }
                            return true
                        }
                        else -> ++i
                    }
                }
            } else {
                speakOut(getString(no_apps_installed))
            }
        } else speakOut(getString(null_variable_error))
        return false
    }

    private fun formatPhoneNumber(number: String): String {
        var num = number.replace(space, nothing).trim()
        when {
            num.length == 10 -> {
                val local = num
                num = nothing
                var k = 0
                while (k < local.length) {
                    num += local[k]
                    if (k == 4) {
                        num += space
                    }
                    ++k
                }
            }
            num.hasCountryCode("IN") -> {
                val local = num
                num = nothing
                var k = 0
                while (k < local.length) {
                    num += local[k]
                    if (k == 2 || k == 7) {
                        num += space
                    }
                    ++k
                }
            }
            num.isLandLine() -> {
                val local = num
                num = nothing
                var k = 0
                while (k < local.length) {
                    num += local[k]
                    if (k == 3) {
                        num += space
                    }
                    ++k
                }
            }
            else -> return number
        }
        return num
    }

    private
    var msgCode = MessageCode()

    //action invoking direct intents
    private fun directActions(text: String): Boolean {
        var localTxt: String
        when {
            //TODO: continued calculation
            text.contains(getString(call)) -> {
                waitingView(getDrawable(ic_phone_dialer))
                localTxt = text.replace(getString(call), nothing, true).trim()
                temp.setPhone(text.replace(skivvy.nonNumeralPattern, nothing))
                if (temp.getPhone() != null) {
                    when {
                        temp.getPhone()!!.contains(skivvy.numberPattern) -> {
                            temp.setPhone(formatPhoneNumber(temp.getPhone()!!))
                            if (!skivvy.hasThisPermission(context, skivvy.CODE_CALL_REQUEST)) {
                                speakOut(getString(require_physical_permission))
                                requestThisPermission(skivvy.CODE_CALL_REQUEST)
                            } else {
                                speakOut(
                                    getString(should_i_call) + "${temp.getPhone()}?",
                                    skivvy.CODE_CALL_CONF
                                )
                            }
                        }
                        else -> {
                            if (localTxt.length > 1) {
                                msgCode.setValues(
                                    localTxt,
                                    skivvy.CODE_CALL_CONF
                                )
                                if (skivvy.hasThisPermission(
                                        context,
                                        skivvy.CODE_CONTACTS_REQUEST
                                    )
                                ) {
                                    SearchContact().execute(msgCode)
                                } else {
                                    speakOut(
                                        getString(
                                            require_physical_permission
                                        )
                                    )
                                    requestThisPermission(skivvy.CODE_CONTACTS_REQUEST)
                                }
                            } else {
                                lastTxt = text
                                speakOut(getString(call_who_), skivvy.CODE_SPEECH_RECORD)
                            }
                        }
                    }
                } else {
                    lastTxt = text
                    speakOut(getString(call_who_), skivvy.CODE_SPEECH_RECORD)
                }
            }
            text.contains(getString(email)) -> {
                waitingView(getDrawable(ic_envelope_open))
                localTxt = text.replace(getString(email), nothing, true).trim()
                temp.setEmail(localTxt.replace(space, nothing).trim())
                when {
                    temp.getEmail()!!.matches(skivvy.emailPattern) -> {
                        inputText.text = temp.getEmail()
                        speakOut(
                            getString(what_is_subject),
                            skivvy.CODE_EMAIL_CONTENT
                        )
                    }
                    localTxt.length > 1 -> {
                        msgCode.setValues(localTxt, skivvy.CODE_EMAIL_CONF)
                        if (skivvy.hasThisPermission(context, skivvy.CODE_CONTACTS_REQUEST)) {
                            SearchContact().execute(msgCode)
                        } else {
                            speakOut(getString(require_physical_permission))
                            requestThisPermission(skivvy.CODE_CONTACTS_REQUEST)
                        }
                    }
                    else -> {
                        lastTxt = text
                        speakOut(getString(email_who_), skivvy.CODE_SPEECH_RECORD)
                    }
                }
            }
            text.contains(getString(R.string.text)) -> {
                waitingView(getDrawable(ic_message))
                localTxt = text.replace(getString(R.string.text), nothing, false)
                localTxt = localTxt.trim()
                temp.setPhone(
                    localTxt.replace(
                        skivvy.nonNumeralPattern,
                        nothing
                    )
                )
                when {
                    temp.getPhone()!!.contains(skivvy.numberPattern) -> {
                        speakOut(
                            getString(what_is_message),
                            skivvy.CODE_TEXT_MESSAGE_BODY
                        )
                    }
                    localTxt.length > 1 -> {
                        msgCode.setValues(localTxt, skivvy.CODE_SMS_CONF)
                        if (skivvy.hasThisPermission(context, skivvy.CODE_CONTACTS_REQUEST)) {
                            SearchContact().execute(msgCode)
                        } else {
                            speakOut(getString(require_physical_permission))
                            requestThisPermission(skivvy.CODE_CONTACTS_REQUEST)
                        }
                    }
                    else -> {
                        lastTxt = text
                        speakOut(getString(text_who_), skivvy.CODE_SPEECH_RECORD)
                    }
                }
            }
            else -> return false
        }
        return true
    }

    private fun textMessageOps(
        target: String,
        payLoad: String,
        code: Int
    ) {
        if (code == skivvy.CODE_SMS_CONF) {
            try {
                successView(null)
                val sms: SmsManager = SmsManager.getDefault()
                sms.sendTextMessage(target, null, payLoad, null, null)
            } catch (e: Exception) {
                speakOut("Failed to send SMS")
            }
        } else {
            speakOut("Not yet supported")
        }
    }

    @SuppressLint("MissingPermission")
    private fun callingOps(number: String?, name: String? = null) {
        if (number != null) {
            if (skivvy.hasThisPermission(context, skivvy.CODE_CALL_REQUEST)) {
                name?.let { speakOut(getString(calling) + name) }
                startActivity(Intent(Intent.ACTION_CALL, Uri.parse("tel:$number")))
            } else {
                requestThisPermission(skivvy.CODE_CALL_REQUEST)
            }
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

    inner class MessageCode {
        private var message: String = nothing
        private var code: Int = 0
        fun setValues(message: String, code: Int) {
            this.message = message
            this.code = code
        }

        fun getMessage(): String = this.message
        fun getCode(): Int = this.code
    }

    inner class SearchContact : AsyncTask<MessageCode, Void, ContactModel>() {
        override fun onPreExecute() {
            super.onPreExecute()
            temp.setPhoneIndex(0)
            temp.setEmailIndex(0)
            waitingView()
            speakOut(getString(searching))
        }

        override fun doInBackground(vararg params: MessageCode): ContactModel? {
            temp.setContactCode(params[0].getCode())
            return contactOps(params[0].getMessage())
        }

        override fun onPostExecute(result: ContactModel?) {
            super.onPostExecute(result)
            handleSearchResults(result)
        }
    }

    private fun handleSearchResults(result: ContactModel?) {
        if (result == null) {
            errorView()
            speakOut(getString(no_contacts_available))
        } else if (!temp.getContactPresence()) {
            errorView()
            speakOut(getString(contact_not_found),
                taskCode = if(skivvy.shouldRetry()) skivvy.CODE_SPEECH_RECORD
                else null
            )
        } else {
            waitingView(cropToCircle(result.photoID))
            when (temp.getContactCode()) {
                skivvy.CODE_EMAIL_CONF -> {
                    if (result.emailList.isNullOrEmpty()) {
                        errorView()
                        speakOut(
                            getString(you_dont_seem_having) + result.displayName + getString(
                                someone_email_address
                            )
                        )
                    } else {
                        speakOut(
                            getString(what_is_subject),
                            skivvy.CODE_EMAIL_CONTENT
                        )
                    }
                }
                else -> {
                    when (temp.getContactCode()) {
                        skivvy.CODE_CALL_CONF -> {
                            when {
                                result.phoneList.isNullOrEmpty() -> {
                                    errorView()
                                    speakOut(
                                        getString(you_dont_seem_having) + result.displayName + getString(
                                            someones_phone_number
                                        )
                                    )
                                }
                                result.phoneList!!.size == 1 -> {
                                    speakOut(
                                        getString(should_i_call) + "${result.displayName}?",
                                        skivvy.CODE_CALL_CONF
                                    )
                                }
                                else -> {
                                    setFeedback(
                                        getString(i_have_) + result.phoneList!!.size + getString(
                                            _phone_nums_of_
                                        ) + result.displayName, !tasksOngoing[NOTIFTASK]
                                    )
                                    if (skivvy.hasThisPermission(
                                            context,
                                            skivvy.CODE_CALL_LOG_REQUEST
                                        )
                                    ) {
                                        ArrangeViaLogs().execute(result.displayName)        //to arrange phone list according to recently called
                                    } else {
                                        speakOut("I can call ${result.displayName} at their recent number, if you allow me to access call logs.")
                                        requestThisPermission(skivvy.CODE_CALL_LOG_REQUEST)
                                    }
                                }
                            }
                        }
                        skivvy.CODE_SMS_CONF -> {
                            if (result.phoneList == null || result.phoneList!!.isEmpty()) {
                                errorView()
                                speakOut(
                                    getString(you_dont_seem_having) + result.displayName + getString(
                                        someones_phone_number
                                    )
                                )
                            } else {
                                speakOut(
                                    getString(what_is_message),
                                    skivvy.CODE_TEXT_MESSAGE_BODY
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    //to arrange phone list in background according to recent calls of given contact name
    inner class ArrangeViaLogs : AsyncTask<String, Void, Array<String?>>() {
        override fun onPreExecute() {
            super.onPreExecute()
            temp.setPhoneIndex(0)
        }

        override fun doInBackground(vararg params: String): Array<String?>? {
            return arrangeNumbersAsCallLogs(params[0])
        }

        override fun onPostExecute(result: Array<String?>?) {
            super.onPostExecute(result)
            result?.let {
                speakOut(
                    getString(should_i_call) + contact.displayName +
                            " at ${result[temp.getPhoneIndex()]}?",
                    skivvy.CODE_CALL_CONF
                )
            }
        }
    }

//TODO: match strings with first letter and so on

    // for nicknames
    private fun getNicknamesOf(contactID: String): Array<String?>? {
        var nickNames: Array<String?>? = null
        val cursor: Cursor? = skivvy.cResolver.query(
            ContactsContract.Data.CONTENT_URI,
            arrayOf(ContactsContract.CommonDataKinds.Nickname.NAME),
            ContactsContract.Data.CONTACT_ID + " = ? AND " + ContactsContract.Data.MIMETYPE + " = ?",
            arrayOf(
                contactID, ContactsContract.CommonDataKinds.Nickname.CONTENT_ITEM_TYPE
            ),
            null
        )
        cursor?.let {
            if (it.count == 0) return null
            var nc = 0
            nickNames = arrayOfNulls(it.count)
            while (it.moveToNext()) {
                nickNames!![nc] = it.getString(0)?.toLowerCase(skivvy.locale)
                ++nc
            }
            it.close()
            return input.removeDuplicateStrings(nickNames!!)
        }
        cursor?.close()
        return nickNames
    }

    private fun getPhoneNumbersOf(contactID: String): Array<String?>? {
        var phones: Array<String?>? = null
        val cursor: Cursor? = skivvy.cResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER),
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
            arrayOf(contactID),
            null
        )
        cursor?.let {
            if (it.count == 0) return null
            phones = arrayOfNulls(it.count)
            var k = 0
            while (it.moveToNext()) {
                phones!![k] = formatPhoneNumber(it.getString(0))
                ++k
            }
            it.close()
            return input.removeDuplicateStrings(phones!!)
        }
        cursor?.close()
        return phones
    }

    private fun getEmailIDsOf(contactID: String): Array<String?>? {
        var emails: Array<String?>? = null
        val cursor: Cursor? = skivvy.cResolver.query(
            ContactsContract.CommonDataKinds.Email.CONTENT_URI,
            arrayOf(ContactsContract.CommonDataKinds.Email.DATA),
            ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?",
            arrayOf(contactID),
            null
        )
        cursor?.let {
            if (it.count == 0) return null
            emails = arrayOfNulls(it.count)
            var k = 0
            while (it.moveToNext()) {
                emails!![k] = it.getString(0)
                ++k
            }
            it.close()
            return input.removeDuplicateStrings(emails!!)
        }
        cursor?.close()
        return emails
    }

    val tag = "contactLook"
    private fun getNameAndImageUri(number: String): ArrayList<String?>? {
        val projection = arrayOf(
            ContactsContract.PhoneLookup.DISPLAY_NAME,
            ContactsContract.PhoneLookup.NUMBER,
            ContactsContract.PhoneLookup.PHOTO_URI,
            ContactsContract.PhoneLookup.HAS_PHONE_NUMBER
        )

        // encode the phone number and build the filter URI
        val contactUri = Uri.withAppendedPath(
            ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
            Uri.encode(number)
        )
        var contactName: String? = null
        var image: String? = null
        // query time
        val cursor = skivvy.cResolver.query(
            contactUri,
            projection, null, null, null
        )
        cursor?.let {
            if (it.moveToFirst()) {
                contactName =
                    it.getString(it.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME))
                image = it.getString(it.getColumnIndex(ContactsContract.PhoneLookup.PHOTO_URI))
            }
            it.close()
            Log.d(tag, "returns id")
            return arrayListOf(contactName, image)
        }
        cursor?.close()
        return null
    }

    //TODO: Create suitable projection and search argument for cursor
    private fun contactOps(keyPhrase: String): ContactModel? {
        val cursor: Cursor? = skivvy.cResolver.query(
            ContactsContract.Contacts.CONTENT_URI,
            arrayOf(
                ContactsContract.Contacts._ID,
                ContactsContract.Contacts.DISPLAY_NAME,
                ContactsContract.Contacts.PHOTO_URI
            ),
            null,
            null,
            null
        )
        cursor?.let {
            if (it.count > 0) {
                while (it.moveToNext()) {
                    contact.contactID = it.getString(0)
                    contact.displayName = it.getString(1)
                    contact.nickName = getNicknamesOf(contact.contactID)
                    temp.setContactReceived(keyPhrase.trim())
                    if (temp.getContactReceived() == contact.displayName.toLowerCase(skivvy.locale)
                        || temp.getContactReceived() == contact.displayName.substringBefore(space)
                            .toLowerCase(skivvy.locale)
                        || !contact.nickName.isNullOrEmpty() && contact.nickName!!.contains(temp.getContactReceived())
                    ) {
                        temp.setContactPresence(true)
                        it.getString(2)?.let { uri -> contact.photoID = uri }
                        contact.emailList = getEmailIDsOf(contact.contactID)
                        contact.phoneList = getPhoneNumbersOf(contact.contactID)
                        break
                    } else
                        temp.setContactPresence(false)
                }
                it.close()
                return contact
            } else {
                it.close()
                return null
            }
        }
        cursor?.close()
        return null
    }

    private fun cropToCircle(uri: String?): RoundedBitmapDrawable? {
        if (uri == null) return null
        val rb: RoundedBitmapDrawable =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                RoundedBitmapDrawableFactory.create(
                    resources,
                    try {
                        ImageDecoder.decodeBitmap(
                            ImageDecoder.createSource(
                                context.contentResolver,
                                Uri.parse(uri)
                            )
                        )
                    } catch (e: Exception) {
                        return null
                    }
                )
            } else {
                RoundedBitmapDrawableFactory.create(
                    resources,
                    try {
                        MediaStore.Images.Media.getBitmap(
                            context.contentResolver,
                            Uri.parse(uri)
                        )
                    } catch (e: Exception) {
                        return null
                    }
                )
            }
        rb.isCircular = true
        rb.setAntiAlias(true)
        return rb
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

//TODO:  airplane mode, power off, restart phone,auto rotation,hotspot, specific settings

    private fun isAirplaneModeEnabled(): Boolean {
        return Settings.System.getInt(
            contentResolver,
            Settings.System.AIRPLANE_MODE_ON,
            0
        ) == 1
    }

    //TODO: Airplane mode not turning on
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
            if (!Settings.System.canWrite(context)) {
                speakOut(getString(request_settings_write_permit))
                startActivityForResult(
                    Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS),
                    skivvy.CODE_SYSTEM_SETTINGS
                )
            }
        }
    }

    private fun saveCalculationResult(result: String) {
        getSharedPreferences(skivvy.PREF_HEAD_CALC, Context.MODE_PRIVATE).edit()
            .putString(skivvy.PREF_KEY_LAST_CALC, result).apply()
    }

    private fun getLastCalculationResult(): String {
        return getSharedPreferences(skivvy.PREF_HEAD_CALC, Context.MODE_PRIVATE)
            .getString(skivvy.PREF_KEY_LAST_CALC, "0")!!
    }

    //Handle incoming phone calls
    private var phoneStateListener: PhoneStateListener? = null
    private var telephonyManager: TelephonyManager? = null
    private var lastState: Int? = null

    //TODO: timer while call
    private fun startTimer(onFeedback: Boolean = false) {
        val timer = object : CountDownTimer(20000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
            }

            override fun onFinish() {
            }
        }
    }
    private var incomingName:String? = null
    private var incomingImage:Drawable? = null
    //TODO: manage incoming contact lookup
    inner class FindContactIncoming : AsyncTask<String, Void, ArrayList<String?>>() {
        var number: String? = null
        var callState: Int? = null
        override fun doInBackground(vararg params: String?): ArrayList<String?>? {
            params[0]?.let {
                number = it
                params[1]?.let { it1 ->
                    callState = it1.toFloat().toInt()
                }
                return getNameAndImageUri(it)
            }
            return null
        }

        override fun onPostExecute(result: ArrayList<String?>?) {
            super.onPostExecute(result)
            if (result != null) {
                if (result[0] != null || result[1] != null) {
                    callStateManager(callState!!, number!!, result[0], cropToCircle(result[1]))
                }
            }
        }
    }

    private fun callStateManager(
        state: Int,
        phone: String,
        name: String? = null,
        image: Drawable? = null
    ) {
        when (state) {
            TelephonyManager.CALL_STATE_RINGING -> {        //incoming
                this.incomingName = name
                this.incomingImage = image
                tasksOngoing[CALLTASK] = true
                feature.silentRinger(audioManager, true)
                successView(image ?: getDrawable(ic_phone_dialer))
                speakOut(
                    if (name != null) name + getString(_is_calling_you)
                    else getString(incoming_call_from_) + phone,
                    taskCode = skivvy.CODE_ANSWER_CALL,
                    isUrgent = true
                )
            }
            TelephonyManager.CALL_STATE_OFFHOOK -> {
                tasksOngoing[CALLTASK] = true
                if (lastState == TelephonyManager.CALL_STATE_RINGING) {     //user picked up
                    speakOut(
                        getString(speaking_to_) + (name ?: phone)
                    )
                    waitingView(image ?: getDrawable(ic_phone_dialer))
                } else {        //dialed by user
                    speakOut(
                        getString(calling_) + when {
                            name != null -> name
                            temp.getContactPresence() -> contact.displayName
                            else -> phone
                        }
                    )
                }
            }
            TelephonyManager.CALL_STATE_IDLE -> {
                tasksOngoing[CALLTASK] = false
                if (lastState == TelephonyManager.CALL_STATE_RINGING) {     //missed call
                    speakOut(
                        getString(you_missed_call_from_) + (this.incomingName ?: phone)
                    )
                    errorView(
                        this.incomingImage ?: getDrawable(ic_phone_dialer)
                    )
                } else if (lastState == TelephonyManager.CALL_STATE_OFFHOOK) {      //call ended
                    speakOut(
                        getString(call_ended_with_) + when {
                            name != null -> name
                            temp.getContactPresence() -> contact.displayName
                            else -> phone
                        }
                    )
                }
            }
        }
        lastState = state
        this.phone = null
        this.image = null
    }

    //TODO : Call state refurbishment

    var name: String? = null
    var phone: String? = null
    var image: Drawable? = null
    private fun callStateListener() {
        telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        phoneStateListener = object : PhoneStateListener() {
            override fun onCallStateChanged(state: Int, number: String) {
                if (state != lastState && number.isNotEmpty()) {
//                    callStateManager(state, number)
                    FindContactIncoming().execute(number, state.toString())
                }
            }
        }
        telephonyManager!!.listen(
            phoneStateListener,
            PhoneStateListener.LISTEN_CALL_STATE
        )
    }

    var batteryLevel: Int = 0
    private val mBatInfoReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(ctxt: Context?, intent: Intent) {
            batteryLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0)
            if (!anyTaskRunning() && skivvy.readBatteryStatus()) {
                when (batteryLevel) {
                    100 -> speakOut(
                        "Battery is full, you may remove charger now.",
                        isFeedback = true
                    )
                    15 -> speakOut(
                        "Battery is low, device might need charging.",
                        isFeedback = true,
                        isUrgent = true
                    )
                    5 -> speakOut(
                        "Battery critically low, needs charging now!",
                        isFeedback = true,
                        isUrgent = true
                    )
                }
            }
        }
    }
    var lastTimeNotif: String? = null
    var lastMsgNotif: String? = null
    var lastNotifFrom: String? = null
    var lastNotifKey: String? = null
    var lastOngoing = false
    var ongoingNotifKey: String? = null
    private val mNotificationReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            val key = intent.getStringExtra(skivvy.notificationID)
            val state = intent.getStringExtra(skivvy.notificationStatus)
            val from = intent.getStringExtra(skivvy.notificationAppName)
            val msg = intent.getStringExtra(skivvy.notificationTicker)?.replace("—", "by")
            val time = intent.getStringExtra(skivvy.notificationTime)
            val ongoing = intent.getBooleanExtra(skivvy.notificationOngoing, false)
            if (state == skivvy.notificationRemoved) {
                if (key == lastNotifKey) {
                    if (tasksOngoing[NOTIFTASK]) {
                        if (key == ongoingNotifKey) {
                            setFeedback(nothing)
                            tasksOngoing[NOTIFTASK] = false
                            initialView()
                        }
                        setOutput(getString(what_next))
                    }
                } else if (key == ongoingNotifKey) {
                    setFeedback(nothing)
                    initialView()
                }
            } else {
                if (ongoing) {        //if notification  is sticky
                    if (tasksOngoing[NOTIFTASK]) {        //if already has a sticky notification
                        if (key == lastNotifKey) {
                            if (msg == lastMsgNotif)
                                msg?.let { setFeedback(msg) }  //if same app post same notification sticky, replace previous one
                            else
                                msg?.let {
                                    speakOut(
                                        it,
                                        isFeedback = true
                                    )
                                }         //if same app post different notification sticky, speak and replace previous one
                        } else
                            msg?.let {
                                speakOut(
                                    it,
                                    isFeedback = false
                                )
                            }       //if other app post notification sticky, show on output
                    } else {
                        msg?.let {
                            speakOut(
                                it,
                                isFeedback = true
                            )
                        }         //if no previous sticky notification, show as feedback
                    }
                    tasksOngoing[NOTIFTASK] = true
                    ongoingNotifKey = key
                    waitingView()
                } else {        //if notification is removable
                    if (tasksOngoing[NOTIFTASK]) {
                        setFeedback(lastMsgNotif)
                        if (key == ongoingNotifKey) {
                            speakOut(nothing)
                        } else {
                            msg?.let { speakOut(it, isFeedback = false) }
                        }
                        tasksOngoing[NOTIFTASK] = true
                    } else {
                        msg?.let { speakOut(it, isFeedback = true) }
                        tasksOngoing[NOTIFTASK] = false
                    }
                }
                lastNotifKey = key
                lastTimeNotif = time
                lastMsgNotif = msg
                lastNotifFrom = from
                lastOngoing = ongoing
            }
        }
    }

    //TODO: notification content display
    override fun onDestroy() {
        speakOut(getString(exit_msg))
        loading.startAnimation(anim.zoomInRotate)
        this.unregisterReceiver(this.mBatInfoReceiver)
        this.unregisterReceiver(this.mNotificationReceiver)
        sendBroadcast(
            Intent(skivvy.actionServiceRestart)
                .putExtra(skivvy.serviceDead, true)
        )
        skivvy.tts?.let {
            it.stop()
            it.shutdown()
        }
        skivvy.isHomePageRunning = false
        super.onDestroy()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        skivvy.isHomePageRunning = false
        speakOut(getString(exit_msg))
    }

    /**
     * Following function checks whether the given string has last response as acceptance or denial
     * @param response : the given string of response
     * @return : returns boolean value according to content of given response (true for positive, false for negative, null for invalid response)
     * TODO: extend this to other types of responses too.
     */
    private fun isCooperative(response: String): Boolean? {
        return when {
            input.containsString(
                input.removeBeforeLastStringsIn(
                    response,
                    arrayOf(
                        resources.getStringArray(R.array.acceptances),
                        resources.getStringArray(R.array.disruptions)
                    )
                ),
                arrayOf(resources.getStringArray(R.array.denials))
            ) -> false      //if denial was the last response in string
            input.containsString(
                input.removeBeforeLastStringsIn(
                    response,
                    arrayOf(
                        resources.getStringArray(R.array.denials),
                        resources.getStringArray(R.array.disruptions)
                    )
                ), arrayOf(resources.getStringArray(R.array.acceptances))
            ) -> true       //if acceptance was the last response in string
            input.containsString(
                response,
                arrayOf(resources.getStringArray(R.array.denials)),
                isSingle = true
            ) -> false      //if response contains denial
            input.containsString(
                response,
                arrayOf(resources.getStringArray(R.array.acceptances)),
                isSingle = true
            ) -> true       //if response contains acceptance
            else -> null        //if response was invalid
        }
    }

    private fun isInitiative(response: String): Boolean? {
        return when {
            input.containsString(
                input.removeBeforeLastStringsIn(
                    response,
                    arrayOf(
                        resources.getStringArray(R.array.acceptances),
                        resources.getStringArray(R.array.initiators)
                    )
                ),
                arrayOf(
                    resources.getStringArray(R.array.denials),
                    resources.getStringArray(R.array.disruptions)
                )
            ) -> false      //if denial was the last response in string
            input.containsString(
                input.removeBeforeLastStringsIn(
                    response,
                    arrayOf(
                        resources.getStringArray(R.array.denials),
                        resources.getStringArray(R.array.disruptions)
                    )
                ),
                arrayOf(
                    resources.getStringArray(R.array.initiators),
                    resources.getStringArray(R.array.acceptances)
                )
            ) -> true       //if acceptance was the last response in string
            input.containsString(
                response,
                arrayOf(
                    resources.getStringArray(R.array.denials),
                    resources.getStringArray(R.array.disruptions)
                ),
                isSingle = true
            ) -> false      //if response contains denial/cancellation
            input.containsString(
                response,
                arrayOf(
                    resources.getStringArray(R.array.initiators),
                    resources.getStringArray(R.array.acceptances)
                ),
                isSingle = true
            ) -> true      //if response contains acceptance/initiatives
            else -> null
        }
    }

    private fun isDisruptive(response: String): Boolean {
        return when {
            input.containsString(
                input.removeBeforeLastStringsIn(
                    response, arrayOf(resources.getStringArray(R.array.disruptions)),
                    excludeLast = true
                ), arrayOf(resources.getStringArray(R.array.disruptions))
            ) -> true
            else -> false
        }
    }

    private fun initialView(onGoing: Boolean = false) {
        if (!onGoing) {
            loading.startAnimation(anim.zoomInOutRotate)
            loading.setImageDrawable(
                getDrawable(
                    when (skivvy.getThemeState()) {
                        R.style.BlueTheme -> dots_in_circle_white
                        else -> dots_in_circle
                    }
                )
            )
            setOutput(getString(what_next))
            setFeedback(null)
            icon.setImageDrawable(null)
            resetVariables()
        }
        lastTxt = null
        txt = null
        SearchContact().cancel(true)
    }

    private fun resetVariables() {
        contact = ContactModel()
        temp = TempDataManager()
        msgCode = MessageCode()
        this.incomingName = null
        this.incomingImage = null
        txt = null
    }

    private fun waitingView(image: Drawable? = null) {
        loading.startAnimation(anim.fadeOnFast)
        loading.startAnimation(anim.focusRotate)
        loading.setImageDrawable(getDrawable(dots_in_circle_yellow))
        if (image != null) {
            icon.setImageDrawable(image)
        }
    }

    private fun errorView(image: Drawable? = null) {
        loading.startAnimation(anim.fadeOnFadeOff)
        loading.setImageDrawable(getDrawable(dots_in_circle_red))
        image?.let { icon.setImageDrawable(image) }
    }

    private fun successView(image: Drawable?) {
        loading.startAnimation(anim.fadeOnFast)
        loading.startAnimation(anim.focusDefocusRotate)
        loading.setImageDrawable(getDrawable(dots_in_circle_green))
        if (image != null) {
            icon.setImageDrawable(image)
        }
    }

    private fun setOutput(text: String?) {
        outputText.startAnimation(anim.fadeOnFast)
        outputText.text = text
    }

    private fun speakOut(
        text: String,
        taskCode: Int? = null,
        parallelReceiver: Boolean = skivvy.getParallelResponseStatus(),
        isFeedback: Boolean = false,
        isUrgent: Boolean = false
    ) {
        if (isFeedback) setFeedback(text)
        else setOutput(text)
        if (skivvy.getVolumeNormal()) feature.setMediaVolume(
            skivvy.getNormalVolume().toFloat(),
            audioManager,
            false
        )
        if (isUrgent) {
            if (skivvy.getVolumeUrgent()) {
                if (feature.getMediaVolume(audioManager) < skivvy.getUrgentVolume()) {
                    feature.setMediaVolume(
                        skivvy.getUrgentVolume().toFloat(),
                        audioManager,
                        false
                    )
                }
            }
        }
        skivvy.tts?.let {
            it.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onDone(utteranceId: String) {
                    if (!parallelReceiver)
                        taskCode?.let { code -> startVoiceRecIntent(code, text) }
                }

                override fun onError(utteranceId: String) {
                    taskCode?.let { code -> startVoiceRecIntent(code, text) }
                }

                override fun onStart(utteranceId: String) {
                    if (parallelReceiver)
                        taskCode?.let { code -> startVoiceRecIntent(code, text) }
                }
            })
            if (!skivvy.getMuteStatus()) it.speak(
                text.replace("\n", nothing),        //removing pause caused by newline character
                TextToSpeech.QUEUE_FLUSH,
                null,
                "$taskCode"
            )
            else taskCode?.let { it1 -> startVoiceRecIntent(it1, text) }
        }
    }

    //intent voice recognition, code according to action command, serving activity result
    private fun startVoiceRecIntent(
        code: Int,
        message: String = getString(generic_voice_rec_text)
    ) {
        recognitionIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, message)
        recognitionIntent.resolveActivity(packageManager)
            ?.let { startActivityForResult(recognitionIntent, code) }
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
                            skivvy.setSecurityPref(biometricOn = false)
                            speakOut(getString(biometric_is_off))
                        }
                        skivvy.CODE_VOICE_AUTH_CONFIRM -> {
                            skivvy.setSecurityPref(vocalAuthOn = false)
                            speakOut(getString(voice_auth_disabled))
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

    private fun requestThisPermission(code: Int) {
        if (code == skivvy.CODE_ALL_PERMISSIONS) {
            ActivityCompat.requestPermissions(
                this, skivvy.permissions,
                skivvy.CODE_ALL_PERMISSIONS
            )
        } else {
            ActivityCompat.requestPermissions(
                this, arrayOf(
                    when (code) {
                        skivvy.CODE_CONTACTS_REQUEST -> Manifest.permission.READ_CONTACTS
                        skivvy.CODE_CALL_REQUEST -> Manifest.permission.CALL_PHONE
                        skivvy.CODE_ANSWER_CALL -> Manifest.permission.ANSWER_PHONE_CALLS
                        skivvy.CODE_SMS_REQUEST -> Manifest.permission.SEND_SMS
                        skivvy.CODE_STORAGE_REQUEST -> Manifest.permission.WRITE_EXTERNAL_STORAGE
                        skivvy.CODE_CALENDER_REQUEST -> Manifest.permission.READ_CALENDAR
                        skivvy.CODE_CALL_LOG_REQUEST -> Manifest.permission.READ_CALL_LOG
                        else -> nothing
                    }
                ), code
            )
        }
    }

    @SuppressLint("MissingPermission")
    private fun arrangeNumbersAsCallLogs(name: String): Array<String?>? {
        var cursor: Cursor? = null
        if (skivvy.hasThisPermission(context, skivvy.CODE_CALL_LOG_REQUEST)) {
            cursor = skivvy.cResolver.query(
                CallLog.Calls.CONTENT_URI,
                arrayOf(
                    CallLog.Calls.CACHED_NAME,
                    CallLog.Calls.NUMBER
                ),
                CallLog.Calls.CACHED_NAME + " = ?",
                arrayOf(name),
                null
            )
        } else requestThisPermission(skivvy.CODE_CALL_LOG_REQUEST)

        cursor?.let {
            it.moveToFirst()
            while (it.moveToNext()) {
                val number = formatPhoneNumber(it.getString(1))        //number index column
                contact.phoneList?.let { list ->
                    if (list.contains(number)) {
                        val index = list.indexOf(number)
                        val temp = list[index]
                        if (list[0] != temp) {
                            list[index] = list[0]
                            list[0] = temp
                        }
                    }
                }
                if (contact.phoneList!![0] == number) break
            }
            it.close()
        }
        cursor?.close()
        return contact.phoneList
    }

    private fun isNotificationServiceRunning(): Boolean =
        NotificationManagerCompat.getEnabledListenerPackages(context).contains(context.packageName)

    private fun getLocalisedString(eng: String, hindi: String): String {
        if (skivvy.isLocaleHindi()) {
            return hindi
        }
        return eng
    }

    private fun String.isLandLine(): Boolean {
        return this[0] == '0' && this[1] == '1' && this[2] == '2' && this[3] == '0'
    }

    private fun String.hasCountryCode(country: String): Boolean {
        if (country == "IN")
            return this[0] == '+' && this[1] == '9' && this[2] == '1'
        return false
    }
}
