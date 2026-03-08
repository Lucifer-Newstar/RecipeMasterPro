package com.example.recipemasterpro.models;

public class Report {
    private String reportId;
    private String targetId;
    private String targetType; // "recipe", "reel", "comment", "user"
    private String reporterId;
    private String reporterName;
    private String reason;
    private long timestamp;
    private String status; // "pending", "resolved", "dismissed"
    private String adminNote;
    private String contentPreview; // Title of recipe, or text of comment

    public Report() {}

    public Report(String targetId, String targetType, String reporterId, String reporterName, String reason, String contentPreview) {
        this.targetId = targetId;
        this.targetType = targetType;
        this.reporterId = reporterId;
        this.reporterName = reporterName;
        this.reason = reason;
        this.contentPreview = contentPreview;
        this.timestamp = System.currentTimeMillis();
        this.status = "pending";
    }

    // Getters and Setters
    public String getReportId() { return reportId; }
    public void setReportId(String reportId) { this.reportId = reportId; }

    public String getTargetId() { return targetId; }
    public void setTargetId(String targetId) { this.targetId = targetId; }

    public String getTargetType() { return targetType; }
    public void setTargetType(String targetType) { this.targetType = targetType; }

    public String getReporterId() { return reporterId; }
    public void setReporterId(String reporterId) { this.reporterId = reporterId; }

    public String getReporterName() { return reporterName; }
    public void setReporterName(String reporterName) { this.reporterName = reporterName; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getAdminNote() { return adminNote; }
    public void setAdminNote(String adminNote) { this.adminNote = adminNote; }

    public String getContentPreview() { return contentPreview; }
    public void setContentPreview(String contentPreview) { this.contentPreview = contentPreview; }
}