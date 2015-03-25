package com.example.root.chat;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by root on 2/4/15.
 */
public class NewDialog extends DialogFragment {

    private ArrayList<String> contactsNameNumber = new ArrayList<String>();
    private Communicator communicator;
    ContactsContent contactsContent;
    DatabaseHelper helper;
    private static Handler handler;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        helper = new DatabaseHelper(getActivity());
        communicator = (Communicator) getActivity();

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_contact, null);

        final AutoCompleteTextView editText = (AutoCompleteTextView) view.findViewById(R.id.at_Contacts);

        /**
         * Async task za rad sa kontaktima
         * Povlaci imena i brojeve telefona
         */

        new ContactsTaskDialogNames().execute();

        /**
         * AutoComplete box koji radi sa imenima korisnika
         */
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 0:
                        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, contactsNameNumber);
                        editText.setThreshold(1);
                        editText.setAdapter(adapter);
                        break;
                    default:
                        break;
                }
            }
        };

        editText.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String contact = (String) parent.getAdapter().getItem(position);
                String[] parts = splitContact(contact);
                String[] parts2 = splitContact2(parts[1]);
                //postavi ime
                editText.setText(parts[0]);
            }
        });

        /**
         * Dialog sa pozitivnim i negativnim button-om
         */
        builder.setView(view)
                .setPositiveButton("New Message", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        boolean isExist = false;

                        /**
                         * Provjerava da li korisnik sa odredjenim imenom vec postoji
                         */
                        for (Contact c : helper.getAllContacts()) {
                            if (c.getContact().toString().equals(editText.getText().toString())) {
                                isExist = true;
                                dialog.cancel();
                                String name = editText.getText().toString();
                                Toast.makeText(getActivity(), "Conversation with " + name + " already exists.", Toast.LENGTH_SHORT).show();
                            }
                        }
                        if (!isExist) {

                            /**
                             * Novi objekat korisnik
                             * Povlaci sliku korisnika iz liste kontakta
                             * Povlaci broj korisnika iz liste kontakta
                             */
                            Contact contact = new Contact(editText.getText().toString(), 0);
                            Uri imageUri = contactsContent.fetchContactImageUri(editText.getText().toString());
                            Log.d("uriE", String.valueOf(imageUri));
                            contact.setImageUri(imageUri);

                            String phone = contactsContent.fetchContactPhoneNumber(editText.getText().toString());
                            contact.setPhone(phone);

                            /**
                             * Dodaje korisnika u bazu
                             * Osvjezava listu korisnika koristeci interface communicator
                             */
                            long contactId = helper.addContact(contact);
                            ArrayList<Contact> contacts = helper.getAllContacts();
                            communicator.onDialogMessage(contacts);

                            /**
                             * Pokreni Chat aktivnost za korisnika i proslijedi njegov ID
                             */
                            Intent intent = new Intent(getActivity().getApplicationContext(), ChatActivity.class);
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

    /**
     * Async task - popunjava listu imena kontakta
     */
    private class ContactsTaskDialogNames extends AsyncTask<Void, Void, ArrayList<ContactsContent.ContactEntry>> {


        @Override
        protected void onPreExecute() {
            contactsContent = new ContactsContent(getActivity().getContentResolver());
        }

        @Override
        protected ArrayList<ContactsContent.ContactEntry> doInBackground(Void... params) {
            /**
             * Povuci sva imena kontakta
             */
            ArrayList<ContactsContent.ContactEntry> contactEntries = contactsContent.getAllContactNames();
            return contactEntries;
        }

        @Override
        protected void onPostExecute(ArrayList<ContactsContent.ContactEntry> contactEntries) {
            for (int i = 0; i < contactEntries.size(); i++) {
                /**
                 * Iz liste kontakta povuci ime i broj i dodaj u konacnu listu
                 */
                ContactsContent.ContactEntry entry = contactEntries.get(i);
                String string = entry.getName() + "\n " + entry.getNumber() + " (" + getActivity().getApplicationContext().getString(entry.getType()) + ")";
                contactsNameNumber.add(string);
                Log.d("imena", string);
            }
            handler.sendEmptyMessage(0);
        }
    }

    public interface Communicator {
        public void onDialogMessage(ArrayList<Contact> contacts);
    }

    /**
     * Funkcija za dijeljenje stringa na ime i broj
     * @param contact
     * @return
     */
    private String[] splitContact(String contact) {
        String[] parts = contact.split("\n ");
        return parts;
    }

    private String[] splitContact2(String contact) {
        String[] parts = contact.split(" ");
        return parts;
    }
}
