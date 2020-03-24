@file:Suppress( "PropertyName")
package org.ranjanistic.skivvy

import android.app.Application

class Skivvy:Application() {
    //permission codes
    var CODE_CALL_REQUEST = 1000
    var CODE_STORAGE_REQUEST = 1001
    var CODE_CONTACTS_REQUEST = 1002

    //action codes
    var CODE_SPEECH_RECORD = 10
    var CODE_OTHER_APP = 11
    var CODE_OTHER_APP_CONF = 12
     var CODE_CALL_CONF = 13

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