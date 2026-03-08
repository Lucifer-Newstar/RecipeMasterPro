package com.example.recipemasterpro.models;

public class Notification {
    private String notificationId;
    private String userId;           // User who receives notification
    private String triggerUserId;     // User who triggered notification
    private String triggerUserName;   // Name of user who triggered
    private String type;              // "follow", "comment", "like", "new_recipe"
    private String message;
    private String recipeId;          // Related recipe (if any)
    private String recipeTitle;       // Recipe title (for display)
    private boolean isRead;
    private long timestamp;

    public Notification() {}

    public Notification(String userId, String triggerUserId, String triggerUserName,
                        String type, String message, String recipeId, String recipeTitle) {
        this.userId = userId;
        this.triggerUserId = triggerUserId;
        this.triggerUserName = triggerUserName;
        this.type = type;
        this.message = message;
        this.recipeId = recipeId;
        this.recipeTitle = recipeTitle;
        this.isRead = false;
        this.timestamp = System.currentTimeMillis();
    }

    // Getters and Setters
    public String getNotificationId() { return notificationId; }
    public void setNotificationId(String notificationId) { this.notificationId = notificationId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getTriggerUserId() { return triggerUserId; }
    public void setTriggerUserId(String triggerUserId) { this.triggerUserId = triggerUserId; }

    public String getTriggerUserName() { return triggerUserName; }
    public void setTriggerUserName(String triggerUserName) { this.triggerUserName = triggerUserName; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getRecipeId() { return recipeId; }
    public void setRecipeId(String recipeId) { this.recipeId = recipeId; }

    public String getRecipeTitle() { return recipeTitle; }
    public void setRecipeTitle(String recipeTitle) { this.recipeTitle = recipeTitle; }

    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}