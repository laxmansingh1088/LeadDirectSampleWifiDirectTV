package com.example.leadp2pdirect.chatmessages

import com.example.leadp2pdirect.chatmessages.enumss.VideoPlayBacks

data class AudioCommands(
    private val playBacks: VideoPlayBacks = VideoPlayBacks.PLAY,
    private val audioFileName: String = ""
)
