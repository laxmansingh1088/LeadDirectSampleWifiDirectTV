package com.example.leaddirectsamplewifidirecttv.ui.fragments


import android.graphics.Point
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidmads.library.qrgenearator.QRGContents
import androidmads.library.qrgenearator.QRGEncoder
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.activityViewModels
import com.example.leaddirectsamplewifidirecttv.R
import com.example.leaddirectsamplewifidirecttv.databinding.FragmentMainBinding
import com.example.leaddirectsamplewifidirecttv.viewmodels.MainActivityViewModel
import com.example.leadp2pdirect.p2p.MyDeviceInfoForQrCode
import com.google.gson.Gson
import com.google.zxing.WriterException


class MainFragment : Fragment(R.layout.fragment_main) {
    private val TAG = "MainFragment"
    private lateinit var binding: FragmentMainBinding
    private lateinit var qrgEncoder: QRGEncoder
    private val viewModel: MainActivityViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = getView().let { FragmentMainBinding.bind(it!!) }
    }


    override fun onResume() {
        super.onResume()
        if (viewModel.selectedDevice != null) {
            Log.d(
                TAG,
                "onResume->viewModel.selectedDevice-> ${viewModel?.selectedDevice?.deviceName}"
            )
            binding.tvconnectedTo.text = "Connected to :- ${viewModel?.selectedDevice?.deviceName}"
            viewModel.leadp2pHander?.checkServerFile()
        }



        viewModel.leadp2pHander?.getWifiP2pManager()?.requestConnectionInfo( viewModel.leadp2pHander?.getChannel()) {
            if (it.groupFormed == true) {
                Log.d(TAG, "onResume->Connected->true")
            } else {
                Log.d(TAG, "onResume->Connected->false")
            }
        }
    }


    fun updateReceivingFileText(message: String) {
        Log.d("progressssss", message)
        binding.tvreceivingInfo.text = message
    }

    fun manageUIWhenP2PIsConnected(isConnected: Boolean, connectedDeviceName: String?) {
        if (isConnected) {
            binding.connectedLayout.visibility = View.VISIBLE
            binding.waitingForConnectionLayout.visibility = View.GONE
            binding.tvconnectedTo.text = "Connected to :- $connectedDeviceName"
        } else {
            myDeviceInfo()
        }
    }

    fun myDeviceInfo() {
        val deviceInfoForQrCode = viewModel.deviceInfoForQrCode
        if(!deviceInfoForQrCode.isNullOrBlank()) {
            binding.connectedLayout.visibility = View.GONE
            binding.waitingForConnectionLayout.visibility = View.VISIBLE
            if (deviceInfoForQrCode != null) {
                Log.d("mydevice_info", deviceInfoForQrCode)
            }
            val myDeviceInfoForQrCode =
                Gson().fromJson(deviceInfoForQrCode, MyDeviceInfoForQrCode::class.java)
            binding.deviceName.setText(myDeviceInfoForQrCode.deviceId)
            if (deviceInfoForQrCode != null) {
                generateQRCode(deviceInfoForQrCode)
            }
        }
    }

    private fun generateQRCode(data: String) {
        val manager =
            requireActivity().getSystemService(FragmentActivity.WINDOW_SERVICE) as WindowManager
        // initializing a variable for default display.
        val display = manager.getDefaultDisplay()
        // creating a variable for point which
        // is to be displayed in QR Code.
        val point = Point()
        display.getSize(point)
        // getting width and
        // height of a point
        val width = point.x
        val height = point.y
        // generating dimension from width and height.
        var dimen = if (width < height) width else height
        dimen = dimen * 3 / 4;
        // setting this dimensions inside our qr code
        // encoder to generate our qr code.
        qrgEncoder = QRGEncoder(data, null, QRGContents.Type.TEXT, dimen)
        try {
            // getting our qrcode in the form of bitmap.
            val bitmap = qrgEncoder?.bitmap
            // the bitmap is set inside our image
            // view using .setimagebitmap method.
            binding.qrImage.setImageBitmap(bitmap);
        } catch (e: WriterException) {
            Log.e("Tag", e.toString());
        }
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        viewModel.showConnectedDeviceInfo()
    }
}