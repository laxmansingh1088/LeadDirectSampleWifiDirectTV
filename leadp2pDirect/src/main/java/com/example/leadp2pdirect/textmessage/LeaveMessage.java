package com.example.leadp2pdirect.textmessage;

/**
 * Created by Koerfer on 22.03.2016.
 */
public class LeaveMessage extends BaseMessage {

    private ClientInfo Client;

    public LeaveMessage(ClientInfo clientInfo) {
        this.Client = clientInfo;
    }

    public ClientInfo getClient() {
        return this.Client;
    }

    public String serialize() {
        return "LEAVE" + this.Client.serialize();
    }

    public static LeaveMessage Deserialize(String part) {
        ClientInfo clientInfo = ClientInfo.Deserialize(part);
        return new LeaveMessage(clientInfo);
    }

}
