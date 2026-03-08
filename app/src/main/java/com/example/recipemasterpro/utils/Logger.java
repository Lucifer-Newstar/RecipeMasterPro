package com.example.recipemasterpro.utils;

import android.util.Log;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

public class Logger {
    
    private static final boolean DEBUG = true; // Set to false for release

    public static void d(String tag, String message) {
        if (DEBUG) {
            Log.d(tag, message);
        }
    }

    public static void e(String tag, String message, Throwable throwable) {
        if (DEBUG) {
            Log.e(tag, message, throwable);
        } else {
            FirebaseCrashlytics.getInstance().recordException(throwable);
        }
    }
}
