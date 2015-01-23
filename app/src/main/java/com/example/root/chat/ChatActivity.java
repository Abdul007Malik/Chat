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

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        newMessage = (EditText) findViewById(R.id.newMessage);
        sendMessage = (Button) findViewById(R.id.sendBtn);
        listMessages = (ListView) findViewById(R.id.listChat);
        contactId = getIntent().getLongExtra("contact", 0);
        contact = helper.getContact(contactId);
        setTitle(contact.getContact());

        contact.setCounter(0);
        helper.updateContactCounterDate(contact);
        isMe = true;

        messages = helper.getAllMessages(contactId);
        adapter = new ChatAdapter(this, messages);

        sendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (newMessage.getText().toString() != null && newMessage.getText().toString().length() >0) {

                    sendMessage();
                    listMessages.setAdapter(adapter);
                    refreshListView(messages);
                    hideSoftKeyboard(ChatActivity.this, v);
                    listMessages.setSelection(messages.size()-1);
                }
            }
        });

        listMessages.setAdapter(adapter);
        listMessages.setSelection(messages.size()-1);
        listMessages.setDivider(null);
        Log.d("log", contact.getContact() + contact.getCounter());

    }
    private boolean sendMessage() {
        message = new Message(newMessage.getText().toString(), isMe);
        helper.addMessage(message, contactId);
        messages = helper.getAllMessages(contactId);
        for (Message m: messages) {
            Log.d("messages: ", m.getMessage());
        }
        if (!isMe) {
            int counter = contact.getCounter() + 1;
            contact.setCounter(counter);
            displayNotification(message);
            contact.setMsgDate(message.getMsgDate());
            Log.d("msgDate", contact.getMsgDate() + "  " + String.valueOf(contact.getCounter()));
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
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    protected void displayNotification(Message message) {
        Log.i("Start", "notification");
        Intent resultIntent = new Intent(this, ContactActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(ContactActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

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

    public void refreshListView(ArrayList<Message> messages) {
        adapter.clear();
        adapter.addAll(messages);
        adapter.notifyDataSetChanged();
    }
}