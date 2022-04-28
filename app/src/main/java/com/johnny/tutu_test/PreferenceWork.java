package com.johnny.tutu_test;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class PreferenceWork {

    private static SharedPreferences preferences;

    private PreferenceWork () {}

    public static void setContext(Context context) {
        preferences =  PreferenceManager.getDefaultSharedPreferences(context);
    }

}
