package com.wingsglory.foru_android.view.receiver;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wingsglory.foru_android.App;
import com.wingsglory.foru_android.R;
import com.wingsglory.foru_android.model.Result;
import com.wingsglory.foru_android.model.Task;
import com.wingsglory.foru_android.model.User;
import com.wingsglory.foru_android.util.LogUtil;
import com.wingsglory.foru_android.util.PreferenceUtil;
import com.wingsglory.foru_android.view.activity.TaskDetailActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import cn.jpush.android.api.JPushInterface;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by hezhujun on 2017/8/30.
 */

public class MyReceiver extends BroadcastReceiver {
    private static final String TAG = "MyReceiver";
    private static int notificationId = 0;

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        LogUtil.d(TAG, "onReceive - " + intent.getAction());
        if (JPushInterface.ACTION_REGISTRATION_ID.equals(intent.getAction())) {
            String regId = bundle.getString(JPushInterface.EXTRA_REGISTRATION_ID);
            LogUtil.d(TAG, "[MyReceiver] 接收Registration Id : " + regId);
        } else if (JPushInterface.ACTION_MESSAGE_RECEIVED.equals(intent.getAction())) {
            LogUtil.d(TAG, "收到了自定义消息。消息ID是：" + bundle.getString(JPushInterface.EXTRA_MSG_ID));
            LogUtil.d(TAG, "收到了自定义消息。消息标题是：" + bundle.getString(JPushInterface.EXTRA_TITLE));
            LogUtil.d(TAG, "收到了自定义消息。消息内容是：" + bundle.getString(JPushInterface.EXTRA_MESSAGE));
            LogUtil.d(TAG, "收到了自定义消息。消息附加内容是：" + bundle.getString(JPushInterface.EXTRA_EXTRA));
            // 自定义消息不会展示在通知栏，完全要开发者写代码去处理
            String title = bundle.getString(JPushInterface.EXTRA_TITLE);
            if (App.TASK_NEW.equals(title)) {
                handleNewTaskPush(context, bundle.getString(JPushInterface.EXTRA_MESSAGE));
            } else if (App.TASK_DOING.equals(title)) {
                handleTaskDoingPush(context, bundle.getString(JPushInterface.EXTRA_MESSAGE));
            }
        } else if (JPushInterface.ACTION_NOTIFICATION_RECEIVED.equals(intent.getAction())) {
            LogUtil.d(TAG, "收到了通知");
            // 在这里可以做些统计，或者做些其他工作
        } else if (JPushInterface.ACTION_NOTIFICATION_OPENED.equals(intent.getAction())) {
            LogUtil.d(TAG, "用户点击打开了通知");
            // 在这里可以自己写代码去定义用户点击后的行为
        } else {
            LogUtil.d(TAG, "Unhandled intent - " + intent.getAction());
        }
    }

    private void handleNewTaskPush(final Context context, final String message) {
        new Thread() {
            @Override
            public void run() {
                try {
                    JSONObject jsonObject = new JSONObject(message);
                    int taskId = jsonObject.getInt("taskId");
                    if (taskId == -1) {
                        return;
                    }
                    Task task = getTask(context, taskId);
                    if (task == null) {
                        // 任务信息获取失败
                        return;
                    }
                    Intent intent = TaskDetailActivity.actionStart(context, task);
                    PendingIntent pi = PendingIntent.getActivity(context, 0, intent, 0);
                    NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                    Notification notification = new NotificationCompat.Builder(context)
                            .setContentTitle("您收到新的任务啦!")
                            .setContentText(task.getContent().getContent())
                            .setContentIntent(pi)
                            .setSmallIcon(R.drawable.logo)
                            .setWhen(System.currentTimeMillis())
                            .setAutoCancel(true)
                            .setPriority(NotificationCompat.PRIORITY_MAX)
                            .build();
                    manager.notify(++notificationId, notification);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }.start();

    }

    private void handleTaskDoingPush(final Context context, final String message) {
        new Thread() {
            @Override
            public void run() {
                try {
                    JSONObject jsonObject = new JSONObject(message);
                    int taskId = jsonObject.getInt("taskId");
                    if (taskId == -1) {
                        return;
                    }
                    Task task = getTask(context, taskId);
                    if (task == null) {
                        // 任务信息获取失败
                        return;
                    }
                    User recipient = task.getRecipient();
                    if (recipient == null || recipient.getLatitude() == null || recipient.getLongitude() == null) {
                        return;
                    }
                    PreferenceUtil.saveUserPosition(context, recipient.getId(),
                            recipient.getLatitude().toString(), recipient.getLongitude().toString());
                } catch (
                        JSONException e)

                {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    private Task getTask(Context context, int taskId) {
        Task task = null;
        int userId = PreferenceUtil.readUserId(context);
        OkHttpClient client = new OkHttpClient();
        FormBody formBody = new FormBody.Builder()
                .add("userId", String.valueOf(userId))
                .add("taskId", String.valueOf(taskId))
                .build();
        Request request = new Request.Builder()
                .url(App.BASE_URL + "/task/get")
                .post(formBody)
                .build();
        try {
            Response response = client.newCall(request).execute();
            String json = response.body().string();
            LogUtil.d(TAG, json);
            if (response.isSuccessful()) {
                JSONObject jsonObject = new JSONObject(json);
                String resultStr = jsonObject.getString("result");
                ObjectMapper objectMapper = new ObjectMapper();
                Result result = objectMapper.readValue(resultStr, Result.class);
                if (result.isSuccess()) {
                    String taskStr = jsonObject.getString("task");
                    task = objectMapper.readValue(taskStr, Task.class);
                } else {
                    LogUtil.d(TAG, result.getErr());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return task;
    }
}
