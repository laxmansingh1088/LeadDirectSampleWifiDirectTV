package com.example.leadp2pdirect.utils

import android.content.Context
import android.widget.Toast
import java.io.File
import java.text.CharacterIterator
import java.text.StringCharacterIterator

object Utils {
    fun showToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
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
}