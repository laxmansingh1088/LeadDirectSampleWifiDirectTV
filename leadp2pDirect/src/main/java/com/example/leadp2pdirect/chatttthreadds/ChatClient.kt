package com.example.leadp2pdirect.chatttthreadds

import android.content.ContentResolver
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.leadp2pdirect.P2PCallBacks
import com.example.leadp2pdirect.constants.Constants
import com.example.leadp2pdirect.servers.FileDownloadUploadProgresssModel
import com.example.leadp2pdirect.servers.FileModel
import com.example.leadp2pdirect.servers.LeadP2PHandlerCallbacks
import com.example.leadp2pdirect.textmessage.BaseMessage
import com.example.leadp2pdirect.textmessage.ReceiveCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.*
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket
import java.util.concurrent.Executors
import kotlin.coroutines.CoroutineContext

class ChatClient(
    private val context: Context,
    private val serverAddress: InetAddress?,
    private val p2PCallBacks: P2PCallBacks,
    private val callback: ReceiveCallback,
    private val leadP2PHandlerCallbacks: LeadP2PHandlerCallbacks
) : Thread() {
    private val TAG = "ChatClient.kt"
    private var socket: Socket? = null
    var inputStream: InputStream? = null
    var outputStream: OutputStream? = null
    private var cr: ContentResolver? = null

    fun sendMessage(message: BaseMessage) {
        GlobalScope.launch(Dispatchers.IO) {
            if (outputStream != null) {
                val messageByteArray = message.serialize().toByteArray()
                outputStream?.write(messageByteArray)
                outputStream?.flush()
            }
        }
    }

    private fun startOrWaitForSocket(connect: Boolean) {
        var connected = connect
        while (!connected) {
            try {
                if (socket == null) {
                    socket = Socket()
                }
                socket!!.connect(InetSocketAddress(serverAddress, Constants.CHAT_PORT))
                inputStream = socket?.getInputStream()
                outputStream = socket?.getOutputStream()
                connected = true
            } catch (e: IOException) {
                socket = null
                sleep(5000)
                e.message?.let { Log.d(TAG, it) }
            }
        }
    }


    override fun run() {
        startOrWaitForSocket(false)
        val executorService = Executors.newSingleThreadExecutor()
        val handler = Handler(Looper.getMainLooper())
        executorService.execute {
            val buffer = ByteArray(1024)
            var bytes: Int
            while (socket != null) {
                try {
                    bytes = inputStream!!.read(buffer)
                    if (bytes > 0) {
                        val finalbytes = bytes
                        handler.post {
                            val message = String(buffer, 0, finalbytes)
                            val received = BaseMessage.Deserialize(message)
                            callback.ReceiveMessage(received)
                        }
                    }
                } catch (e: IOException) {
                    e.message?.let { Log.d(TAG, it) }
                    cleanAndRestartChatClient()
                }
            }
        }
    }


    private fun publishProgress(fileDownloadProgresssModel: FileDownloadUploadProgresssModel) {
        // Total progress
        val progress =
            (fileDownloadProgresssModel.totalProgress * 100 / fileDownloadProgresssModel.fileLength).toInt()
        fileDownloadProgresssModel.progressPercentage = progress
        p2PCallBacks!!.onProgressUpdate(fileDownloadProgresssModel)
        // Log.d("progressss..", "Progress :- " + progress + "%");
    }

    private fun onPostExecute(timeTakenByFile: String) {
        Log.d("Receiver", "onPostExecute")
        p2PCallBacks!!.timeTakenByFileToSend(timeTakenByFile)
        Log.d("Sender", "Finished!")
    }

    private fun onPostExecute(receivedFilesPathList: java.util.ArrayList<FileModel>) {
        Log.d("Receiver", "onPostExecute")
        try {
            p2PCallBacks!!.onFilesReceived(receivedFilesPathList)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }


    private fun cleanAndRestartChatClient() {
        try {
            socket?.close()
            socket = null
            inputStream?.close()
            outputStream?.flush()
            outputStream?.close()
        } catch (ex: Exception) {
            ex.message?.let { Log.d(TAG, it) }
        }
        val handler = Handler(Looper.getMainLooper())
        handler.post() {
            Log.d(TAG, "cleanAndRestartChatClient()")
            leadP2PHandlerCallbacks.cleanAndRestartChatClient()
        }
    }

    public fun cleanUp() {
        try {
            if (socket == null) {
                startOrWaitForSocket(true)
            }
            socket?.close()
            socket = null
            inputStream?.close()
            outputStream?.flush()
            outputStream?.close()
        } catch (ex: Exception) {
        }
        Log.d(TAG, "cleanUp()")
    }


    private fun getContentResolverInstance(): ContentResolver? {
        if (cr == null) {
            context.let {
                if (it != null) {
                    cr = it.contentResolver
                }
            }
        }
        return cr
    }
}