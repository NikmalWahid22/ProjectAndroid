package com.campeat.app.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class PointManager {

    private static final String PREF_NAME = "reward_pref";

    public static int getPoint(Context context, String uid) {
        SharedPreferences prefs =
                context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        return prefs.getInt("user_point_" + uid, 0);
    }

    public static void addPoint(Context context, String uid, int point) {
        SharedPreferences prefs =
                context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        int current = getPoint(context, uid);

        prefs.edit()
                .putInt("user_point_" + uid, current + point)
                .apply();
    }

    public static void resetPoint(Context context, String uid) {
        SharedPreferences prefs =
                context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        prefs.edit()
                .putInt("user_point_" + uid, 0)
                .apply();
    }
}