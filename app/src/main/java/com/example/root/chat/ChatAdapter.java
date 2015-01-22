package com.example.root.chat;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by root on 1/16/15.
 */
public class ChatAdapter extends ArrayAdapter<Message> {

    private Context context;
    private Message messageObj;
    private ArrayList<Message> messages;
    public ChatAdapter(Context c, ArrayList<Message> messages) {
        super(c, R.layout.message_row, messages);
        this.context = c;
        this.messages = messages;
    }

    class MyViewHolder  {
        TextView messageView, messageDate;
        LinearLayout singleMessageLayout, singleMessageContainer;
        public MyViewHolder(View view) {
            messageView = (TextView) view.findViewById(R.id.singleMessage);
            singleMessageLayout = (LinearLayout) view.findViewById(R.id.singleMessageLayout);
            singleMessageContainer = (LinearLayout) view.findViewById(R.id.singleMessageContainer);
            messageDate = (TextView) view.findViewById(R.id.messageDate);
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        MyViewHolder holder = null;
        View row = convertView;

        if (row == null) {
            row = LayoutInflater.from(context).inflate(R.layout.message_row, parent, false);
            holder = new MyViewHolder(row);
            row.setTag(holder);
        } else {
            holder = (MyViewHolder) row.getTag();
        }

        messageObj = getItem(position);
        String message = messageObj.getMessage();
        holder.messageView.setText(message);
        holder.messageDate.setText(messageObj.getMsgDate());
        holder.singleMessageLayout.setBackgroundResource(messageObj.isMe() ? R.drawable.bubble_a : R.drawable.bubble_b);
        holder.singleMessageContainer.setGravity(messageObj.isMe() ? Gravity.RIGHT : Gravity.LEFT);
        if (messageObj.isMe()) {
            holder.singleMessageContainer.setPadding(50, 10, 10, 10);
        } else {
            holder.singleMessageContainer.setPadding(10, 10, 50, 10);
        }

        return row;
    }
}
