package com.akhris.pregnytalk.contract;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.Exclude;

import java.io.Serializable;

public class PlaceData implements Serializable{
    private double lng;
    private double lat;

    private String name;

    public PlaceData() {
    }

    public PlaceData(double lat, double lng, String name) {
        this.lng = lng;
        this.lat = lat;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public double getLng() {
        return lng;
    }

    public double getLat() {
        return lat;
    }

    @Exclude
    public LatLng getLatLng(){
        return new LatLng(lat, lng);
    }
}
