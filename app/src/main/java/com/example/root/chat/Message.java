package com.example.root.chat;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Message {
    private String message, msgDate;
    private boolean isMe;
    private Date date;

    public Message() {

    }

    public Message(String message, boolean isMe) {
        this.message = message;
        this.isMe = isMe;
        // Formatiranje datuma

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
        date = new Date();
        msgDate = new SimpleDateFormat("H:mm a").format(date);
        return msgDate;
    }

    public String getMsgDateLong() {
        date = new Date();
        msgDate = new SimpleDateFormat("dd-MM-yyyy").format(date);
        return msgDate;
    }

    public void setMsgDate(String msgDate) {
        this.msgDate = msgDate;
    }
}
