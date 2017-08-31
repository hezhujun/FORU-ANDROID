package com.wingsglory.foru_android.view.Receiver;

import android.content.Context;

import com.wingsglory.foru_android.JPushOperationDefine;
import com.wingsglory.foru_android.util.LogUtil;

import cn.jpush.android.api.JPushMessage;
import cn.jpush.android.service.JPushMessageReceiver;

/**
 * Created by hezhujun on 2017/8/30.
 */

public class MyJPushMessageReceiver extends JPushMessageReceiver {

    private static final String TAG = "MyJPushMessageReceiver";

    public MyJPushMessageReceiver() {
        super();
    }

    @Override
    public void onTagOperatorResult(Context context, JPushMessage jPushMessage) {
        super.onTagOperatorResult(context, jPushMessage);
    }

    @Override
    public void onCheckTagOperatorResult(Context context, JPushMessage jPushMessage) {
        super.onCheckTagOperatorResult(context, jPushMessage);
    }

    @Override
    public void onAliasOperatorResult(Context context, JPushMessage jPushMessage) {
        if (jPushMessage.getSequence() == JPushOperationDefine.SET_ALIAS_SEQUENCE) {
            if (jPushMessage.getErrorCode() == 0) {
                LogUtil.d(TAG, "设置别名为：" + jPushMessage.getAlias());
            }
        }
    }
}
