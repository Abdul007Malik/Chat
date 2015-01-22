package com.example.root.chat;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by root on 1/19/15.
 */
public class Contact {

    private String contact, msgDate;
    private int counter;

    public Contact() {

    }

    public Contact(String contact, int counter) {
        this.contact = contact;
        this.counter = counter;
        Date date = new Date();
        msgDate= new SimpleDateFormat("H:mm").format(date);
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public int getCounter() {
        return counter;
    }

    public void setCounter(int counter) {
        this.counter = counter;
    }

    public String getMsgDate() {
        return msgDate;
    }

    public void setMsgDate(String msgDate) {
        this.msgDate = msgDate;
    }
}
