package org.ranjanistic.skivvy

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.widget.Toast
import kotlin.coroutines.coroutineContext

class ResumeService: BroadcastReceiver() {
    override fun onReceive(p0: Context?, p1: Intent?) {
        Log.d("oof","dead")
        if(p1!!.action == "ACTION_UNINSTALL_RESULT"){

        }
        /*
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            p0!!.startForegroundService(Intent(p0, CommandService::class.java))
        } else {
            p0!!.startService(Intent(p0, CommandService::class.java))
        }

         */
    }
}