package org.ranjanistic.skivvy

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.view.Gravity
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT

class CommandService : Service() {

    override fun onBind(intent: Intent): IBinder {
        TODO("Return the communication channel to the service.")
    }
    override fun onDestroy() {
        super.onDestroy()
        Toast.makeText(this,"Destroyed", LENGTH_SHORT).show()
    }
}
