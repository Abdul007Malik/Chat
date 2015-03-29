package com.example.root.chat;

import android.util.Log;

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

    /**
     * Getter za contacts listu, prikazuje vrijeme posljednje primljene poruke u dd-MM-yyyy formatu
     * @return
     */
    public String getMsgDateLong() {
        date = new Date();
        return new SimpleDateFormat("dd-MM-yyyy").format(date);
    }

    public void setMsgDate(String msgDate) {
        this.msgDate = msgDate;
    }

    public String getMsgDateOld() {
        return msgDate;
    }

    public String getMsgDateForDatabase() {
        date = new Date();
        return new SimpleDateFormat("EEEE, MMM dd, yyyy HH:mm").format(date);
    }


    /**
     * Povlaci datum kao string iz baze i provjerava ga u odnosu na trenutni datum
     * Postoje 3 slucaja: danasnji datum, datum unutar 7 dana od danasnjeg i ostali dani
     * @param dateString
     * @return date
     */
    public String checkDate(String dateString) {
        SimpleDateFormat format = new SimpleDateFormat("EEEE, MMM dd, yyyy HH:mm");
        String checkedDate;

        try {
            Date date = format.parse(dateString);
            Date today = new Date();

            long diff = today.getTime() - date.getTime();
            long days = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
            Log.i("days", "" + days);

            if (days == 0 ) {
                checkedDate = new SimpleDateFormat("HH:mm").format(date);
                return "Today, " + checkedDate;
            } else if (days < 6) {
                checkedDate = new SimpleDateFormat("EEEE, HH:mm").format(date);
                return checkedDate;
            } else {
                checkedDate = new SimpleDateFormat("MMM dd, yyyy").format(date);
                return checkedDate;
            }
        } catch (ParseException e) {
            e.printStackTrace();
            Log.i("days", "error");
        }
        return null;
    }
}
