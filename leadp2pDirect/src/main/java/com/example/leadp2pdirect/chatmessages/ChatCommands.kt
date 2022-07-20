package com.example.leadp2pdirect.chatmessages

import com.example.leadp2pdirect.chatmessages.enumss.TransferStatus

data class ChatCommands(
    val chatType: Int,
    var transferStatus: TransferStatus,
    val videoCommands: VideoCommands? = null,
    val audioCommands: AudioCommands? = null,
    val resourceSyncCommand: ResourceSyncCommand? = null,
    var textMessage: TextMessage? = null
)