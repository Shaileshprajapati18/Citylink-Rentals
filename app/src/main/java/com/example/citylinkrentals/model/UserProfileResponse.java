package com.example.citylinkrentals.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;

public class UserProfileResponse implements Serializable {
    @SerializedName("statusCode")
    private int statusCode;

    @SerializedName("statusMessage")
    private String statusMessage;

    @SerializedName("messageBody")
    private List<User> messageBody;

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    public List<User> getMessageBody() {
        return messageBody;
    }

    public void setMessageBody(List<User> messageBody) {
        this.messageBody = messageBody;
    }

    @Override
    public String toString() {
        return "UserProfileResponse{" +
                "statusCode=" + statusCode +
                ", statusMessage='" + statusMessage + '\'' +
                ", messageBody=" + messageBody +
                '}';
    }
}