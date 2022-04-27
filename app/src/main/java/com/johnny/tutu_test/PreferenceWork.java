package com.johnny.tutu_test;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class PreferenceWork {

    private static final String PREF_SAVED_APP_VERSION = "savedAppVersion";
    private static SharedPreferences preferences;

    private PreferenceWork () {}

    public static void setContext(Context context) {
        preferences =  PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static String getSavedAppVersion() {
        if (preferences != null)
            return preferences.getString(PREF_SAVED_APP_VERSION, "");
        return "";
    }

    public static void setSavedAppVersion(String savedAppVersion) {
        if (preferences != null)
            preferences.edit().putString(PREF_SAVED_APP_VERSION, savedAppVersion).apply();
    }
}
