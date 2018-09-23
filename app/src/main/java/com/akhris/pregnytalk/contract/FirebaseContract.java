package com.akhris.pregnytalk.contract;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Util class representing Firebase Contract
 */

public class FirebaseContract {
    public static final String CHILD_ROOM_MESSAGES="room-messages";
    public static final String CHILD_ROOM_META_DATA="room-meta-data";
    public static final String CHILD_USERS="users";

    /**
     * Firebase Storage
     */

    public static final String STORAGE_CHILD_USER_AVATARS = "user-pictures";
    public static final String STORAGE_CHILD_CHATROOM_FILES = "chatroom-files";

    /**
     * USER CONTRACT
     */

    public static final String CHILD_USER_USER_LOCATION_PLACEDATA="userLocationPlaceData";
    public static final String CHILD_USER_HOSPITAL_LOCATION_PLACEDATA="hospitalLocationPlaceData";
    public static final String CHILD_USER_BIRTH_DATE_MILLIS = "birthDateMillis";
    public static final String CHILD_USER_ESTIMATED_DATE_MILLIS = "estimatedDateMillis";
    public static final String CHILD_USER_NAME = "name";
    public static final String CHILD_USER_PICTURE_URL = "pictureUrl";
    public static final String CHILD_USER_CONTACTS = "contacts";
    public static final String CHILD_USER_CHILDREN = "children";

    /**
     * CHATROOM CONTRACT
     */

    public static final String CHILD_ROOM_LAST_MESSAGE = "lastMessage";
    public static final String CHILD_ROOM_USERS_MAP = "usersMap";
    public static final String CHILD_ROOM_NAME = "name";
    public static final String CHILD_ROOM_DESCRIPTION = "description";
    public static final String CHILD_ROOM_TYPE = "type";



    public static DatabaseReference getRoomsMetaDataReference() {
        return FirebaseDatabase
                .getInstance()
                .getReference()
                .child(FirebaseContract.CHILD_ROOM_META_DATA);
    }

    public static DatabaseReference getRoomMetaDataReference(String roomId){
        return getRoomsMetaDataReference().child(roomId);
    }
}
