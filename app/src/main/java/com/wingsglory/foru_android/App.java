package com.wingsglory.foru_android;

import android.app.Application;

import com.wingsglory.foru_android.model.User;

/**
 * Created by hezhujun on 2017/6/28.
 */

public class App extends Application {
//    public static final String BASE_URL = "http://192.168.1.106:8080";
    public static final String BASE_URL = "http://192.168.244.86:8080";
    public static final String DEFAULT_IMAGE_URL = "https://ps.ssl.qhimg.com/t0123f47c7eae031cbb.jpg";

    private User user;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
