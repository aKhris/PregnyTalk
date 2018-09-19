package com.akhris.pregnytalk.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefUtils {

    private static final String HELPERS_PREFS_NAME = "helpers_preferences";
    private static final int BOUNCE_MAX_COUNT=3;

    public static boolean wasBounced(Context context, Class adapter){
        SharedPreferences sharedPreferences = getHelpersSharedPrefs(context);
        int previousBounced = sharedPreferences.getInt(adapter.getSimpleName(), 0);
        if(previousBounced>=BOUNCE_MAX_COUNT){
            return true;
        } else {
            sharedPreferences.edit().putInt(adapter.getSimpleName(), previousBounced+1).apply();
            return false;
        }
    }

    private static SharedPreferences getHelpersSharedPrefs(Context context){
        return context.getSharedPreferences(HELPERS_PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static void resetHelpers(Context context) {
        getHelpersSharedPrefs(context).edit().clear().apply();
    }
}
