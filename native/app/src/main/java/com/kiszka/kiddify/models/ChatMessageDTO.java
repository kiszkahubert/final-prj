package com.kiszka.kiddify.models;

public class ChatMessageDTO {
    private String senderType;
    private int senderId;
    private String content;
    private String sentAt;

    public ChatMessageDTO() {}
    public ChatMessageDTO(String senderType, int senderId, String content, String sentAt) {
        this.senderType = senderType;
        this.senderId = senderId;
        this.content = content;
        this.sentAt = sentAt;
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
