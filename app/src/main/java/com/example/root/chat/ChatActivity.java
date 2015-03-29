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
import android.widget.ImageView;
import android.widget.ListView;

import com.beardedhen.androidbootstrap.BootstrapButton;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

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

    private int notificationID = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);

        // Toolbar
        toolbar = (Toolbar) findViewById(R.id.appBar);
        setSupportActionBar(toolbar);

        /**
         * Povratak na parent activity
         */
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        newMessage = (EditText) findViewById(R.id.newMessage);
        sendMessage = (Button) findViewById(R.id.sendBtn);
        listMessages = (ListView) findViewById(R.id.listChat);

        /**
         * Izvuci ID koji je parent aktivnost proslijedila
         * Izvuci korisnika na osnovu dobijenog ID
         * Postavi title na toolbaru
         */
        contactId = getIntent().getLongExtra("contact", 0);
        contact = helper.getContact(contactId);

        getSupportActionBar().setTitle(contact.getContact());
        Log.d("imageuri", String.valueOf(contact.getImageUri()));

        /**
         * Izvuci uri slike kontakta
         * Pretvori drawable u Bitmap i nazad u drawable kako bi promijenio veliicinu
         */
        Uri uri = contact.getImageUri();
        Bitmap bitmap = ((BitmapDrawable) getResources().getDrawable(R.drawable.ic_contact_picture)).getBitmap();
        final Drawable d = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(bitmap, 120, 120, true));

        /**
         * Picasso loader -
         * Za uspjesnu operaciju postavi logo na toolbaru korisnikovu sliku
         * Za neuspjesnu operaciju i preloading postavi za logo default sliku
         */
        Target target = new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                Drawable drawable = new BitmapDrawable(getResources(), bitmap);
                toolbar.setLogo(drawable);
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {
                Bitmap bitmap = ((BitmapDrawable) errorDrawable).getBitmap();
                Drawable d = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(bitmap, 120, 120, true));
                toolbar.setLogo(d);
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
                toolbar.setLogo(d);
            }
        };


        /**
         * U slucaju da ne pronadje uri korisnika (npr. novi korisnik) postavi default sliku za logo
         * U suprotnom neka pokusa Picasso da povuce sliku
         */
        if (uri == null) {
            toolbar.setLogo(d);
        } else {
            Picasso.with(getApplicationContext()).load(uri).resize(120, 120).placeholder(d).error(d).into(target);
        }


        /**
         * Kad se upali ovaj activity brojac neprocitanih poruka se resetuje
         * Unos u bazu novih vrijednosti brojaca i datuma poruke
         */
        contact.setCounter(0);
        helper.updateContactCounter(contact);

        isMe = true;

        /**
         * Povuci sve poruke iz baze za odredjeni id korisnika
         * Proslijedi listu poruka adapteru
         */
        messages = helper.getAllMessages(contactId);
        adapter = new ChatAdapter(this, messages);
        listMessages.setAdapter(adapter);

        /**
         * Send message click event
         */
        sendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /**
                 * Provjera jel pravilan unos poruke
                 * Salje poruku
                 * Osvjezi listu poruka
                 * Sakrij tastaturu
                 * Postavi da se prikazuje kraj liste
                 */
                if ((newMessage.getText().toString().trim().length() > 0) && (newMessage.getText().toString().trim().length() > 0)) {

                    sendMessage();
                    refreshListView(messages);
                    hideSoftKeyboard(ChatActivity.this, v);
                    listMessages.setSelection(messages.size() - 1);
                }
            }
        });

        listMessages.setSelection(messages.size() - 1);

        // Nema granice izmedju itema liste
        listMessages.setDivider(null);
        Log.d("log", contact.getContact() + contact.getCounter());

    }

    /**
     * Funkcija za slanje poruke -
     * Kreira novu poruku i ubacuje u bazu
     * Provjera jel primljena ili poslana poruka
     * @return
     */
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

            /**
             * TODO
             *
             * Servici i Broadcast Receiver + Notification
             *
             * Kad primis Selmanov objekat izvuci iz baze objekat Contact (konverzaciju) na osnovu broja kontakta
             * Postavi getterima nove vrijednosti za Contact objekt: datum posljednje primljene poruke, counter
             * Dodaj novu poruku sa odgovarajucim parametrima u bazu za gore dobijenog objekta Contact
             * Updatuj ostale podatke u bazi
             * Refresh lista
             *
             * TODO
             * U slucaju da nema objekta Contact u bazi, napravi novi objekat i dodaj u bazu
             * Updatuj objekte contact i message u bazi
             * Refresh lista
             * TODO
             * Razlikovanje novih poruka od novih brojeva ili postojecih brojeva u imeniku
             * Prikaz u listi imena ili broja
             */

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
        newMessage.setText("");

        return true;
    }

    /**
     * Funkcija za skrivanje tastature nakon slanja poruke
     * @param activity
     * @param view
     */
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

    /**
     * Funkcija za prikazivanje notifikacije kad stigne nova poruka
     * @param message
     */
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

    /**
     * Funkcija za osvjezavanje liste poruka
     * @param messages
     */
    public void refreshListView(ArrayList<Message> messages) {
        adapter.clear();
        adapter.addAll(messages);
        adapter.notifyDataSetChanged();
    }
}