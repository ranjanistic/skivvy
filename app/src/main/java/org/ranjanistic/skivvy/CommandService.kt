package org.ranjanistic.skivvy


import android.app.Notification.EXTRA_NOTIFICATION_ID
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Bundle
import android.os.IBinder
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import org.ranjanistic.skivvy.R.color
import org.ranjanistic.skivvy.R.drawable
import java.util.*


class CommandService : Service(){
    private lateinit var skivvy:Skivvy
    @ExperimentalStdlibApi
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        speakOut("Birth of skivvy has taken place")
        showNotification()
        return START_STICKY
    }
    override fun onCreate() {
        super.onCreate()
        skivvy = this.application as Skivvy
        skivvy.tts = TextToSpeech(this, TextToSpeech.OnInitListener {
            if (it == TextToSpeech.SUCCESS) {
                skivvy.tts!!.language = skivvy.locale
            } else
                Toast.makeText(this, "Error in speaking", Toast.LENGTH_SHORT).show()
        })
    }
    override fun onBind(p0: Intent?): IBinder? {
        speakOut("Bind")
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        speakOut("Death")
    }

    @ExperimentalStdlibApi
    private fun showNotification() {
        val intent = Intent(this, Splash::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, intent, 0)
//        val snoozeIntent = Intent(this, ResumeService::class.java)
            //.apply {
//                action = skivvy.FINISH_ACTION
//            putExtra(EXTRA_NOTIFICATION_ID, 69)
     //   }
        //val snoozePendingIntent: PendingIntent =
            //PendingIntent.getBroadcast(this, 0, snoozeIntent, 0)
        val builder = NotificationCompat.Builder(this,
            resources.getStringArray(R.array.notification_channel)[0])
            .setSmallIcon(drawable.ic_yellow_dotsincircle)
            .setContentTitle("Skivvy is running")
            .setContentText("Skivvy is running in background as a service, and is listening to your commands.")
            .setOngoing(true)
            .setAutoCancel(true)
            .setColorized(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            //.addAction(drawable.ic_yellow_dotsincircle, "Stop",
                //snoozePendingIntent)
        val notificationManager: NotificationManagerCompat =
            NotificationManagerCompat.from(this)
        notificationManager.notify(69, builder.build())
    }

    private fun speakOut(text:String) {
        skivvy.tts!!.speak(text, TextToSpeech.QUEUE_FLUSH, null,"")
    }
}