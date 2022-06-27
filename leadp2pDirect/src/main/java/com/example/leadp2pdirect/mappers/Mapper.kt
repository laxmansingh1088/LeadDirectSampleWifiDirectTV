package com.example.leadp2pdirect.mappers

import android.net.wifi.p2p.WifiP2pDeviceList
import com.example.leadp2p.p2p.PeerDevice

object Mapper {

    fun mapWifiP2PDeviceListToPeerDeviceList(p2pDeviceList: WifiP2pDeviceList?): ArrayList<PeerDevice>? {
        if (p2pDeviceList != null) {
            var list = ArrayList<PeerDevice>()
            p2pDeviceList?.deviceList?.forEach { element ->
                val peerDevice =
                    PeerDevice(element.deviceName, element.deviceAddress, element.status, element)
                list.add(peerDevice)
            }
            return list
        }
        return null

    }
}