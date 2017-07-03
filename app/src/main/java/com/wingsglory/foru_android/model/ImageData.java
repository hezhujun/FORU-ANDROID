package com.wingsglory.foru_android.model;

import android.graphics.Bitmap;

import java.io.Serializable;

/**
 * Created by hezhujun on 2017/7/2.
 */

public class ImageData implements Serializable {
    private String url;
    private Bitmap bitmap;

    public ImageData() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ImageData imageData = (ImageData) o;

        return url != null ? url.equals(imageData.url) : imageData.url == null;

    }

    @Override
    public int hashCode() {
        return url != null ? url.hashCode() : 0;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }
}
