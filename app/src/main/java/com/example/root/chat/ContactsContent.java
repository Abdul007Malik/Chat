package com.example.root.chat;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;

import java.util.ArrayList;

public class ContactsContent {

    ContentResolver contentResolver;
    Uri CONTENT_URI = ContactsContract.Contacts.CONTENT_URI;
    String _ID = ContactsContract.Contacts._ID;
    String DISPLAY_NAME = ContactsContract.Contacts.DISPLAY_NAME;
    String HAS_PHONE_NUMBER = ContactsContract.Contacts.HAS_PHONE_NUMBER;

    public ContactsContent(ContentResolver cr) {
        contentResolver = cr;
    }

    public Uri fetchContactImageUri(String nameContact) {

        Uri PhoneCONTENT_URI = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        String Phone_CONTACT_ID = ContactsContract.CommonDataKinds.Phone.CONTACT_ID;
        String NUMBER = ContactsContract.CommonDataKinds.Phone.NUMBER;

        Cursor cursor = contentResolver.query(CONTENT_URI, null, null, null, null);

        // Loop for every contact in the phone
        if (cursor.getCount() > 0) {

            while (cursor.moveToNext()) {

                String contact_id = cursor.getString(cursor.getColumnIndex(_ID));
                String name = cursor.getString(cursor.getColumnIndex(DISPLAY_NAME));

                int hasPhoneNumber = Integer.parseInt(cursor.getString(cursor.getColumnIndex(HAS_PHONE_NUMBER)));

                if (hasPhoneNumber > 0 && nameContact.equals(name)) {

                    Uri imageURI = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI, contact_id);
                    return imageURI;
                }
            }
        }
        return null;
    }

    public ArrayList<String> getAllContactNames() {
        ArrayList<String> contactNames = new ArrayList<String>();
        Cursor cursor = contentResolver.query(CONTENT_URI,null,null,null,null);

        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                contactNames.add(cursor.getString(cursor.getColumnIndex(DISPLAY_NAME)));
            }
        }
        return contactNames;
    }
}
