package com.example.recipemasterpro.utils;

import android.util.Log;
import com.example.recipemasterpro.models.Notification;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import java.util.HashMap;
import java.util.Map;

public class NotificationHelper {

    private static final String TAG = "NotificationHelper";
    private FirebaseFirestore db;

    public NotificationHelper() {
        db = FirebaseFirestore.getInstance();
    }

    public interface NotificationCallback {
        void onSuccess();
        void onFailure(String error);
    }

    // Send notification to a specific user
    public void sendNotification(String userId, String triggerUserId, String triggerUserName,
                                 String type, String message, String recipeId, String recipeTitle,
                                 NotificationCallback callback) {

        Notification notification = new Notification(userId, triggerUserId, triggerUserName,
                type, message, recipeId, recipeTitle);

        db.collection(Constants.NOTIFICATIONS_COLLECTION)
                .add(notification)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "Notification saved with ID: " + documentReference.getId());

                    // Also trigger push notification via FCM
                    sendPushNotification(userId, type, message, recipeId);

                    if (callback != null) {
                        callback.onSuccess();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error saving notification", e);
                    if (callback != null) {
                        callback.onFailure(e.getMessage());
                    }
                });
    }

    // Send push notification via FCM (simplified - you'd typically use a cloud function)
    private void sendPushNotification(String userId, String type, String message, String recipeId) {
        // In a real app, you'd use Firebase Cloud Functions to send FCM messages
        // This is a placeholder for the concept
        Log.d(TAG, "Would send push to user: " + userId + " type: " + type);
    }

    // Mark notification as read
    public void markAsRead(String notificationId) {
        db.collection(Constants.NOTIFICATIONS_COLLECTION)
                .document(notificationId)
                .update("isRead", true);
    }

    // Mark all notifications as read for a user
    public void markAllAsRead(String userId) {
        db.collection(Constants.NOTIFICATIONS_COLLECTION)
                .whereEqualTo("userId", userId)
                .whereEqualTo("isRead", false)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (var doc : queryDocumentSnapshots) {
                        doc.getReference().update("isRead", true);
                    }
                });
    }

    // Get unread count for a user
    public void getUnreadCount(String userId, OnUnreadCountListener listener) {
        db.collection(Constants.NOTIFICATIONS_COLLECTION)
                .whereEqualTo("userId", userId)
                .whereEqualTo("isRead", false)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    listener.onCount(queryDocumentSnapshots.size());
                })
                .addOnFailureListener(e -> {
                    listener.onCount(0);
                });
    }

    public interface OnUnreadCountListener {
        void onCount(int count);
    }
}