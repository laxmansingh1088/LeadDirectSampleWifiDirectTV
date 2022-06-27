package com.example.leadp2pdirect.chatconnection;


import com.example.leadp2pdirect.textmessage.BaseMessage;
import com.example.leadp2pdirect.textmessage.ReceiveCallback;

public abstract class ChatConnection {

    public static final int CHAT_PORT = 8050;

    public boolean IsConnected = false;

    protected boolean ShouldStop = false;

    protected ReceiveCallback Callback;

    protected ChatConnection(ReceiveCallback callback) {
        this.Callback = callback;
    }

    public abstract void SendMessage(BaseMessage message);

    public void Stop() {
        this.IsConnected = false;
        this.ShouldStop = true;
    }

}

