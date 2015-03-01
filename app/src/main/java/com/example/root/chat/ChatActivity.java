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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

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
    private Toolbar toolbar;
    private DisplayImageOptions options;

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
        sendMessage = (Button) findViewById(R.id.sendBtn);
        listMessages = (ListView) findViewById(R.id.listChat);

        //Izvuci ID koji je parent aktivnost proslijedila
        contactId = getIntent().getLongExtra("contact", 0);

        // Izvuci korisnika na osnovu dobijenog ID
        contact = helper.getContact(contactId);

        Log.d("imageuri", contact.getContact());


        // Postavi contact photo
        ContactsContent entry = new ContactsContent(getContentResolver());
        Uri uri = entry.fetchContactImageUri(contact.getContact());

        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .cacheOnDisc(true).cacheInMemory(true)
                .imageScaleType(ImageScaleType.EXACTLY).showImageOnLoading(R.drawable.ic_contact_picture)
                .displayer(new FadeInBitmapDisplayer(300)).build();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
                getApplicationContext())
                .defaultDisplayImageOptions(defaultOptions)
                .memoryCache(new WeakMemoryCache())
                .discCacheSize(100 * 1024 * 1024).build();

        ImageLoader.getInstance().init(config);

        if ("null".equals(String.valueOf(uri))) {
            getSupportActionBar().setIcon(R.drawable.ic_contact_picture);
            getSupportActionBar().setTitle(contact.getContact());
        } else {
            options = new DisplayImageOptions.Builder().cacheInMemory(true)
                    .cacheOnDisc(true).resetViewBeforeLoading(true).build();
            ImageLoader.getInstance().loadImage(String.valueOf(uri), options, new SimpleImageLoadingListener() {
               /* @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    if (loadedImage != null) {
                        Drawable drawable = new BitmapDrawable(getResources(), loadedImage);
                        //getSupportActionBar().setLogo(drawable);
                        toolbar.setLogo(drawable);
                        getSupportActionBar().setTitle(contact.getContact());
                    } else {
                        getSupportActionBar().setIcon(R.drawable.ic_contact_picture);
                        getSupportActionBar().setTitle(contact.getContact());
                    }
                }*/

                @Override
                public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                    super.onLoadingFailed(imageUri, view, failReason);
                    Log.d("loading", "loading failed");
                    getSupportActionBar().setIcon(R.drawable.ic_contact_picture);
                    getSupportActionBar().setTitle(contact.getContact());
                }

                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    super.onLoadingComplete(imageUri, view, loadedImage);
                    if (loadedImage != null) {
                        Drawable drawable = new BitmapDrawable(getResources(), loadedImage);
                        toolbar.setLogo(drawable);
                        getSupportActionBar().setTitle(contact.getContact());
                    } else {
                        getSupportActionBar().setIcon(R.drawable.ic_contact_picture);
                        getSupportActionBar().setTitle(contact.getContact());
                    }
                }
            });
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
                setSmallIcon(R.drawable.message30).
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