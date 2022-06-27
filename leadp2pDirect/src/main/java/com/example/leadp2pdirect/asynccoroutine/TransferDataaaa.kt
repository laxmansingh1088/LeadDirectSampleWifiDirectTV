package com.example.leadp2pdirect.asynccoroutine

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import com.example.leadp2pdirect.P2PCallBacks
import com.example.leadp2pdirect.constants.Constants
import com.example.leadp2pdirect.servers.FileDownloadUploadProgresssModel
import com.example.leadp2pdirect.servers.FileHelper.mimeType
import com.example.leadp2pdirect.servers.FileModel
import java.io.ObjectOutputStream
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket
import java.text.CharacterIterator
import java.text.StringCharacterIterator
import java.util.concurrent.TimeUnit

class TransferDataaaa(
    context: Context?,
    uris: java.util.ArrayList<Uri?>?, serverAddress: InetAddress?,
    p2PCallBacks: P2PCallBacks?
) : CoroutinesAsyncTask<Unit, FileDownloadUploadProgresssModel, Unit>("MysAsyncTask") {

    private var context: Context? = null
    private var serverAddress: InetAddress? = null
    private var uris: ArrayList<Uri?>? = null
    private var cr: ContentResolver? = null
    private var p2PCallBacks: P2PCallBacks? = null
    private var timeTakenbyFile = ""

    init {
        this.context = context
        this.uris = uris
        this.serverAddress = serverAddress
        this.p2PCallBacks = p2PCallBacks
    }


    private fun getFileModelsListFromUris(uris: java.util.ArrayList<Uri?>?): java.util.ArrayList<FileModel>? {
        initializeContentResolver()
        val fileModelArrayList = java.util.ArrayList<FileModel>()
        for (i in uris?.indices!!) {
            val returnUri = uris[i]
            val returnCursor = returnUri?.let { cr!!.query(it, null, null, null, null) }
            val nameIndex = returnCursor!!.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            val sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE)
            returnCursor.moveToFirst()
            val fileName = returnCursor.getString(nameIndex)
            val fileLength = returnCursor.getLong(sizeIndex)
            val mimeType = mimeType(returnUri, cr!!)
            Log.d("fileinfoooooo", mimeType!!)
            val fileModel = FileModel()
            fileModel.fileName = fileName
            fileModel.fileLength = fileLength
            fileModel.mimeType = mimeType
            if (mimeType != null) {
                if (mimeType.contains("image")) {
                    fileModel.type = FileModel.TYPE_PHOTO
                } else if (mimeType.contains("video")) {
                    fileModel.type = FileModel.TYPE_VIDEO
                } else if (mimeType.contains("pdf")) {
                    fileModel.type = FileModel.TYPE_PDF
                } else if (mimeType.contains("application/vnd.android.package-archive")) {
                    fileModel.type = FileModel.TYPE_APPLICATION
                } else if (mimeType.contains("application/zip")) {
                    fileModel.type = FileModel.TYPE_ZIP
                }
            }
            fileModelArrayList.add(fileModel)
            returnCursor.close()
        }
        return fileModelArrayList
    }

    private fun initializeContentResolver() {
        if (cr == null) {
            cr = context!!.contentResolver
        }
    }


    private fun sendData(uris: java.util.ArrayList<Uri?>?) {
        var len = 0
        val buf = ByteArray(8192)
        val socket = Socket()
        try {
            socket.bind(null)
            Log.d("Client Address", socket.localSocketAddress.toString())
            socket.connect(InetSocketAddress(serverAddress, Constants.FILE_TRANSFER_PORT))
            val outputStream = socket.getOutputStream()
            val objectOutputStream = ObjectOutputStream(outputStream)
            val fileModelArrayList = getFileModelsListFromUris(uris)
            objectOutputStream.writeObject(fileModelArrayList)
            objectOutputStream.flush()
            for (i in uris?.indices!!) {
                val inputStream = uris[i]?.let { cr!!.openInputStream(it) }
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
                    objectOutputStream.write(buf, 0, len)
                    objectOutputStream.flush()
                    progress.dataIncrement = len.toLong()
                    if ((progress.totalProgress * 100 / fileModel.fileLength).toInt() ==
                        ((progress.totalProgress + progress.dataIncrement) * 100 / fileModel.fileLength).toInt()
                    ) {
                        progress.totalProgress = progress.totalProgress + progress.dataIncrement
                        continue
                    }
                    progress.totalProgress = progress.totalProgress + progress.dataIncrement
                    publishProgress(progress)
                }
                inputStream.close()
                Log.d("TRANSFER", "Writing Data Final   -$len")
                val endTime = System.currentTimeMillis()
                val timeElapsed = endTime - startTime
                val minutes = TimeUnit.MILLISECONDS.toMinutes(timeElapsed)
                val seconds = TimeUnit.MILLISECONDS.toSeconds(timeElapsed) % 60
                timeTakenbyFile =
                    "FileSize:- " +humanReadableByteCountSI(fileModel.fileLength) + " &&  Time Taken:- " + minutes + " min : " + seconds + " sec"
                Log.d("Time--elapsed:->", timeTakenbyFile)
            }
            outputStream.close()
            objectOutputStream.close()
            socket.close()
        } catch (e: Exception) {
            Log.d("Data Transfer", e.toString())
            e.printStackTrace()
        } finally {
            if (socket.isConnected) {
                try {
                    socket.close()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun humanReadableByteCountSI(bytes: Long): String? {
        var bytes = bytes
        if (-1000 < bytes && bytes < 1000) {
            return "$bytes B"
        }
        val ci: CharacterIterator = StringCharacterIterator("kMGTPE")
        while (bytes <= -999950 || bytes >= 999950) {
            bytes /= 1000
            ci.next()
        }
        return String.format("%.1f %cB", bytes / 1000.0, ci.current())
    }


    override fun onPreExecute() {
        super.onPreExecute()
        initializeContentResolver()
    }

    override fun doInBackground(vararg params: Unit?) {
        uris?.let { sendData(it) }
    }



    override fun onPostExecute(result: Unit?) {
        super.onPostExecute(result)
        p2PCallBacks!!.timeTakenByFileToSend(timeTakenbyFile)
        Log.d("Sender", "Finished!")
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
    }


    override fun onCancelled(result: Unit?) {
        super.onCancelled(result)
    }

}