package com.example.root.chat;

import android.net.Uri;

/**
 * Created by root on 1/19/15.
 */
public class Contact {

    private String contact, msgDate, imageUri, phone;
    private int counter;

    public Contact() {

    }

    public Contact(String contact, int counter) {
        this.contact = contact;
        this.counter = counter;
        msgDate = getMsgDate();
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

    public String getImageUri() {
        return imageUri;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
