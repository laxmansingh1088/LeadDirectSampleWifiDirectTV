package com.example.leadp2p.p2p

import android.net.wifi.p2p.WifiP2pDevice
import android.util.Log

data class PeerDevice(val deviceName: String,
                      val deviceId: String,
                      val status: Int,
                      val p2pdevice: WifiP2pDevice) {
    
    fun statusToString(): String? {
        return if (status == WifiP2pDevice.AVAILABLE) {
            "Available"
        } else if (status == WifiP2pDevice.INVITED) {
            "Invited"
        } else if (status == WifiP2pDevice.CONNECTED) {
            "Connected"
        } else if (status == WifiP2pDevice.FAILED) {
            "Failed"
        } else if (status == WifiP2pDevice.UNAVAILABLE) {
            "Unavailable"
        } else {
            "Unknown"
        }
    }
}
