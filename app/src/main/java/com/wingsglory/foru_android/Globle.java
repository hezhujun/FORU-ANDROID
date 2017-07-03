package com.wingsglory.foru_android;

import android.graphics.Bitmap;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wingsglory.foru_android.model.ImageData;
import com.wingsglory.foru_android.model.TaskDTO;
import com.wingsglory.foru_android.model.User;
import com.wingsglory.foru_android.view.activity.LoginActivity;
import com.wingsglory.foru_android.view.activity.MainActivity;
import com.wingsglory.foru_android.view.activity.RegisterActivity;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by hezhujun on 2017/6/29.
 */

public class Globle {
    public static LoginActivity loginActivity;
    public static RegisterActivity registerActivity;
    public static MainActivity mainActivity;
    public static User user;
    public static Map<String, Bitmap> imageBuffer = new HashMap<>();
    public static TaskDTO currentTask;

    static {
        init();
    }

    public static void init() {
        String userStr = "{\"id\":1,\"username\":\"hezhujun\",\"phone\":\"18932442453\",\"password\":\"4QrcOUm6Wau+VuBX8g+IPg==\",\"name\":null,\"email\":null,\"protraitUrl\":\"http://p1.qq181.com/cms/120507/2012050705435364461.jpg\",\"rongToken\":null,\"position\":null,\"creditValue\":0,\"publishCount\":0,\"doneCount\":0,\"failCount\":0,\"realName\":null,\"idCardNo\":null,\"deposit\":0.00,\"gmtCreate\":1498291311000,\"gmtModified\":1498291311000}";
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            user = objectMapper.readValue(userStr, User.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
