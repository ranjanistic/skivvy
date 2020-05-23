package org.ranjanistic.skivvy

import android.content.Intent
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log


class NotificationWatcher : NotificationListenerService() {

    private val tag = "hellNo"
    private lateinit var skivvy: Skivvy

    override fun onCreate() {
        super.onCreate()
        skivvy = this.application as Skivvy
    }

    var i = 0
    override fun onNotificationPosted(sbn: StatusBarNotification) {
        ++i
        Log.i("$tag $i: ", "POSTED")
        Log.i( "$tag $i ID: " , "${sbn.id}")
        Log.i( "$tag $i ticker: " , "${sbn.notification.tickerText}")
        Log.i( "$tag $i from: ", sbn.packageName)
        Log.i( "$tag $i ongoing: ", sbn.isOngoing.toString())
        Log.i( "$tag $i time: ", sbn.postTime.toString())

        sendBroadcast(
            Intent(skivvy.actionNotification)
                .putExtra(skivvy.notificationTime, sbn.postTime.toString())
                .putExtra(skivvy.notificationPackageName, sbn.packageName)
                .putExtra(skivvy.notificationTicker, "${sbn.notification.tickerText}")
        )
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        val j = i
        Log.i( "$tag $j ID: " , "REMOVED")
        Log.i( "$tag $j ID: " , "${sbn.id}")
        Log.i( "$tag $j ticker: " , "${sbn.notification.tickerText}")
        Log.i( "$tag $j from: ", sbn.packageName)
    }

}