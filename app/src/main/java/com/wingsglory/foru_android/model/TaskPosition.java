package com.wingsglory.foru_android.model;

/**
 * Created by hezhujun on 2017/6/30.
 */

public class TaskPosition {

    private String position;
    private String latitude;
    private String longitude;

    public TaskPosition() {
    }

    public TaskPosition(String position, String latitude, String longitude) {
        this.position = position;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }
}
