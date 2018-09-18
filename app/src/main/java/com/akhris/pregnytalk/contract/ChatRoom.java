package com.akhris.pregnytalk.contract;

import android.support.annotation.NonNull;

import com.google.firebase.database.Exclude;

import java.io.Serializable;
import java.util.HashMap;

public class ChatRoom implements Serializable, Comparable<ChatRoom>{

    public static final int TYPE_PUBLIC = 0;
    public static final int TYPE_PRIVATE = 1;

    @Exclude
    private String chatRoomId;

    private String adminId;
    private String name;
    private String description;
    private Message lastMessage;
    private Long createdAt;
    private PlaceData location;
    private HashMap<String, String> usersMap;
    private int type=TYPE_PUBLIC;

    public ChatRoom() {
    }



    public String getAdminId() {
        return adminId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public PlaceData getLocation() {
        return location;
    }

    public int getType() {
        return type;
    }

    public void setAdminId(String adminId) {
        this.adminId = adminId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }

    public void setLocation(PlaceData location) {
        this.location = location;
    }

    public void setType(int type) {
        this.type = type;
    }

    public Message getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(Message lastMessage) {
        this.lastMessage = lastMessage;
    }

    public String getChatRoomId() {
        return chatRoomId;
    }

    public void setChatRoomId(String chatRoomId) {
        this.chatRoomId = chatRoomId;
    }

    public HashMap<String, String> getUsersMap() {
        return usersMap;
    }

    public void setUsersMap(HashMap<String, String> usersMap) {
        this.usersMap = usersMap;
    }

    @Override
    public int compareTo(@NonNull ChatRoom o) {
        if (o.getLastMessage()==null || this.getLastMessage()==null){return -1;}
        return (int)(o.getLastMessage().getTimeStamp()-this.getLastMessage().getTimeStamp());
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof ChatRoom)) {return false;}
        ChatRoom c = (ChatRoom) obj;
        return c.getChatRoomId().equals(this.getChatRoomId());
    }
}
