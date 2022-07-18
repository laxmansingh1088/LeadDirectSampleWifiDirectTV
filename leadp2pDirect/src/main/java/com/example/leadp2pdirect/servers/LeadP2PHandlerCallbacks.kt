package com.example.leadp2pdirect.servers

interface LeadP2PHandlerCallbacks {
    fun cleanAndRestartServer()
    fun cleanAndRestartClient()
    fun cleanAndRestartChatServer()
    fun cleanAndRestartChatClient()
}