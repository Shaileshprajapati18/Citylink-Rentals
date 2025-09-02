package com.example.citylinkrentals.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ResponseDTO {

    @SerializedName("statusCode")
    private int statusCode;

    @SerializedName("statusMessage")
    private String statusMessage;

    @SerializedName("messageBody")
    private List<Property> messageBody;

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

    public List<Property> getMessageBody() {
        return messageBody;
    }

    public void setMessageBody(List<Property> messageBody) {
        this.messageBody = messageBody;
    }
}
