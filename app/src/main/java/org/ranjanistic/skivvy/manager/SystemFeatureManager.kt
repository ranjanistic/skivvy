package org.ranjanistic.skivvy.manager

import android.bluetooth.BluetoothAdapter
import android.content.Context
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
    fun wirelessFidelity(on: Boolean?, context: Context): Boolean? {
        val wifiManager: WifiManager =
            context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
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
}