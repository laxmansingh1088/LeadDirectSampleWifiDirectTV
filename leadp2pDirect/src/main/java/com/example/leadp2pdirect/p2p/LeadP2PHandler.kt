package com.example.leadp2p.p2p

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.NetworkInfo
import android.net.Uri
import android.net.wifi.WifiManager
import android.net.wifi.WpsInfo
import android.net.wifi.p2p.*
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.checkSelfPermission
import com.example.leadp2pdirect.P2PCallBacks
import com.example.leadp2pdirect.chatmessages.ChatCommands
import com.example.leadp2pdirect.chatttthreadds.ChatClient
import com.example.leadp2pdirect.chatttthreadds.ChatServer
import com.example.leadp2pdirect.constants.Constants
import com.example.leadp2pdirect.mappers.Mapper
import com.example.leadp2pdirect.p2p.MyDeviceInfoForQrCode
import com.example.leadp2pdirect.servers.FileServerAsyncTask
import com.example.leadp2pdirect.servers.LeadP2PHandlerCallbacks
import com.example.leadp2pdirect.textmessage.*
import com.example.leadp2pdirect.threadss.ClientFileTransfer
import com.example.leadp2pdirect.threadss.ServerFileTransfer
import com.example.leadp2pdirect.utils.Utils
import com.google.gson.Gson
import java.net.InetAddress
import java.net.ServerSocket


//https://stackoverflow.com/questions/21722767/how-do-i-get-a-peer-to-peer-wifi-service-discovery-to-work

@SuppressLint("NewApi")
class LeadP2PHandler(
    private val activity: Activity,
    private val p2PCallBacks: P2PCallBacks,
    private val becomeGroupOwnerIntent: Boolean
) : LeadP2PHandlerCallbacks {
    private var manager: WifiP2pManager? = null
    private var channel: WifiP2pManager.Channel? = null
    private var receiver: P2PBroadcastReceiver? = null
    private val wifiManager: WifiManager? by lazy {
        activity.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    }

    private val TAG = "LeadP2PHandler.kt"
    private var wifiP2pGroup: WifiP2pGroup? = null
    private var prevP2pInfo: WifiP2pInfo? = null
    private var isServer = false

    private var serverAddress: InetAddress? = null
    private var serverSocket: ServerSocket? = null
    private var chatserverSocket: ServerSocket? = null
    private var fileServerAsyncTask: FileServerAsyncTask? = null

    private var client: ClientInfo? = null

    private var serverFileTransfer: ServerFileTransfer? = null
    private var clientFileTransfer: ClientFileTransfer? = null
    private var chatServer: ChatServer? = null
    private var chatClient: ChatClient? = null


    init {
        manager = activity.getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager
        channel = manager?.initialize(activity, activity.mainLooper, null)
        channel.also {
            receiver = P2PBroadcastReceiver(this::onP2PStateReceiver)
        }
       // manager?.removeGroup(channel,null)
          //createGroupOwnerr()
        discoverPeers()
    }


    fun getWifiP2pManager(): WifiP2pManager? {
        return manager
    }

    fun getChannel(): WifiP2pManager.Channel? {
        return channel
    }

    fun createGroupOwnerr() {
        if (becomeGroupOwnerIntent) {
            if (checkSelfPermission(
                    activity,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            manager?.createGroup(channel, object : WifiP2pManager.ActionListener {
                override fun onSuccess() {
                    Toast.makeText(activity, "CreateGroup Successful", Toast.LENGTH_SHORT).show()
                }

                override fun onFailure(p0: Int) {
                    Toast.makeText(activity, "CreateGroup Fail", Toast.LENGTH_SHORT).show()
                }

            })
        }
    }

    private fun discoverPeers() {
        val discoveryRunnable: Runnable = object : Runnable {
            override fun run() {
                manager?.clearLocalServices(channel, object : WifiP2pManager.ActionListener {
                    override fun onSuccess() {
                        val record: HashMap<String, String> = HashMap()
                        record["name"] = "Amos"
                        val serviceInfo = WifiP2pDnsSdServiceInfo.newInstance(
                            "_test",
                            "_presence.tcp", record
                        )
                        if (checkSelfPermission(
                                activity,
                                Manifest.permission.ACCESS_FINE_LOCATION
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            return
                        }
                        manager?.addLocalService(channel, serviceInfo,
                            object : WifiP2pManager.ActionListener {
                                override fun onSuccess() {
                                    manager?.setDnsSdResponseListeners(
                                        channel,
                                        WifiP2pManager.DnsSdServiceResponseListener { s, s2, wifiP2pDevice ->
                                            Log.wtf(
                                                "whooooa",
                                                "device " + wifiP2pDevice.deviceName + "/" + wifiP2pDevice.deviceAddress
                                            );
                                        },
                                        WifiP2pManager.DnsSdTxtRecordListener { s, mutableMap, wifiP2pDevice ->
                                            Log.wtf("whooooa", "got txt " + mutableMap.get("name"));
                                        }
                                    )
                                    manager?.clearServiceRequests(
                                        channel,
                                        object : WifiP2pManager.ActionListener {
                                            override fun onSuccess() {
                                                manager?.addServiceRequest(
                                                    channel,
                                                    WifiP2pDnsSdServiceRequest.newInstance(),
                                                    object : WifiP2pManager.ActionListener {
                                                        override fun onSuccess() {
                                                            if (checkSelfPermission(
                                                                    activity,
                                                                    Manifest.permission.ACCESS_FINE_LOCATION
                                                                ) != PackageManager.PERMISSION_GRANTED
                                                            ) {
                                                                return
                                                            }
                                                            manager?.discoverPeers(
                                                                channel,
                                                                object :
                                                                    WifiP2pManager.ActionListener {
                                                                    override fun onSuccess() {
                                                                        if (checkSelfPermission(
                                                                                activity,
                                                                                Manifest.permission.ACCESS_FINE_LOCATION
                                                                            ) != PackageManager.PERMISSION_GRANTED
                                                                        ) {
                                                                            return
                                                                        }
                                                                        manager?.discoverServices(
                                                                            channel,
                                                                            object :
                                                                                WifiP2pManager.ActionListener {
                                                                                override fun onSuccess() {
                                                                                    // this is my recursive discovery approach
                                                                                }

                                                                                override fun onFailure(
                                                                                    code: Int
                                                                                ) {
                                                                                }
                                                                            })
                                                                    }

                                                                    override fun onFailure(code: Int) {}
                                                                })
                                                        }

                                                        override fun onFailure(code: Int) {}
                                                    })
                                            }

                                            override fun onFailure(code: Int) {}
                                        })
                                }

                                override fun onFailure(code: Int) {}
                            })
                    }

                    override fun onFailure(code: Int) {}
                })

            }
        }
        discoveryRunnable.run()

/*

        val mServiceBroadcastingRunnable: Runnable = object : Runnable {
            override fun run() {
                if (ActivityCompat.checkSelfPermission(
                        activity,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    )
                    != PackageManager.PERMISSION_GRANTED
                ) {
                    return
                }
                manager?.discoverPeers(
                    channel,
                    object : WifiP2pManager.ActionListener {
                        override fun onSuccess() {
                            Log.i("WIFI Discover", "Peers discovery successfully")
                            Utils.showToast(activity, "Peers discovery successfully")
                        }

                        override fun onFailure(error: Int) {
                            Log.i("WIFI Discover", "Peers discovery failed $error")
                            Utils.showToast(activity, "Peers discovery failed  Code :-  $error")
                        }
                    })
                manager?.requestConnectionInfo(channel) {
                    if (!it.groupFormed) {
                        Handler(Looper.getMainLooper()).postDelayed(this, 10000)
                    } else {
                        stopDiscoveringPeers()
                    }
                }
            }
        }
        mServiceBroadcastingRunnable.run()

*/

        /*  if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)
              != PackageManager.PERMISSION_GRANTED
          ) {
              return
          }
          manager?.discoverPeers(channel, object : WifiP2pManager.ActionListener {
              override fun onSuccess() {
                  Log.i("WIFI Discover", "Peers discovery successfully")
                  Utils.showToast(activity, "Peers discovery successfully")
                  if (!isConnected()) {
                      Handler().postDelayed(Runnable { startPeersDiscovery() },6000)
                  } else {
                      manager?.stopPeerDiscovery(channel, null)
                  }
              }

              override fun onFailure(p0: Int) {
                  if (!isConnected()) {
                      Handler().postDelayed(Runnable { startPeersDiscovery() },6000)
                  } else {
                      manager?.stopPeerDiscovery(channel, null)
                  }
                  Log.i("WIFI Discover", "Peers discovery failed $p0")
                  Utils.showToast(activity, "Peers discovery failed  Code :-  $p0")
              }
          })*/
    }

    fun startPeersDiscovery() {
        discoverPeers()
    }

    private fun onP2PStateReceiver(intent: Intent, state: P2PState) {
        Log.i("LeadP2PHandler", "state -->$state")
        when (state) {
            P2PState.P2P_ENABLED -> {
                Log.i("LeadP2PHandler", "LeadP2PHandler")
                Toast.makeText(this.activity, "Wifi is enabled", Toast.LENGTH_SHORT).show()
            }

            P2PState.P2P_NOT_ENABLED -> {
                Log.i("LeadP2PHandler", "P2P_NOT_ENABLED")
                Toast.makeText(this.activity, "Wifi is NOT enabled", Toast.LENGTH_SHORT).show()
                turnWifiOnOff()
            }

            P2PState.CONNECTION_CHANGED -> {
                Log.i("LeadP2PHandler", "CONNECTION_CHANGED")
                this.connectionChanged(intent)
            }

            P2PState.PEERS_CHANGED -> {
                Log.i("LeadP2PHandler", "PEERS_CHANGED")
                Toast.makeText(this.activity, "Peers Changed", Toast.LENGTH_SHORT).show()

                if (ActivityCompat.checkSelfPermission(
                        activity,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    )
                    != PackageManager.PERMISSION_GRANTED
                ) {
                    return
                }
                manager?.requestPeers(channel) { peers: WifiP2pDeviceList? ->
                    if (peers != null) {
                        Log.i("LeadP2PHandler", "Peers discovered successfully ${peers.toString()}")
                        p2PCallBacks.updatePeersList(
                            Mapper.mapWifiP2PDeviceListToPeerDeviceList(
                                peers
                            )
                        )
                    } else {
                        Log.i("LeadP2PHandler", "No Peers list is available")
                    }
                }
            }


        /*    {"deviceId":"7a:d6:dc:40:e6:92",
                "deviceName":"Lenovo Tab M7 lakshya",
                "p2pdevice":{"deviceAddress":"7a:d6:dc:40:e6:92",
                    "deviceCapability":37,
                    "deviceName":"Lenovo Tab M7 lakshya",
                    "groupCapability":0,
                    "primaryDeviceType":"10-0050F204-5",
                    "status":3,
                    "wfdInfo":{},
                    "wpsConfigMethodsSupported":392},
                "status":3}*/

            P2PState.THIS_DEVICE_CHANGE -> {
                val myDevice =
                    intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE) as WifiP2pDevice?
                val deviceName = myDevice?.deviceName
                if (deviceName != null && myDevice.status == 3) {
                    Log.d("My_Device", deviceName)
                    val myDeviceInfoForQrCode = MyDeviceInfoForQrCode(
                        myDevice.deviceName,
                        myDevice.deviceAddress,
                        myDevice.primaryDeviceType,
                        myDevice.secondaryDeviceType,
                        myDevice.status
                    )


                    val info = Gson().toJson(myDeviceInfoForQrCode)
                    p2PCallBacks.myDeviceInfo(info)
                    Log.d(TAG, "2PCallBacks.myDeviceInfo(info) --> line 214")
                }
                Log.i("LeadP2PHandler", "THIS_DEVICE_CHANGE")
            }

            P2PState.NOT_VALID_ACTION -> {
                Log.i("LeadP2PHandler", "NOT_VALID_ACTION")
            }
        }
    }


    fun connect(device: PeerDevice?) {
        if (device!!.p2pdevice.status == WifiP2pDevice.INVITED) {
            channel?.also { channel ->
                manager?.removeGroup(channel, object : WifiP2pManager.ActionListener {
                    override fun onSuccess() {
                        Log.d("WIFI Discover", "removeGroup onSuccess -")
                    }

                    override fun onFailure(reason: Int) {
                        Log.d("WIFI Discover", "removeGroup onFailure -$reason")
                    }
                })
            }
            return
        }

        if (device!!.p2pdevice.status == WifiP2pDevice.CONNECTED) {
            this.reconnect()
            return
        }

        val config = WifiP2pConfig().apply {
            if (device != null) {
                deviceAddress = device.p2pdevice.deviceAddress
            }
            wps.setup = WpsInfo.PBC
            if (becomeGroupOwnerIntent) {
                groupOwnerIntent = WifiP2pConfig.GROUP_OWNER_INTENT_MAX
            } else {
                groupOwnerIntent = WifiP2pConfig.GROUP_OWNER_INTENT_MIN
            }

        }

        if (device != null) {
            config.deviceAddress = device.p2pdevice.deviceAddress
        }
        channel?.also { channel ->
            if (ActivityCompat.checkSelfPermission(
                    activity,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
                != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            if (becomeGroupOwnerIntent) {
                manager?.createGroup(channel, object : WifiP2pManager.ActionListener {
                    override fun onSuccess() {
                        Toast.makeText(activity, "CreateGroup Successful", Toast.LENGTH_SHORT)
                            .show()
                    }

                    override fun onFailure(p0: Int) {
                        Toast.makeText(activity, "CreateGroup Fail", Toast.LENGTH_SHORT).show()
                    }

                })
            } else {
                manager?.connect(channel, config, object : WifiP2pManager.ActionListener {
                    override fun onSuccess() {
                        //success logic
                        Log.i("WIFI Discover", "channel connect successfully")
                    }

                    override fun onFailure(reason: Int) {
                        Log.i("WIFI Discover", "channel connect failed $reason")
                    }
                })
            }
        }
    }

    public fun isServer() = isServer

    private fun initChatClient() {
        isConnected()
        if (chatClient == null) {
            Log.d(TAG, "initChatClient()")
            chatClient =
                ChatClient(activity, serverAddress, p2PCallBacks, this.MessageReceiver, this)
            chatClient?.priority = Thread.MAX_PRIORITY
            chatClient?.start()
        }
    }


    private fun initChatSockets() {
        try {
            if (chatserverSocket == null) {
                chatserverSocket = ServerSocket(Constants.CHAT_PORT)
                Log.d(TAG, "chatserverSocket==null")
                Log.d(
                    TAG,
                    "initChatSockets---> if block -> +${chatserverSocket?.isClosed} && ${chatserverSocket?.isBound}"
                )
            } else {
                Log.d(
                    TAG,
                    "initChatSockets---> else block -> +${chatserverSocket?.isClosed} && ${chatserverSocket?.isBound}"
                )
            }
            Log.d(TAG, "chatserverSocket== assigned")
        } catch (e: Exception) {
            Log.d(TAG, "chatserverSocket== Exception--> " + e.message)
            e.printStackTrace()
        }
    }


    private fun initChatServer() {
        if (chatServer != null) {
            chatServer = null
        }
        Log.d(
            TAG,
            "initChatServer---> +${chatserverSocket?.isClosed} && ${chatserverSocket?.isBound}"
        )
        chatServer =
            ChatServer(activity, chatserverSocket, p2PCallBacks, this.MessageReceiver, this)
        chatServer?.priority = Thread.MAX_PRIORITY
        chatServer?.start()

    }

    private fun initChatSocketConnection() {
        Log.d("serverssss","chatttt server")
        initChatSockets()
        initChatServer()
    }


    private fun initSocketConnection() {
        Log.d("serverssss","file server")
        //init sockets for transport servers and callbacks for reinit servers
        initSockets()
        initFileServer()
    }

    private val MessageReceiver: ReceiveCallback = object : ReceiveCallback {
        override fun ReceiveMessage(message: BaseMessage) {
            if (message is JoinMessage) {
                val client: ClientInfo = (message as JoinMessage).getClient()
                this@LeadP2PHandler.client = client
            } else if (message is LeaveMessage) {
                val client: ClientInfo = (message as LeaveMessage).getClient()
                this@LeadP2PHandler.client = null
            }
            val s = message.serialize()
            this@LeadP2PHandler.activity.runOnUiThread {
                //code that runs in main
                val chatMessage = ChatMessage.Deserialize(s)
                Log.d(TAG, "Received Chat messag:-->  $s")
                Toast.makeText(this@LeadP2PHandler.activity, s, Toast.LENGTH_SHORT).show()
                val chatCommands = Gson().fromJson(chatMessage.content, ChatCommands::class.java)
                p2PCallBacks.onReceiveChatCommands(chatCommands)
            }
        }
    }

    private fun openSocket(p2pInfo: WifiP2pInfo) {
        Log.d("LeadP2PHandler", "Received info")
        //if (!areInfoEquals(this.prevP2pInfo, p2pInfo)) {
        Log.d("LeadP2PHandler", "New info")
        // stop possible previous connection
        if (p2pInfo != null && p2pInfo.groupFormed) {
            serverAddress = p2pInfo.groupOwnerAddress
            if (p2pInfo.isGroupOwner) {
                Log.d("LeadP2PHandler", "I'm Server")
                Utils.showToast(activity, "I am Server...")
                this.isServer = true
                initSocketConnection()
                initChatSocketConnection()
            } else {
                Log.d("LeadP2PHandler", "I'm Client")
                this.isServer = false
                val info = ClientInfo("deviceAddress", "user")
                initClient()
                initChatClient()
            }
        }
        // }
        this.prevP2pInfo = p2pInfo
    }

    private fun initClient() {
        isConnected()
        if (clientFileTransfer == null) {
            Log.d(TAG, "initClient()")
            clientFileTransfer = ClientFileTransfer(activity, serverAddress, p2PCallBacks, this)
            clientFileTransfer?.priority = Thread.MAX_PRIORITY
            clientFileTransfer?.start()
        }
    }

    private fun reconnect() {
        if (manager != null && channel != null) {
            manager?.requestConnectionInfo(
                channel
            ) { p0 ->
                Log.d("P2P", "requestConnectedInfo $p0")
                if (p0 != null) this@LeadP2PHandler.openSocket(p0)
            }
        }
    }


    private fun connectionChanged(intent: Intent) {
        val networkInfo: NetworkInfo? =
            intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO) as? NetworkInfo
        if (networkInfo != null && networkInfo.isConnected) {
            //Other device is connected
            val wifiP2pInfo: WifiP2pInfo? =
                intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_INFO) as? WifiP2pInfo
            if (wifiP2pInfo != null) {
                //Start connection
                openSocket(wifiP2pInfo);
            }
            var peerDevice: PeerDevice? = null

            val wifiP2pGroup: WifiP2pGroup? =
                intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_GROUP) as? WifiP2pGroup

            if (wifiP2pInfo?.groupFormed == true && wifiP2pInfo?.isGroupOwner == true) {
                var clientList: MutableList<WifiP2pDevice> = mutableListOf<WifiP2pDevice>()
                clientList.addAll(wifiP2pGroup?.clientList as Collection<WifiP2pDevice>)
                if (clientList != null && clientList.isNotEmpty()) {
                    val wifiP2pDevice = clientList[0]
                    peerDevice = PeerDevice(
                        wifiP2pDevice?.deviceName.toString(),
                        wifiP2pDevice?.deviceAddress.toString(),
                        wifiP2pDevice?.status!!, wifiP2pDevice
                    )
                    p2PCallBacks.onConnected(peerDevice)
                } else {
                    val wifiP2pDevice = wifiP2pGroup?.owner
                    val deviceName = wifiP2pDevice?.deviceName
                    val myDeviceInfoForQrCode = wifiP2pDevice?.let {
                        MyDeviceInfoForQrCode(
                            wifiP2pDevice?.deviceName,
                            wifiP2pDevice?.deviceAddress,
                            wifiP2pDevice?.primaryDeviceType,
                            wifiP2pDevice?.secondaryDeviceType,
                            it.status
                        )
                    }
                    val info = Gson().toJson(myDeviceInfoForQrCode)
                    p2PCallBacks.myDeviceInfo(info)
                    p2PCallBacks.onDisconnected()
                    Log.d(TAG, "2PCallBacks.myDeviceInfo(info) --> line 437")

                }
                Utils.showToast(activity, "Server Started.....")
            } else if (wifiP2pInfo?.groupFormed == true) {
                val wifiP2pDevice = WifiP2pDevice()
                wifiP2pDevice.deviceName = wifiP2pGroup?.owner?.deviceName.toString()
                wifiP2pDevice.deviceAddress = wifiP2pGroup?.owner?.deviceAddress.toString()
                wifiP2pDevice.status = wifiP2pGroup?.owner?.status!!
                wifiP2pDevice.primaryDeviceType = wifiP2pGroup?.owner!!.primaryDeviceType
                wifiP2pDevice.secondaryDeviceType = wifiP2pGroup?.owner!!.secondaryDeviceType
                peerDevice = PeerDevice(
                    wifiP2pGroup?.owner?.deviceName.toString(),
                    wifiP2pGroup?.owner?.deviceAddress.toString(),
                    wifiP2pGroup?.owner?.status!!, wifiP2pDevice
                )
                Utils.showToast(activity, "Client Started.....")
                p2PCallBacks.onConnected(peerDevice)
            }

            Log.i("LeadP2PHandler", "Connected to P2P network. Requesting connection info")
        } else {
            p2PCallBacks.onDisconnected()
            cleanUpServerSockets()
            Utils.showToast(activity, "COMMUNICATION_DISCONNECTED")
        }

        // Requests peer-to-peer group information
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        manager?.requestGroupInfo(
            channel
        ) { wifiP2pGroup ->
            if (wifiP2pGroup != null) {
                Log.i("LeadP2PHandler", "Group info available")
                this.wifiP2pGroup = wifiP2pGroup
                if (wifiP2pGroup.isGroupOwner) {
                    val clientList: MutableList<WifiP2pDevice> = mutableListOf<WifiP2pDevice>()
                    clientList.addAll(wifiP2pGroup?.clientList as Collection<WifiP2pDevice>)
                    if (clientList != null && clientList.isNotEmpty()) {
                        val wifiP2pDevice = clientList.get(0)
                        val peerDevice = PeerDevice(
                            wifiP2pDevice?.deviceName.toString(),
                            wifiP2pDevice?.deviceAddress.toString(),
                            wifiP2pDevice?.status!!, wifiP2pDevice
                        )
                        p2PCallBacks.onConnected(peerDevice)
                    }
                }
            }
        }
    }

    private fun areInfoEquals(info1: WifiP2pInfo?, info2: WifiP2pInfo?): Boolean {
        return if (info1 == null || info2 == null) {
            false
        } else {
            info1.groupFormed == info2.groupFormed && info1.isGroupOwner == info2.isGroupOwner && info1.groupOwnerAddress == info2.groupOwnerAddress
        }
    }

    fun isConnected(): Boolean {
        var isConnected = false
        manager?.requestConnectionInfo(channel) {
            isConnected = it.groupFormed
            Log.d(TAG, "Isconnected -> ${it.groupFormed}")
        }
        return isConnected
    }

    fun disconnect(device: PeerDevice?) {
        if (manager != null && channel != null) {
            if (ActivityCompat.checkSelfPermission(
                    activity,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
                != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            manager?.requestGroupInfo(channel) { group ->
                if (group != null && manager != null && channel != null) {
                    manager?.removeGroup(channel, object : WifiP2pManager.ActionListener {
                        override fun onSuccess() {
                            Log.d("WIFI Discover", "removeGroup onSuccess -")
                            cleanUpClient()
                            cleanUpChatClient()
                        }

                        override fun onFailure(reason: Int) {
                            Log.d("WIFI Discover", "removeGroup onFailure -$reason")
                        }
                    })
                }
            }
        }
    }


    fun sendMessage(text: String, device: PeerDevice?) {
        val message = ChatMessage(
            ClientInfo(device!!.deviceId, device!!.deviceName),
            text
        )
        if (isServer) {
            chatServer?.sendMessage(message)
        } else {
            chatClient?.sendMessage(message)
        }
    }

    fun sendLogs(text: String, device: PeerDevice?) {
        if (device == null) return
        val message = ChatMessage(
            ClientInfo(device!!.deviceName, device!!.deviceName),
            text
        )
        if (isServer) {
            chatServer?.sendMessage(message)
        } else {
            chatClient?.sendMessage(message)
        }
    }

    private fun initFileServer() {
        if (serverFileTransfer != null) {
            serverFileTransfer = null
        }
        Log.d(TAG, "initFileServer---> +${serverSocket?.isClosed} && ${serverSocket?.isBound}")
        serverFileTransfer = ServerFileTransfer(activity, serverSocket, p2PCallBacks, this)
        serverFileTransfer?.priority = Thread.MAX_PRIORITY
        serverFileTransfer?.start()
    }

    private fun initSockets() {
        try {
            if (serverSocket == null) {
                serverSocket = ServerSocket(Constants.FILE_TRANSFER_PORT)
                Log.d(TAG, "serverSocket==null")
                Log.d(
                    TAG,
                    "initSockets---> if block -> +${serverSocket?.isClosed} && ${serverSocket?.isBound}"
                )
            } else {
                Log.d(
                    TAG,
                    "initSockets---> else block -> +${serverSocket?.isClosed} && ${serverSocket?.isBound}"
                )
            }
            Log.d(TAG, "serverSocket== assigned")
        } catch (e: Exception) {
            Log.d(TAG, "serverSocket== Exception--> " + e.message)
            e.printStackTrace()
        }
    }


    private fun cleanUpServerSockets() {
        try {
            if (serverSocket != null) {
                serverSocket?.close()
                serverSocket = null
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun cleanUpClient() {
        if (clientFileTransfer != null) {
            Log.d(TAG, "cleanUpClient()")
            clientFileTransfer?.cleanUp()
            clientFileTransfer = null
        }
    }

    private fun cleanUpChatClient() {
        if (chatClient != null) {
            Log.d(TAG, "cleanUpChatClient()")
            chatClient?.cleanUp()
            chatClient = null
        }
    }

    fun cleanUp() {
        try {
            serverSocket?.close()
            chatserverSocket?.close()
            fileServerAsyncTask!!.cancel(true)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        manager?.stopPeerDiscovery(channel,null)
        manager = null
        channel = null
        receiver = null
        wifiP2pGroup = null
    }


    fun registerReceiver() {
        receiver?.also { receiver ->
            activity.registerReceiver(receiver, Constants.p2pIntentFilter)
        }
        discoverPeers()
    }


    fun unRegisterReceiver() {
        stopDiscoveringPeers()

        receiver?.also { receiver ->
            activity.unregisterReceiver(receiver)
        }
    }

    fun stopDiscoveringPeers() {
        manager?.stopPeerDiscovery(channel, null)
    }

    fun isWifiEnabled(): Boolean? {
        return wifiManager?.isWifiEnabled
    }


    fun turnWifiOnOff() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            if (isWifiEnabled() == true) {
                wifiManager?.setWifiEnabled(false)
            } else {
                wifiManager?.setWifiEnabled(true)
            }
        } else {
            val intent = Intent(Settings.ACTION_WIFI_SETTINGS)
            activity.startActivityForResult(intent, 112)
        }
    }

    fun transferFile(uris: ArrayList<Uri>) {
        if (isServer) {
            serverFileTransfer?.sendFiles(uris)
        } else {
            clientFileTransfer?.sendFiles(uris)
        }
    }

    fun checkServerFile() {
        if (serverFileTransfer != null) {
            val isalive = serverFileTransfer?.isAlive
            Log.d("aliveeee", "$isalive")
        }
    }


    override fun cleanAndRestartChatServer() {
        Log.d(TAG, "Chat SErver restarted....>>..>>")
        initChatSocketConnection()
    }

    override fun cleanAndRestartChatClient() {
        Log.d(TAG, "Chat Client restarted....>>..>>")
        if (chatClient != null) {
            chatClient = null
        }
        manager?.requestConnectionInfo(channel) {
            if (it.groupFormed) {
                initChatClient()
            }
        }
    }

    override fun cleanAndRestartServer() {
        Log.d(TAG, "SErver restarted....>>..>>")
        initSocketConnection()
    }

    override fun cleanAndRestartClient() {
        Log.d(TAG, "Client restarted....>>..>>")
        if (clientFileTransfer != null) {
            clientFileTransfer = null
        }
        manager?.requestConnectionInfo(channel) {
            if (it.groupFormed) {
                initClient()
            }
        }
    }
}