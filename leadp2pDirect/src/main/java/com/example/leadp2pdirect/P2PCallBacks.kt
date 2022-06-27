package com.example.leadp2pdirect

import android.net.wifi.p2p.WifiP2pDevice
import com.example.leadp2p.p2p.PeerDevice
import com.example.leadp2pdirect.chatmessages.ChatCommands
import com.example.leadp2pdirect.p2p.MyDeviceInfoForQrCode
import com.example.leadp2pdirect.servers.FileDownloadUploadProgresssModel
import com.example.leadp2pdirect.servers.FileModel

interface P2PCallBacks {
    fun onProgressUpdate(fileDownloadProgresssModel: FileDownloadUploadProgresssModel)
    fun onFilesReceived(filePathsList: ArrayList<FileModel>)
    fun onConnected(peerDevice: PeerDevice?)
    fun onDisconnected()
    fun updatePeersList(listPeerDevice: ArrayList<PeerDevice>?)
    fun timeTakenByFileToSend(message: String)
    fun sendLogBackToSender(logMesaage: String)
    fun myDeviceInfo(deviceInfoForQrCode: String)
    fun onReceiveChatCommands(chatCommands: ChatCommands)
}