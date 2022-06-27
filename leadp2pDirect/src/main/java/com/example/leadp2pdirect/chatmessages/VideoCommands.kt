package com.example.leadp2pdirect.chatmessages

import com.example.leadp2pdirect.chatmessages.enumss.VideoPlayBacks


data class VideoCommands(
     val playBacks: VideoPlayBacks = VideoPlayBacks.PLAY,
     val videoFileName: String = ""
)