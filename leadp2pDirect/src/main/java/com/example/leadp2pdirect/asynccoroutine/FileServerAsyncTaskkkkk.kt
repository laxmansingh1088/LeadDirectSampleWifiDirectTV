package com.example.leadp2pdirect.asynccoroutine

import android.content.Context
import android.content.SharedPreferences
import android.os.Environment
import android.preference.PreferenceManager
import android.util.Log
import com.example.leadp2pdirect.P2PCallBacks
import com.example.leadp2pdirect.helpers.callbacks.Callback
import com.example.leadp2pdirect.servers.FileDownloadUploadProgresssModel
import com.example.leadp2pdirect.servers.FileHelper
import com.example.leadp2pdirect.servers.FileModel
import com.example.leadp2pdirect.servers.Variables
import java.io.File
import java.io.FileOutputStream
import java.io.ObjectInputStream
import java.io.OutputStream
import java.net.ServerSocket
import java.net.Socket

class FileServerAsyncTaskkkkk(
    contextWeakReference: Context?,
    reference: ServerSocket?,
    receivedFiles: Array<File?>?, callback: Callback?, p2PCallBacks: P2PCallBacks?
) : CoroutinesAsyncTask<Unit, FileDownloadUploadProgresssModel, Unit>("FileServerAsyncTaskkkkk") {
    private var context: Context? = null
    private var serverSocket: ServerSocket? = null
    private var client: Socket? = null
    private val file: File? = null
    private var fileSize: Long? = null
    private var fileSizeOriginal: Long? = null
    var receivedFiles: Array<File?>?
    private var referenceCallback: Callback? = null
    private var sharedPreferences: SharedPreferences? = null
    private var p2PCallBacks: P2PCallBacks? = null
    private val receivedFilesPathList = ArrayList<FileModel>()


    init {
        context = contextWeakReference
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(contextWeakReference)
        serverSocket = reference
        this.receivedFiles = receivedFiles
        referenceCallback = callback
        this.p2PCallBacks = p2PCallBacks
    }


    private fun receiveData() {
        val buf = ByteArray(8192)
        var len = 0
        try {
            Log.d("Receiver", "Server Listening")
            client = serverSocket!!.accept()
            Log.d("Receiver", "Server Connected")
            if (isCancelled) return
            val inputStream = client?.getInputStream()
            val objectInputStream = ObjectInputStream(inputStream)
            var file: File = File(
                sharedPreferences!!.getString(
                    Variables.APP_TYPE,
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                        .toString() + "/"
                            + context!!.applicationContext.packageName
                )
            )
            if (file != null && file.exists()) {
                FileHelper.deleteDir(file)
            }
            //Get filemodel.........
            val fileModelArrayList =
                objectInputStream.readObject() as java.util.ArrayList<FileModel>
            for (i in fileModelArrayList.indices) {
                val fileModel = fileModelArrayList[i]
                val fileName = fileModel.fileName
                fileSize = fileModel.fileLength
                fileSizeOriginal = fileSize
                file = File(
                    sharedPreferences!!.getString(
                        Variables.APP_TYPE,
                        (Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                            .toString() + "/"
                                + context!!.applicationContext.packageName)
                    ) + "/" + fileName
                )
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
                progress.setFileLength(fileSize!!)
                try {
                    Log.d(
                        "whileloopp",
                        "FileServerAsyncTask.java -- Receiving Started==================="
                    )
                    while (fileSize!! > 0 &&
                        (objectInputStream.read(
                            buf,
                            0,
                            Math.min(buf.size.toLong(), fileSize!!).toInt()
                        ).also {
                            len = it
                        }) != -1
                    ) {
                        outputStream.write(buf, 0, len)
                        outputStream.flush()
                        fileSize = fileSize!! - len.toLong()
                        progress.dataIncrement = len.toLong()
                        if (((progress.totalProgress * 100 / fileSizeOriginal!!).toInt()) ==
                            (((progress.totalProgress + progress.dataIncrement) * 100 / fileSizeOriginal!!).toInt())
                        ) {
                            progress.totalProgress = progress.totalProgress + progress.dataIncrement
                            continue
                        }
                        progress.totalProgress = progress.totalProgress + progress.dataIncrement
                        publishProgress(progress)
                        if (this.isCancelled) return
                    }
                    fileModel.absoluteFilePath = file.absolutePath
                    receivedFilesPathList.add(fileModel)
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
            objectInputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun doInBackground(vararg params: Unit?) {
        receiveData()
    }

    override fun onProgressUpdate(vararg values: FileDownloadUploadProgresssModel?) {
        super.onProgressUpdate(*values)
        val fileDownloadProgresssModel = values[0]!!
        // Total progress
        // Total progress
        val progress =
            (fileDownloadProgresssModel.totalProgress * 100 / fileDownloadProgresssModel.fileLength).toInt()
        fileDownloadProgresssModel.progressPercentage = progress
        p2PCallBacks!!.onProgressUpdate(fileDownloadProgresssModel)
        // Log.d("progressss..", "Progress :- " + progress + "%");
    }

    override fun onPostExecute(result: Unit?) {
        super.onPostExecute(result)
        Log.d("Receiver", "onPostExecute")
        try {
//      serverSocket.close();
            client!!.close()
            referenceCallback!!.call()
            p2PCallBacks!!.onFilesReceived(receivedFilesPathList)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    override fun onCancelled(result: Unit?) {
        super.onCancelled(result)
        Log.d("Receiver", "Transfer Cancelled")
        try {
//      if (client.isConnected()) serverSocket.close();
            client!!.close()
            referenceCallback!!.call()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }
}