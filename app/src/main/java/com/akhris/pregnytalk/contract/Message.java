package com.akhris.pregnytalk.contract;

import java.io.Serializable;

/**
 * Class representing Message in Firebase Realtime Database
 */

public class Message implements Serializable{
    private String messageId;
    private String userId;
    private String userName;
    private String message;
    private Long timeStamp;
    private String pictureUrl;

    public Message() {
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getPictureUrl() {
        return pictureUrl;
    }

    public void setPictureUrl(String pictureUrl) {
        this.pictureUrl = pictureUrl;
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof Message)) {return false;}
        Message m = (Message)obj;

        return m.getMessage().equals(this.getMessage())
                && m.getUserId().equals(this.getUserId())
                && m.getTimeStamp().equals(this.getTimeStamp())
                && m.getUserName().equals(this.getUserName());
    }
}
