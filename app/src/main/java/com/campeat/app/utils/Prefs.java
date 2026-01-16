package com.campeat.app.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class Prefs {

    private static final String PREF_NAME = "campeat_prefs";

    private static final String KEY_NAME = "name";
    private static final String KEY_UNI = "university";
    private static final String KEY_POINTS = "points";
    private static final String KEY_LANG = "language";

    private static SharedPreferences getPrefs(Context c) {
        return c.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    // NAME
    public static void saveName(Context c, String val) {
        getPrefs(c).edit().putString(KEY_NAME, val).apply();
    }

    public static String getName(Context c) {
        return getPrefs(c).getString(KEY_NAME, "User");
    }

    // UNIVERSITY
    public static void saveUniversity(Context c, String val) {
        getPrefs(c).edit().putString(KEY_UNI, val).apply();
    }

    public static String getUniversity(Context c) {
        return getPrefs(c).getString(KEY_UNI, "Universitas");
    }

    // POINTS
    public static void savePoints(Context c, int val) {
        getPrefs(c).edit().putInt(KEY_POINTS, val).apply();
    }

    public static int getPoints(Context c) {
        return getPrefs(c).getInt(KEY_POINTS, 0);
    }

    public static void addPoints(Context c, int plus) {
        int now = getPoints(c);
        savePoints(c, now + plus);
    }

    // LANGUAGE
    public static void saveLanguage(Context c, String lang) {
        getPrefs(c).edit().putString(KEY_LANG, lang).apply();
    }

    public static String getLanguage(Context c) {
        return getPrefs(c).getString(KEY_LANG, "id"); // default Indo
    }

    // CLEAR (logout)
    public static void clear(Context c) {
        getPrefs(c).edit().clear().apply();
    }
}
