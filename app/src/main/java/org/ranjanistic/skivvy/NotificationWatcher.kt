package org.ranjanistic.skivvy

import android.content.Intent
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.speech.tts.TextToSpeech
import org.ranjanistic.skivvy.manager.PackageDataManager


class NotificationWatcher : NotificationListenerService() {
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
    var lastKey: String? = null

    @ExperimentalStdlibApi
    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val appName = packageData.appNameOfPackage(sbn.packageName)
        if (skivvy.showNotifications() && sbn.notification.tickerText != null && sbn.packageName != null && appName != null
            && appName.toLowerCase(skivvy.locale) != sbn.notification.tickerText.toString()
                .toLowerCase(skivvy.locale) && sbn.postTime.toString() != lastTimeNotif
        ) {     //TODO create skivvy background service for voice outputs
            if (skivvy.isHomePageRunning) {
                sendBroadcast(
                    Intent(skivvy.actionNotification)
                        .putExtra(skivvy.notificationID, sbn.key)
                        .putExtra(skivvy.notificationStatus, skivvy.notificationPosted)
                        .putExtra(skivvy.notificationTime, sbn.postTime.toString())
                        .putExtra(skivvy.notificationPackage, sbn.packageName)
                        .putExtra(
                            skivvy.notificationAppName,
                            appName
                        )
                        .putExtra(
                            skivvy.notificationTicker,
                            "${sbn.notification.tickerText}, on $appName"
                        )
                        .putExtra(skivvy.notificationOngoing, sbn.isOngoing)
                )
            } else {
                speakOutNotification(
                    "${sbn.notification.tickerText}, on $appName"
                )
            }
            lastTimeNotif = sbn.postTime.toString()
            lastMsgNotif = sbn.notification.tickerText.toString()
            lastNotifFrom = sbn.packageName
            lastOngoing = sbn.isOngoing
            lastKey = sbn.key
        }
    }

    @ExperimentalStdlibApi
    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        sendBroadcast(
            Intent(skivvy.actionNotification)
                .putExtra(skivvy.notificationID, sbn.key)
                .putExtra(skivvy.notificationStatus, skivvy.notificationRemoved)
                .putExtra(skivvy.notificationPackage, sbn.packageName)
                .putExtra(skivvy.notificationAppName, packageData.appNameOfPackage(sbn.packageName))
                .putExtra(skivvy.notificationTicker, "${sbn.notification.tickerText}")
        )
    }

    private fun speakOutNotification(message: String) {
        if (!skivvy.getMuteStatus())
            skivvy.tts?.speak(message, TextToSpeech.QUEUE_FLUSH, null, "")
    }
}