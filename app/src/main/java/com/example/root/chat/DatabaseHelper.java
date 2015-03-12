package com.example.root.chat;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by root on 1/22/15.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    // Logcat tag
    private static final String LOG = "DatabaseHelper";

    // Database Version
    private static final int DATABASE_VERSION = 7;

    // Database Name
    private static final String DATABASE_NAME = "chatDatabase2";

    // Table Names
    private static final String TABLE_CONTACT = "contacts";
    private static final String TABLE_MESSAGE = "messages";

    // Common column names
    private static final String KEY_ID = "id";

    // CONTACT Table - column names
    private static final String KEY_NAME = "name";
    private static final String KEY_NUMBER = "number";
    private static final String KEY_COUNTER = "counter";
    private static final String KEY_LAST_MSG_DATE = "last_msg_date";
    private static final String KEY_IMAGE_URI = "image_uri";

    // MESSAGE Table - column names
    private static final String KEY_MESSAGE_TEXT = "text";
    private static final String KEY_IS_ME = "is_me";
    private static final String KEY_DATE = "date";
    private static final String KEY_CONTACT_ID = "contact_id";


    // Table Create Statements
    // CONTACT table
    private static final String CREATE_TABLE_CONTACT = "CREATE TABLE "
            + TABLE_CONTACT + "(" + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + KEY_NAME
            + " VARCHAR(255)," + KEY_NUMBER + " VARCHAR(255)," + KEY_COUNTER + " INTEGER," + KEY_LAST_MSG_DATE
            + " VARCHAR(255)," + KEY_IMAGE_URI + " VARCHAR(255)" + ")";

    // MESSAGE table
    private static final String CREATE_TABLE_MESSAGE = "CREATE TABLE " + TABLE_MESSAGE
            + "(" + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + KEY_MESSAGE_TEXT + " VARCHAR(255),"
            + KEY_IS_ME + " BOOLEAN," + KEY_DATE + " VARCHAR(255)," + KEY_CONTACT_ID + " INTEGER" + ")";


    // Konstruktor
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        // Kreiraj tabele sa kolonama
        db.execSQL(CREATE_TABLE_CONTACT);
        db.execSQL(CREATE_TABLE_MESSAGE);
        Log.d(LOG, "onCreate database called");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Upgrade tabela
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONTACT);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MESSAGE);
        Log.d(LOG, "onUpdate database called" + TABLE_MESSAGE);

        // create new tables
        onCreate(db);
    }

    // Dodavanje novog kontakta u bazu
    public long addContact(Contact contact) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_NAME, contact.getContact());
        values.put(KEY_NUMBER, contact.getPhone());
        values.put(KEY_COUNTER, contact.getCounter());
        values.put(KEY_LAST_MSG_DATE, contact.getMsgDate());
        values.put(KEY_IMAGE_URI, String.valueOf(contact.getImageUri()));

        // insert row
        long contact_id = db.insert(TABLE_CONTACT, null, values);
        db.close();
        return contact_id;
    }

    // Povuci sve kontakte
    public ArrayList<Contact> getAllContacts() {
        ArrayList<Contact> contacts = new ArrayList<Contact>();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_CONTACT, null, null, null, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                Contact contact = new Contact();
                contact.setContact((cursor.getString(cursor.getColumnIndex(KEY_NAME))));
                contact.setPhone((cursor.getString(cursor.getColumnIndex(KEY_NUMBER))));
                contact.setCounter(cursor.getInt(cursor.getColumnIndex(KEY_COUNTER)));
                contact.setMsgDate(cursor.getString(cursor.getColumnIndex(KEY_LAST_MSG_DATE)));
                contact.setImageUri(Uri.parse(cursor.getString(cursor.getColumnIndex(KEY_IMAGE_URI))));

                contacts.add(contact);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return contacts;
    }

    // Povuci kontakt na osnovu ID
    public Contact getContact(long id) {
        String[] columns = {KEY_NAME, KEY_NUMBER, KEY_COUNTER, KEY_IMAGE_URI};
        String[] args = {String.valueOf(id)};

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_CONTACT, columns, KEY_ID + " =?", args, null, null, null);

        if (cursor != null)
            cursor.moveToFirst();

        Contact contact = new Contact(cursor.getString(cursor.getColumnIndex(KEY_NAME)), cursor.getInt(cursor.getColumnIndex(KEY_COUNTER)));
        contact.setPhone(cursor.getString(cursor.getColumnIndex(KEY_NUMBER)));
        contact.setImageUri(Uri.parse(cursor.getString(cursor.getColumnIndex(KEY_IMAGE_URI))));

        return contact;
    }

    // Povuci kontaktov ID
    public long getContactId(Contact contact) {
        String[] column = {KEY_ID};
        String[] args = {contact.getContact()};

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_CONTACT, column, KEY_NAME + " =?", args, null, null, null);

        if (cursor != null)
            cursor.moveToFirst();

        long id = cursor.getLong(cursor.getColumnIndex(KEY_ID));
        cursor.close();
        return id;

    }

    // Update brojaca novih poruka i datuma poruke
    public void updateContactCounterDate(Contact contact) {
        SQLiteDatabase db = this.getWritableDatabase();

        String[] args = {contact.getContact()};
        ContentValues values = new ContentValues();
        values.put(KEY_COUNTER, contact.getCounter());
        values.put(KEY_LAST_MSG_DATE, contact.getMsgDate());

        // updating row
        db.update(TABLE_CONTACT, values, KEY_NAME + " =?", args);
    }

    // Update brojaca novih poruka
    public void updateContactCounter(Contact contact) {
        SQLiteDatabase db = this.getWritableDatabase();

        String[] args = {contact.getContact()};
        ContentValues values = new ContentValues();
        values.put(KEY_COUNTER, contact.getCounter());
        // updating row
        db.update(TABLE_CONTACT, values, KEY_NAME + " =?", args);
    }

    // Brisi odredjeni kontakt
    public void deleteContact(Contact contact) {
        SQLiteDatabase db = this.getWritableDatabase();
        String[] args = {contact.getContact()};
        db.delete(TABLE_CONTACT, KEY_NAME + " =?", args);
        db.close();
    }

    // Brisi sve poruke za odredjenog korisnika (ID)
    public void deleteMessages(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        String[] args = {String.valueOf(id)};
        db.delete(TABLE_MESSAGE, KEY_CONTACT_ID + " =?", args);
        db.close();
    }


    // Dodaj novu poruku
    public void addMessage(Message message, long id) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_MESSAGE_TEXT, message.getMessage());
        values.put(KEY_IS_ME, message.isMe());
        values.put(KEY_DATE, message.getMsgDate());
        values.put(KEY_CONTACT_ID, id);

        db.insert(TABLE_MESSAGE, null, values);
    }

    // Povuci sve poruke
    public ArrayList<Message> getAllMessages(long id) {
        ArrayList<Message> messages = new ArrayList<Message>();

        String[] args = {String.valueOf(id)};
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_MESSAGE, null, KEY_CONTACT_ID + " =?", args, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                Message message = new Message();
                message.setMessage((cursor.getString(cursor.getColumnIndex(KEY_MESSAGE_TEXT))));
                message.setMe(cursor.getInt(cursor.getColumnIndex(KEY_IS_ME)) != 0);
                message.setMsgDate(cursor.getString(cursor.getColumnIndex(KEY_DATE)));

                messages.add(message);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return messages;
    }
}
