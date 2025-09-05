package com.example.citylinkrentals.model;

import com.google.gson.annotations.SerializedName;

public class MessageDTO {
    @SerializedName("firebaseUid")
    private String firebaseUid;

    @SerializedName("content")
    private String content;

    @SerializedName("isSent")
    private boolean isSent;

    @SerializedName("timestamp")
    private String timestamp; // Changed from LocalDateTime to String

    public MessageDTO(String content, boolean isSent, String timestamp) {
        this.content = content;
        this.isSent = isSent;
        this.timestamp = timestamp;
    }

    public String getFirebaseUid() { return firebaseUid; }
    public String getContent() { return content; }
    public boolean isSent() { return isSent; }
    public String getTimestamp() { return timestamp; }

    public void setFirebaseUid(String firebaseUid) { this.firebaseUid = firebaseUid; }
    public void setContent(String content) { this.content = content; }
    public void setIsSent(boolean isSent) { this.isSent = isSent; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
}