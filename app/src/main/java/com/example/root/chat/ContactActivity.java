package com.example.root.chat;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ActionMode;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;


public class ContactActivity extends ActionBarActivity implements NewDialog.Communicator {

    /**
     * UI elementi
     */
    private ListView contactList;
    private TextView noConversations;
    private Toolbar toolbar;

    /**
     * adapter i lista kontakta
     */
    private ContactAdapter adapter;
    private ArrayList<Contact> contacts;

    /**
     * DB objekat
     */
    DatabaseHelper helper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("log4", "onCreate");
        setContentView(R.layout.activity_contacts);

        helper = new DatabaseHelper(this);

        /**
         * Toolbar
         * enabled Home button
         */
        toolbar = (Toolbar) findViewById(R.id.appBar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Povuci sve kontakte iz baze i popuni niz contacts
        /**
         * Povuci sve konverzacije (kontakte) iz baze
         * Popuni niz konverzacija (kontakta)
         */
        contacts = helper.getAllContacts();

        contactList = (ListView) findViewById(R.id.contactList);
        noConversations = (TextView) findViewById(R.id.noConversations);

        /**
         * Ako nema konverzacija prikazi tekst
         */
        checkConversations();

        /**
         * Custom list adapter
         */
        adapter = new ContactAdapter(this, contacts);
        contactList.setAdapter(adapter);

        /**
         * Click na item u listi (konverzaciju
         */
        contactList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                /**
                 * Pokreni novu aktivnost - ChatActivity
                 * Radi sa kontaktom koji je kliknut i povuci njegov id iz baze
                 * Proslijedi ChatActivity id kliknutog korisnika
                 */
                Intent intent = new Intent(ContactActivity.this, ChatActivity.class);
                Contact contact = contacts.get(position);
                intent.putExtra("contact", helper.getContactId(contact));
                startActivityForResult(intent, 0);

                Log.d("log", contacts.get(position).getContact() + " is clicked " + contacts.get(position).getCounter());
            }
        });

        /**
         * Floating context menu ili contextual u zavisnosti od verzije
         */
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            // Use floating context menus on Froyo and Gingerbread
            registerForContextMenu(contactList);
        } else {
            // Use contextual action bar on Honeycomb and higher
            // MultiChoice mode
            contactList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
            contactList.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
                @Override
                public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {

                }

                @Override
                public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                    getMenuInflater().inflate(R.menu.context_menu, menu);
                    return true;
                }

                @Override
                public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                    return false;
                }

                @Override
                public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                    for (int i = adapter.getCount() - 1; i >= 0; i--) {
                        if (contactList.isItemChecked(i)) {

                            /**
                             * Odaberi oznacene iteme(korisnike) u listi
                             * Izvuci id oznacenog korisnika - potrebno za brisanje njemu
                             * pridruzenih poruka
                             * Brise korisnika iz baze
                             * Brise sve poruke za korisnika iz baze
                             */
                            long id = helper.getContactId(adapter.getItem(i));
                            helper.deleteContact(adapter.getItem(i));
                            helper.deleteMessages(id);
                        }
                    }
                    mode.finish();

                    /**
                     * Refresh niza koji cuva kontakte i liste koja ih prikazuje
                     */
                    contacts = helper.getAllContacts();
                    checkConversations();
                    refreshListView(contacts);

                    return true;
                }

                @Override
                public void onDestroyActionMode(ActionMode mode) {

                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        /**
         * Osvjezi listu nakon povratka iz Chat aktivnosti u Kontakt aktivnost
         */
        adapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /**
         * Inflate the menu; this adds items to the action bar if it is present.
         */
        getMenuInflater().inflate(R.menu.menu_contact, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        /**
         * Pokretanje dialoga za dodavanje novih korisnika
         */
        int itemId = item.getItemId();
        if (itemId == R.id.menu_item_new_contact) {
            NewDialog newDialog = new NewDialog();
            newDialog.show(getFragmentManager(), "NewDialog");
        }
        return false;
    }


    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        getMenuInflater().inflate(R.menu.context_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int position = info.position;
        Contact contact = adapter.getItem(position);

        helper.deleteContact(contact);
        contacts = helper.getAllContacts();
        adapter.notifyDataSetChanged();
        return true;
    }

    /**
     * Funkcija za osvjezavanje liste korisnika
     * Prazni adapter i popunjava ga osvjezenim nizom korisnika
     * @param contacts
     */
    public void refreshListView(ArrayList<Contact> contacts) {
        adapter.clear();
        adapter.addAll(contacts);
        adapter.notifyDataSetChanged();
    }

    public void checkConversations() {
        if (contacts.isEmpty()) {
            contactList.setVisibility(View.GONE);
            noConversations.setVisibility(View.VISIBLE);
        } else {
            noConversations.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDialogMessage(ArrayList<Contact> contacts) {
        refreshListView(contacts);
    }
}
