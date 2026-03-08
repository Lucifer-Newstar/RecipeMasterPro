package com.example.recipemasterpro.adapters.admin;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.recipemasterpro.R;
import com.example.recipemasterpro.models.Report;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.ReportViewHolder> {

    private Context context;
    private List<Report> reportList;
    private OnReportActionListener listener;

    public interface OnReportActionListener {
        void onReportAction(Report report);
        void onReportDismiss(Report report);
    }

    public ReportAdapter(Context context, List<Report> reportList, OnReportActionListener listener) {
        this.context = context;
        this.reportList = reportList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_report, parent, false);
        return new ReportViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReportViewHolder holder, int position) {
        Report report = reportList.get(position);

        holder.typeText.setText(report.getTargetType().toUpperCase());
        holder.contentPreviewText.setText(report.getContentPreview());
        holder.reasonText.setText("Reason: " + report.getReason());
        holder.reporterText.setText("Reported by: " + report.getReporterName());

        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        holder.timestampText.setText(sdf.format(new Date(report.getTimestamp())));

        holder.actionButton.setOnClickListener(v -> {
            if (listener != null) listener.onReportAction(report);
        });

        holder.dismissButton.setOnClickListener(v -> {
            if (listener != null) listener.onReportDismiss(report);
        });
    }

    @Override
    public int getItemCount() {
        return reportList.size();
    }

    static class ReportViewHolder extends RecyclerView.ViewHolder {
        TextView typeText, contentPreviewText, reasonText, reporterText, timestampText;
        Button actionButton, dismissButton;

        ReportViewHolder(@NonNull View itemView) {
            super(itemView);
            typeText = itemView.findViewById(R.id.typeText);
            contentPreviewText = itemView.findViewById(R.id.contentPreviewText);
            reasonText = itemView.findViewById(R.id.reasonText);
            reporterText = itemView.findViewById(R.id.reporterText);
            timestampText = itemView.findViewById(R.id.timestampText);
            actionButton = itemView.findViewById(R.id.actionButton);
            dismissButton = itemView.findViewById(R.id.dismissButton);
        }
    }
}