package com.wingsglory.foru_android.model;

import java.io.Serializable;

/**
 * Created by hezhujun on 2017/6/30.
 */

public class Position implements Serializable {
    private String position;
    private double lat;
    private double lng;

    public Position() {
    }

    public Position(String position, double lat, double lng) {
        this.position = position;
        this.lat = lat;
        this.lng = lng;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    @Override
    public String toString() {
        return "Position{" +
                "position='" + position + '\'' +
                ", lat=" + lat +
                ", lng=" + lng +
                '}';
    }
}
