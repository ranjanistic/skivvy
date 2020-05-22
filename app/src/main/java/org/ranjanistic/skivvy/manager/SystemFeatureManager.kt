package org.ranjanistic.skivvy.manager

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.content.ContentResolver
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.net.wifi.WifiManager
import android.os.Build
import android.provider.Settings
import android.provider.Settings.SettingNotFoundException
import android.telecom.TelecomManager
import android.telephony.TelephonyManager
import android.util.Log
import android.view.Window
import android.view.WindowManager
import androidx.core.app.ActivityCompat


class SystemFeatureManager {
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
        val mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        return mBluetoothAdapter.isEnabled
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
    fun phoneCall(telephonyManager: TelephonyManager?, telecomManager: TelecomManager?, pickUp:Boolean):Boolean{
        when(pickUp){
            true->{     //means answer call
                try { //reflection
                    telephonyManager?.javaClass?.getMethod("answerRingingCall")?.invoke(telephonyManager)
                } catch (e: Exception) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        try {
                            telecomManager?.let {
                                it.acceptRingingCall()
                                it.showInCallScreen(false)
                            }
                        } catch (e:Exception){
                            return false
                        }
                    } else return false
                }
            }
            false->{        //means abort call
            }
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
    //TODO: GPS location for device security
}