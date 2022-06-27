package com.example.leadp2pdirect.threadss

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.leadp2pdirect.P2PCallBacks
import com.example.leadp2pdirect.constants.Constants
import com.example.leadp2pdirect.servers.FileDownloadUploadProgresssModel
import com.example.leadp2pdirect.servers.FileHelper
import com.example.leadp2pdirect.servers.FileModel
import com.example.leadp2pdirect.servers.LeadP2PHandlerCallbacks
import com.example.leadp2pdirect.utils.Utils
import java.io.*
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class ClientFileTransfer(
    private val context: Context,
    private val serverAddress: InetAddress?,
    private val p2PCallBacks: P2PCallBacks,
    private val leadP2PHandlerCallbacks: LeadP2PHandlerCallbacks
) : Thread() {
    private val TAG = "ClientFileTransfer.kt"
    private var socket: Socket? = null
    private var objectInputStream: ObjectInputStream? = null
    private var objectOutputStream: ObjectOutputStream? = null
    private var cr: ContentResolver? = null


    fun sendFiles(uris: ArrayList<Uri>) {
        val executorService = Executors.newSingleThreadExecutor()
        executorService.execute {
            var len = 0
            val buf = ByteArray(8192)
            var timeTakenbyFile = ""
            val handler = Handler(Looper.getMainLooper())
            try {
                val fileModelArrayList =
                    FileHelper.getFileModelsListFromUris(uris, getContentResolverInstance()!!)
                objectOutputStream?.writeObject(fileModelArrayList)
                objectOutputStream?.flush()
                for (i in uris.indices) {
                    timeTakenbyFile = ""
                    val inputStream = cr!!.openInputStream(uris[i])
                    val fileModel = fileModelArrayList!![i]
                    val progress = FileDownloadUploadProgresssModel()
                    progress.fileName = fileModel.fileName
                    progress.dataIncrement = 0
                    progress.totalProgress = 0
                    progress.sendingOrReceiving =
                        FileDownloadUploadProgresssModel.SendingOrReceiving.Sending.name
                    progress.fileLength = fileModel.fileLength
                    val startTime = System.currentTimeMillis()
                    while (inputStream!!.read(buf).also { len = it } != -1) {
                        objectOutputStream?.write(buf, 0, len)
                        objectOutputStream?.flush()
                        progress.dataIncrement = len.toLong()
                        if ((progress.totalProgress * 100 / fileModel.fileLength).toInt() ==
                            ((progress.totalProgress + progress.dataIncrement) * 100 / fileModel.fileLength).toInt()
                        ) {
                            progress.totalProgress = progress.totalProgress + progress.dataIncrement
                            continue
                        }
                        progress.totalProgress = progress.totalProgress + progress.dataIncrement
                        handler.post {
                            publishProgress(progress)
                        }
                    }
                    inputStream.close()
                    val endTime = System.currentTimeMillis()
                    val timeElapsed = endTime - startTime
                    val minutes = TimeUnit.MILLISECONDS.toMinutes(timeElapsed)
                    val seconds = TimeUnit.MILLISECONDS.toSeconds(timeElapsed) % 60
                    timeTakenbyFile =
                        "FileSize:- " + Utils.humanReadableByteCountSI(fileModel.fileLength) + " &&  Time Taken:- " + minutes + " min : " + seconds + " sec"
                    Log.d("Time--elapsed:->", timeTakenbyFile)
                    handler.post {
                        onPostExecute(timeTakenbyFile)
                    }
                }
                objectOutputStream?.flush()

            } catch (e: Exception) {
                Log.d(TAG, "Exception --> line 90")
                cleanAndRestartClient(e.message)
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
                socket!!.connect(InetSocketAddress(serverAddress, Constants.FILE_TRANSFER_PORT))
                objectOutputStream = ObjectOutputStream(socket?.getOutputStream())
                objectOutputStream?.flush()
                objectInputStream = ObjectInputStream(socket?.getInputStream())
                connected = true
            } catch (e: IOException) {
                socket = null
                sleep(3000)
                e.printStackTrace()
            }
        }
    }


    override fun run() {
        startOrWaitForSocket(false)
        val executorService = Executors.newSingleThreadExecutor()
        val handler = Handler(Looper.getMainLooper())
        executorService.execute {
            val buf = ByteArray(8192)
            var len = 0
            var receivedFilesPathList: java.util.ArrayList<FileModel>? = null

            while (socket != null) {
                try {
                    val fileModelArrayList =
                        objectInputStream!!.readObject() as ArrayList<FileModel>
                    var file = File(context?.let { FileHelper.getRootDirectoryPath(it) })
                    if (file != null && file.exists()) {
                        FileHelper.deleteDir(file)
                    }
                    //Get filemodel.........

                    receivedFilesPathList = java.util.ArrayList<FileModel>()
                    for (i in fileModelArrayList.indices) {
                        val fileModel = fileModelArrayList[i]
                        val fileName = fileModel.fileName
                        var fileSize = fileModel.fileLength
                        val fileSizeOriginal = fileSize
                        file =
                            File(context?.let { FileHelper.getRootDirectoryPath(it) } + "/" + fileName)
                        Log.d("Receiver", file.path)
                        val dir = file.parentFile
                        // Utils.INSTANCE.deleteDir(dir);
                        if (!dir.exists()) {
                            dir.mkdirs()
                        }
                        if (file.exists()) file.delete()
                        if (file.createNewFile()) {
                            Log.d("Receiver", "File Created")
                        } else Log.d("Receiver", "File Not Created")
                        val outputStream: OutputStream = FileOutputStream(file)
                        //customObject need for progress update
                        val progress = FileDownloadUploadProgresssModel()
                        progress.fileName = fileName
                        progress.dataIncrement = 0
                        progress.totalProgress = 0
                        progress.sendingOrReceiving =
                            FileDownloadUploadProgresssModel.SendingOrReceiving.Receiving.name
                        progress.fileLength = fileModel.fileLength
                        try {
                            Log.d(
                                "whileloopp",
                                "ClientFileTransfer.kt -- Receiving Started==================="
                            )
                            while (fileSize > 0 && objectInputStream!!.read(
                                    buf,
                                    0,
                                    Math.min(buf.size.toLong(), fileSize).toInt()
                                ).also { len = it } != -1
                            ) {
                                outputStream.write(buf, 0, len)
                                outputStream.flush()
                                fileSize -= len.toLong()
                                progress.dataIncrement = len.toLong()
                                if ((progress.totalProgress * 100 / fileSizeOriginal).toInt() ==
                                    ((progress.totalProgress + progress.dataIncrement) * 100 / fileSizeOriginal).toInt()
                                ) {
                                    progress.totalProgress =
                                        progress.totalProgress + progress.dataIncrement
                                    continue
                                }
                                progress.totalProgress =
                                    progress.totalProgress + progress.dataIncrement
                                handler.post {
                                    publishProgress(progress)
                                }
                                // if (!this.isAlive()) return@execute
                            }
                            fileModel.absoluteFilePath = file.absolutePath
                            receivedFilesPathList?.add(fileModel)
                            handler.post {
                                val list = java.util.ArrayList<FileModel>()
                                receivedFilesPathList?.let { list.addAll(it) }
                                onPostExecute(list)
                                receivedFilesPathList = null
                            }
                            Log.d(
                                "whileloopp",
                                "FileServerAsyncTask.java -- Receiving Finished==================="
                            )

                        } catch (e: Exception) {
                            Log.d("Receiver", "oops")
                            e.printStackTrace()
                        }
                        outputStream.flush()
                        outputStream.close()
                    }
                } catch (e: IOException) {
                    Log.d(TAG, "IOException --> line214")
                    cleanAndRestartClient(e.message)
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


    private fun cleanAndRestartClient(exceptionMessage: String?) {
        try {
            socket?.close()
            socket = null
            objectInputStream?.close()
            objectOutputStream?.flush()
            objectOutputStream?.close()
        } catch (ex: Exception) {
            Log.d(TAG, "cleanAndRestartClient -> Exception --> ${ex.message}")
        }
        val handler = Handler(Looper.getMainLooper())
        handler.post() {
            if (context != null) {
                exceptionMessage?.let { Utils.showToast(context, it) }
            }
            Log.d(TAG, "cleanAndRestartClient()")
            leadP2PHandlerCallbacks.cleanAndRestartClient()
        }
    }

    public fun cleanUp() {
        try {
            if (socket == null) {
                startOrWaitForSocket(true)
            }
            socket?.close()
            socket = null
            objectInputStream?.close()
            objectOutputStream?.flush()
            objectOutputStream?.close()
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

