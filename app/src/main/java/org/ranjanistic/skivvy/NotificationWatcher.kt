package org.ranjanistic.skivvy

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log


class NotificationWatcher : NotificationListenerService() {
    private val tag = this.javaClass.simpleName
    private var nwservicereciver: NotificationWatcherReceiver? = null
    override fun onCreate() {
        super.onCreate()
        nwservicereciver = NotificationWatcherReceiver()
        val filter = IntentFilter()
        filter.addAction("${BuildConfig.APPLICATION_ID}.NOTIFICATION_LISTENER_SERVICE_EXAMPLE")
        registerReceiver(nwservicereciver, filter)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(nwservicereciver)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        Log.i(tag, "**********  onNotificationPosted")
        Log.i(tag, "ID :" + sbn.id + "\t" + sbn.notification.tickerText + "\t" + sbn.packageName)
        sendBroadcast(Intent("${BuildConfig.APPLICATION_ID}.NOTIFICATION_LISTENER_EXAMPLE")
            .putExtra(
                "notification_event", """onNotificationPosted :${sbn.packageName}""".trimIndent()
            )
        )
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        Log.i(tag, "********** onNOtificationRemoved")
        Log.i(
            tag,
            "ID :" + sbn.id + "\t" + sbn.notification.tickerText + "\t" + sbn.packageName
        )
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