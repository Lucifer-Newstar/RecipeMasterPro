package com.example.recipemasterpro.utils;

import android.content.Context;
import android.util.Log;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import java.util.HashMap;
import java.util.Map;

public class FCMTokenManager {

    private static final String TAG = "FCMTokenManager";
    private FirebaseFirestore db;
    private SessionManager sessionManager;

    public FCMTokenManager(Context context) {
        db = FirebaseFirestore.getInstance();
        sessionManager = SessionManager.getInstance(context);
    }

    public void initializeToken() {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                        return;
                    }

                    // Get new FCM registration token
                    String token = task.getResult();
                    Log.d(TAG, "FCM Token: " + token);

                    // Save token locally
                    sessionManager.saveFcmToken(token);

                    // Update token in Firestore
                    updateTokenInFirestore(token);
                });
    }

    private void updateTokenInFirestore(String token) {
        String userId = sessionManager.getUserId();
        if (userId == null) return;

        Map<String, Object> updates = new HashMap<>();
        updates.put("fcmToken", token);

        db.collection(Constants.USERS_COLLECTION)
                .document(userId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "FCM token updated in Firestore");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating FCM token", e);
                });
    }

    public void deleteToken() {
        String userId = sessionManager.getUserId();
        if (userId == null) return;

        // Remove token from Firestore
        Map<String, Object> updates = new HashMap<>();
        updates.put("fcmToken", null);

        db.collection(Constants.USERS_COLLECTION)
                .document(userId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "FCM token removed from Firestore");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error removing FCM token", e);
                });

        // Clear local token
        sessionManager.saveFcmToken(null);
    }
}