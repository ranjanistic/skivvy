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
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.location.LocationManager
import android.media.AudioManager
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Bundle
import android.os.Environment
import android.provider.ContactsContract
import android.provider.MediaStore
import android.provider.Settings
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.telephony.SmsManager
import android.text.format.DateFormat
import android.view.KeyEvent
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import org.ranjanistic.skivvy.R.drawable.*
import org.ranjanistic.skivvy.R.string.*
import java.io.File
import java.io.FileOutputStream
import java.util.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tan

@ExperimentalStdlibApi
class MainActivity : AppCompatActivity() {
    lateinit var skivvy: Skivvy
    private var tts: TextToSpeech? = null
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
    private var fadeOffAnimation: Animation? = null
    private var translateAnimation: Animation? = null
    private var receiver: TextView? = null
    private lateinit var setting: ImageButton
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
        tts = TextToSpeech(context, TextToSpeech.OnInitListener {
            when (it) {
                TextToSpeech.SUCCESS -> {
                    when (tts!!.setLanguage(skivvy.locale)) {
                        TextToSpeech.LANG_MISSING_DATA,
                        TextToSpeech.LANG_NOT_SUPPORTED -> outPut!!.text =
                            getString(language_not_supported)
                    }
                }
                else -> outPut!!.text = getString(output_error)
            }
        })
        setListeners()
    }

    private fun setViewAndDefaults() {
        setting = findViewById(R.id.setting)
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
        riseAnimation = AnimationUtils.loadAnimation(context, R.anim.rise_back)
        bubbleAnimation = AnimationUtils.loadAnimation(context, R.anim.bubble_wave)
        normalRotate = AnimationUtils.loadAnimation(context, R.anim.rotate_emerge_demerge)
        focusRotate = AnimationUtils.loadAnimation(context, R.anim.rotate_focus)
        rotateSlow = AnimationUtils.loadAnimation(context, R.anim.rotate_slow)
        fadeAnimation = AnimationUtils.loadAnimation(context, R.anim.fade)
        exitAnimation = AnimationUtils.loadAnimation(context, R.anim.rotate_exit)
        /*fadeOffAnimation = AnimationUtils.loadAnimation(context, R.anim.fade_off)
        translateAnimation = AnimationUtils.loadAnimation(context, R.anim.scale_translate_setting)
        extendAnimation = AnimationUtils.loadAnimation(context, R.anim.extend_back)
         */
        backfall.startAnimation(fallAnimation)
        setting.startAnimation(bubbleAnimation)
        receiver!!.startAnimation(bubbleAnimation)
        greet!!.startAnimation(bubbleAnimation)
    }

    private fun startSettingAnimate() {
        setting.startAnimation(translateAnimation)
        backfall.startAnimation(extendAnimation)
        loading!!.startAnimation(exitAnimation)
    }

    private fun setListeners() {
/*        translateAnimation!!.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationEnd(p0: Animation?) {
            }
            override fun onAnimationRepeat(p0: Animation?) {}
            override fun onAnimationStart(p0: Animation?) {}
        })

 */
        setting.setOnClickListener {
            setButtonsClickable(false)
            startActivity(Intent(context, Setup::class.java))
            //overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out)
        }
        receiver?.setOnClickListener {
            setButtonsClickable(false)
            normalView()
            startVoiceRecIntent(skivvy.CODE_SPEECH_RECORD)
        }
    }

    override fun onStart() {
        super.onStart()
        setButtonsClickable(true)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            skivvy.CODE_ALL_PERMISSIONS -> {
                //TODO: if(grantResults[])
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
            if (outputStat!!.visibility != View.VISIBLE) {
                setButtonsClickable(false)
                normalView()
                startVoiceRecIntent(skivvy.CODE_SPEECH_RECORD)
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    @Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        setButtonsClickable(true)
        tts!!.language = skivvy.locale
        when (requestCode) {
            skivvy.CODE_SPEECH_RECORD -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    txt = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)[0].toString()
                        .toLowerCase(skivvy.locale)
                    if (txt != null) {
                        input?.text = txt
                        if (!respondToCommand(txt!!)) {
                            if (!appOptions(txt)) {
                                if (!directActions(txt!!)) {
                                    if (!computerOps(txt!!)) {
                                        errorView()
                                        speakOut(getString(recognize_error))
                                    }
                                }
                            }
                        }
                    }
                } else {
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
                                getString(recognize_error)+"\n" + getString(do_u_want_open) + skivvy.packagesAppName[tempPackageIndex!!] + "?",
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
                                    getString(recognize_error)+"\n" + getString(should_i_call) + "${contact.displayName} at ${contact.phoneList!![tempPhoneNumberIndex!!]}?",
                                    skivvy.CODE_CALL_CONF
                                )
                            } else {
                                speakOut(
                                    getString(recognize_error) +"\n"+ getString(should_i_call) + "$tempPhone?",
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
                                    getString(subject_added)+"\n" + getString(what_is_body),
                                    skivvy.CODE_EMAIL_CONTENT
                                )
                            } else if (tempMailBody == null) {
                                tempMailBody = txt
                                if (contact.emailList != null) {
                                    if (contact.emailList!!.size == 1) {
                                        speakOut(
                                            getString(body_added)+"\n" +
                                                    getString(should_i_email) + "${contact.displayName} at\n${contact.emailList!![tempEmailIndex!!]}?",
                                            skivvy.CODE_EMAIL_CONF
                                        )
                                    } else {
                                        speakOut(
                                            getString(body_added) + "I've got ${contact.emailList!!.size} addresses of\n${contact.displayName}.\n" +
                                                    getString(should_i_email) + "them at\n${contact.emailList!![tempEmailIndex!!]}?",
                                            skivvy.CODE_EMAIL_CONF
                                        )
                                    }
                                } else {
                                    speakOut(
                                        getString(body_added)+"\n" +
                                                getString(should_i_email) + "$tempMail?",
                                        skivvy.CODE_EMAIL_CONF
                                    )
                                }
                            }
                        }
                        else -> {
                            if (tempMailSubject == null) {
                                speakOut(
                                    getString(recognize_error)+"\n" + getString(what_is_subject),
                                    skivvy.CODE_EMAIL_CONTENT
                                )
                            } else if (tempMailBody == null) {
                                speakOut(
                                    getString(recognize_error)+"\n" + getString(what_is_body),
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
                                    getString(recognize_error)+"\n" +
                                            getString(should_i_email) + "${contact.displayName} at\n${contact.emailList!![tempEmailIndex!!]}?",
                                    skivvy.CODE_EMAIL_CONF
                                )
                            } else {
                                speakOut(
                                    getString(recognize_error)+"\n" +
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
                            successView(null)
                            speakOut(getString(sending_sms_at) + "$tempPhone")
                            textMessageOps(tempPhone!!, tempTextBody!!, skivvy.CODE_SMS_CONF)
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

    //TODO:  Mathematical calculations command
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
            resources.getStringArray(array[0]).contains(text) -> {
                startActivity(Intent(context, Setup::class.java))
            }
            resources.getStringArray(array[1]).contains(text) -> {
                bluetoothOps(text)
            }
            resources.getStringArray(array[2]).contains(text) -> {
                waitingView(getDrawable(ic_wifi_connected))
                wifiOps(text)
            }
            resources.getStringArray(array[3]).contains(text) -> {
                locationOps()
            }
            resources.getStringArray(array[4]).contains(text) -> {
                deviceLockOps()
            }
            resources.getStringArray(array[5]).contains(text) -> {
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
            text.contains("volume") -> {
                when {
                    text.contains("up") || text.contains("raise") -> volumeOps(true)
                    text.contains("down") -> volumeOps(false)
                    else -> volumeOps(null)
                }
            }
            text == "mute" -> {
                saveMuteStatus(true)
                speakOut("Muted")
            }
            text == "speak" || text == "unmute" -> {
                if (getMuteStatus()) {
                    saveMuteStatus(false)
                    speakOut(getString(okay))
                } else {
                    speakOut("Already speaking")
                }
            }
            text == getString(exit) -> {
                finish()
            }
            text == "get permissions" -> {
                if (!hasPermissions(this, *skivvy.permissions)) {
                    ActivityCompat.requestPermissions(
                        this, skivvy.permissions,
                        skivvy.CODE_ALL_PERMISSIONS
                    )
                } else {
                    speakOut("I have all the permissions that I need from you.")
                }
            }
            else -> {
                return false
            }
        }
        return true
    }

    private fun expressionize(expression: String): String {
        var finalExpression = expression.replace("calculate", "")
        finalExpression = finalExpression.replace("compute", "")
        finalExpression = finalExpression.replace("solve", "")
        finalExpression = finalExpression.replace(" ", "")
        finalExpression = finalExpression.replace("x", "*")
        finalExpression = finalExpression.replace("dividedby", "/")
        finalExpression = finalExpression.replace("over", "/")
        finalExpression = finalExpression.replace("upon", "/")
        finalExpression = finalExpression.replace("multipliedby", "*")
        finalExpression = finalExpression.replace("by", "/")
        finalExpression = finalExpression.replace("into", "*")
        finalExpression = finalExpression.replace("plus", "+")
        finalExpression = finalExpression.replace("minus", "-")
        return finalExpression
    }

    //for direct mathematical operations.
    private fun mathematicalFunctions(expression: String):Boolean{
        val triangleRatios = arrayOf("sin", "cos", "tan", "cot", "sec", "cosec")
        var trignometric = false
        if (expression.contains(skivvy.textPattern) && expression.contains(skivvy.numberPattern)) {
            var k = 0
            while (k < triangleRatios.size) {
                if (expression.contains(triangleRatios[k])) {
                    trignometric = true
                }
                ++k
            }
        }
        if (trignometric) {
            when {
                expression.contains(triangleRatios[0]) -> {
                    speakOut(
                        sin(
                            expression.replace(skivvy.nonNumeralPattern, "").toFloat() * (PI / 180)
                        ).toString()
                    )
                }
                expression.contains(triangleRatios[1]) -> {
                    speakOut(
                        cos(
                            expression.replace(skivvy.nonNumeralPattern, "").toFloat() * (PI / 180)
                        ).toString()
                    )
                }
                expression.contains(triangleRatios[2]) -> {
                    speakOut(
                        tan(
                            expression.replace(skivvy.nonNumeralPattern, "").toFloat() * (PI / 180)
                        ).toString()
                    )
                }
                expression.contains(triangleRatios[3]) -> {
                    speakOut(
                        (1 / tan(
                            expression.replace(skivvy.nonNumeralPattern, "").toFloat() * (PI / 180)
                        )).toString()
                    )
                }
                expression.contains(triangleRatios[4]) -> {
                    speakOut(
                        (1 / cos(
                            expression.replace(skivvy.nonNumeralPattern, "").toFloat() * (PI / 180)
                        )).toString()
                    )
                }
                expression.contains(triangleRatios[5]) -> {
                    speakOut(
                        (1 / sin(
                            expression.replace(skivvy.nonNumeralPattern, "").toFloat() * (PI / 180)
                        )).toString()
                    )
                }
                else -> return false
            }
            return true
        }
        return false
    }

    //for expression evaluation
    private fun computerOps(expressionString: String): Boolean {
        val expression = expressionize(expressionString)
        if (!expression.contains(skivvy.numberPattern)) {
            return false
        }
        if(mathematicalFunctions(expression)){
            return true
        } else {
            if (expression.contains(skivvy.textPattern)) {
                return false
            }
        }
        val operatorBool: Array<Boolean> = arrayOf(false, false, false, false)
        val operators: Array<Char> = arrayOf('/', '*', '+', '-')
        var opIndex = 0

        /**
         * Storing availability of all operators in given expression, to an array of booleans.
         */
        while (opIndex < operators.size) {
            operatorBool[opIndex] = expression.contains(operators[opIndex])
            ++opIndex
        }
        if (!operatorBool.contains(true)) {
            speakOut(getString(invalid_expression))
            return false
        }

        /**
         *  The following block stores the position of operators in the given expression
         *  in  a new array (of Integers), which will help the further block of code to contain
         *  and create a distinction between operands (numbers) and operators.
         */

        var expIndex = 0
        var totalOps = 0
        while (expIndex < expression.length) {
            opIndex = 0
            while (opIndex < operators.size) {
                if (expression[expIndex] == operators[opIndex]) {
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
            while (opIndex < operators.size) {
                if (expression[expIndex] == operators[opIndex]) {
                    expOperatorPos[expOpIndex] = expIndex         //saving operator positions
                    ++expOpIndex
                }
                ++opIndex
            }
            ++expIndex
        }

        /**
         * The following block extracts values from given expression, char by char, and stores them
         * in an array of Strings, by grouping digits in form of numbers at the same index as string,
         * and operators in the expression at a separate index if array of Strings.
         *  For ex - Let the given expression be :   1234/556*89+4-23
         *  Starting from index = 0, the following block will store digits till '/'  at index =0 of empty array of Strings, then
         *  will store '/' itself at index =  1 of empty array of Strings. Then proceeds to store 5, 5  and 6
         *  at the same index = 2 of e.a. of strings. And stores the next operator '*' at index = 3, and so on.
         *  Thus a distinction between operands and operators is created and stored in a new array (of strings).
         */

        val arrayOfExpression = arrayOfNulls<String>(2 * totalOps + 1)
        var expArrayIndex = 0
        var positionInExpression = 0
        var positionInOperatorPos = 0
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

        /**
         * Now, as we have the new array of strings, having the proper
         * expression, with operators at every even position of the array (at odd indices),
         * the following block of code will evaluate the expression according to the BODMAS rule.
         */

        var nullPosCount = 0
        opIndex = 0
        while (opIndex < operators.size) {
            var opPos = 1
            while (opPos < arrayOfExpression.size - nullPosCount) {
                if (arrayOfExpression[opPos] == operators[opIndex].toString()) {
                    arrayOfExpression[opPos - 1] = operate(
                        arrayOfExpression[opPos - 1]!!.toFloat(),
                        operators[opIndex],
                        arrayOfExpression[opPos + 1]!!.toFloat()
                    ).toString()
                    var j = opPos
                    while (j + 2 < arrayOfExpression.size) {
                        arrayOfExpression[j] = arrayOfExpression[j + 2]
                        ++j
                    }
                    nullPosCount += 2
                    if (arrayOfExpression.size > 3 &&
                        arrayOfExpression[opPos] == operators[opIndex].toString()) {    //if replacing operator is same as the replaced one
                        opPos -= 2            //index two indices back so that it returns at same position again
                    }
                }
                opPos += 2        //next index of operator in array of expression
            }
            ++opIndex       //next operator
        }
        if (arrayOfExpression[0]!!.toFloat() - arrayOfExpression[0]!!.toFloat().toInt() == 0F) {
            speakOut(
                arrayOfExpression[0]!!.toFloat().toInt().toString()
            )         //final result stored at index = 0
        } else {
            speakOut(arrayOfExpression[0]!!)
        }
        return true
    }

    private fun operate(a: Float, operator: Char, b: Float): Float? {
        return when (operator) {
            '/' -> a / b
            '*' -> a * b
            '+' -> a + b
            '-' -> a - b
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
        val wifiManager: WifiManager = getSystemService(Context.WIFI_SERVICE) as WifiManager
        if (text.contains("on")) {
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
        deviceManger = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
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

    //TODO: More volume customizations
    private fun volumeOps(action: Boolean?) {
        val audioManager: AudioManager =
            applicationContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        when (action) {
            true -> audioManager.adjustStreamVolume(
                AudioManager.STREAM_MUSIC,
                AudioManager.ADJUST_RAISE,
                AudioManager.FLAG_SHOW_UI
            )
            false -> audioManager.adjustStreamVolume(
                AudioManager.STREAM_MUSIC,
                AudioManager.ADJUST_LOWER,
                AudioManager.FLAG_SHOW_UI
            )
            else -> audioManager.adjustStreamVolume(
                AudioManager.STREAM_MUSIC,
                AudioManager.ADJUST_SAME,
                AudioManager.FLAG_SHOW_UI
            )
        }
    }

    //TODO: specific app actions

    //actions invoking other applications
    private fun appOptions(text: String?): Boolean {
        if (text != null) {
            if (skivvy.packagesTotal > 0) {
                var i = 0
                while (i < skivvy.packagesTotal) {
                    when {
                        text == getString(app_name).toLowerCase(skivvy.locale) -> {
                            speakOut(getString(i_am) + getString(app_name))
                            return true
                        }
                        text == skivvy.packagesAppName[i] -> {
                            successView(skivvy.packagesIcon[i])
                            speakOut(getString(opening) + skivvy.packagesAppName[i])
                            startActivity(Intent(skivvy.packagesMain[i]))
                            return true
                        }
                        skivvy.packagesName[i]!!.contains(text) -> {
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
            localTxt = text.replace(getString(call), "", true)
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
                            errorView()
                            speakOut(getString(invalid_call_request))
                        }
                    }
                }
            } else return false
            return true
        } else if (text.contains(getString(email))) {
            waitingView(getDrawable(ic_email_envelope))
            localTxt = text.replace(getString(email), "", true)
            tempMail = localTxt.replace(" ", "")
            tempMail = tempMail!!.trim()
            when {
                tempMail!!.matches(skivvy.emailPattern) -> {
                    speakOut(getString(what_is_subject), skivvy.CODE_EMAIL_CONTENT)
                }
                localTxt.length > 1 -> {
                    contactOps(localTxt, skivvy.CODE_EMAIL_CONF)
                }
                else -> {
                    errorView()
                    speakOut(getString(invalid_email_request))
                }
            }
            return true
        } else if (text.contains("text")) {
            waitingView(null)
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
                    errorView()
                    speakOut("Invalid SMS request")
                }
            }
            return true
        }
        return false
    }

    private fun textMessageOps(target: String, payLoad: String, code: Int) {
        if (code == skivvy.CODE_SMS_CONF) {
            successView(null)
            val sms: SmsManager = SmsManager.getDefault()
            sms.sendTextMessage(target, null, payLoad, null, null)
        } else {
            speakOut("Not yet supported")
        }
    }

    @SuppressLint("MissingPermission")
    private fun callingOps(number: String?) {
        if (number != null) {
            speakOut(getString(calling) + "$number")
            val intent = Intent(Intent.ACTION_CALL)
            intent.data = Uri.parse("tel:$number")
            startActivity(intent)
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
                            waitingView(
                                BitmapDrawable(
                                    resources,
                                    MediaStore.Images.Media.getBitmap(
                                        this.contentResolver,
                                        Uri.parse(contact.photoID)
                                    )
                                )
                            )
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
                                            "I've got $size phone numbers of \n${contact.displayName}.\nShould I call them at " +
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
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            .putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            .putExtra(RecognizerIntent.EXTRA_LANGUAGE, skivvy.locale)
            .putExtra(RecognizerIntent.EXTRA_PROMPT, "I'm listening");
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
        if (tts != null) {
            tts!!.stop()
            tts!!.shutdown()
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
        receiver?.isClickable = state
    }

    private fun speakOut(text: String) {
        outPut?.text = text
        tts!!.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onDone(utteranceId: String) {
//                 outputStat!!.visibility = View.INVISIBLE
            }

            override fun onError(utteranceId: String) {}
            override fun onStart(utteranceId: String) {
                //            outputStat!!.visibility = View.VISIBLE
            }
        })
        if (!getMuteStatus()) tts!!.speak(text, TextToSpeech.QUEUE_FLUSH, null, "")
    }

    private fun speakOut(text: String, taskCode: Int?) {
        outPut?.text = text
        tts!!.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
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
        if (!getMuteStatus()) tts!!.speak(text, TextToSpeech.QUEUE_FLUSH, null, "")
        else {
            if (taskCode != null) startVoiceRecIntent(taskCode)
        }
    }

    private fun getBiometricStatus(): Boolean {
        return getSharedPreferences(skivvy.PREF_HEAD_SECURITY, MODE_PRIVATE)
            .getBoolean(skivvy.PREF_KEY_BIOMETRIC, false)
    }

    private fun getTrainingStatus(): Boolean {
        return getSharedPreferences(skivvy.PREF_HEAD_APP_MODE, MODE_PRIVATE)
            .getBoolean(skivvy.PREF_KEY_TRAINING, false)
    }

    private fun getMuteStatus(): Boolean {
        return getSharedPreferences(skivvy.PREF_HEAD_VOICE, MODE_PRIVATE)
            .getBoolean(skivvy.PREF_KEY_MUTE_UNMUTE, false)
    }

    private fun saveMuteStatus(isMuted: Boolean) {
        getSharedPreferences(skivvy.PREF_HEAD_VOICE, MODE_PRIVATE).edit()
            .putBoolean(skivvy.PREF_KEY_MUTE_UNMUTE, isMuted).apply()
    }
}
