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

public class Global {
    public static Map<String, Bitmap> imageBuffer = new HashMap<>();
}
