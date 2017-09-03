package com.wingsglory.foru_android.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by hezhujun on 2017/9/2.
 */

public class UserSaveUtil {
    public static void save(Context context , int userId, String phone, String password) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt("userId", userId);
        editor.putString("phone", phone);
        editor.putString("password", password);
        editor.commit();
    }

    public static int readUserId(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getInt("userId", -1);
    }

    public static String readUserPhone(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getString("phone", "");
    }

    public static String readUserPassword(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getString("password", "");
    }

    /**
     * 设置是否自动登录
     * @param context
     * @param isAuto
     */
    public static void setAutoLogin(Context context, boolean isAuto) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean("isAutoLogin", isAuto);
        editor.commit();
    }

    public static boolean isAutoLogin(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean("isAutoLogin", false);
    }
}
