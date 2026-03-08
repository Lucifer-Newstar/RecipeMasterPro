package com.example.recipemasterpro.utils;

import android.content.Context;
import android.widget.Toast;
import com.google.firebase.firestore.FirebaseFirestoreException;

public class ErrorHandler {

    public static void handleFirestoreError(Context context, Exception e) {
        if (e instanceof FirebaseFirestoreException) {
            FirebaseFirestoreException fe = (FirebaseFirestoreException) e;
            switch (fe.getCode()) {
                case PERMISSION_DENIED:
                    Toast.makeText(context, "You don't have permission to do this", 
                            Toast.LENGTH_SHORT).show();
                    break;
                case UNAVAILABLE:
                    Toast.makeText(context, "Network unavailable. Please check your connection", 
                            Toast.LENGTH_SHORT).show();
                    break;
                case NOT_FOUND:
                    Toast.makeText(context, "The requested item was not found", 
                            Toast.LENGTH_SHORT).show();
                    break;
                default:
                    Toast.makeText(context, "An error occurred: " + e.getMessage(), 
                            Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(context, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
