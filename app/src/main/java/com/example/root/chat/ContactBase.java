package com.example.root.chat;

import android.content.Context;

import java.util.ArrayList;

/**
 * Created by root on 1/19/15.
 */
public class ContactBase {
    private ArrayList<Contact> mContacts;

    private static ContactBase sContactBase;
    private Context mAppContext;

    private ContactBase(Context mAppContext) {
        this.mAppContext = mAppContext;
        mContacts = new ArrayList<Contact>();
    }

    public static ContactBase get(Context c) {
        if (sContactBase == null) {
            sContactBase = new ContactBase(c.getApplicationContext());
        }
        return sContactBase;
    }

    public Contact getContact(String contact) {
        for (Contact c : mContacts) {
            if (c.getContact().equals(contact))
                return c;
        }
        return null;
    }

    public void addContact(Contact c) {
        mContacts.add(c);
    }

    public void deleteContact(Contact c) {
        mContacts.remove(c);
    }

    public ArrayList<Contact> getContacts() {
        return mContacts;
    }
}
