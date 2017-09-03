package com.wingsglory.foru_android.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.amap.api.maps.model.LatLng;

import java.math.BigDecimal;

/**
 * Created by hezhujun on 2017/9/2.
 */

public class PreferenceUtil {
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

    public static void saveUserPosition(Context context, int userId, String latitude, String longitude) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();
        String latitudeKey = String.format("%d-position-latitude", userId);
        String longitudeKey = String.format("%d-position-longitude", userId);
        String updateTimeKey = String.format("%d-position-update-time", userId);
        editor.putString(latitudeKey, latitude);
        editor.putString(longitudeKey, longitude);
        editor.putLong(updateTimeKey, System.currentTimeMillis());
        editor.commit();
    }

    public static boolean isUserPositionExists(Context context, int userId) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        long updateTime = getUserPositionUpdateTime(context, userId);
        return updateTime != -1;
    }

    public static long getUserPositionUpdateTime(Context context, int userId) {
        String updateTimeKey = String.format("%d-position-update-time", userId);
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getLong(updateTimeKey, -1);
    }

    public static LatLng getUserPosition(Context context, int userId) {
        String latitudeKey = String.format("%d-position-latitude", userId);
        String longitudeKey = String.format("%d-position-longitude", userId);
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        String latitude = sp.getString(latitudeKey, "");
        String longitude = sp.getString(longitudeKey, "");
        LatLng position = null;
        try {
            position = new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude));
        }catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return position;
    }
}
