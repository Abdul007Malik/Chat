package com.example.root.chat;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ListView;

import com.beardedhen.androidbootstrap.BootstrapButton;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;


public class ChatActivity extends ActionBarActivity {

    private ListView listMessages;
    private EditText newMessage;
    private BootstrapButton sendMessage;
    private Message message;
    private boolean isMe;
    private Contact contact;
    private ArrayList<Message> messages;
    private ChatAdapter adapter;
    DatabaseHelper helper = new DatabaseHelper(this);
    private long contactId;
    private Toolbar toolbar;

    private int notificationID = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);

        toolbar = (Toolbar) findViewById(R.id.appBar);
        setSupportActionBar(toolbar);

        // Povratak na parent aktivnost
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        newMessage = (EditText) findViewById(R.id.newMessage);
        sendMessage = (BootstrapButton) findViewById(R.id.sendBtn);
        listMessages = (ListView) findViewById(R.id.listChat);

        //Izvuci ID koji je parent aktivnost proslijedila
        contactId = getIntent().getLongExtra("contact", 0);

        // Izvuci korisnika na osnovu dobijenog ID
        contact = helper.getContact(contactId);

        Log.d("imageuri", String.valueOf(contact.getImageUri()));
        getSupportActionBar().setTitle(contact.getContact());


        // Postavi contact photo
        Uri uri = contact.getImageUri();

        Target target = new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                Drawable drawable = new BitmapDrawable(getResources(), bitmap);
                getSupportActionBar().setIcon(drawable);
                getSupportActionBar().setTitle(contact.getContact());
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {
                Bitmap bitmap = ((BitmapDrawable) errorDrawable).getBitmap();
                Drawable d = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(bitmap, 120, 120, true));
                getSupportActionBar().setIcon(d);

            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
                getSupportActionBar().setIcon(R.drawable.ic_contact_picture);
            }
        };

        if (uri == null) {
            Bitmap bitmap = ((BitmapDrawable) getResources().getDrawable(R.drawable.ic_contact_picture)).getBitmap();
            Drawable d = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(bitmap, 120, 120, true));
            getSupportActionBar().setIcon(d);
        } else {
            Picasso.with(getApplicationContext()).load(uri).resize(120, 120).placeholder(R.drawable.ic_contact_picture).error(R.drawable.ic_contact_picture).into(target);
        }


        // Resetuj brojac novih poruka
        contact.setCounter(0);

        // Update brojaca i datuma poruke u bazi
        helper.updateContactCounter(contact);

        isMe = true;

        // Povuci sve poruke iz baze za odredjeni korisnicki ID i strpaj u niz
        messages = helper.getAllMessages(contactId);

        // Custom adapter za listu poruka
        adapter = new ChatAdapter(this, messages);
        listMessages.setAdapter(adapter);

        sendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Provjerava unos poruke
                if (newMessage.getText().toString() != null && newMessage.getText().toString().length() > 0) {

                    // Posalji poruku
                    sendMessage();

                    // Osvjezi listu poruka
                    refreshListView(messages);

                    // Sakrij tastaturu
                    hideSoftKeyboard(ChatActivity.this, v);


                    listMessages.setSelection(messages.size() - 1);
                }
            }
        });

        // Postavi da se prikazuje kraj liste
        listMessages.setSelection(messages.size() - 1);

        // Nema granice izmedju itema liste
        listMessages.setDivider(null);
        Log.d("log", contact.getContact() + contact.getCounter());

    }

    //Funkcija za slanje poruke
    private boolean sendMessage() {

        // Kreira novu poruku
        message = new Message(newMessage.getText().toString(), isMe);

        // Ubaci u bazu
        helper.addMessage(message, contactId);

        // Povuci sve poruke iz baze
        messages = helper.getAllMessages(contactId);

        for (Message m : messages) {
            Log.d("messages: ", m.getMessage());
        }

        // Primljena poruka
        if (!isMe) {

            // Povecaj broj primljenih poruka za 1
            int counter = contact.getCounter() + 1;
            contact.setCounter(counter);

            // Prikazi notifikaciju
            displayNotification(message);


            contact.setMsgDate(message.getMsgDateLong());
            Log.d("msgDate", contact.getMsgDate() + "  " + String.valueOf(contact.getCounter()));

            // Update baze sa novim brojacem i datumom
            helper.updateContactCounterDate(contact);
        }
        isMe = !isMe;
        /*String msgDate = message.getStringDate();
        Intent intent = new Intent();
        intent.putExtra("msgDate", msgDate);
        setResult(RESULT_OK, intent);*/
        newMessage.setText("");

        return true;
    }

    // Funkcija za skrivanje tastature nakon slanja poruke
    public static void hideSoftKeyboard(Activity activity, View view) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getApplicationWindowToken(), 0);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_messages, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Povratak na parent aktivnost
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // Funkcija za prikazivanje notifikacije kad stigne nova poruka
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    protected void displayNotification(Message message) {
        Log.i("Start", "notification");

        // Klik na notifikaciju ce pokrenuti aktivnost Contact
        Intent resultIntent = new Intent(this, ContactActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(ContactActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        // Notifikacija i njen izgled
        Notification notification = new Notification.Builder(this).
                setContentTitle("New Message from " + contact.getContact().toString()).
                setContentText(message.getMessage().toString()).
                setTicker("New Message Alert!").
                setAutoCancel(true).
                setSound(Uri.parse("android.resource://"
                        + getApplicationContext().getPackageName() + "/" + R.raw.springtime)).
                setSmallIcon(R.drawable.message30).
                setContentIntent(resultPendingIntent).build();

        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        //notification.defaults |= Notification.DEFAULT_SOUND;
        notification.defaults |= Notification.DEFAULT_VIBRATE;
        mNotificationManager.notify(notificationID, notification);
    }

    // Funkcija za osvjezavanje liste poruka
    public void refreshListView(ArrayList<Message> messages) {
        adapter.clear();
        adapter.addAll(messages);
        adapter.notifyDataSetChanged();
    }
}