package com.example.root.chat;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by root on 1/16/15.
 */
public class Message {
    private String message, msgDate;
    private boolean isMe;

    public Message() {

    }

    public Message(String message, boolean isMe) {
        this.message = message;
        this.isMe = isMe;
        Date date = new Date();
        msgDate = new SimpleDateFormat("H:mm").format(date);
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isMe() {
        return isMe;
    }



    public void setMe(boolean isMe) {
        this.isMe = isMe;
    }

    public String getMsgDate() {
        return msgDate;
    }

    public void setMsgDate(String msgDate) {
        this.msgDate = msgDate;
    }
}
