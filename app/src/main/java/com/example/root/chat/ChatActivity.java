package com.example.root.chat;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;


public class ChatActivity extends ActionBarActivity {

    private ListView listMessages;
    private EditText newMessage;
    private Button sendMessage;
    private Message message;
    private boolean isMe;
    private Contact contact;
    private ArrayList<Message> messages;
    private ChatAdapter adapter;
    DatabaseHelper helper = new DatabaseHelper(this);
    private long contactId;

    private int notificationID = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Povratak na parent aktivnost
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        newMessage = (EditText) findViewById(R.id.newMessage);
        sendMessage = (Button) findViewById(R.id.sendBtn);
        listMessages = (ListView) findViewById(R.id.listChat);

        //Izvuci ID koji je parent aktivnost proslijedila
        contactId = getIntent().getLongExtra("contact", 0);

        // Izvuci korisnika na osnovu dobijenog ID
        contact = helper.getContact(contactId);

        // Postavi title
        setTitle(contact.getContact());

        // Resetuj brojac novih poruka
        contact.setCounter(0);

        // Update brojaca i datuma poruke u bazi
        helper.updateContactCounterDate(contact);
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
                if (newMessage.getText().toString() != null && newMessage.getText().toString().length() >0) {

                    // Posalji poruku
                    sendMessage();

                    // Osvjezi listu poruka
                    refreshListView(messages);

                    // Sakrij tastaturu
                    hideSoftKeyboard(ChatActivity.this, v);


                    listMessages.setSelection(messages.size()-1);
                }
            }
        });

        // Postavi da se prikazuje kraj liste
        listMessages.setSelection(messages.size()-1);

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

        for (Message m: messages) {
            Log.d("messages: ", m.getMessage());
        }

        // Primljena poruka
        if (!isMe) {

            // Povecaj broj primljenih poruka za 1
            int counter = contact.getCounter() + 1;
            contact.setCounter(counter);

            // Prikazi notifikaciju
            displayNotification(message);


            contact.setMsgDate(message.getMsgDate());
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
    public static void hideSoftKeyboard (Activity activity, View view)
    {
        InputMethodManager imm = (InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getApplicationWindowToken(), 0);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
                setSmallIcon(R.drawable.notification_metro).
                setContentIntent(resultPendingIntent).build();

        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        mNotificationManager.notify(notificationID, notification);
    }

    // Funkcija za osvjezavanje liste poruka
    public void refreshListView(ArrayList<Message> messages) {
        adapter.clear();
        adapter.addAll(messages);
        adapter.notifyDataSetChanged();
    }
}