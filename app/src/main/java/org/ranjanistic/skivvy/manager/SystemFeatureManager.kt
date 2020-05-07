package org.ranjanistic.skivvy.manager

import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.media.AudioManager
import android.net.wifi.WifiManager

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

    //set media volume
    fun setMediaVolume(percent: Float, audioManager:AudioManager) {
        audioManager.setStreamVolume(
            AudioManager.STREAM_MUSIC,
            (audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) * (percent / 100)).toInt(),
            AudioManager.FLAG_SHOW_UI
        )
    }

    //TODO: brightness function
}