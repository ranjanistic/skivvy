package org.ranjanistic.skivvy

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.speech.tts.TextToSpeech

class SkivvyService : Service() {
    lateinit var skivvy: Skivvy
    lateinit var tts: TextToSpeech
    override fun onCreate() {
        super.onCreate()
        skivvy = this.application as Skivvy
        skivvy.tts?.let { tts = it }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        sendBroadcast(
            Intent(skivvy.actionServiceRestart)
                .putExtra(skivvy.serviceDead, true)
        )
        speakOutService("I'm still here!")
        return super.onStartCommand(intent, flags, startId)
    }
    override fun onBind(intent: Intent): IBinder {
        speakOutService("I'm bind")
        TODO("Return the communication channel to the service.")
    }

    override fun onDestroy() {
        super.onDestroy()
        speakOutService("I'm dead")
        sendBroadcast(
            Intent(skivvy.actionServiceRestart)
                .putExtra(skivvy.serviceDead, true)
        )
    }

    private fun speakOutService(message: String) {
        if (!skivvy.getMuteStatus())
            tts.speak(message, TextToSpeech.QUEUE_FLUSH, null, "")
    }
}