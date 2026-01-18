package com.example.geolearn.auth;

import android.content.Context;
import android.content.SharedPreferences;

public class UserSession {
    private static final String PREF_NAME = "GeoLearnPrefs";
    private static final String KEY_IS_GUEST = "is_guest";

    /**
     * Saves the guest mode status to SharedPreferences.
     */
    public static void setGuestMode(Context context, boolean isGuest) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(KEY_IS_GUEST, isGuest);
        editor.apply();
    }

    /**
     * Checks if the current user is a guest.
     */
    public static boolean isGuestMode(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_IS_GUEST, false);
    }

    /**
     * Clears the session (useful for logout).
     */
    public static void clear(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().clear().apply();
    }
}