package com.example.recipemasterpro.models;

public class Reel {
    private String reelId;
    private String recipeId;
    private String chefId;
    private String chefName;
    private String title;
    private String videoUrl;
    private String thumbnailUrl;
    private String description;
    private int duration;
    private int views;
    private int likes;
    private int comments;
    private int shares;
    private long createdAt;

    public Reel() {}

    public Reel(String recipeId, String chefId, String chefName, String title, String videoUrl) {
        this.recipeId = recipeId;
        this.chefId = chefId;
        this.chefName = chefName;
        this.title = title;
        this.videoUrl = videoUrl;
        this.views = 0;
        this.likes = 0;
        this.comments = 0;
        this.shares = 0;
        this.createdAt = System.currentTimeMillis();
    }

    // Getters and Setters
    public String getReelId() { return reelId; }
    public void setReelId(String reelId) { this.reelId = reelId; }

    public String getRecipeId() { return recipeId; }
    public void setRecipeId(String recipeId) { this.recipeId = recipeId; }

    public String getChefId() { return chefId; }
    public void setChefId(String chefId) { this.chefId = chefId; }

    public String getChefName() { return chefName; }
    public void setChefName(String chefName) { this.chefName = chefName; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getVideoUrl() { return videoUrl; }
    public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }

    public String getThumbnailUrl() { return thumbnailUrl; }
    public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getDuration() { return duration; }
    public void setDuration(int duration) { this.duration = duration; }

    public int getViews() { return views; }
    public void setViews(int views) { this.views = views; }

    public int getLikes() { return likes; }
    public void setLikes(int likes) { this.likes = likes; }

    public int getComments() { return comments; }
    public void setComments(int comments) { this.comments = comments; }

    public int getShares() { return shares; }
    public void setShares(int shares) { this.shares = shares; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}