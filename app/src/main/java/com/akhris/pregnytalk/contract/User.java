package com.akhris.pregnytalk.contract;

import com.google.firebase.database.Exclude;

import java.util.HashMap;

/**
 * Class representing User in Firebase Realtime Database
 */
public class User {

    @Exclude
    private String uId;

    // Name of the user
    private String name;

    // User's picture
    private String pictureUrl;

    // Map of users that this user added to it's contacts list
    private HashMap<String, String> contacts;   //<UserId,Name>

    // Map of children
    private HashMap<String, Child> children;    //<ChildId, Child>

    // Date of birth
    private Long birthDateMillis;

    // Estimated delivery date
    private Long estimatedDateMillis;

    // PlaceData object representing user's location
    private PlaceData userLocationPlaceData;

    // PlaceData object representing hospital location
    private PlaceData hospitalLocationPlaceData;

    public User() {
    }

    public String getName() {
        return name;
    }

    public String getPictureUrl() {
        return pictureUrl;
    }


    public Long getBirthDateMillis() {
        return birthDateMillis;
    }

    public Long getEstimatedDateMillis() {
        return estimatedDateMillis;
    }

    public PlaceData getUserLocationPlaceData() {
        return userLocationPlaceData;
    }

    public void setUserLocationPlaceData(PlaceData userLocationPlaceData) {
        this.userLocationPlaceData = userLocationPlaceData;
    }

    public PlaceData getHospitalLocationPlaceData() {
        return hospitalLocationPlaceData;
    }

    public void setHospitalLocationPlaceData(PlaceData hospitalLocationPlaceData) {
        this.hospitalLocationPlaceData = hospitalLocationPlaceData;
    }

    public HashMap<String, String> getContacts() {
        return contacts;
    }

    public String getuId() {
        return uId;
    }

    public void setuId(String uId) {
        this.uId = uId;
    }

    public HashMap<String, Child> getChildren() {
        return children;
    }

    public void setChildren(HashMap<String, Child> children) {
        this.children = children;
    }
}
