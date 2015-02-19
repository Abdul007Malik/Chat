package com.example.root.chat;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * Created by root on 1/19/15.
 */
public class ContactAdapter extends ArrayAdapter<Contact> {

    private Context context;
    private Contact contactObj;
    private ArrayList<Contact> contacts;
    private InputStream inputStream;

    public ContactAdapter(Context c, ArrayList<Contact> contacts) {
        super(c, R.layout.contact_row, contacts);
        this.context = c;
        this.contacts = contacts;

    }

    class MyViewHolder {
        TextView contact;
        TextView counter;
        TextView msgDate;
        ImageView avatar;

        public MyViewHolder(View view) {
            contact = (TextView) view.findViewById(R.id.contact);
            counter = (TextView) view.findViewById(R.id.counter);
            avatar = (ImageView) view.findViewById(R.id.avatar);
            msgDate = (TextView) view.findViewById(R.id.msgDate);
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        MyViewHolder holder = null;
        View row = convertView;

        if (row == null) {
            row = LayoutInflater.from(context).inflate(R.layout.contact_row, parent, false);
            holder = new MyViewHolder(row);
            row.setTag(holder);
        } else {
            holder = (MyViewHolder) row.getTag();
        }

        contactObj = getItem(position);
        String contactName = contactObj.getContact();
        holder.contact.setText(contactName);
        int counter = contactObj.getCounter();

        // Na osnovu broja novih poruka postavlja odredjeni tekst
        if (counter > 1) {
            String textCounter = String.valueOf(counter);
            holder.counter.setText(textCounter + " new messages.");
        } else if (counter == 0) {
            String textCounter = String.valueOf(counter);
            holder.counter.setText("");
        } else {
            String textCounter = String.valueOf(counter);
            holder.counter.setText(textCounter + " new message.");
        }

        // Avatar korisnika
        String stringUri = contactObj.getImageUri();
        Uri imageUri = Uri.parse(stringUri);
        Log.d("uri", String.valueOf(imageUri));

        if ("null".equals(stringUri)) {
            holder.avatar.setImageBitmap(BitmapFactory.decodeResource(getContext().getResources(), R.drawable.ic_contact_picture));
        } else {
            new ImageLoader(imageUri, holder).execute();
        }



        // Datum posljednje primljene poruke
        holder.msgDate.setText(contactObj.getMsgDate());
        return row;
    }

    private class ImageLoader extends AsyncTask<Void, Void, Bitmap> {

        private Uri imageUri;
        private MyViewHolder holder;

        public ImageLoader(Uri imageUri, MyViewHolder holder) {
            this.imageUri = imageUri;
            this.holder = holder;
        }

        @Override
        protected Bitmap doInBackground(Void... params) {
            inputStream = ContactsContract.Contacts.openContactPhotoInputStream(getContext().getContentResolver(), imageUri);
            BufferedInputStream buff = new BufferedInputStream(inputStream);
            Bitmap bitmap = BitmapFactory.decodeStream(buff);
            if (bitmap == null) {
                bitmap = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.ic_contact_picture);
            }

            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            holder.avatar.setImageBitmap(bitmap);
        }
    }
}
