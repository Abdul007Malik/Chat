package com.example.root.chat;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

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
        final EditText editText = (EditText) view.findViewById(R.id.username);

        builder.setView(view)
                .setPositiveButton("New Message", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        boolean isExist = false;

                        //Provjerava da li korisnik sa odredjenim imenom vec postoji
                        for (Contact c: helper.getAllContacts()) {
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
                            ArrayList<Contact> contacts = helper.getAllContacts();
                            //communicator.onDialogMessage(contacts);

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
