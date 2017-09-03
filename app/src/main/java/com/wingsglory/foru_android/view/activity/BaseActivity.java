package com.wingsglory.foru_android.view.activity;

import android.content.IntentFilter;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.wingsglory.foru_android.App;
import com.wingsglory.foru_android.view.receiver.MyReceiver;

/**
 * Created by hezhujun on 2017/9/1.
 */

public class BaseActivity extends AppCompatActivity {
    protected App app;
    private MyReceiver myReceiver;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app = (App) getApplication();
        ActivityCollector.addActivity(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityCollector.removeActivity(this);
    }
}
