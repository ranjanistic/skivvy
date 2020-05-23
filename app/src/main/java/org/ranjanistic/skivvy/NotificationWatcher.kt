package org.ranjanistic.skivvy

import android.content.Intent
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.speech.tts.TextToSpeech
import android.util.Log
import org.ranjanistic.skivvy.manager.PackageDataManager


class NotificationWatcher : NotificationListenerService() {

    private val tag = "hellNo"
    private lateinit var skivvy: Skivvy
    private lateinit var packageData: PackageDataManager

    override fun onCreate() {
        super.onCreate()
        skivvy = this.application as Skivvy
        packageData = PackageDataManager(skivvy)
    }

    var lastTimeNotif: String? = null
    var lastMsgNotif: String? = null
    var lastNotifFrom: String? = null
    var lastOngoing = false
    var i = 0

    @ExperimentalStdlibApi
    override fun onNotificationPosted(sbn: StatusBarNotification) {
        ++i
        Log.i("$tag $i: ", "POSTED")
        Log.i( "$tag $i ID: " , "${sbn.id}")
        Log.i( "$tag $i ticker: " , "${sbn.notification.tickerText}")
        Log.i( "$tag $i from: ", sbn.packageName)
        Log.i( "$tag $i ongoing: ", sbn.isOngoing.toString())
        Log.i( "$tag $i time: ", sbn.postTime.toString())
        if (skivvy.showNotifications() && sbn.notification.tickerText != null && packageData.appNameOfPackage(
                sbn.packageName
            ) != null
        ) {
            if (skivvy.isHomePageRunning) {
                sendBroadcast(
                    Intent(skivvy.actionNotification)
                        .putExtra(skivvy.notificationTime, sbn.postTime.toString())
                        .putExtra(
                            skivvy.notificationAppName,
                            packageData.appNameOfPackage(sbn.packageName)
                        )
                        .putExtra(skivvy.notificationTicker, "${sbn.notification.tickerText}")
                        .putExtra(skivvy.notificationOngoing, sbn.isOngoing)
                )
            } else {
                if (sbn.postTime.toString() != lastTimeNotif && sbn.notification.tickerText != lastMsgNotif && sbn.packageName != lastNotifFrom
                    || sbn.isOngoing != lastOngoing
                ) {
                    speakOutNotification(
                        "${sbn.notification.tickerText}" + ", on " + packageData.appNameOfPackage(
                            sbn.packageName
                        )
                    )
                    lastTimeNotif = sbn.postTime.toString()
                    lastMsgNotif = sbn.postTime.toString()
                    lastNotifFrom = sbn.postTime.toString()
                    lastOngoing = sbn.isOngoing
                }
            }
        }
    }

    private fun speakOutNotification(message: String) {
        if (!skivvy.getMuteStatus())
            skivvy.tts?.speak(message, TextToSpeech.QUEUE_FLUSH, null, "")
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        //TODO: keep displaying until not removed
        val j = i
        Log.i( "$tag $j ID: " , "REMOVED")
        Log.i( "$tag $j ID: " , "${sbn.id}")
        Log.i( "$tag $j ticker: " , "${sbn.notification.tickerText}")
        Log.i( "$tag $j from: ", sbn.packageName)
    }

}