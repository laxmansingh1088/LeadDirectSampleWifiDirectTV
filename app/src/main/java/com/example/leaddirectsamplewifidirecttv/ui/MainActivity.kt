package com.example.leaddirectsamplewifidirecttv.ui


import android.content.Intent
import android.content.IntentSender.SendIntentException
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.example.leaddirectsamplewifidirecttv.BuildConfig
import com.example.leaddirectsamplewifidirecttv.FileManager
import com.example.leaddirectsamplewifidirecttv.Mapper
import com.example.leaddirectsamplewifidirecttv.R
import com.example.leaddirectsamplewifidirecttv.persistence.LeadResourcesRoomDatabase
import com.example.leaddirectsamplewifidirecttv.ui.fragments.ImageFragment
import com.example.leaddirectsamplewifidirecttv.ui.fragments.MainFragment
import com.example.leaddirectsamplewifidirecttv.ui.fragments.PDFFragment
import com.example.leaddirectsamplewifidirecttv.ui.fragments.VideoFragment
import com.example.leaddirectsamplewifidirecttv.viewmodels.MainActivityViewModel
import com.example.leadp2p.p2p.LeadP2PHandler
import com.example.leadp2p.p2p.PeerDevice
import com.example.leadp2pdirect.P2PCallBacks
import com.example.leadp2pdirect.chatmessages.ChatCommands
import com.example.leadp2pdirect.chatmessages.VideoCommands
import com.example.leadp2pdirect.chatmessages.constants.ChatType
import com.example.leadp2pdirect.chatmessages.enumss.TransferStatus
import com.example.leadp2pdirect.chatmessages.enumss.VideoPlayBacks
import com.example.leadp2pdirect.helpers.PermissionsHelper
import com.example.leadp2pdirect.servers.FileDownloadUploadProgresssModel
import com.example.leadp2pdirect.servers.FileModel
import com.example.leadp2pdirect.utils.Utils
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.*
import layout.WebViewFragment
import java.io.File


class MainActivity : FragmentActivity(R.layout.activity_main), P2PCallBacks {
    private val viewModel: MainActivityViewModel by viewModels()
    private var doubleBackToExitPressedOnce = false

    companion object {
        private const val PROVIDER_PATH = ".provider"
        private const val APP_INSTALL_PATH = "\"application/vnd.android.package-archive\""
    }


    fun tempfun(filename: String) {
        val chatCommands = ChatCommands(ChatType.VIDEO_CHAT_TYPE, TransferStatus.RECEIVED)
        val videoCommands = VideoCommands(
            playBacks = VideoPlayBacks.PLAY,
            videoFileName = filename
        )
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (PermissionsHelper.isReadStorageAndLocationPermissionGranted(this)) {
            initializeWifiDirectSdk()
        }

        if (savedInstanceState == null) {
            val mainFragment = MainFragment()
            viewModel.openFragment(mainFragment, supportFragmentManager)
        }
        requestPermissionInstallApk()
    }

    private fun initializeWifiDirectSdk() {
        viewModel.leadp2pHander = LeadP2PHandler(this, this, true)
        // leadp2pHander?.createGroupOwner()
    }


    private fun requestPermissionInstallApk() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!getPackageManager().canRequestPackageInstalls()) {
                startActivityForResult(
                    Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES)
                        .setData(Uri.parse(String.format("package:%s", getPackageName()))), 1
                );
            }
        }
    }

    /* register the broadcast receiver with the intent values to be matched */
    override fun onResume() {
        super.onResume()
        viewModel.leadp2pHander?.registerReceiver()
        viewModel.showConnectedDeviceInfo()
    }


    /* unregister the broadcast receiver */
    override fun onPause() {
        super.onPause()
        viewModel.leadp2pHander?.unRegisterReceiver()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.leadp2pHander?.unRegisterReceiver()
        viewModel.leadp2pHander?.cleanUp()
    }


    //............... Library method callbacks.............................

    override fun onProgressUpdate(fileDownloadProgresssModel: FileDownloadUploadProgresssModel) {
        runOnUiThread(Runnable {
            if (viewModel.currentFragment != null && viewModel.currentFragment is MainFragment) {
                val receivingFileProgess =
                    if (fileDownloadProgresssModel.progressPercentage == 100) ""
                    else "${fileDownloadProgresssModel.fileName}: ${fileDownloadProgresssModel.progressPercentage}%"
                (viewModel.currentFragment as MainFragment)?.updateReceivingFileText(
                    receivingFileProgess
                )
            }
        })
    }

    override fun onFilesReceived(filePathsList: ArrayList<FileModel>) {

        Utils.showToast(this, "File Received.....")
        Log.d("patthhhh", filePathsList.get(0).absoluteFilePath)
        val bundle = Bundle()
        bundle.putSerializable("filePathsList", filePathsList)
        val fragment: Fragment? = when (filePathsList[0].type) {
            FileModel.TYPE_VIDEO -> {
                val videoFragment = VideoFragment()
                videoFragment.arguments = bundle
                videoFragment
            }
            FileModel.TYPE_AUDIO -> {
                val videoFragment = VideoFragment()
                videoFragment.arguments = bundle
                videoFragment
            }
            FileModel.TYPE_PHOTO -> {
                val imageFragment = ImageFragment()
                imageFragment.arguments = bundle
                imageFragment
            }
            FileModel.TYPE_PDF -> {
                val pdfFragment = PDFFragment()
                pdfFragment.arguments = bundle
                pdfFragment
            }
            FileModel.TYPE_APPLICATION -> {
                installAPK(filePathsList[0].absoluteFilePath)
                null
            }
            FileModel.TYPE_ZIP -> {
                val zipFilePath = filePathsList[0].absoluteFilePath;
                val targetLocation =
                    filePathsList[0].absoluteFilePath.replace(
                        filePathsList[0].fileName,
                        ""
                    ) + "ir_res_folder"
                Log.d("targetLocation", targetLocation)
                var indexFilePath: String?
                runBlocking {
                    indexFilePath =
                        withContext(Dispatchers.IO) {
                            FileManager().unzip(
                                zipFilePath,
                                targetLocation
                            )
                        }
                }
                Log.d("Unzip", "-------->  " + indexFilePath)
                Toast.makeText(applicationContext, indexFilePath, Toast.LENGTH_LONG).show()
                val bundle = Bundle()
                bundle.putString("ir_res_path_index", indexFilePath)
                val webviewFragment = WebViewFragment()
                webviewFragment.arguments = bundle
                webviewFragment
            }
            else -> {
                null
            }
        }

        if (fragment != null) {
            viewModel.openFragment(fragment, supportFragmentManager)
        }

    }


    override fun onConnected(peerDevice: PeerDevice?) {
        viewModel.selectedDevice = peerDevice
        viewModel.showConnectedDeviceInfo()
        viewModel.selectedDevice?.deviceName?.let {
            Utils.showToast(
                this,
                "Connect to Device --  $it"
            )
        }
    }

    override fun onDisconnected() {
        val mainFragment = MainFragment()
        viewModel.openFragment(mainFragment, supportFragmentManager)
        viewModel.leadp2pHander?.startPeersDiscovery()
    }


    override fun updatePeersList(listPeerDevice: ArrayList<PeerDevice>?) {
        viewModel.peersList?.clear()
    }

    override fun timeTakenByFileToSend(message: String) {
    }

    override fun sendLogBackToSender(logMesaage: String) {
        viewModel.leadp2pHander?.sendLogs(logMesaage, viewModel.selectedDevice)
    }

    override fun myDeviceInfo(deviceInfoForQrCode: String) {
        if (!deviceInfoForQrCode.isNullOrEmpty()) {
            viewModel.deviceInfoForQrCode = deviceInfoForQrCode
        }
        if (viewModel.selectedDevice == null) {
            //  leadp2pHander?.startDiscovery()
            if (deviceInfoForQrCode != null && viewModel.currentFragment is MainFragment) {
                (viewModel.currentFragment as MainFragment)?.myDeviceInfo()
            }
        } else {
            viewModel.leadp2pHander?.getWifiP2pManager()?.requestConnectionInfo( viewModel.leadp2pHander?.getChannel()) {
                if (!it.groupFormed) {
                    viewModel.selectedDevice = null
                    if (deviceInfoForQrCode != null && viewModel.currentFragment is MainFragment) {
                        (viewModel.currentFragment as MainFragment)?.myDeviceInfo()
                    }
                }
            }
           /* if (viewModel.leadp2pHander?.isConnected() == false) {
                viewModel.selectedDevice = null
                if (deviceInfoForQrCode != null && viewModel.currentFragment is MainFragment) {
                    (viewModel.currentFragment as MainFragment)?.myDeviceInfo()
                }
            }*/
        }
    }

    override fun onReceiveChatCommands(chatCommands: ChatCommands) {
        if (chatCommands != null) {
            when (chatCommands.chatType) {
                ChatType.VIDEO_CHAT_TYPE -> {
                    if (viewModel.currentFragment != null && viewModel.currentFragment is VideoFragment) {
                        (viewModel.currentFragment as VideoFragment)?.handleChatMessage(chatCommands.videoCommands)
                    }
                }
                ChatType.RESOURCE_SYNC_CHAT_TYPE -> {
                    val resourceSyncCommand = chatCommands.resourceSyncCommand?.resourceData
                    val myType = object : TypeToken<ArrayList<FileModel>>() {}.type
                    val fileModelArraylist =
                        Gson().fromJson<ArrayList<FileModel>>(resourceSyncCommand, myType)
                    runBlocking {
                        GlobalScope.launch(Dispatchers.IO) {
                            LeadResourcesRoomDatabase.getDatabase(applicationContext)
                                .resourceDetailDao()
                                .insert(Mapper.convertToResourceDetailList(fileModelArraylist))
                            chatCommands.transferStatus = TransferStatus.RECEIVED
                            chatCommands.resourceSyncCommand?.isSynced = true
                            val info = Gson().toJson(chatCommands)
                            viewModel.leadp2pHander?.sendMessage(
                                info.toString(),
                                viewModel.selectedDevice
                            )
                        }
                    }
                }
            }
        }
    }


    override fun onBackPressed() {
        val f: Fragment? = supportFragmentManager?.findFragmentById(R.id.fragment_container_view)
        if (f is MainFragment) {
            if (doubleBackToExitPressedOnce) {
                super.onBackPressed()
                return
            }
            this.doubleBackToExitPressedOnce = true
            Utils.showToast(this, "Please click BACK again to exit")

            Handler(Looper.getMainLooper()).postDelayed(Runnable {
                doubleBackToExitPressedOnce = false
            }, 2000)
        } else {
            val mainFragment = MainFragment()
            viewModel.openFragment(mainFragment, supportFragmentManager)
        }
    }

    private fun installAPK(path: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val contentUri = FileProvider.getUriForFile(
                this,
                BuildConfig.APPLICATION_ID + PROVIDER_PATH,
                File(path)
            )
            val install = Intent(Intent.ACTION_VIEW)
            install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            install.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            install.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true)
            install.data = contentUri
            startActivity(install)
        } else {
            val install = Intent(Intent.ACTION_VIEW)
            install.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            install.setDataAndType(Uri.fromFile(File(path)), APP_INSTALL_PATH)
            startActivity(install)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            initializeWifiDirectSdk()
        }
    }
}