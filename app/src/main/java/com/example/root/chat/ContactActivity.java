package com.example.root.chat;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.ActionMode;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;


public class ContactActivity extends ActionBarActivity {

    private ListView contactList;
    private ContactAdapter adapter;
    private ArrayList<Contact> contacts;
    private TextView noConversations;

    //Database object
    DatabaseHelper helper = new DatabaseHelper(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("log4", "onCreate");
        setContentView(R.layout.activity_contact);

        // Povuci sve kontakte u baze i popuni niz contacts
        contacts = helper.getAllContacts();

        contactList = (ListView) findViewById(R.id.contactList);
        noConversations = (TextView) findViewById(R.id.noConversations);

        checkConversations();

        //custom adapter
        adapter = new ContactAdapter(this, contacts);
        contactList.setAdapter(adapter);

        // Klik na item(korisnika) u listi
        contactList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                // Pokreni novu aktivnost - ChatActivity
                Intent intent = new Intent(ContactActivity.this, ChatActivity.class);

                //Radi sa kontaktom koji je kliknut
                Contact contact = contacts.get(position);

                // Proslijedi Chat aktivnosti id kliknutog kontakta - povlaci se id iz baze
                intent.putExtra("contact", helper.getContactId(contact));
                startActivityForResult(intent, 0);
                Log.d("log", contacts.get(position).getContact() + " is clicked " + contacts.get(position).getCounter());
            }
        });

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
                    for (int i = adapter.getCount() -1; i >= 0; i--) {
                        if (contactList.isItemChecked(i)) {

                            // Odaberi oznacene iteme(korisnike) u listi
                            // Izvuci id oznacenog korisnika - potrebno za brisanje njemu
                            // pridruzenih poruka
                            long id = helper.getContactId(adapter.getItem(i));

                            // Brisi korisnika iz baze
                            helper.deleteContact(adapter.getItem(i));
                            // Brisi sve poruke za korisnika iz baze
                            helper.deleteMessages(id);
                        }
                    }
                    mode.finish();
                    // Refresh niza koji cuva kontakte i liste koja ih prikazuje
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
        //Osvjezi listu nakon povratka iz Chat aktivnosti u Kontakt aktivnost
        adapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_contact, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pokretanje dialoga za dodavanje novih korisnika
        Dialog dialog = onCreateDialog(10);
        dialog.show();
        return true;
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        final View view = inflater.inflate(R.layout.dialog_contact, null);
        final EditText editText = (EditText) view.findViewById(R.id.username);


        builder.setView(view)
                .setPositiveButton("New Message", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        boolean isExist = false;
                        //Provjerava da li korisnik sa odredjenim imenom vec postoji
                        for (Contact c: contacts) {
                            if (c.getContact().toString().equals(editText.getText().toString())) {
                                isExist = true;
                                dialog.cancel();
                            }
                        }
                        if (!isExist) {
                            //Novi objekat korisnik
                            Contact contact = new Contact(editText.getText().toString(), 0);

                            // Dodavanje korisnika u bazu
                            long contactId = helper.addContact(contact);

                            // Osvjezi listu korisnika
                            contacts = helper.getAllContacts();
                            refreshListView(contacts);

                            // Pokreni Chat aktivnost za korisnika i proslijedi njegov ID
                            Intent intent = new Intent(ContactActivity.this, ChatActivity.class);
                            intent.putExtra("contact", contactId);
                            startActivityForResult(intent, 0);
                        }
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        return builder.create();
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

    // Funkcija za osvjezavanje liste korisnika - Prazni adapter i popunjava ga osvjezenim nizom korisnika
    public void refreshListView(ArrayList<Contact> contacts) {
        adapter.clear();
        adapter.addAll(contacts);
        adapter.notifyDataSetChanged();
    }

    public void checkConversations () {
        if (contacts.isEmpty()) {
            contactList.setVisibility(View.GONE);
            noConversations.setVisibility(View.VISIBLE);
        } else  {
            noConversations.setVisibility(View.GONE);
        }
    }
}
