package com.example.adarsh.quickliftdriver.model;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by pandey on 1/4/18.
 */

public class SequenceModel {

    private LatLng latLng;
    private Double lat;
    private Double lng;
    private String name;
    private String type;
    private String id;

    public Double getLat() {
        return lat;
    }

    public String getId(){
        return id;
    }

    public void setId(String id){
        this.id = id;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLng() {
        return lng;
    }

    public void setLng(Double lng) {
        this.lng = lng;
    }

    public LatLng getLatLng() {
        return latLng;
    }

    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
