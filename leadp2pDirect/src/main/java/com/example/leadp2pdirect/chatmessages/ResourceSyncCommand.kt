package com.example.leadp2pdirect.chatmessages

data class ResourceSyncCommand(
    val resourceData: String?,
    var isSynced: Boolean = false
)
