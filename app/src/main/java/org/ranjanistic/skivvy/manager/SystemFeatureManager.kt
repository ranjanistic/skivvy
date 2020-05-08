package org.ranjanistic.skivvy.manager

import android.bluetooth.BluetoothAdapter
import android.content.ContentResolver
import android.media.AudioManager
import android.net.wifi.WifiManager
import android.provider.Settings
import android.provider.Settings.SettingNotFoundException
import android.util.Log
import android.view.Window
import android.view.WindowManager

class SystemFeatureManager {
    //bluetooth toggle function
    fun bluetooth(on: Boolean?): Boolean? {
        val mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (on == null)
            return mBluetoothAdapter.isEnabled
        if (on) {
            if (!mBluetoothAdapter.isEnabled)
                return mBluetoothAdapter.enable()
        } else {
            if (mBluetoothAdapter.isEnabled)
                return !mBluetoothAdapter.disable()
        }
        return null
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
    fun setMediaVolume(percent: Float, audioManager: AudioManager) {
        audioManager.setStreamVolume(
            AudioManager.STREAM_MUSIC,
            (audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) * (percent / 100)).toInt(),
            AudioManager.FLAG_SHOW_UI
        )
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
            Log.e("Error", "Cannot access system brightness")
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