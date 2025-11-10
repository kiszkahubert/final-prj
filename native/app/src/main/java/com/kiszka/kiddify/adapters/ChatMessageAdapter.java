package com.kiszka.kiddify.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.kiszka.kiddify.databinding.ItemMessageReceivedBinding;
import com.kiszka.kiddify.databinding.ItemMessageSentBinding;
import com.kiszka.kiddify.models.Message;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class ChatMessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_MY_MESSAGE = 1;
    private static final int VIEW_TYPE_OTHER_MESSAGE = 2;
    private final LayoutInflater inflater;
    private List<Message> messages;
    private final int myUserId;
    private final Map<String, String> peopleMap = new HashMap<>();

    public ChatMessageAdapter(Context context, int myUserId) {
        this.inflater = LayoutInflater.from(context);
        this.messages = new ArrayList<>();
        this.myUserId = myUserId;
    }
    public void setMessages(List<Message> messages) {
        this.messages = messages;
        notifyDataSetChanged(); // updates the message list and refreshes the RecyclerView
    }
    public void setPeopleMap(Map<String, String> map) {
        if (map == null) return;
        this.peopleMap.clear();
        this.peopleMap.putAll(map);
        notifyDataSetChanged();
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
            String ownNameKey = message.getSenderType() + ":" + message.getSenderId();
            String ownName = peopleMap.getOrDefault(ownNameKey, "Me");
            myHolder.binding.senderText.setText(ownName);
        } else if (holder instanceof OtherMessageViewHolder) {
            OtherMessageViewHolder otherHolder = (OtherMessageViewHolder) holder;
            otherHolder.binding.messageText.setText(message.getContent());
            otherHolder.binding.timeText.setText(formatTime(message.getSentAt()));
            otherHolder.binding.senderText.setText(getSenderName(message));
        }
    }
    private String getSenderName(Message message) {
        String key = message.getSenderType() + ":" + message.getSenderId();
        if (peopleMap.containsKey(key)) {
            return peopleMap.get(key);
        }
        if (message.getSenderType().equals("PARENT")) {
            return "Parent";
        } else if (message.getSenderType().equals("KID")) {
            return "Kid";
        }
        return "Unknown";
    }
    @Override
    public int getItemCount() {
        return messages.size();
    }
    private String formatTime(String sentAt) {
        if (sentAt == null || sentAt.isEmpty()) return "";
        SimpleDateFormat outputFormat = new SimpleDateFormat("dd.MM.yyyy, HH:mm", Locale.getDefault());
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                try {
                    OffsetDateTime odt = OffsetDateTime.parse(sentAt);
                    Date date = Date.from(odt.toInstant());
                    return outputFormat.format(date);
                } catch (Exception ignored) {}
            }
        } catch (Throwable ignored) {}
        try {
            String normalized = sentAt;
            boolean isUTC = false;
            if (normalized.endsWith("Z")) {
                isUTC = true;
                normalized = normalized.substring(0, normalized.length() - 1);
            }
            if (normalized.contains(".")) {
                int dot = normalized.indexOf('.');
                String before = normalized.substring(0, dot);
                String after = normalized.substring(dot + 1);
                String tzSuffix = "";
                int plusIdx = after.indexOf('+');
                int minusIdx = after.indexOf('-');
                int tzIdx = plusIdx > 0 ? plusIdx : (minusIdx > 0 ? minusIdx : -1);
                if (tzIdx > 0) {
                    tzSuffix = after.substring(tzIdx);
                    after = after.substring(0, tzIdx);
                }
                after = after.replaceAll("\\D", "");
                if (after.length() > 3) after = after.substring(0, 3);
                while (after.length() < 3) after = after + "0";
                normalized = before + "." + after + tzSuffix;
            }
            try {
                SimpleDateFormat inputMs = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.getDefault());
                if (isUTC){
                    inputMs.setTimeZone(TimeZone.getTimeZone("UTC"));
                }
                Date date = inputMs.parse(normalized);
                if (date != null) {
                    return outputFormat.format(date);
                }
            } catch (ParseException ignored) {}
            try {
                SimpleDateFormat inputNoMs = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                if (isUTC){
                    inputNoMs.setTimeZone(TimeZone.getTimeZone("UTC"));
                }
                Date date = inputNoMs.parse(normalized);
                if (date != null) return outputFormat.format(date);
            } catch (Exception ignored) {}
            String[] patterns = new String[] {
                "yyyy-MM-dd HH:mm:ss",
                "yyyy-MM-dd"
            };
            for (String p : patterns) {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat(p, Locale.getDefault());
                    Date date = sdf.parse(sentAt);
                    if (date != null) return outputFormat.format(date);
                } catch (Exception ignored) {}
            }
        } catch (Exception ignored) {}
        return sentAt;
    }
    // ViewHolders for displaying messages sent by the current user/other users
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