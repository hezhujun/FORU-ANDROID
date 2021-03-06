package com.wingsglory.foru_android;

import android.app.Application;
import android.content.Context;
import android.content.IntentFilter;
import android.support.multidex.MultiDexApplication;

import com.wingsglory.foru_android.model.Task;
import com.wingsglory.foru_android.model.User;
import com.wingsglory.foru_android.view.receiver.MyReceiver;

import java.util.HashMap;
import java.util.Map;

import cn.jpush.android.api.JPushInterface;
import cn.jpush.im.android.api.JMessageClient;

/**
 * Created by hezhujun on 2017/6/28.
 */

public class App extends MultiDexApplication {
    public static final String BASE_URL = "http://118.89.51.45:8080/foru";
//    public static final String BASE_URL = "http://192.168.1.120:8080/foru";
    public static final String DEFAULT_IMAGE_URL = "https://ps.ssl.qhimg.com/t0123f47c7eae031cbb.jpg";

    // 推送信息的标题
    public static final String TASK_NEW = "TASK_NEW";
    public static final String TASK_ACCEPT = "TASK_ACCEPT";
    public static final String TASK_COMPLETE = "TASK_COMPLETE";
    public static final String TASK_FINISH = "TASK_FINISH";
    public static final String TASK_DOING = "TASK_DOING";
    public static final String TASK_ABANDON = "TASK_ABANDON";
    public static final String TASK_DELETE = "TASK_DELETE";
    public static final String APP_KEY = "8efb491917ed24458c73528f";

    private User user;
    private Map<Integer, Task> taskBuffer = new HashMap<>();
    private static App app;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
        // 设置JPush的别名
        JPushInterface.setAlias(this,
                JPushOperationDefine.SET_ALIAS_SEQUENCE,
                String.valueOf(user.getId()));
    }

    public Map<Integer, Task> getTaskBuffer() {
        return taskBuffer;
    }

    public void addTask(Task task){
        taskBuffer.put(task.getId(), task);
    }

    public void remove(Integer taskId) {
        taskBuffer.remove(taskId);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        app = this;
        JPushInterface.setDebugMode(true);
        JPushInterface.init(this);
        JMessageClient.setDebugMode(true);
        JMessageClient.init(this, true);
        JPushInterface.requestPermission(this);
    }

    public static App getApp() {
        return app;
    }
}
