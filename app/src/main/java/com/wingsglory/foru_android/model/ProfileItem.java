package com.wingsglory.foru_android.model;

/**
 * Created by King on 2017/6/28.
 */

public class ProfileItem {
    private int imgId;
    private String text;

    public ProfileItem(int imgId,String text){
        this.imgId = imgId;
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getImgId() {
        return imgId;
    }

    public void setImgId(int imgId) {
        this.imgId = imgId;
    }
}
