package com.wingsglory.foru_android.view.service;

import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.wingsglory.foru_android.App;
import com.wingsglory.foru_android.model.User;
import com.wingsglory.foru_android.util.LogUtil;

import java.io.IOException;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by hezhujun on 2017/8/30.
 */

public class ForuService extends Service {
    private static final String TAG = "ForuService";

    private App app;

    public class ServiceController extends Binder {
        public void updateUserPosition(String latitude, String longitude) {
            ForuService.this.updateUserPosition(latitude, longitude);
        }
    }

    private ServiceController controller = new ServiceController();

    @Override
    public void onCreate() {
        super.onCreate();
        app = (App) getApplication();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return controller;
    }

    private void updateUserPosition(final String latitude, final String longitude){
        if (app.getUser() == null) {
            return;
        }
        new Thread() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient();
                FormBody formBody = new FormBody.Builder()
                        .add("latitude", latitude)
                        .add("longitude", longitude)
                        .add("userId", String.valueOf(app.getUser().getId()))
                        .build();
                Request request = new Request.Builder()
                        .url(App.BASE_URL + "/user/position")
                        .post(formBody)
                        .build();
                try {
                    Response response = client.newCall(request).execute();
                    String json = response.body().string();
                    LogUtil.d(TAG, json);
                    if (response.isSuccessful()) {
                        // 不需要返回结果
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

}
