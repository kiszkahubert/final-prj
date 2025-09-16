package com.kiszka.kiddify.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.kiszka.kiddify.databinding.ItemMessageReceivedBinding;
import com.kiszka.kiddify.databinding.ItemMessageSentBinding;
import com.kiszka.kiddify.models.Message;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;


public class ChatMessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_MY_MESSAGE = 1;
    private static final int VIEW_TYPE_OTHER_MESSAGE = 2;
    private LayoutInflater inflater;
    private Context context;
    private List<Message> messages;
    private int myUserId;

    public ChatMessageAdapter(Context context, int myUserId) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.messages = new ArrayList<>();
        this.myUserId = myUserId;
    }
    public void setMessages(List<Message> messages) {
        this.messages = messages;
        notifyDataSetChanged();
    }
    public void addMessage(Message message) {
        this.messages.add(message);
        notifyItemInserted(messages.size() - 1);
    }
    @Override
    public int getItemViewType(int position) {
        Message message = messages.get(position);
        if (message.getSenderType().equals("KID") && message.getSenderId() == myUserId) {
            return VIEW_TYPE_MY_MESSAGE;
        } else {
            return VIEW_TYPE_OTHER_MESSAGE;
        }
    }
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_MY_MESSAGE) {
            ItemMessageSentBinding binding = ItemMessageSentBinding.inflate(inflater, parent, false);
            return new MyMessageViewHolder(binding);
        } else {
            ItemMessageReceivedBinding binding = ItemMessageReceivedBinding.inflate(inflater, parent, false);
            return new OtherMessageViewHolder(binding);
        }
    }
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messages.get(position);
        if (holder instanceof MyMessageViewHolder) {
            MyMessageViewHolder myHolder = (MyMessageViewHolder) holder;
            myHolder.binding.messageText.setText(message.getContent());
            myHolder.binding.timeText.setText(formatTime(message.getSentAt()));
        } else if (holder instanceof OtherMessageViewHolder) {
            OtherMessageViewHolder otherHolder = (OtherMessageViewHolder) holder;
            otherHolder.binding.messageText.setText(message.getContent());
            otherHolder.binding.timeText.setText(formatTime(message.getSentAt()));
            String senderName = getSenderName(message);
            otherHolder.binding.senderText.setText(senderName);
        }
    }
    private String getSenderName(Message message) {
        if (message.getSenderType().equals("PARENT")) {
            return "Rodzic";
        } else if (message.getSenderType().equals("KID")) {
            return "Dziecko";
        }
        return "Nieznany";
    }

    private String formatTime(String sentAt) {
        try {
            if (sentAt != null && sentAt.contains("T")) {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS", java.util.Locale.getDefault());
                SimpleDateFormat outputFormat = new SimpleDateFormat("HH:mm", java.util.Locale.getDefault());
                try {
                    java.util.Date date = inputFormat.parse(sentAt.replace("Z", ""));
                    return outputFormat.format(date);
                } catch (ParseException e) {}
            }
            return sentAt != null ? sentAt : "";
        } catch (Exception e) {
            return sentAt != null ? sentAt : "";
        }
    }
    @Override
    public int getItemCount() {
        return messages.size();
    }
    public static class MyMessageViewHolder extends RecyclerView.ViewHolder {
        ItemMessageSentBinding binding;
        public MyMessageViewHolder(ItemMessageSentBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
    public static class OtherMessageViewHolder extends RecyclerView.ViewHolder {
        ItemMessageReceivedBinding binding;
        public OtherMessageViewHolder(ItemMessageReceivedBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}