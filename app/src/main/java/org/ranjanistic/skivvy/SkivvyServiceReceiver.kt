package org.ranjanistic.skivvy

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class SkivvyServiceReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val dead = intent.getBooleanExtra("Service Destroyed", false)
        if (dead) {
            Log.d("serviceSkivvy", "isDead")
            context.startService(Intent(context, SkivvyService::class.java))
        } else {
            Log.d("serviceSkivvy", "isAliveOn start command")
        }
    }
}
