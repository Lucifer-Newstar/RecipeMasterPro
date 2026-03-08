package com.example.recipemasterpro.utils;

import android.content.Context;
import android.widget.Toast;
import com.example.recipemasterpro.models.Report;
import com.google.firebase.firestore.FirebaseFirestore;

public class ReportHelper {
    private Context context;
    private FirebaseFirestore db;
    private SessionManager sessionManager;

    public ReportHelper(Context context) {
        this.context = context;
        this.db = FirebaseFirestore.getInstance();
        this.sessionManager = SessionManager.getInstance(context);
    }

    public void reportContent(String targetId, String targetType, String reason, String contentPreview) {
        String reporterId = sessionManager.getUserId();
        String reporterName = sessionManager.getUserName();

        if (reporterId == null) {
            Toast.makeText(context, "Please login to report content", Toast.LENGTH_SHORT).show();
            return;
        }

        Report report = new Report(targetId, targetType, reporterId, reporterName, reason, contentPreview);

        db.collection(Constants.REPORTS_COLLECTION)
                .add(report)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(context, "Report submitted. Thank you for keeping our community safe.", Toast.LENGTH_LONG).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Failed to submit report. Please try again later.", Toast.LENGTH_SHORT).show();
                });
    }
}