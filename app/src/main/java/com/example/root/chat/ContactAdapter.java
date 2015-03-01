package com.example.root.chat;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.beardedhen.androidbootstrap.BootstrapCircleThumbnail;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;

/**
 * Created by root on 1/19/15.
 */
public class ContactAdapter extends ArrayAdapter<Contact> {

    private Context context;
    private Contact contactObj;
    private ArrayList<Contact> contacts;


    public ContactAdapter(Context c, ArrayList<Contact> contacts) {
        super(c, R.layout.contact_row, contacts);
        this.context = c;
        this.contacts = contacts;

    }

    class MyViewHolder {
        TextView contact;
        TextView counter;
        TextView msgDate;
        BootstrapCircleThumbnail avatar;

        public MyViewHolder(View view) {
            contact = (TextView) view.findViewById(R.id.contact);
            counter = (TextView) view.findViewById(R.id.counter);
            avatar = (BootstrapCircleThumbnail) view.findViewById(R.id.thumbnailOneTest);
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
        String stringUri = String.valueOf(contactObj.getImageUri());
        Uri imageUri = Uri.parse(stringUri);
        Log.d("uri", String.valueOf(imageUri));
        final MyViewHolder finalHolder = holder;
        Target target = new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                finalHolder.avatar.setImage(bitmap);
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {
                finalHolder.avatar.setImage(R.drawable.ic_contact_picture);
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
                finalHolder.avatar.setImage(R.drawable.ic_contact_picture);
            }
        };
        Picasso.with(getContext()).load(imageUri).placeholder(R.drawable.ic_contact_picture).error(R.drawable.ic_contact_picture).into(target);

        // Datum posljednje primljene poruke
        holder.msgDate.setText(contactObj.getMsgDate());
        return row;
    }

}
