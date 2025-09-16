package com.kiszka.kiddify.models;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "messages")
public class Message {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "message_id")
    private int id;
    @ColumnInfo(name = "sender_type")
    private String senderType;
    @ColumnInfo(name = "sender_id")
    private int senderId;
    @ColumnInfo(name = "content")
    private String content;
    @ColumnInfo(name = "sent_at")
    private String sentAt;

    public Message() {}

    public Message(String senderType, int senderId, String content, String sentAt) {
        this.senderType = senderType;
        this.senderId = senderId;
        this.content = content;
        this.sentAt = sentAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSenderType() {
        return senderType;
    }

    public void setSenderType(String senderType) {
        this.senderType = senderType;
    }

    public int getSenderId() {
        return senderId;
    }

    public void setSenderId(int senderId) {
        this.senderId = senderId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getSentAt() {
        return sentAt;
    }

    public void setSentAt(String sentAt) {
        this.sentAt = sentAt;
    }
}
