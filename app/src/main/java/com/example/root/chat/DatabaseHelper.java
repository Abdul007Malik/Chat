package com.example.root.chat;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by root on 1/22/15.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    // Logcat tag
    private static final String LOG = "DatabaseHelper";

    // Database Version
    private static final int DATABASE_VERSION = 3;

    // Database Name
    private static final String DATABASE_NAME = "chatDatabase";

    // Table Names
    private static final String TABLE_CONTACT = "contacts";
    private static final String TABLE_MESSAGE = "messages";
    private static final String TABLE_CONTACT_MESSAGE = "contact_messages";

    // Common column names
    private static final String KEY_ID = "id";

    // CONTACT Table - column names
    private static final String KEY_NAME = "name";
    private static final String KEY_COUNTER = "counter";
    private static final String KEY_LAST_MSGDATE = "last_msg_date";

    // MESSAGE Table - column names
    private static final String KEY_TAG_TEXT = "text";
    private static final String KEY_IS_ME = "is_me";
    private static final String KEY_MSGDATE = "msg_date";

    // CONTACT-MESSAGE Table - column names
    private static final String KEY_CONTACT_ID = "contact_id";
    private static final String KEY_MESSAGE_ID = "message_id";

    // Table Create Statements
    // CONTACT table
    private static final String CREATE_TABLE_CONTACT = "CREATE TABLE "
            + TABLE_CONTACT + "(" + KEY_ID + " INTEGER PRIMARY KEY," + KEY_NAME
            + " VARCHAR(255)," + KEY_COUNTER + " INTEGER," + KEY_LAST_MSGDATE
            + " VARCHAR(255)" + ")";

    // MESSAGE table
    private static final String CREATE_TABLE_MESSAGE = "CREATE TABLE " + TABLE_MESSAGE
            + "(" + KEY_ID + " INTEGER PRIMARY KEY," + KEY_TAG_TEXT + " VARCHAR(255),"
            + KEY_IS_ME + " BOOLEAN" + KEY_MSGDATE + " VARCHAR(255)" + ")";

    // CONTACT-MESSAGE table
    private static final String CREATE_TABLE_CONTACT_MESSAGE = "CREATE TABLE "
            + TABLE_CONTACT_MESSAGE + "(" + KEY_ID + " INTEGER PRIMARY KEY,"
            + KEY_CONTACT_ID + " INTEGER," + KEY_MESSAGE_ID + " INTEGER" + ")";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        // creating required tables
        db.execSQL(CREATE_TABLE_CONTACT);
        db.execSQL(CREATE_TABLE_MESSAGE);
        db.execSQL(CREATE_TABLE_CONTACT_MESSAGE);
        Log.d(LOG, "onCreate database called");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // on upgrade drop older tables
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONTACT);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MESSAGE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONTACT_MESSAGE);
        Log.d(LOG, "onUpdate database called");

        // create new tables
        onCreate(db);
    }

    // Insert new Contact
    public void createContact(Contact contact) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_NAME, contact.getContact());
        values.put(KEY_COUNTER, contact.getCounter());
        values.put(KEY_LAST_MSGDATE, contact.getMsgDate());

        // insert row
        long todo_id = db.insert(TABLE_CONTACT, null, values);
        db.close();
    }
}
