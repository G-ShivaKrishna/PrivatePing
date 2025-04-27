package com.example.privateping;

import android.content.Context;
import android.text.util.Linkify;
import android.view.*;
import android.widget.*;
import java.util.ArrayList;

public class ChatAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<ChatMessage> messages;

    public ChatAdapter(Context context, ArrayList<ChatMessage> messages) {
        this.context = context;
        this.messages = messages;
    }

    @Override
    public int getCount() {
        return messages.size();
    }

    @Override
    public Object getItem(int position) {
        return messages.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ChatMessage message = messages.get(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.chat_message_item, parent, false);
        }

        TextView usernameText = convertView.findViewById(R.id.usernameText);
        TextView messageText = convertView.findViewById(R.id.messageText);

        usernameText.setText(message.getUser());
        messageText.setText(message.getMessage());

        // Make links clickable
        Linkify.addLinks(messageText, Linkify.ALL);

        return convertView;
    }
}