package com.example.leadp2pdirect.chatttthreadds

import android.content.ContentResolver
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.leadp2pdirect.P2PCallBacks
import com.example.leadp2pdirect.servers.FileDownloadUploadProgresssModel
import com.example.leadp2pdirect.servers.FileModel
import com.example.leadp2pdirect.servers.LeadP2PHandlerCallbacks
import com.example.leadp2pdirect.textmessage.BaseMessage
import com.example.leadp2pdirect.textmessage.ReceiveCallback
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.*
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.Executors

class ChatServer(
    private val context: Context?,
    private val serverSocket: ServerSocket?,
    private val p2PCallBacks: P2PCallBacks,
    private val callback: ReceiveCallback,
    private val leadP2PHandlerCallbacks: LeadP2PHandlerCallbacks
) :
    Thread() {
    private val TAG = "ChatServer.kt"
    private var socket: Socket? = null
    var inputStream: InputStream? = null
    var outputStream: OutputStream? = null
    private var cr: ContentResolver? = null

    fun sendMessage(message: BaseMessage) {
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val messageByteArray = message.serialize().toByteArray()
                    outputStream?.write(messageByteArray)
                    outputStream?.flush()
                }catch (ex :Exception){
                    withContext(Dispatchers.Main){
                        cleanAndRestartChatServer()
                    }
                }
        }
    }

    override fun run() {
        val handler = Handler(Looper.getMainLooper())
        try {
            Log.d(TAG, "serverFiletranser== run() method");
            socket = serverSocket!!.accept()
            outputStream = socket?.getOutputStream()
            outputStream?.flush()
            inputStream = socket?.getInputStream()
        } catch (e: IOException) {
            e.message?.let { Log.d(TAG, it) }
            handler.post {
                cleanAndRestartChatServer()
            }
        }
        val executorService = Executors.newSingleThreadExecutor()
        executorService.execute {
            val buffer = ByteArray(1024)
            var bytes: Int
            while (socket != null) {
                try {
                    bytes = inputStream!!.read(buffer)
                    if (bytes == -1) {
                        Log.d("checkinggg", "bytessss==-1")
                       throw IOException()
                    }
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
                    handler.post {
                        cleanAndRestartChatServer()
                    }
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


    private fun onPostExecute(receivedFilesPathList: java.util.ArrayList<FileModel>) {
        Log.d("Receiver", "onPostExecute")
        try {
            p2PCallBacks!!.onFilesReceived(receivedFilesPathList)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    private fun onPostExecute(timeTakenByFile: String) {
        Log.d("Receiver", "onPostExecute")
        p2PCallBacks!!.timeTakenByFileToSend(timeTakenByFile)
        Log.d("Sender", "Finished!")
    }


    private fun cleanAndRestartChatServer() {
        try {
            socket?.close()
            socket = null
            inputStream?.close()
            outputStream?.flush()
            outputStream?.close()
        } catch (ex: Exception) {
            ex.message?.let { Log.d(TAG, it) }
        }
        if (context != null) {
            Log.d(TAG, "cleanAndRestartChatServer() -->")
        }
        leadP2PHandlerCallbacks.cleanAndRestartChatServer()

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