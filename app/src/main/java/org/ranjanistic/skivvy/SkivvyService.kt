package org.ranjanistic.skivvy

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import android.speech.tts.TextToSpeech
import android.widget.Toast
import java.util.*

class SkivvyService : Service() {
    lateinit var skivvy: Skivvy
    lateinit var tts: TextToSpeech
    val actionServiceRestart = BuildConfig.APPLICATION_ID + "SKIVVY_SERVICE_DEAD"
    override fun onCreate() {
        super.onCreate()
        skivvy = this.application as Skivvy
        this.tts = TextToSpeech(this, TextToSpeech.OnInitListener {
            if (it == TextToSpeech.SUCCESS) {
                this.tts.language = Locale.getDefault()
            } else
                Toast.makeText(
                    applicationContext,
                    getString(R.string.output_error),
                    Toast.LENGTH_SHORT
                ).show()
        })
        this.registerReceiver(mNotificationReceiver, IntentFilter(skivvy.actionNotification))
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        speakOut("I'm still here!")
        return super.onStartCommand(intent, flags, startId)
    }
    override fun onBind(intent: Intent): IBinder {
        speakOut("I'm bind")
        TODO("Return the communication channel to the service.")
    }

    override fun onDestroy() {
        super.onDestroy()
        this.unregisterReceiver(mNotificationReceiver)
        speakOut("I'm dead")
        sendBroadcast(
            Intent(skivvy.actionServiceRestart)
                .putExtra(skivvy.serviceDead, true)
        )
    }

    var lastTimeNotif: String? = null
    var lastMsgNotif: String? = null
    var lastNotifFrom: String? = null
    var lastNotifKey: String? = null
    var lastOngoing = false
    var ongoingNotifKey: String? = null
    private val mNotificationReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            val key = intent.getStringExtra(skivvy.notificationID)
            val state = intent.getStringExtra(skivvy.notificationStatus)
            val from = intent.getStringExtra(skivvy.notificationAppName)
            val msg = intent.getStringExtra(skivvy.notificationTicker)?.replace("—", "by")
            val time = intent.getStringExtra(skivvy.notificationTime)
            val ongoing = intent.getBooleanExtra(skivvy.notificationOngoing, false)
            if (state == skivvy.notificationRemoved) {
                if (key == lastNotifKey) {
                    if (lastOngoing) {
                        if (key == ongoingNotifKey) {
                            lastOngoing = false
                        }
                    }
                }
            } else {
                if (ongoing) {        //if notification  is sticky
                    if (lastOngoing) {        //if already has a sticky notification
                        if (key == lastNotifKey) {
                            msg?.let { speakOut(it) }         //if same app post different notification sticky, speak and replace previous one
                        } else
                            msg?.let { speakOut(it) }       //if other app post notification sticky, show on output
                    } else {
                        msg?.let { speakOut(it) }         //if no previous sticky notification, show as feedback
                    }
                    lastOngoing = true
                    ongoingNotifKey = key
                } else {        //if notification is removable
                    lastOngoing = if (lastOngoing) {
                        if (key == ongoingNotifKey) {
                            speakOut("")
                        } else {
                            msg?.let { speakOut(it) }
                        }
                        true
                    } else {
                        msg?.let { speakOut(it) }
                        false
                    }
                }
                lastNotifKey = key
                lastTimeNotif = time
                lastMsgNotif = msg
                lastNotifFrom = from
                lastOngoing = ongoing
            }
        }
    }


    private fun speakOut(message: String) {
        if (!skivvy.getMuteStatus())
            tts.speak(message, TextToSpeech.QUEUE_FLUSH, null, "")
    }
}