package com.example.leadp2p.p2p

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pManager
import android.util.Log

enum class P2PState {
    P2P_ENABLED,
    P2P_NOT_ENABLED,
    CONNECTION_CHANGED,
    PEERS_CHANGED,
    THIS_DEVICE_CHANGE,
    NOT_VALID_ACTION
}

class P2PBroadcastReceiver(private val p2pCallback: (Intent, P2PState) -> Unit) :
    BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.i("P2PBroadcastReceiver", "Broadcast received  -> "+intent.action)
        intent.action?.let {
            when (it) {
                WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION -> {
                    // Check to see if Wi-Fi is enabled and notify appropriate activity
                    when (intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1)) {
                        WifiP2pManager.WIFI_P2P_STATE_ENABLED -> {
                            Log.i("P2PBroadcastReceiver", "Wifi P2P is enabled")
                            p2pCallback.invoke(intent, P2PState.P2P_ENABLED)
                        }
                        else -> {
                            Log.i("P2PBroadcastReceiver", "Wifi P2P is not enabled")
                            p2pCallback.invoke(intent, P2PState.P2P_NOT_ENABLED)
                        }
                    }
                }
                WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION -> {
                    Log.i("P2PBroadcastReceiver", "WIFI_P2P_CONNECTION_CHANGED_ACTION")
                    p2pCallback.invoke(intent, P2PState.CONNECTION_CHANGED)
                }
                WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION -> {
                    Log.i("P2PBroadcastReceiver", "WIFI_P2P_PEERS_CHANGED_ACTION")
                    p2pCallback.invoke(intent, P2PState.PEERS_CHANGED)
                }
                WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION -> {
                    Log.i("P2PBroadcastReceiver", "WIFI_P2P_THIS_DEVICE_CHANGED_ACTION")
                   val devicee : WifiP2pDevice? = intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE)
                    p2pCallback.invoke(intent, P2PState.THIS_DEVICE_CHANGE)
                }
                else -> {
                    Log.i("P2PBroadcastReceiver", "Not a valid action  --  "+intent.action)
                    p2pCallback.invoke(intent, P2PState.NOT_VALID_ACTION)
                }
            }
        }
    }
}
