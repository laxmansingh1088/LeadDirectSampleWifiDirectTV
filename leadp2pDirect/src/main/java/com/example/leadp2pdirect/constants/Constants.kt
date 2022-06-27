package com.example.leadp2pdirect.constants

import android.content.IntentFilter
import android.net.wifi.p2p.WifiP2pManager

object Constants {
     const val FILE_TRANSFER_PORT = 9999
     val p2pIntentFilter = IntentFilter().apply {
        addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)
        addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)
        addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)
        addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)
    }
}