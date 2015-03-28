package com.example.root.chat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class Message {
    private String message, msgDate;
    private boolean isMe;
    private Date date;

    public Message() {

    }

    public Message(String message, boolean isMe) {
        this.message = message;
        this.isMe = isMe;
    }

    /**
     * TODO
     * Formatiranje datuma:
     *   - Ako je timestamp danasnji prikazi vrijeme
     *   - Ako timestamp nije danasnji datum prikazi dan i vrijeme
     *   - Ako timestamp nije trenutna sedmica prikazi datum i godinu
     */

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
        msgDate = new SimpleDateFormat("H:mm").format(date);
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

    public String getMsgDateOld() {
        return msgDate;
    }

    public String getMsgDateForDatabase() {
        date = new Date();
        msgDate = new SimpleDateFormat("EEEE, MMM dd, yyyy HH:mm").format(date);
        return msgDate;
    }

    public String checkDate(String dateString) {
        SimpleDateFormat format = new SimpleDateFormat("EEEE, MMM dd, yyyy HH:mm");

        try {
            Date date = format.parse(dateString);
            Date today = new Date();

            long diff = today.getTime() - date.getTime();
            long days = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);

            if (days == 0 ) {
                return "today";
            } else if (days < 7) {
                return "this week";
            } else {
                return "other";
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }
}
