package com.example.leaddirectsamplewifidirecttv

import android.util.Log
import java.io.*
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

class FileManager {
    private val APPLE_RESOURCE = "__MACOSX"

    suspend fun  unzip(filePath: String, destination: String): String? {
        var indexFilePath = destination
        try {
            val inputStream = FileInputStream(filePath)
            val zipStream = ZipInputStream(inputStream)
            var zEntry: ZipEntry? = null
            var i = 0;
            while (zipStream.nextEntry.also { zEntry = it } != null) {
                if (zEntry?.name?.uppercase(Locale.getDefault())
                        ?.contains(APPLE_RESOURCE) == false
                ) {
                    Log.d(
                        "Unzip", "Unzipping " + zEntry!!.name + " at "
                                + destination
                    )
                    if (i == 0) {
                        val firstfolderName = zEntry!!.name
                        indexFilePath += "/" + firstfolderName + firstfolderName.replace(
                            "/",
                            ""
                        ) + ".html"
                        Log.d("Unzip", "-------->  " + indexFilePath)
                        i++
                    }
                    if (zEntry!!.isDirectory) {
                        hanldeDirectory(zEntry!!.name, destination)
                    } else {
                        val newfile = File(destination.toString() + "/" + zEntry!!.name)
                        if (!newfile.getParentFile().exists()) {
                            newfile.getParentFile().mkdirs()
                        }
                        if (!newfile.exists()) {
                            newfile.createNewFile()
                        }
                        val fout = FileOutputStream(
                            destination.toString() + "/" + zEntry!!.name
                        )
                        val bufout = BufferedOutputStream(fout)
                        val buffer = ByteArray(1024)
                        var read = 0
                        while (zipStream.read(buffer).also { read = it } != -1) {
                            bufout.write(buffer, 0, read)
                        }
                        zipStream.closeEntry()
                        bufout.close()
                        fout.close()
                    }
                }
            }
            zipStream.close()
            Log.d("Unzip", "Unzipping complete. path :  $destination")
            return indexFilePath
        } catch (e: java.lang.Exception) {
            Log.d("Unzip", "Unzipping failed")
            e.printStackTrace()
        }
        return null
    }

    fun hanldeDirectory(dir: String, destination: String) {
        val f = File(destination.toString() + dir)
        if (!f.isDirectory) {
            f.mkdirs()
        }
    }
}