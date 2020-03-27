@file:Suppress( "PropertyName")
package org.ranjanistic.skivvy

import android.Manifest
import android.app.Application
import java.util.*

class Skivvy:Application() {
    //permission codes
    val CODE_ALL_PERMISSIONS = 999
    val CODE_CALL_REQUEST = 1000
    val CODE_STORAGE_REQUEST = 1001
    val CODE_CONTACTS_REQUEST = 1002
    val permissions = arrayOf(
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_CONTACTS,
        Manifest.permission.READ_CONTACTS,
        Manifest.permission.CALL_PHONE,
        Manifest.permission.ACCESS_WIFI_STATE,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.READ_SMS,
        Manifest.permission.BODY_SENSORS
    )
    //defaults
    val locale: Locale = Locale.US
    val phonePattern = "[0-9]".toRegex()
    val emailPattern = "[a-zA-Z0-9._-]+@[a-z.]+\\.+[a-z]+".toRegex()

    //action codes
    val CODE_SPEECH_RECORD = 10
    val CODE_OTHER_APP = 11
    val CODE_OTHER_APP_CONF = 12
    val CODE_CALL_CONF = 13
    val CODE_CONTACT_CALL_CONF =14
    val CODE_EMAIL_CONF = 15
    val CODE_CONTACT_EMAIL_CONF = 16
    val CODE_EMAIL_SUBJECT = 17
    val CODE_EMAIL_BODY = 18

    //command codes
    val CODE_LOCATION_SERVICE = 100
    val CODE_LOCK_SCREEN = 101

    //preference keys
    val PREF_HEAD_SECURITY = "security"
    val PREF_KEY_BIOMETRIC = "fingerprint"
    val PREF_HEAD_VOICE = "voice"
    val PREF_KEY_MUTE_UNMUTE = "voiceStat"
    val PREF_HEAD_APP_MODE = "appMode"
    val PREF_KEY_TRAINING = "training"
}