package com.akhris.pregnytalk.contract;

import com.google.firebase.database.Exclude;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class User {

    @Exclude
    private String uId;

    private String name;
    private String pictureUrl;
    private HashMap<String, String> contacts;   //<UserId,Name>
    private HashMap<String, Child> children;    //<ChildId, Child>
    private Long birthDateMillis;
    private Long estimatedDateMillis;
    private PlaceData userLocationPlaceData;
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
