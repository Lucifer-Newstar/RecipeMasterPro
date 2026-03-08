package com.example.recipemasterpro.models;

public class Comment {
    private String commentId;
    private String recipeId;
    private String userId;
    private String userName;
    private String userImageUrl;
    private String comment;
    private long timestamp;

    public Comment() {}

    public Comment(String recipeId, String userId, String userName, String comment) {
        this.recipeId = recipeId;
        this.userId = userId;
        this.userName = userName;
        this.comment = comment;
        this.timestamp = System.currentTimeMillis();
    }

    // Getters and Setters
    public String getCommentId() { return commentId; }
    public void setCommentId(String commentId) { this.commentId = commentId; }

    public String getRecipeId() { return recipeId; }
    public void setRecipeId(String recipeId) { this.recipeId = recipeId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getUserImageUrl() { return userImageUrl; }
    public void setUserImageUrl(String userImageUrl) { this.userImageUrl = userImageUrl; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}