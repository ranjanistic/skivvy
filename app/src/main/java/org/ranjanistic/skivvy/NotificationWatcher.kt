package org.ranjanistic.skivvy

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log


class NotificationWatcher : NotificationListenerService() {
    private val tag = "hellNo"
    private var nwservicereciver: NotificationWatcherReceiver? = null
    override fun onCreate() {
        super.onCreate()
        nwservicereciver = NotificationWatcherReceiver()
        val filter = IntentFilter()
        filter.addAction("${BuildConfig.APPLICATION_ID}.NOTIFICATION_LISTENER_SERVICE")
        registerReceiver(nwservicereciver, filter)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(nwservicereciver)
    }
    var i = 0
    override fun onNotificationPosted(sbn: StatusBarNotification) {
        ++i
        Log.i( "$tag $i ID: " , "POSTED")
        Log.i( "$tag $i ID: " , "${sbn.id}")
        Log.i( "$tag $i ticker: " , "${sbn.notification.tickerText}")
        Log.i( "$tag $i from: ", sbn.packageName)
        Log.i( "$tag $i ongoing: ", sbn.isOngoing.toString())
        Log.i( "$tag $i time: ", sbn.postTime.toString())
        sendBroadcast(Intent("${BuildConfig.APPLICATION_ID}.NOTIFICATION_LISTENER_EXAMPLE")
            .putExtra("notification_event", "onNotificationPosted :${sbn.packageName}".trimIndent())
        )
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        val j = i
        Log.i( "$tag $j ID: " , "REMOVED")
        Log.i( "$tag $j ID: " , "${sbn.id}")
        Log.i( "$tag $j ticker: " , "${sbn.notification.tickerText}")
        Log.i( "$tag $j from: ", sbn.packageName)

        sendBroadcast(Intent("${BuildConfig.APPLICATION_ID}.NOTIFICATION_LISTENER_EXAMPLE").putExtra(
            "notification_event", """onNotificationRemoved :${sbn.packageName}""".trimIndent()
        ))
    }

    internal inner class NotificationWatcherReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            if (intent.getStringExtra("command") == "clearall") {
                cancelAllNotifications()
            } else if (intent.getStringExtra("command") == "list") {
                sendBroadcast(Intent("${BuildConfig.APPLICATION_ID}.NOTIFICATION_LISTENER_EXAMPLE")
                    .putExtra("notification_event", "=====================")
                )
                var i = 1
                for (sbn in this@NotificationWatcher.activeNotifications) {
                    sendBroadcast(Intent("${BuildConfig.APPLICATION_ID}.NOTIFICATION_LISTENER_EXAMPLE")
                        .putExtra("notification_event", """$i ${sbn.packageName}""")
                    )
                    i++
                }
                sendBroadcast(Intent("${BuildConfig.APPLICATION_ID}.NOTIFICATION_LISTENER_EXAMPLE")
                    .putExtra("notification_event", "===== Notification List ====")
                )
            }
        }
    }
}