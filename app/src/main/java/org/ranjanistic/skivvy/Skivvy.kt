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
import android.provider.ContactsContract
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.core.app.ActivityCompat
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.ranjanistic.skivvy.manager.ContactDataManager
import org.ranjanistic.skivvy.manager.PackageDataManager
import java.util.*

open class Skivvy : Application() {
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
    val locale: Locale = Locale.US
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
    val nonVocalRequestCodes = intArrayOf(CODE_LOCATION_SERVICE,CODE_DEVICE_ADMIN)
    //default strings and arrays
    val PREF_HEAD_SECURITY = "security"
    val PREF_KEY_BIOMETRIC = "fingerprint"
    val PREF_KEY_VOCAL_AUTH = "voiceAuth"
    val PREF_KEY_VOCAL_PHRASE  = "voicePhrase"
    val PREF_HEAD_VOICE = "voice"
    val PREF_KEY_MUTE_UNMUTE = "voiceStat"
    val PREF_HEAD_APP_MODE = "appMode"
    val PREF_KEY_TRAINING = "training"
    val PREF_KEY_THEME = "theme"
    val PREF_KEY_PARLLEL_TALK = "paralledResponse"
    val PREF_KEY_ANGLE_UNIT = "angleUnit"
    val PREF_HEAD_CALC = "calculator"
    val PREF_KEY_LAST_CALC = "lastResult"
    val FINISH_ACTION = "finish"
    val mathFunctions = arrayOf("sin", "cos", "tan", "cot", "sec", "cosec", "log", "ln","sqrt","cbrt","exp")
    val operators = arrayOf("^", "p", "/", "*", "m", "-", "+")

    //default objects
    var tts: TextToSpeech? = null
    var packageDataManager: PackageDataManager =
        PackageDataManager()
    var contactDataManager: ContactDataManager =
        ContactDataManager()
    lateinit var deviceManager: DevicePolicyManager
    lateinit var compName: ComponentName
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
                Toast.makeText(this, "Error in speaking", Toast.LENGTH_SHORT).show()
        })
        /*
        GlobalScope.launch {    //Long running task, getting all contacts
            getLocalContacts()
        }
         */
        createNotificationChannel()
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
                packageData.appName[count] = pm.getApplicationLabel(packageInfo).toString().toLowerCase(this.locale)
                packageData.appPackage[count] = packageInfo.packageName.toLowerCase(this.locale)
                packageData.appIntent[count] = pm.getLaunchIntentForPackage(packageInfo.packageName)!!
                packageData.appIcon[count] = pm.getApplicationIcon(packageInfo)
                ++count
            } else {                    //removing un-launchable packages
                this.packageDataManager.setTotalPackages(this.packageDataManager.getTotalPackages()-1)
            }
        }
        this.packageDataManager.setPackagesDetail(packageData)
    }

    private fun getLocalContacts(){
        var contactCount = 0
        lateinit var contactData: ContactDataManager.ContactData
        val cr: ContentResolver = contentResolver
        val cur: Cursor? = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null)
        if (cur!!.count > 0) {
            this.contactDataManager.setTotalContacts(cur.count)
            contactData = ContactDataManager.ContactData(this.contactDataManager.getTotalContacts())
            /*
            this.contactDataManager.setContactDataInitials(
                arrayOfNulls(this.contactDataManager.getTotalContacts()),
                arrayOfNulls(this.contactDataManager.getTotalContacts()),
                arrayOfNulls(this.contactDataManager.getTotalContacts()),
                arrayOfNulls(this.contactDataManager.getTotalContacts()),
                arrayOfNulls(this.contactDataManager.getTotalContacts()),
                arrayOfNulls(this.contactDataManager.getTotalContacts())
            )
             */
            while (cur.moveToNext()) {
                contactData.iDs[contactCount] = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID))
                contactData.photoIDs[contactCount] = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.PHOTO_URI))
                contactData.names[contactCount] = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
                /*
                this.contactDataManager.setContactSoloData(contactCount,
                    cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID)),
                    cur.getString(cur.getColumnIndex(ContactsContract.Contacts.PHOTO_URI)),
                    cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
                )

                 */

                //for nicknames
                var deepCur:Cursor? = cr.query(ContactsContract.Data.CONTENT_URI,
                    null,
                    ContactsContract.Data.CONTACT_ID + " = ? AND " + ContactsContract.Data.MIMETYPE + " = ?",
                    arrayOf(contactData.iDs[contactCount], ContactsContract.CommonDataKinds.Nickname.CONTENT_ITEM_TYPE),
                    null
                )
                if(deepCur!!.count>0) {
//                    this.contactDataManager.setContactNicknameInitials(contactCount, arrayOfNulls(deepCur.count))
                    contactData.nickNames[contactCount] = arrayOfNulls(deepCur.count)
                    var nickCount = 0
                    while (deepCur.moveToNext()) {
                        val nicknameName = deepCur.getString(deepCur.getColumnIndex(ContactsContract.CommonDataKinds.Nickname.NAME))?.toLowerCase(this.locale)
                        contactData.nickNames[contactCount]?.set(nickCount, nicknameName)
//                        this.contactDataManager.setContactNicknameData(contactCount,nickCount,nicknameName)
                        ++nickCount
                    }
                }

                //for phone numbers
                deepCur = cr.query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    null,
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                    arrayOf(contactDataManager.getContactIDs()[contactCount]),
                    null
                )
                    var size = 0
                    while(deepCur!!.moveToNext()){
                        ++size
                    }
                contactData.phones[contactCount] = arrayOfNulls(size)
                val localPhone = arrayOfNulls<String>(size)
                    var pCount = 0
                    deepCur.moveToFirst()
                    while (pCount<size) {
                        localPhone[pCount] = deepCur.getString(deepCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                        //TODO: Check this too if using this method
                        var tempPhone = ""
                        if(localPhone[pCount]!=null) {
                            localPhone[pCount] = localPhone[pCount]!!.replace("+", "")
                            if (!(localPhone[pCount]!!.toCharArray()[0] == '0' && localPhone[pCount]!!.toCharArray()[1] == '1'
                                        && localPhone[pCount]!!.toCharArray()[2] == '2' && localPhone[pCount]!!.toCharArray()[3] =='0') ){
                                localPhone[pCount]!!.replace(" ", "")
                                if (localPhone[pCount]!!.toCharArray()[0] == '0') {
                                    var k = 1
                                    while (k < localPhone[pCount]!!.length) {
                                        localPhone[pCount]!!.toCharArray()[k-1] =
                                            localPhone[pCount]!!.toCharArray()[k]
                                        ++k
                                    }
                                }
                                if(localPhone[pCount].toString().length==10){
                                    var x = 0
                                    while(x<5) {
                                        if(tempPhone == ""){
                                            tempPhone = localPhone[pCount]!!.toCharArray()[x].toString()
                                        } else {
                                            tempPhone += localPhone[pCount]!!.toCharArray()[x].toString()
                                        }
                                        ++x
                                    }
                                    tempPhone+=" "
                                    while(x<10) {
                                        tempPhone+= localPhone[pCount]!!.toCharArray()[x].toString()
                                        ++x
                                    }
                                }
                            }
                            contactData.phones[contactCount]?.set(pCount, tempPhone)
//                            this.contactDataManager.setContactPhoneData(contactCount, pCount, tempPhone)
                        }
                        deepCur.moveToNext()
                        if(tempPhone!=" ") {
                            ++pCount
                        }
                    }

                //for email IDs
                deepCur = cr.query(
                    ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                    null,
                    ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?",
                    arrayOf(contactDataManager.getContactIDs()[contactCount]),
                    null
                )
                if(deepCur!!.count>0) {
                    this.contactDataManager.setContactEmailsInitials(contactCount, arrayOfNulls(deepCur.count))
                    var eCount = 0
                    while(deepCur.moveToNext()) {
                        contactData.emails[contactCount]?.set(eCount,deepCur.getString(deepCur.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA)))
//                        this.contactDataManager.setContactEmailData(contactCount,eCount, deepCur.getString(deepCur.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA)))
                        ++eCount
                    }
                }
                deepCur.close()
                ++contactCount
            }
        }
        cur.close()
        this.contactDataManager.setContactDetails(contactData)
    }


    //Security prefs
    fun getBiometricStatus(): Boolean {
        return getSharedPreferences(this.PREF_HEAD_SECURITY, AppCompatActivity.MODE_PRIVATE)
            .getBoolean(this.PREF_KEY_BIOMETRIC, false)
    }
    fun setBiometricsStatus(isEnabled: Boolean) {
        getSharedPreferences(this.PREF_HEAD_SECURITY, AppCompatActivity.MODE_PRIVATE).edit()
            .putBoolean(this.PREF_KEY_BIOMETRIC, isEnabled).apply()
    }
    fun getPhraseKeyStatus():Boolean{
        return getSharedPreferences(this.PREF_HEAD_SECURITY, AppCompatActivity.MODE_PRIVATE)
            .getBoolean(this.PREF_KEY_VOCAL_AUTH, false)
    }
    fun setPhraseKeyStatus(voiceAuthStatus:Boolean){
        getSharedPreferences(this.PREF_HEAD_SECURITY, AppCompatActivity.MODE_PRIVATE).edit()
            .putBoolean(this.PREF_KEY_VOCAL_AUTH, voiceAuthStatus).apply()
    }
    fun getVoiceKeyPhrase():String?{
        return getSharedPreferences(this.PREF_HEAD_SECURITY, AppCompatActivity.MODE_PRIVATE)
            .getString(this.PREF_KEY_VOCAL_PHRASE,null)
    }
    fun setVoiceKeyPhrase(phrase: String?){
        getSharedPreferences(this.PREF_HEAD_SECURITY, AppCompatActivity.MODE_PRIVATE).edit()
            .putString(this.PREF_KEY_VOCAL_PHRASE, phrase).apply()
    }

    //custom prefs
    fun getTrainingStatus(): Boolean {
        return getSharedPreferences(this.PREF_HEAD_APP_MODE, AppCompatActivity.MODE_PRIVATE)
            .getBoolean(this.PREF_KEY_TRAINING, false)
    }
    fun setTrainingStatus(isTraining: Boolean) {
        getSharedPreferences(this.PREF_HEAD_APP_MODE, MODE_PRIVATE).edit()
            .putBoolean(this.PREF_KEY_TRAINING, isTraining).apply()
    }
    fun getMuteStatus(): Boolean {
        return getSharedPreferences(this.PREF_HEAD_APP_MODE, AppCompatActivity.MODE_PRIVATE)
            .getBoolean(this.PREF_KEY_MUTE_UNMUTE, false)
    }
    fun saveMuteStatus(isMuted: Boolean) {
        getSharedPreferences(this.PREF_HEAD_APP_MODE, AppCompatActivity.MODE_PRIVATE).edit()
            .putBoolean(this.PREF_KEY_MUTE_UNMUTE, isMuted).apply()
    }
    fun setThemeState(themeCode:Int){
        getSharedPreferences(this.PREF_HEAD_APP_MODE, AppCompatActivity.MODE_PRIVATE).edit()
            .putInt(this.PREF_KEY_THEME, themeCode).apply()
    }
    fun getThemeState():Int{
        return getSharedPreferences(this.PREF_HEAD_APP_MODE, AppCompatActivity.MODE_PRIVATE)
            .getInt(this.PREF_KEY_THEME, R.style.DarkTheme)
    }
    fun setParallelResponseStatus(isParallel:Boolean){
        getSharedPreferences(this.PREF_HEAD_APP_MODE, AppCompatActivity.MODE_PRIVATE).edit()
            .putBoolean(this.PREF_KEY_PARLLEL_TALK, isParallel).apply()
    }
    fun getParallelResponseStatus():Boolean{
        return getSharedPreferences(this.PREF_HEAD_APP_MODE, AppCompatActivity.MODE_PRIVATE)
            .getBoolean(this.PREF_KEY_PARLLEL_TALK, false)
    }
    fun setAngleUnit(unit:String){
        getSharedPreferences(this.PREF_HEAD_APP_MODE, AppCompatActivity.MODE_PRIVATE).edit()
            .putString(this.PREF_KEY_ANGLE_UNIT, unit).apply()
    }
    fun getAngleUnit():String{
        return getSharedPreferences(this.PREF_HEAD_APP_MODE, AppCompatActivity.MODE_PRIVATE)
            .getString(this.PREF_KEY_ANGLE_UNIT, "deg")!!
    }
    fun checkBioMetrics(): Boolean {
        val biometricManager = BiometricManager.from(this)
        return when (biometricManager.canAuthenticate()) {
            BiometricManager.BIOMETRIC_SUCCESS -> true
            else -> false
        }
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
    fun hasPermissions(context: Context): Boolean = this.permissions.all {
            ActivityCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }
}