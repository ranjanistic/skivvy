@file:Suppress( "PropertyName")
package org.ranjanistic.skivvy

import android.Manifest
import android.app.Application
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
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
    val CODE_TEXT_MESSAGE_BODY = 16

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

    //package list variables
    lateinit var packagesAppName:Array<String?>
    lateinit var packagesName:Array<String?>
    lateinit var packagesMain:Array<Intent?>
    lateinit var packagesIcon:Array<Drawable?>
    var packagesTotal:Int = 0

    override fun onCreate() {
        super.onCreate()
        GlobalScope.launch {    //Long running task, getting all packages
            getLocalPackages()
        }
    }
    //gets all packages and respective details available on device
    private fun getLocalPackages(){
        var counter = 0
        val pm: PackageManager = packageManager
        val packages: List<ApplicationInfo> = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        this.packagesTotal = packages.size
        this.packagesAppName = arrayOfNulls(this.packagesTotal)
        this.packagesName = arrayOfNulls(this.packagesTotal)
        this.packagesIcon = arrayOfNulls(this.packagesTotal)
        this.packagesMain = arrayOfNulls(this.packagesTotal)
        for (packageInfo in packages) {
            if(pm.getLaunchIntentForPackage(packageInfo.packageName) != null) {
                this.packagesAppName[counter] = pm.getApplicationLabel(packageInfo).toString().toLowerCase(this.locale)
                this.packagesName[counter] = packageInfo.packageName.toLowerCase(this.locale)
                this.packagesIcon[counter] = pm.getApplicationIcon(packageInfo)
                this.packagesMain[counter] = pm.getLaunchIntentForPackage(packageInfo.packageName)
                ++counter
            } else {
                --this.packagesTotal    //removing un-launchable packages
            }
        }
    }
}