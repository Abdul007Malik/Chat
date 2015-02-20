package com.example.root.chat;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by root on 2/4/15.
 */
public class NewDialog extends DialogFragment {

    private Communicator communicator;
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final DatabaseHelper helper = new DatabaseHelper(getActivity());
        communicator = (Communicator) getActivity();

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_contact, null);

        final AutoCompleteTextView editText = (AutoCompleteTextView) view.findViewById(R.id.at_Contacts);
        final AutoCompleteTextView editText2 = (AutoCompleteTextView) view.findViewById(R.id.at_Contact_Number);

        // Rad sa bazom kontakata
        final ContactsContent contactsContent = new ContactsContent(getActivity().getContentResolver());

        // AutoComplete box koji radi sa imenima korisnika
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, contactsContent.getAllContactNames());
        editText.setThreshold(1);
        editText.setAdapter(adapter);

        ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, contactsContent.getAllContactNumbers());
        editText2.setThreshold(1);
        editText2.setAdapter(adapter2);

       /* ArrayList<String> names = contactsContent.getAllContactNames();
        ArrayList<String> numbers = contactsContent.getAllContactNumbers();
        ArrayList<Contact> contacts = new ArrayList<Contact>();
        for (int i = 0; i < names.size(); i++) {
            Contact contact = new Contact();
            contact.setContact(names.get(i));
            contact.setPhone(numbers.get(i));
            contacts.add(contact);
        }
        DialogAdapter dialogAdapter = new DialogAdapter(getActivity(), contacts);
        editText.setThreshold(1);
        editText.setAdapter(dialogAdapter);*/

        // Dialog sa pozitivnim i negativnim button-om
        builder.setView(view)
                .setPositiveButton("New Message", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        boolean isExist = false;

                        //Provjerava da li korisnik sa odredjenim imenom vec postoji
                        for (Contact c : helper.getAllContacts()) {
                            if (c.getContact().toString().equals(editText.getText().toString())) {
                                isExist = true;
                                dialog.cancel();
                                String name = editText.getText().toString();
                                Toast.makeText(getActivity(), "Conversation with " + name + " already exists.", Toast.LENGTH_SHORT).show();
                            }
                        }
                        if (!isExist) {

                            //Novi objekat korisnik
                            Contact contact = new Contact(editText.getText().toString(), 0);

                            // Povlaci sliku korisnika iz liste kontakta

                            Uri imageUri = contactsContent.fetchContactImageUri(editText.getText().toString());
                            Log.d("uriE", String.valueOf(imageUri));
                            contact.setImageUri(String.valueOf(imageUri));

                            // Dodavanje korisnika u bazu
                            long contactId = helper.addContact(contact);

                            // Osvjezi listu korisnika
                            ArrayList<Contact> contacts = helper.getAllContacts();
                            communicator.onDialogMessage(contacts);

                            // Pokreni Chat aktivnost za korisnika i proslijedi njegov ID
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

    public interface Communicator {
        public void onDialogMessage(ArrayList<Contact> contacts);
    }
}
