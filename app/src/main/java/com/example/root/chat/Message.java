package com.example.root.chat;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by root on 1/16/15.
 */
public class Message {
    private String message;
    private boolean isMe;
    private Date msgDate;

    public Message(String message, boolean isMe) {
        this.message = message;
        this.isMe = isMe;
        msgDate = new Date();
    }

    public String getMessage() {
        return message;
    }

    public boolean isMe() {
        return isMe;
    }

    public void setMe(boolean isMe) {
        this.isMe = isMe;
    }

    public Date getMsgDate() {
        return msgDate;
    }

    public String getStringDate() {
        Date date = getMsgDate();
        String newString = new SimpleDateFormat("H:mm").format(date);
        return newString;
    }
}
