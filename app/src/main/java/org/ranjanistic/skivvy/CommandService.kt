package org.ranjanistic.skivvy

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.speech.tts.TextToSpeech
import android.widget.Toast
import java.util.*

class CommandService : Service(){
    private var tts: TextToSpeech? = null
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        speakOut("Service started and is still running even if the activity is closed and now destroyed therefore it is something to ponder")
        return super.onStartCommand(intent, flags, startId)
    }
    override fun onCreate() {
        super.onCreate()
        tts = TextToSpeech(this, TextToSpeech.OnInitListener {
            if (it == TextToSpeech.SUCCESS) {
                tts!!.language = Locale.US
            } else
                Toast.makeText(this, "Error in speaking", Toast.LENGTH_SHORT).show()
        })
    }
    override fun onBind(p0: Intent?): IBinder? {
        speakOut("Service onBind")
        TODO("Not yet implemented")
    }
    private fun speakOut(text:String) {
        tts!!.speak(text, TextToSpeech.QUEUE_FLUSH, null,"")
    }
}