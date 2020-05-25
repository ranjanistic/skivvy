package org.ranjanistic.skivvy

import android.app.PendingIntent
import android.graphics.drawable.Icon

interface NotificationContent {
    fun notificationBadge(icon: Icon)
    fun notificationIntent(intent: PendingIntent)
}