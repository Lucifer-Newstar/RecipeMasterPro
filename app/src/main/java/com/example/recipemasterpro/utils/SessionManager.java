package com.example.recipemasterpro.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static SessionManager instance;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    private SessionManager(Context context) {
        sharedPreferences = context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public static synchronized SessionManager getInstance(Context context) {
        if (instance == null) {
            instance = new SessionManager(context.getApplicationContext());
        }
        return instance;
    }

    public void saveUserSession(String userId, String userName, String userEmail, String userRole) {
        editor.putString(Constants.KEY_USER_ID, userId);
        editor.putString(Constants.KEY_USER_NAME, userName);
        editor.putString(Constants.KEY_USER_EMAIL, userEmail);
        editor.putString(Constants.KEY_USER_ROLE, userRole);
        editor.putBoolean(Constants.KEY_IS_LOGGED_IN, true);
        editor.apply();
    }

    public void saveFcmToken(String token) {
        editor.putString(Constants.KEY_FCM_TOKEN, token);
        editor.apply();
    }

    public String getFcmToken() {
        return sharedPreferences.getString(Constants.KEY_FCM_TOKEN, null);
    }

    public String getUserId() {
        return sharedPreferences.getString(Constants.KEY_USER_ID, null);
    }

    public String getUserName() {
        return sharedPreferences.getString(Constants.KEY_USER_NAME, null);
    }

    public String getUserEmail() {
        return sharedPreferences.getString(Constants.KEY_USER_EMAIL, null);
    }

    public String getUserRole() {
        return sharedPreferences.getString(Constants.KEY_USER_ROLE, null);
    }

    public boolean isLoggedIn() {
        return sharedPreferences.getBoolean(Constants.KEY_IS_LOGGED_IN, false);
    }

    public void logout() {
        editor.clear();
        editor.apply();
    }
}