package com.example.leadp2pdirect.servers

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import android.preference.PreferenceManager
import android.provider.OpenableColumns
import android.util.Log
import android.webkit.MimeTypeMap
import java.io.File
import java.util.*

object FileHelper {

    fun getRootDirectoryPath(context: Context) =
        PreferenceManager.getDefaultSharedPreferences(context).getString(
            Variables.APP_TYPE,
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                .toString() + "/"
                    + context.getApplicationContext().getPackageName()
        )

    fun mimeType(uri: Uri, contentResolver: ContentResolver): String? {
        if (uri.scheme.equals(ContentResolver.SCHEME_CONTENT)) {
            // get (image/jpeg, video/mp4) from ContentResolver if uri scheme is "content://"
            return contentResolver.getType(uri)
        } else {
            // get (.jpeg, .mp4) from uri "file://example/example.mp4"
            val fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri.toString())
            // turn ".mp4" into "video/mp4"
            return MimeTypeMap.getSingleton()
                .getMimeTypeFromExtension(fileExtension.lowercase(Locale.US))
        }
    }

     fun getFileModelsListFromUris(
        uris: ArrayList<Uri>,
        cr: ContentResolver
    ): ArrayList<FileModel>? {
        val fileModelArrayList = ArrayList<FileModel>()
        for (i in uris.indices) {
            val returnUri = uris[i]
            val returnCursor: Cursor? = cr.query(returnUri, null, null, null, null)
            val nameIndex = returnCursor?.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            val sizeIndex = returnCursor?.getColumnIndex(OpenableColumns.SIZE)
            returnCursor?.moveToFirst()
            val fileName = nameIndex?.let { returnCursor?.getString(it) }
            val fileLength = sizeIndex?.let { returnCursor?.getLong(it) }
            val mimeType = mimeType(returnUri, cr)
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
            returnCursor?.close()
        }
        return fileModelArrayList
    }

    // For to Delete the directory inside list of files and inner Directory
    fun deleteDir(dir: File): Boolean {
        if (dir.isDirectory()) {
            val children: Array<String> = dir.list()
            for (i in children.indices) {
                val success = deleteDir(File(dir, children[i]))
                if (!success) {
                    return false
                }
            }
        }
        // The directory is now empty so delete it
        return dir.delete()
    }
}