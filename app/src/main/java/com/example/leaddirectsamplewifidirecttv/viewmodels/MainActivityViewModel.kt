package com.example.leaddirectsamplewifidirecttv.viewmodels

import android.net.wifi.p2p.WifiP2pDevice
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModel
import com.example.leaddirectsamplewifidirecttv.R
import com.example.leaddirectsamplewifidirecttv.ui.fragments.MainFragment
import com.example.leadp2p.p2p.LeadP2PHandler
import com.example.leadp2p.p2p.PeerDevice

class MainActivityViewModel : ViewModel() {

    var leadp2pHander: LeadP2PHandler? = null
    var currentFragment: Fragment? = null
    var peersList = ArrayList<PeerDevice>()
    var selectedDevice: PeerDevice? = null
    var deviceInfoForQrCode: String? = null


    fun openFragment(fragment: Fragment, supportFragmentManager: FragmentManager) {
        currentFragment = fragment
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container_view, fragment)
            .commitNow()
    }

    fun showConnectedDeviceInfo() {
        if (selectedDevice != null && selectedDevice?.p2pdevice?.status == WifiP2pDevice.CONNECTED) {
            if (currentFragment != null && currentFragment is MainFragment) {
                (currentFragment as MainFragment).manageUIWhenP2PIsConnected(
                    true,
                    selectedDevice?.deviceName
                )
            } else {
                (currentFragment as MainFragment).manageUIWhenP2PIsConnected(false, null)
            }
        } else {
            leadp2pHander?.getWifiP2pManager()?.requestConnectionInfo(leadp2pHander?.getChannel()) {
                if (currentFragment != null && it.groupFormed == false) {
                    (currentFragment as MainFragment).manageUIWhenP2PIsConnected(false, null)
                }
            }
        }
    }

}