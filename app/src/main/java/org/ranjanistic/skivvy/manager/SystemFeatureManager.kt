package org.ranjanistic.skivvy.manager

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.content.ContentResolver
import android.content.Intent
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.net.wifi.WifiManager
import android.os.Build
import android.provider.Settings
import android.provider.Settings.SettingNotFoundException
import android.telecom.TelecomManager
import android.telephony.TelephonyManager
import android.view.Window
import android.view.WindowManager
import org.ranjanistic.skivvy.Skivvy


class SystemFeatureManager(val skivvy: Skivvy) {
    //bluetooth toggle function
    fun bluetooth(on: Boolean?): Boolean? {
        val mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (on == null)
            return isBluetoothOn()
        if (on) {
            if (!isBluetoothOn())
                return mBluetoothAdapter.enable()
        } else {
            if (isBluetoothOn())
                return !mBluetoothAdapter.disable()
        }
        return null
    }
    fun isBluetoothOn():Boolean{
        return BluetoothAdapter.getDefaultAdapter().isEnabled
    }

    //wifi toggle function
    fun wirelessFidelity(on: Boolean?, wifiManager: WifiManager): Boolean? {
        if (on == null)
            return wifiManager.isWifiEnabled
        if (on) {
            if (!wifiManager.isWifiEnabled) {
                wifiManager.isWifiEnabled = true
                return true
            }
        } else {
            if (wifiManager.isWifiEnabled) {
                wifiManager.isWifiEnabled = false
                return false
            }
        }
        return null
    }

    //set media volume
    fun getMediaVolume(audioManager: AudioManager):Int{
        return (audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) * 100)/
                audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
    }
    //set media volume
    fun setMediaVolume(percent: Float, audioManager: AudioManager, showUI:Boolean = true) {
        audioManager.setStreamVolume(
            AudioManager.STREAM_MUSIC,
            (audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) * (percent / 100)).toInt(),
            if(showUI)AudioManager.FLAG_SHOW_UI
            else 0
        )
    }

    //get ringing volume
    fun getRingerVolume(audioManager: AudioManager): Int {
        return (audioManager.getStreamVolume(AudioManager.STREAM_RING) * 100) /
                audioManager.getStreamMaxVolume(AudioManager.STREAM_RING)
    }

    //set ringing volume
    fun setRingerVolume(percent: Float, audioManager: AudioManager, showUI: Boolean = true) {
        audioManager.setStreamVolume(
            AudioManager.STREAM_RING,
            (audioManager.getStreamMaxVolume(AudioManager.STREAM_RING) * (percent / 100)).toInt(),
            if (showUI) AudioManager.FLAG_SHOW_UI
            else 0
        )
    }

    //silent ringer
    fun silentRinger(audioManager: AudioManager, silent: Boolean) {
        if (audioManager.ringerMode == AudioManager.RINGER_MODE_NORMAL) {
            audioManager.ringerMode =
                if (silent) AudioManager.RINGER_MODE_SILENT
                else AudioManager.RINGER_MODE_NORMAL
        } else {
            audioManager.ringerMode =
                if (silent) AudioManager.RINGER_MODE_SILENT
                else AudioManager.RINGER_MODE_NORMAL
        }
    }

    fun setFlashLight(cameraManager: CameraManager, status: Boolean):Boolean{
        try {
            val mCameraId = cameraManager.cameraIdList[0]
            cameraManager.setTorchMode(mCameraId, status)
        } catch (e: CameraAccessException) {
            return false
        }
        return true
    }

    @SuppressLint("MissingPermission")
    fun respondToCall(
        telephonyManager: TelephonyManager?,
        telecomManager: TelecomManager?,
        pickUp: Boolean
    ): Boolean {
        try {
            telephonyManager?.javaClass?.getMethod(
                when (pickUp) {
                    true -> "answerRingingCall"
                    false -> "endCall"
                }
            )?.invoke(telephonyManager)
        } catch (e: Exception) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                try {
                    telecomManager?.let {
                        if (pickUp) it.acceptRingingCall()
                    }
                } catch (e: Exception) {
                    return false
                }
            } else return false
        }
        return true
    }

    fun getSystemBrightness(contentResolver: ContentResolver):Int?{
        return try {
            // To handle the auto
            Settings.System.putInt(
                contentResolver,
                Settings.System.SCREEN_BRIGHTNESS_MODE,
                Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL
            )
            //Get the current system brightness
            Settings.System.getInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS)
        } catch (e: SettingNotFoundException) {
            null
        }
    }

    //TODO: brightness function
    fun setBrightness(brightness:Int, window: Window, contentResolver: ContentResolver) {
        //Set the system brightness using the brightness variable value
        Settings.System.putInt(
            contentResolver,
            Settings.System.SCREEN_BRIGHTNESS,
            brightness
        )
        //Get the current window attributes
        //Get the current window attributes
        val layout: WindowManager.LayoutParams = window.attributes
        //Set the brightness of this window
        //Set the brightness of this window
        layout.screenBrightness = brightness / 255.toFloat()
        //Apply attribute changes to this window
        //Apply attribute changes to this window
        window.attributes = layout
    }

    fun isAirplaneModeEnabled(): Boolean {
        return Settings.System.getInt(
            skivvy.cResolver,
            Settings.System.AIRPLANE_MODE_ON,
            0
        ) == 1
    }

    //TODO: Airplane mode not turning on
    fun setAirplaneMode(status: Boolean) {
        Settings.System.putInt(
            skivvy.cResolver, Settings.System.AIRPLANE_MODE_ON,
            if (status) 1
            else 0
        )
        try {
            skivvy.sendBroadcast(
                Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED).putExtra(
                    "state", status
                )
            )
        } catch (e: SecurityException) {
        }
    }


    //TODO: GPS location for device security
}