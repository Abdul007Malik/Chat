package com.example.root.chat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by root on 2/20/15.
 */
public class DialogAdapter extends ArrayAdapter<Contact> {

    private Context context;
    private Contact contactObj;

    public DialogAdapter(Context context, ArrayList<Contact> contacts) {
        super(context, R.layout.dialog_contact_row, contacts);
        this.context = context;
    }

    class MyViewHolder {
        TextView name, number;

        public MyViewHolder(View view) {
            name = (TextView) view.findViewById(R.id.name);
            number = (TextView) view.findViewById(R.id.number);
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
        holder.name.setText(contactName);

        String contactNumber = contactObj.getPhone();
        holder.number.setText(contactNumber);
        return row;
    }
}
