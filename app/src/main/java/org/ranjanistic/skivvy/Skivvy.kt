@file:Suppress("PropertyName")

package org.ranjanistic.skivvy

import android.Manifest
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ContentResolver
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.database.Cursor
import android.os.Build
import android.provider.ContactsContract
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*

class Skivvy : Application() {
    //permission codes
    val CODE_ALL_PERMISSIONS = 999
    val CODE_CALL_REQUEST = 1000
    val CODE_STORAGE_REQUEST = 1001
    val CODE_CONTACTS_REQUEST = 1002
    val CODE_SMS_REQUEST = 1003
    val permissions = arrayOf(
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_CONTACTS,
        Manifest.permission.READ_CONTACTS,
        Manifest.permission.CALL_PHONE,
        Manifest.permission.ACCESS_WIFI_STATE,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.SEND_SMS,
        Manifest.permission.READ_SMS,
        Manifest.permission.BODY_SENSORS
    )

    //defaults
    val locale: Locale = Locale.US
    val numberPattern = "[0-9]".toRegex()
    val textPattern = "[a-zA-Z]".toRegex()
    val emailPattern = "[a-zA-Z0-9._-]+@[a-z.]+\\.+[a-z]+".toRegex()
    val nonNumeralPattern = "[^0-9]".toRegex()

    //action codes
    val CODE_SPEECH_RECORD = 10
    val CODE_APP_CONF = 11
    val CODE_CALL_CONF = 12
    val CODE_EMAIL_CONF = 13
    val CODE_EMAIL_CONTENT = 14
    val CODE_SMS_CONF = 15
    val CODE_WHATSAPP_ACTION = 151
    val CODE_TEXT_MESSAGE_BODY = 16
    val CODE_VOICE_AUTH_INIT = 17
    val CODE_VOICE_AUTH_CONFIRM = 18
    val CODE_BIOMETRIC_CONFIRM = 19
    //command codes
    val CODE_LOCATION_SERVICE = 100
    val CODE_LOCK_SCREEN = 101

    //default strings and arrays
    val PREF_HEAD_SECURITY = "security"
    val PREF_KEY_BIOMETRIC = "fingerprint"
    val PREF_KEY_VOCAL_AUTH = "voiceAuth"
    val PREF_HEAD_APP_MODE = "appMode"
    val PREF_HEAD_VOICE = "voice"
    val PREF_KEY_MUTE_UNMUTE = "voiceStat"
    val PREF_KEY_TRAINING = "training"
    val FINISH_ACTION = "finish"
    val mathFunctions = arrayOf("sin", "cos", "tan", "cot", "sec", "cosec", "log", "ln","sqrt","cbrt","exp")
    val operators: Array<Char> = arrayOf('^', 'p', '/', '*', 'm', '-', '+')

    var tts: TextToSpeech? = null
    var packageData:PackageData = PackageData()
    var contactData:ContactData = ContactData()
    //var callReceiver:CallReceiver = CallReceiver()

    override fun onCreate() {
        super.onCreate()
        this.tts = TextToSpeech(this, TextToSpeech.OnInitListener {
            if (it == TextToSpeech.SUCCESS) {
                this.tts!!.language = this.locale
            } else
                Toast.makeText(this, "Error in speaking", Toast.LENGTH_SHORT).show()
        })
        GlobalScope.launch {    //Long running task, getting all packages
            getLocalPackages()
        }
        /*
        GlobalScope.launch {    //Long running task, getting all contacts
            getLocalContacts()
        }
         */
        createNotificationChannel()
    }

    //gets all packages and respective details available on device
    private fun getLocalPackages() {
        var counter = 0
        val pm: PackageManager = packageManager
        val packages: List<ApplicationInfo> =
            pm.getInstalledApplications(PackageManager.GET_META_DATA)
        packageData.setTotalPackages(packages.size)
        packageData.setPackageInitials(arrayOfNulls(packageData.getTotalPackages()),
            arrayOfNulls(packageData.getTotalPackages()),arrayOfNulls(packageData.getTotalPackages()),
            arrayOfNulls(packageData.getTotalPackages())
        )
        for (packageInfo in packages) {
            if (pm.getLaunchIntentForPackage(packageInfo.packageName) != null) {
                packageData.setPackageDetails(counter,pm.getApplicationLabel(packageInfo).toString().toLowerCase(this.locale),
                    packageInfo.packageName.toLowerCase(this.locale),
                    pm.getLaunchIntentForPackage(packageInfo.packageName)!!,
                    pm.getApplicationIcon(packageInfo)
                )
                ++counter
            } else {
                packageData.setTotalPackages(packageData.getTotalPackages()-1)                    //removing un-launchable packages
            }
        }
    }

    private fun getLocalContacts(){
        var contactCount = 0
        val cr: ContentResolver = contentResolver
        val cur: Cursor? = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null)
        if (cur!!.count > 0) {
            this.contactData.setTotalContacts(cur.count)
            this.contactData.setContactDataInitials(
                arrayOfNulls(this.contactData.getTotalContacts()),
                arrayOfNulls(this.contactData.getTotalContacts()),
                arrayOfNulls(this.contactData.getTotalContacts()),
                arrayOfNulls(this.contactData.getTotalContacts()),
                arrayOfNulls(this.contactData.getTotalContacts()),
                arrayOfNulls(this.contactData.getTotalContacts())
            )
            while (cur.moveToNext()) {
                this.contactData.setContactSoloData(contactCount,
                    cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID)),
                    cur.getString(cur.getColumnIndex(ContactsContract.Contacts.PHOTO_URI)),
                    cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
                )

                //for nicknames
                var deepCur:Cursor? = cr.query(ContactsContract.Data.CONTENT_URI,
                    null,
                    ContactsContract.Data.CONTACT_ID + " = ? AND " + ContactsContract.Data.MIMETYPE + " = ?",
                    arrayOf(contactData.getContactIDs()[contactCount], ContactsContract.CommonDataKinds.Nickname.CONTENT_ITEM_TYPE),
                    null
                )
                if(deepCur!!.count>0) {
                    this.contactData.setContactNicknameInitials(contactCount, arrayOfNulls(deepCur.count))
                    var nickCount = 0
                    while (deepCur.moveToNext()) {
                        val nicknameName = deepCur.getString(deepCur.getColumnIndex(ContactsContract.CommonDataKinds.Nickname.NAME))?.toLowerCase(this.locale)
                        this.contactData.setContactNicknameData(contactCount,nickCount,nicknameName)
                        ++nickCount
                    }
                }

                //for phone numbers
                deepCur = cr.query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    null,
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                    arrayOf(contactData.getContactIDs()[contactCount]),
                    null
                )
                    var size = 0
                    while(deepCur!!.moveToNext()){
                        ++size
                    }
                val localPhone = arrayOfNulls<String>(size)
                    var pCount = 0
                    deepCur.moveToFirst()
                    while (pCount<size) {
                        localPhone[pCount] = deepCur.getString(deepCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
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
                            this.contactData.setContactPhoneData(
                                contactCount, pCount,
                                tempPhone
                            )
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
                    arrayOf(contactData.getContactIDs()[contactCount]),
                    null
                )
                if(deepCur!!.count>0) {
                    this.contactData.setContactEmailsInitials(contactCount, arrayOfNulls(deepCur.count))
                    var eCount = 0
                    while(deepCur.moveToNext()) {
                        this.contactData.setContactEmailData(
                            contactCount,eCount,
                            deepCur.getString(deepCur.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA))
                        )
                        ++eCount
                    }
                }
                deepCur.close()
                ++contactCount
            }
        }
        cur.close()
    }

    fun getBiometricStatus(): Boolean {
        return getSharedPreferences(this.PREF_HEAD_SECURITY, AppCompatActivity.MODE_PRIVATE)
            .getBoolean(this.PREF_KEY_BIOMETRIC, false)
    }

    fun setBiometricsStatus(isEnabled: Boolean) {
        getSharedPreferences(this.PREF_HEAD_SECURITY, AppCompatActivity.MODE_PRIVATE).edit()
            .putBoolean(this.PREF_KEY_BIOMETRIC, isEnabled).apply()
    }

    fun getMuteStatus(): Boolean {
        return getSharedPreferences(this.PREF_HEAD_VOICE, AppCompatActivity.MODE_PRIVATE)
            .getBoolean(this.PREF_KEY_MUTE_UNMUTE, false)
    }

    fun saveMuteStatus(isMuted: Boolean) {
        getSharedPreferences(this.PREF_HEAD_VOICE, AppCompatActivity.MODE_PRIVATE).edit()
            .putBoolean(this.PREF_KEY_MUTE_UNMUTE, isMuted).apply()
    }
    fun getPhraseKeyStatus():Boolean{
        return getSharedPreferences(this.PREF_HEAD_SECURITY, AppCompatActivity.MODE_PRIVATE)
            .getBoolean(this.PREF_KEY_VOCAL_AUTH, false)
    }
    fun setPhraseKeyStatus(voiceAuthStatus:Boolean){
        getSharedPreferences(this.PREF_HEAD_SECURITY, AppCompatActivity.MODE_PRIVATE).edit()
            .putBoolean(this.PREF_KEY_VOCAL_AUTH, voiceAuthStatus).apply()
    }
    fun setVoiceKeyPhrase(phrase: String?){
        getSharedPreferences(this.PREF_HEAD_SECURITY, AppCompatActivity.MODE_PRIVATE).edit()
            .putString(this.PREF_KEY_TRAINING, phrase).apply()
    }
    fun getVoiceKeyPhrase():String?{
        return getSharedPreferences(this.PREF_HEAD_SECURITY, AppCompatActivity.MODE_PRIVATE)
            .getString(this.PREF_KEY_TRAINING,null)
    }

    fun getTrainingStatus(): Boolean {
        return getSharedPreferences(this.PREF_HEAD_APP_MODE, AppCompatActivity.MODE_PRIVATE)
            .getBoolean(this.PREF_KEY_TRAINING, false)
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
}