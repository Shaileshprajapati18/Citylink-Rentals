package com.example.citylinkrentals.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class PropertyListResponse {

        @SerializedName("statusCode")
        private int statusCode;

        @SerializedName("statusMessage")
        private String statusMessage;

        @SerializedName("messageBody")
        private List<PropertyListRequest> messageBody;

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

        public List<PropertyListRequest> getMessageBody() {
            return messageBody;
        }

        public void setMessageBody(List<PropertyListRequest> messageBody) {
            this.messageBody = messageBody;
        }
}
