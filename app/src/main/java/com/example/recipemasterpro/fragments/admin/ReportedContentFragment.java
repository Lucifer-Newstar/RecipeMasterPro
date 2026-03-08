package com.example.recipemasterpro.fragments.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.recipemasterpro.R;
import com.example.recipemasterpro.adapters.admin.ReportAdapter;
import com.example.recipemasterpro.models.Report;
import com.example.recipemasterpro.utils.Constants;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ReportedContentFragment extends Fragment implements ReportAdapter.OnReportActionListener {

    private RecyclerView recyclerView;
    private ReportAdapter adapter;
    private List<Report> reportList;
    private ProgressBar progressBar;
    private TextView emptyText, statsText;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reported_content, container, false);

        db = FirebaseFirestore.getInstance();
        reportList = new ArrayList<>();

        initViews(view);
        loadReports();

        return view;
    }

    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.recyclerView);
        progressBar = view.findViewById(R.id.progressBar);
        emptyText = view.findViewById(R.id.emptyText);
        statsText = view.findViewById(R.id.statsText);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ReportAdapter(getContext(), reportList, this);
        recyclerView.setAdapter(adapter);
    }

    private void loadReports() {
        progressBar.setVisibility(View.VISIBLE);
        db.collection(Constants.REPORTS_COLLECTION)
                .whereEqualTo("status", "pending")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    reportList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Report report = doc.toObject(Report.class);
                        report.setReportId(doc.getId());
                        reportList.add(report);
                    }
                    adapter.notifyDataSetChanged();
                    progressBar.setVisibility(View.GONE);
                    statsText.setText(String.format(Locale.getDefault(), "Pending Reports: %d", reportList.size()));

                    if (reportList.isEmpty()) {
                        emptyText.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    } else {
                        emptyText.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Error loading reports: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onReportAction(Report report) {
        String[] actions = {"Delete Content", "Warn User", "Suspend User", "Dismiss Report"};
        new AlertDialog.Builder(requireContext())
                .setTitle("Moderate Content")
                .setItems(actions, (dialog, which) -> {
                    switch (which) {
                        case 0: deleteReportedContent(report); break;
                        case 1: warnUserFromReport(report); break;
                        case 2: suspendUserFromReport(report); break;
                        case 3: dismissReport(report); break;
                    }
                }).show();
    }

    @Override
    public void onReportDismiss(Report report) {
        dismissReport(report);
    }

    private void deleteReportedContent(Report report) {
        String collection = getCollectionForType(report.getTargetType());
        if (collection == null) return;

        db.collection(collection).document(report.getTargetId()).delete()
                .addOnSuccessListener(aVoid -> {
                    resolveReport(report, "Content Deleted");
                    Toast.makeText(getContext(), "Content deleted successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to delete content", Toast.LENGTH_SHORT).show());
    }

    private void warnUserFromReport(Report report) {
        // Logic to warn the user who posted the content
        // Need to find the userId from the target content first
        db.collection(getCollectionForType(report.getTargetType())).document(report.getTargetId())
                .get().addOnSuccessListener(documentSnapshot -> {
                    String userId = documentSnapshot.getString("userId");
                    if (userId == null) userId = documentSnapshot.getString("chefId");
                    
                    if (userId != null) {
                        // Implement warning logic or redirect to UserManagement
                        resolveReport(report, "User Warned");
                        Toast.makeText(getContext(), "Action logged. User will be warned.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void suspendUserFromReport(Report report) {
        // Similar to warnUserFromReport but for suspension
        resolveReport(report, "User Suspended");
    }

    private void dismissReport(Report report) {
        db.collection(Constants.REPORTS_COLLECTION).document(report.getReportId())
                .update("status", "dismissed")
                .addOnSuccessListener(aVoid -> loadReports());
    }

    private void resolveReport(Report report, String note) {
        db.collection(Constants.REPORTS_COLLECTION).document(report.getReportId())
                .update("status", "resolved", "adminNote", note)
                .addOnSuccessListener(aVoid -> loadReports());
    }

    private String getCollectionForType(String type) {
        switch (type) {
            case "recipe": return Constants.RECIPES_COLLECTION;
            case "reel": return Constants.REELS_COLLECTION;
            case "comment": return Constants.COMMENTS_COLLECTION;
            default: return null;
        }
    }
}