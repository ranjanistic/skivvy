package org.ranjanistic.skivvy

import android.R.id.message
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat.startActivity
import java.util.*


@ExperimentalStdlibApi
class CallReceiver : PhoneCallReceiver(){
    var isIncomingCallReceived = false
    var isIncomingCallAnswered = false
    var isIncomingCallEnded = false
    var isCallMissed = false
    var isOutgoingCallStarted = false
    var isOutgoingCallEnded = false
    private var phoneNumber:String? = null
    private var startTime:String? = null
    private var endTime:String? = null

    fun getPhoneNumber():String{
        return this.phoneNumber!!
    }
    fun getStartTime():String {
        return this.startTime!!
    }
    fun getEndTime():String{
        return this.endTime!!
    }

    private val mainActivity:MainActivity = MainActivity()
    override fun onIncomingCallReceived(ctx: Context?, number: String?, start: Date?) {
        if(mainActivity.isCreated){
            mainActivity.setOutput("Incoming $number")
            mainActivity.successView(null)
            mainActivity.setInput(start.toString())
        }
    }
    override fun onIncomingCallAnswered(ctx: Context?, number: String?, start: Date?) {
        this.isIncomingCallAnswered = true
        this.phoneNumber = number
        this.startTime = start.toString()
    }

    override fun onIncomingCallEnded(ctx: Context?, number: String?, start: Date?, end: Date?) {
        this.isIncomingCallEnded = true
        this.phoneNumber = number
        this.startTime = start.toString()
        this.endTime = end.toString()
    }

    override fun onOutgoingCallStarted(ctx: Context?, number: String?, start: Date?) {
        this.isOutgoingCallStarted = true
        this.phoneNumber = number
        this.startTime = start.toString()
    }

    override fun onOutgoingCallEnded(ctx: Context?, number: String?, start: Date?, end: Date?) {
        if(mainActivity.isCreated){
            mainActivity.setOutput("Last called $number")
            mainActivity.successView(null)
            mainActivity.setInput(start.toString())
        }
        this.isOutgoingCallEnded = true
        this.phoneNumber = number
        this.startTime = start.toString()
        this.endTime = end.toString()
    }

    override fun onMissedCall(ctx: Context?, number: String?, start: Date?) {
        this.isCallMissed = true
        this.phoneNumber = number
        this.startTime = start.toString()
    }
}