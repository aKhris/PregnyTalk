package com.akhris.pregnytalk.contract;

import android.support.annotation.NonNull;

import com.akhris.pregnytalk.MainActivity;
import com.google.firebase.database.Exclude;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Class representing ChatRoom in Firebase Realtime Database
 */
public class ChatRoom implements Serializable, Comparable<ChatRoom>{

    // Public type of chat - visible on map for all the users
    public static final int TYPE_PUBLIC = 0;

    // Private type of chat - not visible on map
    public static final int TYPE_PRIVATE = 1;

    @Exclude
    private String chatRoomId;

    // User that created the chat room becomes it's admin
    private String adminId;

    // Name of the chat room
    private String name;

    // Description of the chat room
    private String description;

    // Last message to show in chats list
    private Message lastMessage;

    // Created at - timestamp to show in the chats list when there is no last message
    private Long createdAt;

    // Location of the chat room to show chatroom on the map
    private PlaceData location;

    // Chat room users map where Key is user ID and Value is user name
    private HashMap<String, String> usersMap;

    // Type of chat room
    private int type=TYPE_PUBLIC;

    public ChatRoom() {
    }



    public String getAdminId() {
        return adminId;
    }

    public String getName() {
        return name;
    }

    @Exclude
    public String getNameExtended(String format) {
        if (isPrivateTwoUsersRoom()) {
            if(usersMap.containsKey(MainActivity.sMyUid)){
                for (Map.Entry<String, String> entry:usersMap.entrySet()) {
                    if(!(entry.getKey().equals(MainActivity.sMyUid))){
                        return String.format(Locale.getDefault(), format, entry.getValue());
                    }
                }
            }
        }
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

    @Exclude
    private boolean isPrivateTwoUsersRoom() {
        return type == TYPE_PRIVATE && usersMap != null && usersMap.size() == 2;
    }

    @Override
    public int compareTo(@NonNull ChatRoom o) {
        int cmp=0;

        if(this.getLastMessage()!=null && o.getLastMessage()!=null){
            cmp = (int)(o.getLastMessage().getTimeStamp()
                    -this.getLastMessage().getTimeStamp());
        }
        if(cmp!=0){return cmp;}

        if(this.getLastMessage()==null && o.getLastMessage()!=null && this.getCreatedAt()!=null){
            cmp = (int)(o.getLastMessage().getTimeStamp()- this.getCreatedAt());
        }
        if(cmp!=0){return cmp;}

        if(this.getLastMessage()!=null && o.getLastMessage()==null && o.getCreatedAt()!=null){
            cmp = (int)(o.getCreatedAt() - this.getLastMessage().getTimeStamp());
        }
        if(cmp!=0){return cmp;}


        if(this.getCreatedAt()!=null && o.getCreatedAt()!=null){
            cmp = (int)(o.getCreatedAt()-this.getCreatedAt());
        }
        if(cmp!=0){return cmp;}

        if(this.getName()!=null && o.getName()!=null){
            cmp = o.getName().compareTo(this.getName());
        }
        if(cmp!=0){return cmp;}

        if(this.getChatRoomId()!=null && o.getChatRoomId()!=null){
            cmp = o.getChatRoomId().compareTo(this.getChatRoomId());
        }

        return cmp;
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof ChatRoom)) {return false;}
        ChatRoom c = (ChatRoom) obj;
        return c.getChatRoomId().equals(this.getChatRoomId());
    }


}
