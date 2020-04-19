package org.ranjanistic.skivvy

import android.R.id.message
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import java.util.*


abstract class PhoneCallReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent) {
        //We listen to two intents.  The new outgoing call only tells us of an outgoing call.  We use it to get the number.
        if (intent.action == "android.intent.action.NEW_OUTGOING_CALL") {
            savedNumber = intent.extras!!.getString("android.intent.extra.PHONE_NUMBER")
        } else {
            val stateStr = intent.extras!!.getString(TelephonyManager.EXTRA_STATE)
            val number = intent.extras!!.getString(TelephonyManager.EXTRA_INCOMING_NUMBER)
            var state = 0
            when (stateStr) {
                TelephonyManager.EXTRA_STATE_IDLE -> {
                    state = TelephonyManager.CALL_STATE_IDLE
                }
                TelephonyManager.EXTRA_STATE_OFFHOOK -> {
                    state = TelephonyManager.CALL_STATE_OFFHOOK
                }
                TelephonyManager.EXTRA_STATE_RINGING -> {
                    state = TelephonyManager.CALL_STATE_RINGING
                }
            }
            onCallStateChanged(context, state, number)
        }
    }

    //Derived classes should override these to respond to specific events of interest
    protected abstract fun onIncomingCallReceived(ctx: Context?, number: String?, start: Date?)
    protected abstract fun onIncomingCallAnswered(ctx: Context?, number: String?, start: Date?)
    protected abstract fun onIncomingCallEnded(ctx: Context?, number: String?, start: Date?, end: Date?)
    protected abstract fun onOutgoingCallStarted(ctx: Context?, number: String?, start: Date?)
    protected abstract fun onOutgoingCallEnded(ctx: Context?, number: String?, start: Date?, end: Date?)
    protected abstract fun onMissedCall(ctx: Context?, number: String?, start: Date?)

    //Deals with actual events
    //Incoming call-  goes from IDLE to RINGING when it rings, to OFFHOOK when it's answered, to IDLE when its hung up
    //Outgoing call-  goes from IDLE to OFFHOOK when it dials out, to IDLE when hung up
    private fun onCallStateChanged(context: Context?, state: Int, number: String?) {
        if (lastState == state) {return }
        when (state) {
            TelephonyManager.CALL_STATE_RINGING -> {
                isIncoming = true
                callStartTime = Date()
                savedNumber = number
                onIncomingCallReceived(context, number, callStartTime)
            }
            TelephonyManager.CALL_STATE_OFFHOOK -> {
                if (lastState != TelephonyManager.CALL_STATE_RINGING) {
                    isIncoming = false
                    callStartTime = Date()
                    onOutgoingCallStarted(context, savedNumber, callStartTime)
                } else {
                    isIncoming = true
                    callStartTime = Date()
                    onIncomingCallAnswered(context, savedNumber, callStartTime)
                }
            }
            TelephonyManager.CALL_STATE_IDLE ->                 //Went to idle-  this is the end of a call.  What type depends on previous state(s)
                when {
                    lastState == TelephonyManager.CALL_STATE_RINGING -> {
                        //Ring but no pickup-  a miss
                        onMissedCall(context, savedNumber, callStartTime)
                    }
                    isIncoming -> {
                        onIncomingCallEnded(context, savedNumber, callStartTime, Date())
                    }
                    else -> {
                        onOutgoingCallEnded(context, savedNumber, callStartTime, Date())
                    }
                }
        }
        lastState = state
    }

    companion object {
        //The receiver will be recreated whenever android feels like it.
        // We need a static variable to remember data between instantiations
        // because the passed incoming is only valid in ringing
        private var lastState = TelephonyManager.CALL_STATE_IDLE
        private var callStartTime: Date? = null
        private var isIncoming = false
        private var savedNumber: String? = null
    }
}