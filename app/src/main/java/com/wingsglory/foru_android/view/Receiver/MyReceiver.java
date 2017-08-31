package com.wingsglory.foru_android.view.Receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.wingsglory.foru_android.util.LogUtil;

import cn.jpush.android.api.JPushInterface;

/**
 * Created by hezhujun on 2017/8/30.
 */

public class MyReceiver extends BroadcastReceiver {
    private static final String TAG = "MyReceiver";
    
    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        LogUtil.d(TAG, "onReceive - " + intent.getAction());

        if (JPushInterface.ACTION_REGISTRATION_ID.equals(intent.getAction())) {
            String regId = bundle.getString(JPushInterface.EXTRA_REGISTRATION_ID);
            LogUtil.d(TAG, "[MyReceiver] 接收Registration Id : " + regId);
        }else if (JPushInterface.ACTION_MESSAGE_RECEIVED.equals(intent.getAction())) {
            LogUtil.d(TAG, "收到了自定义消息。消息ID是：" + bundle.getString(JPushInterface.EXTRA_MSG_ID));
            LogUtil.d(TAG, "收到了自定义消息。消息标题是：" + bundle.getString(JPushInterface.EXTRA_TITLE));
            LogUtil.d(TAG, "收到了自定义消息。消息内容是：" + bundle.getString(JPushInterface.EXTRA_MESSAGE));
            LogUtil.d(TAG, "收到了自定义消息。消息附加内容是：" + bundle.getString(JPushInterface.EXTRA_EXTRA));
            // 自定义消息不会展示在通知栏，完全要开发者写代码去处理
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
}
