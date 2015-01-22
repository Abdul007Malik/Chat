package com.example.root.chat;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
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

import java.util.ArrayList;


public class ContactActivity extends ActionBarActivity {

    private ListView contactList;
    private ContactAdapter adapter;
    private ArrayList<Contact> contacts;
    DatabaseHelper helper = new DatabaseHelper(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("log4", "onCreate");
        setContentView(R.layout.activity_contact);

        contacts = helper.getAllContacts();

        contactList = (ListView) findViewById(R.id.contactList);
        adapter = new ContactAdapter(this, contacts);
        contactList.setAdapter(adapter);

        contactList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(ContactActivity.this, ChatActivity.class);
                Contact contact = contacts.get(position);
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
                    ContactBase contactBase = ContactBase.get(ContactActivity.this);
                    for (int i = adapter.getCount() -1; i >= 0; i--) {
                        if (contactList.isItemChecked(i)) {
                            helper.deleteContact(adapter.getItem(i));
                        }
                    }
                    mode.finish();
                    contacts = helper.getAllContacts();
                    adapter.notifyDataSetChanged();
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

        final ContactBase contactBase = ContactBase.get(ContactActivity.this);
        builder.setView(view)
                .setPositiveButton("New Message", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        boolean isExist = false;
                        for (Contact c: contactBase.getContacts()) {
                            if (c.getContact().toString().equals(editText.getText().toString())) {
                                isExist = true;
                                dialog.cancel();
                            }
                        }
                        if (!isExist) {
                            Contact contact = new Contact(editText.getText().toString(), 0);
                            long contactId = helper.addContact(contact);
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
}
