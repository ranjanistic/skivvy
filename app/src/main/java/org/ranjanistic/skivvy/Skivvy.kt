@file:Suppress( "PropertyName")
package org.ranjanistic.skivvy

import android.app.Application
import java.util.*

class Skivvy:Application() {
    //permission codes
    var CODE_CALL_REQUEST = 1000
    var CODE_STORAGE_REQUEST = 1001
    var CODE_CONTACTS_REQUEST = 1002
    var CODE_EMAIL_REQUEST = 1003

    //defaults
    var locale: Locale = Locale.US
    val phonePattern = "[0-9]".toRegex()
    val emailPattern = "[a-zA-Z0-9._-]+@[a-z.]+\\.+[a-z]+".toRegex()
    val simpleTextPattern = "[a-zA-Z]".toRegex()

    //action codes
    var CODE_SPEECH_RECORD = 10
    var CODE_OTHER_APP = 11
    var CODE_OTHER_APP_CONF = 12
     var CODE_CALL_CONF = 13
    var CODE_CONTACT_CALL_CONF =14
    var CODE_EMAIL_CONF = 15
    var CODE_CONTACT_EMAIL_CONF = 16

    //command codes
    var CODE_LOCATION_SERVICE = 100
    var CODE_LOCK_SCREEN = 101

    //preference keys
    var PREF_HEAD_SECURITY = "security"
    var PREF_KEY_BIOMETRIC = "fingerprint"
    var PREF_HEAD_VOICE = "voice"
    var PREF_KEY_MUTE_UNMUTE = "voiceStat"
    var PREF_HEAD_APP_MODE = "appMode"
    var PREF_KEY_TRAINING = "training"
}