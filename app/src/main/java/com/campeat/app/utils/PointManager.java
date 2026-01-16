package com.campeat.app.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class PointManager {

    private static final String PREF_NAME = "reward_pref";
    private static final String KEY_POINT = "user_point";

    public static int getPoint(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(KEY_POINT, 0);
    }

    public static void addPoint(Context context, int point) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        int current = prefs.getInt(KEY_POINT, 0);
        prefs.edit().putInt(KEY_POINT, current + point).apply();
    }

    public static void resetPoint(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putInt(KEY_POINT, 0).apply();
    }
}
