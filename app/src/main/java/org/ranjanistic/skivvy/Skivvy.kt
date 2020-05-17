@file:Suppress("PropertyName")

package org.ranjanistic.skivvy

import android.Manifest
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.ContentResolver
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.database.Cursor
import android.os.Build
import android.os.Environment
import android.provider.ContactsContract
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.core.app.ActivityCompat
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import org.ranjanistic.skivvy.manager.ContactDataManager
import org.ranjanistic.skivvy.manager.PackageDataManager
import java.io.File
import java.io.IOException
import java.nio.charset.Charset
import java.util.*

class Skivvy : Application() {
    //permission codes
    val CODE_ALL_PERMISSIONS = 999
    val CODE_CALL_REQUEST = 1000
    val CODE_STORAGE_REQUEST = 1001
    val CODE_CONTACTS_REQUEST = 1002
    val CODE_SMS_REQUEST = 1003
    val CODE_CALL_LOG_REQUEST = 1004
    val CODE_BODY_SENSOR_REQUEST = 1005
    val CODE_CALENDER_REQUEST = 1006
    val permissions = arrayOf(
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_CONTACTS,
        Manifest.permission.READ_CONTACTS,
        Manifest.permission.CALL_PHONE,
        Manifest.permission.READ_CALL_LOG,
        Manifest.permission.WRITE_CALENDAR,
        Manifest.permission.READ_CALENDAR,
        Manifest.permission.ACCESS_WIFI_STATE,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.SEND_SMS,
        Manifest.permission.READ_SMS,
        Manifest.permission.CAMERA,
        Manifest.permission.BODY_SENSORS
    )

    //defaults
    val locale: Locale = Locale.getDefault()
    val numberPattern = "[0-9]".toRegex()
    val textPattern = "[a-zA-Z]".toRegex()
    val emailPattern = "[a-zA-Z0-9._-]+@[a-z.]+\\.+[a-z]+".toRegex()
    val nonNumeralPattern = "[^0-9]".toRegex()

    //action codes
    val CODE_TRAINING_MODE = 9
    val CODE_SPEECH_RECORD = 10
    val CODE_APP_CONF = 11
    val CODE_CALL_CONF = 12
    val CODE_EMAIL_CONF = 13
    val CODE_EMAIL_CONTENT = 14
    val CODE_SMS_CONF = 15
    val CODE_TEXT_MESSAGE_BODY = 16
    val CODE_WHATSAPP_ACTION = 17
    val CODE_VOICE_AUTH_INIT = 18
    val CODE_VOICE_AUTH_CONFIRM = 19
    val CODE_BIOMETRIC_CONFIRM = 20
    val CODE_VOLUME_CONFIRM = 21

    val CODE_LOCATION_SERVICE = 100
    val CODE_DEVICE_ADMIN = 102
    val CODE_SYSTEM_SETTINGS = 103
    val CODE_NOTIFICATION_ACCESS = 104
    val nonVocalRequestCodes =
        intArrayOf(CODE_LOCATION_SERVICE, CODE_DEVICE_ADMIN, CODE_SYSTEM_SETTINGS,CODE_NOTIFICATION_ACCESS)

    //default strings and arrays
    val PREF_HEAD_SECURITY = "security"
    val PREF_KEY_BIOMETRIC = "fingerprint"
    val PREF_KEY_VOCAL_AUTH = "voiceAuth"
    val PREF_KEY_VOCAL_PHRASE = "voicePhrase"
    
    val PREF_HEAD_APP_SETUP = "appSetup"
    val PREF_KEY_TRAINING = "training"
    val PREF_KEY_THEME = "theme"
    val PREF_KEY_CUSTOM_THEME = "customTheme"
    val PREF_KEY_PARLLEL_TALK = "parallelResponse"
    val PREF_KEY_HANDY = "handSide"
    val PREF_KEY_START_TALK = "talkOnStart"
    val PREF_HEAD_MATHS = "mathsSetup"
    val PREF_KEY_ANGLE_UNIT = "angleUnit"
    
    val PREF_HEAD_VOICE = "voiceSetup"
    val PREF_KEY_MUTE_UNMUTE = "muteStatus"
    val PREF_KEY_VOLUME_NORMAL = "normalVolumeStatus"
    val PREF_KEY_NORMAL_VOLUME = "normalVolumeLevel"
    val PREF_KEY_VOLUME_URGENT = "urgentVolumeStatus"
    val PREF_KEY_URGENT_VOLUME = "urgentVolumeLevel"

    
    val PREF_HEAD_CALC = "calculator"
    val PREF_KEY_LAST_CALC = "lastResult"
    val FINISH_ACTION = "finish"
    val degree = "deg"
    val radian = "rad"
    var pathToFile = ""
    val defaultTheme = R.style.DarkTheme
    //default objects
    var tts: TextToSpeech? = null
    var packageDataManager: PackageDataManager =
        PackageDataManager(this)
    lateinit var deviceManager: DevicePolicyManager
    lateinit var compName: ComponentName
    private val hindiLocale = "hi_IN"
    //TODO: Settings grouping header

    @Suppress("NonAsciiCharacters", "LocalVariableName")
    @ExperimentalStdlibApi
    override fun onCreate() {
        super.onCreate()
        deviceManager = applicationContext.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        compName = ComponentName(this, Administrator::class.java)
        GlobalScope.launch {    //Long running task, getting all packages
            getLocalPackages()
        }
        this.tts = TextToSpeech(this, TextToSpeech.OnInitListener {
            if (it == TextToSpeech.SUCCESS) {
                this.tts!!.language = this.locale
            } else
                Toast.makeText(this, getString(R.string.output_error), Toast.LENGTH_SHORT).show()
        })
        //TODO: createNotificationChannel()
    }

    //gets all packages and respective details available on device
    private fun getLocalPackages() {
        var count = 0
        val pm: PackageManager = packageManager
        val packages: List<ApplicationInfo> =
            pm.getInstalledApplications(PackageManager.GET_META_DATA)
        this.packageDataManager.setTotalPackages(packages.size)
        val packageData = PackageDataManager.PackageData(this.packageDataManager.getTotalPackages())
        for (packageInfo in packages) {
            if (pm.getLaunchIntentForPackage(packageInfo.packageName) != null) {
                packageData.appName[count] =
                    pm.getApplicationLabel(packageInfo).toString().toLowerCase(this.locale)
                packageData.appPackage[count] = packageInfo.packageName.toLowerCase(this.locale)
                packageData.appIntent[count] =
                    pm.getLaunchIntentForPackage(packageInfo.packageName)!!
                packageData.appIcon[count] = pm.getApplicationIcon(packageInfo)
                ++count
            } else {                    //removing un-launchable packages
                this.packageDataManager.setTotalPackages(this.packageDataManager.getTotalPackages() - 1)
            }
        }
        this.packageDataManager.setPackagesDetail(packageData)
    }

    //Voice and volume preferences
    fun setVoicePreference(
        voiceMute:Boolean? = null,
        normalizeVolume:Boolean? = null,
        normalVolumeLevel:Int? = null,
        urgentVolume:Boolean? = null,
        urgentVolumeLevel:Int? = null
    ){
        val editor = getSharedPreferences(this.PREF_HEAD_VOICE, AppCompatActivity.MODE_PRIVATE).edit()
        voiceMute?.let{ editor.putBoolean(this.PREF_KEY_MUTE_UNMUTE, it).apply() }
        normalizeVolume?.let{ editor.putBoolean(this.PREF_KEY_VOLUME_NORMAL, it).apply() }
        normalVolumeLevel?.let { editor.putInt(this.PREF_KEY_NORMAL_VOLUME, it).apply() }
        urgentVolume?.let{ editor.putBoolean(this.PREF_KEY_VOLUME_URGENT, it).apply() }
        urgentVolumeLevel?.let { editor.putInt(this.PREF_KEY_URGENT_VOLUME, it).apply() }
    }

    fun getMuteStatus(): Boolean = getSharedPreferences(this.PREF_HEAD_VOICE, AppCompatActivity.MODE_PRIVATE)
            .getBoolean(this.PREF_KEY_MUTE_UNMUTE, false)
    fun getVolumeNormal():Boolean = getSharedPreferences(this.PREF_HEAD_VOICE, AppCompatActivity.MODE_PRIVATE)
        .getBoolean(this.PREF_KEY_VOLUME_NORMAL, false)
    fun getNormalVolume():Int = getSharedPreferences(this.PREF_HEAD_VOICE, AppCompatActivity.MODE_PRIVATE)
        .getInt(this.PREF_KEY_NORMAL_VOLUME, 0)
    fun getVolumeUrgent(): Boolean = getSharedPreferences(this.PREF_HEAD_VOICE, AppCompatActivity.MODE_PRIVATE)
            .getBoolean(this.PREF_KEY_VOLUME_URGENT, false)
    fun getUrgentVolume():Int = getSharedPreferences(this.PREF_HEAD_VOICE, AppCompatActivity.MODE_PRIVATE)
            .getInt(this.PREF_KEY_URGENT_VOLUME, 0)

    //App setup and UI preferences
    fun setAppModePref(
        isTraining: Boolean? = null,
        isCustomTheme:Boolean? = null,
        customTheme:Int? = null,
        parallelListen:Boolean? = null,
        leftHandy:Boolean? = null,
        onStartListen:Boolean? = null
    ){
        val editor = getSharedPreferences(this.PREF_HEAD_APP_SETUP, MODE_PRIVATE).edit()
        isTraining?.let{ editor.putBoolean(this.PREF_KEY_TRAINING, it).apply() }
        isCustomTheme?.let{ editor.putBoolean(this.PREF_KEY_CUSTOM_THEME, it).apply() }
        customTheme?.let { editor.putInt(this.PREF_KEY_THEME, it).apply() }
        parallelListen?.let{ editor.putBoolean(this.PREF_KEY_PARLLEL_TALK, it).apply() }
        leftHandy?.let { editor.putBoolean(this.PREF_KEY_HANDY, it).apply() }
        onStartListen?.let { editor.putBoolean(this.PREF_KEY_START_TALK, it).apply() }
    }

    fun getTrainingStatus(): Boolean = getSharedPreferences(this.PREF_HEAD_APP_SETUP, AppCompatActivity.MODE_PRIVATE)
        .getBoolean(this.PREF_KEY_TRAINING, false)
    fun isCustomTheme():Boolean = getSharedPreferences(this.PREF_HEAD_APP_SETUP, AppCompatActivity.MODE_PRIVATE)
        .getBoolean(this.PREF_KEY_CUSTOM_THEME, false)
    fun getThemeState(): Int = getSharedPreferences(this.PREF_HEAD_APP_SETUP, AppCompatActivity.MODE_PRIVATE)
        .getInt(this.PREF_KEY_THEME, this.defaultTheme)
    fun getParallelResponseStatus(): Boolean = getSharedPreferences(this.PREF_HEAD_APP_SETUP, AppCompatActivity.MODE_PRIVATE)
        .getBoolean(this.PREF_KEY_PARLLEL_TALK, false)
    fun getLeftHandy():Boolean = getSharedPreferences(this.PREF_HEAD_APP_SETUP, AppCompatActivity.MODE_PRIVATE)
        .getBoolean(this.PREF_KEY_HANDY, false)
    fun shouldListenStartup():Boolean = getSharedPreferences(this.PREF_HEAD_APP_SETUP, AppCompatActivity.MODE_PRIVATE)
        .getBoolean(this.PREF_KEY_START_TALK, false)

    //Mathematics and calculation preferences
    fun setMathsPref(angleUnit:String? = null){
        val editor = getSharedPreferences(this.PREF_HEAD_MATHS, MODE_PRIVATE).edit()
        angleUnit?.let{ editor.putString(this.PREF_KEY_ANGLE_UNIT, it).apply() }
    }
    fun getAngleUnit(): String = getSharedPreferences(this.PREF_HEAD_MATHS, AppCompatActivity.MODE_PRIVATE)
        .getString(this.PREF_KEY_ANGLE_UNIT, this.degree)!!

    //Security preferences
    fun setSecurityPref(
        biometricOn:Boolean? = null,
        vocalAuthOn:Boolean? = null,
        vocalAuthPhrase:String? = null
    ){
        val editor = getSharedPreferences(this.PREF_HEAD_SECURITY, MODE_PRIVATE).edit()
        biometricOn?.let { editor.putBoolean(this.PREF_KEY_BIOMETRIC, it).apply() }
        vocalAuthOn?.let { editor.putBoolean(this.PREF_KEY_VOCAL_AUTH, it).apply() }
        vocalAuthPhrase?.let { editor.putString(this.PREF_KEY_VOCAL_PHRASE, it).apply() }
    }

    fun getBiometricStatus(): Boolean = getSharedPreferences(this.PREF_HEAD_SECURITY, AppCompatActivity.MODE_PRIVATE)
        .getBoolean(this.PREF_KEY_BIOMETRIC, false)
    fun getPhraseKeyStatus(): Boolean = getSharedPreferences(this.PREF_HEAD_SECURITY, AppCompatActivity.MODE_PRIVATE)
        .getBoolean(this.PREF_KEY_VOCAL_AUTH, false)
    fun getVoiceKeyPhrase(): String? = getSharedPreferences(this.PREF_HEAD_SECURITY, AppCompatActivity.MODE_PRIVATE)
        .getString(this.PREF_KEY_VOCAL_PHRASE, null)

    fun checkBioMetrics(): Boolean = when (BiometricManager.from(this).canAuthenticate()) {
        BiometricManager.BIOMETRIC_SUCCESS -> true
        else -> false
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelID = resources.getStringArray(R.array.notification_channel)[0]
            val name = resources.getStringArray(R.array.notification_channel)[1]
            val descriptionText = resources.getStringArray(R.array.notification_channel)[2]
            val importance = NotificationManager.IMPORTANCE_HIGH
            val mChannel = NotificationChannel(channelID, name, importance)
            mChannel.description = descriptionText
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(mChannel)
        }
    }

    fun isLocaleHindi():Boolean = this.locale.toString() == this.hindiLocale

    fun hasPermissions(context: Context): Boolean = this.permissions.all {
        ActivityCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }
}