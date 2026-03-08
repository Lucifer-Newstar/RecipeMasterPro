package com.example.recipemasterpro.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import com.example.recipemasterpro.R;
import com.example.recipemasterpro.activities.NotificationActivity;
import com.example.recipemasterpro.activities.ProfileActivity;
import com.example.recipemasterpro.activities.RecipeDetailActivity;
import com.example.recipemasterpro.utils.Constants;
import com.example.recipemasterpro.utils.SessionManager;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import java.util.HashMap;
import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "FCM Service";
    private static final String CHANNEL_ID = "recipe_master_channel";
    private static final String CHANNEL_NAME = "Recipe Master Notifications";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data: " + remoteMessage.getData());
            handleDataMessage(remoteMessage.getData());
        }

        // Check if message contains a notification payload
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
            sendNotification(remoteMessage.getNotification().getTitle(),
                    remoteMessage.getNotification().getBody(),
                    remoteMessage.getData());
        }
    }

    private void handleDataMessage(Map<String, String> data) {
        String title = data.get("title");
        String message = data.get("message");
        String type = data.get("type");

        // Default title if not provided
        if (title == null) {
            if (type != null) {
                switch (type) {
                    case Constants.NOTIFICATION_TYPE_FOLLOW:
                        title = "New Follower";
                        break;
                    case Constants.NOTIFICATION_TYPE_COMMENT:
                        title = "New Comment";
                        break;
                    case Constants.NOTIFICATION_TYPE_LIKE:
                        title = "New Like";
                        break;
                    case Constants.NOTIFICATION_TYPE_NEW_RECIPE:
                        title = "New Recipe";
                        break;
                    default:
                        title = "Recipe Master";
                }
            } else {
                title = "Recipe Master";
            }
        }

        sendNotification(title, message, data);
    }

    private void sendNotification(String title, String messageBody, Map<String, String> data) {
        Intent intent;

        // Determine which activity to open based on notification type
        String type = data.get("type");
        if (Constants.NOTIFICATION_TYPE_FOLLOW.equals(type)) {
            // Open profile of the follower
            intent = new Intent(this, ProfileActivity.class);
            intent.putExtra("userId", data.get("triggerUserId"));
        } else if (Constants.NOTIFICATION_TYPE_COMMENT.equals(type) ||
                Constants.NOTIFICATION_TYPE_LIKE.equals(type) ||
                Constants.NOTIFICATION_TYPE_NEW_RECIPE.equals(type)) {
            // Open recipe detail
            intent = new Intent(this, RecipeDetailActivity.class);
            intent.putExtra("recipeId", data.get("recipeId"));
        } else {
            // Default to notifications list
            intent = new Intent(this, NotificationActivity.class);
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_notification)
                        .setContentTitle(title)
                        .setContentText(messageBody)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent)
                        .setPriority(NotificationCompat.PRIORITY_HIGH);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Create notification channel for Android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Recipe Master Notifications");
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify((int) System.currentTimeMillis(), notificationBuilder.build());
    }

    @Override
    public void onNewToken(String token) {
        Log.d(TAG, "Refreshed token: " + token);
        
        // Save token to SessionManager
        SessionManager.getInstance(this).saveFcmToken(token);
        
        sendRegistrationToServer(token);
    }

    private void sendRegistrationToServer(String token) {
        // Store token in Firestore for the current user
        String userId = SessionManager.getInstance(this).getUserId();
        if (userId != null) {
            FirebaseFirestore.getInstance().collection(Constants.USERS_COLLECTION)
                    .document(userId)
                    .update("fcmToken", token)
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Token updated in Firestore"))
                    .addOnFailureListener(e -> Log.e(TAG, "Failed to update token", e));
        }
    }
}