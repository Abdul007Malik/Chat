package com.example.root.chat;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

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
    private ImageLoader imageLoader;
    private DisplayImageOptions options;

    public ImageLoader getImageLoader() {
        return imageLoader;
    }

    public ContactAdapter(Context c, ArrayList<Contact> contacts) {
        super(c, R.layout.contact_row, contacts);
        this.context = c;
        this.contacts = contacts;

        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .cacheOnDisc(true).cacheInMemory(true)
                .imageScaleType(ImageScaleType.EXACTLY)
                .displayer(new FadeInBitmapDisplayer(300)).build();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
                getContext())
                .defaultDisplayImageOptions(defaultOptions)
                .memoryCache(new WeakMemoryCache())
                .discCacheSize(100 * 1024 * 1024).build();

        ImageLoader.getInstance().init(config);

        options = new DisplayImageOptions.Builder().cacheInMemory(true)
                .cacheOnDisc(true).resetViewBeforeLoading(true).showImageForEmptyUri(R.drawable.ic_contact_picture).build();
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
            final MyViewHolder finalHolder = holder;

            ImageLoader.getInstance().loadImage(stringUri, options, new SimpleImageLoadingListener() {
                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    if (loadedImage != null) {
                        finalHolder.avatar.setImageBitmap(loadedImage);
                    } else
                        finalHolder.avatar.setImageBitmap(BitmapFactory.decodeResource(getContext().getResources(), R.drawable.ic_contact_picture));

                }
            });
        }



        // Datum posljednje primljene poruke
        holder.msgDate.setText(contactObj.getMsgDate());
        return row;
    }

}
