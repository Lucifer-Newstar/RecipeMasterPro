package com.example.recipemasterpro.models;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class User {
    private String userId;
    private String name;
    private String email;
    private String role;

    // Warning and Suspension fields
    private List<Warning> warnings;
    private boolean isSuspended;
    private long suspendedUntil;
    private String suspensionReason;

    // New profile fields
    private String profileImageUrl;
    private String coverImageUrl;
    private String bio;
    private String location;
    private String website;
    private long createdAt;
    private long lastLogin;
    private String fcmToken;

    // Stats
    private int recipeCount;
    private int reelCount;
    private int followersCount;
    private int followingCount;
    private int totalLikes;

    // Lists
    private List<String> followers;
    private List<String> following;
    private List<String> savedRecipes;
    private List<String> likedRecipes;

    // Default constructor (required for Firestore)
    public User() {
        this.followers = new ArrayList<>();
        this.following = new ArrayList<>();
        this.savedRecipes = new ArrayList<>();
        this.likedRecipes = new ArrayList<>();
        this.warnings = new ArrayList<>();
        this.createdAt = System.currentTimeMillis();
        this.isSuspended = false;
    }

    // Original constructor (preserved)
    public User(String userId, String name, String email, String role) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.role = role;
        this.createdAt = System.currentTimeMillis();
        this.followers = new ArrayList<>();
        this.following = new ArrayList<>();
        this.savedRecipes = new ArrayList<>();
        this.likedRecipes = new ArrayList<>();
        this.warnings = new ArrayList<>();
        this.recipeCount = 0;
        this.reelCount = 0;
        this.followersCount = 0;
        this.followingCount = 0;
        this.totalLikes = 0;
        this.isSuspended = false;
    }

    // Full constructor with all fields
    public User(String userId, String name, String email, String role,
                String profileImageUrl, String bio, String location) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.role = role;
        this.profileImageUrl = profileImageUrl;
        this.bio = bio;
        this.location = location;
        this.createdAt = System.currentTimeMillis();
        this.followers = new ArrayList<>();
        this.following = new ArrayList<>();
        this.savedRecipes = new ArrayList<>();
        this.likedRecipes = new ArrayList<>();
        this.warnings = new ArrayList<>();
        this.isSuspended = false;
    }

    // Add Warning inner class
    public static class Warning {
        private String warningId;
        private String adminId;
        private String adminName;
        private String reason;
        private long timestamp;
        private boolean isRead;

        public Warning() {}

        public Warning(String adminId, String adminName, String reason) {
            this.warningId = UUID.randomUUID().toString();
            this.adminId = adminId;
            this.adminName = adminName;
            this.reason = reason;
            this.timestamp = System.currentTimeMillis();
            this.isRead = false;
        }

        // Getters and setters
        public String getWarningId() { return warningId; }
        public void setWarningId(String warningId) { this.warningId = warningId; }

        public String getAdminId() { return adminId; }
        public void setAdminId(String adminId) { this.adminId = adminId; }

        public String getAdminName() { return adminName; }
        public void setAdminName(String adminName) { this.adminName = adminName; }

        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }

        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

        public boolean isRead() { return isRead; }
        public void setRead(boolean read) { isRead = read; }
    }

    // Getters and Setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public List<Warning> getWarnings() { return warnings; }
    public void setWarnings(List<Warning> warnings) { this.warnings = warnings; }

    public boolean isSuspended() { return isSuspended; }
    public void setSuspended(boolean suspended) { isSuspended = suspended; }

    public long getSuspendedUntil() { return suspendedUntil; }
    public void setSuspendedUntil(long suspendedUntil) { this.suspendedUntil = suspendedUntil; }

    public String getSuspensionReason() { return suspensionReason; }
    public void setSuspensionReason(String suspensionReason) { this.suspensionReason = suspensionReason; }

    public String getProfileImageUrl() { return profileImageUrl; }
    public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }

    public String getCoverImageUrl() { return coverImageUrl; }
    public void setCoverImageUrl(String coverImageUrl) { this.coverImageUrl = coverImageUrl; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public long getLastLogin() { return lastLogin; }
    public void setLastLogin(long lastLogin) { this.lastLogin = lastLogin; }

    public String getFcmToken() { return fcmToken; }
    public void setFcmToken(String fcmToken) { this.fcmToken = fcmToken; }

    public int getRecipeCount() { return recipeCount; }
    public void setRecipeCount(int recipeCount) { this.recipeCount = recipeCount; }

    public int getReelCount() { return reelCount; }
    public void setReelCount(int reelCount) { this.reelCount = reelCount; }

    public int getFollowersCount() { return followersCount; }
    public void setFollowersCount(int followersCount) { this.followersCount = followersCount; }

    public int getFollowingCount() { return followingCount; }
    public void setFollowingCount(int followingCount) { this.followingCount = followingCount; }

    public int getTotalLikes() { return totalLikes; }
    public void setTotalLikes(int totalLikes) { this.totalLikes = totalLikes; }

    public List<String> getFollowers() { return followers; }
    public void setFollowers(List<String> followers) { this.followers = followers; }

    public List<String> getFollowing() { return following; }
    public void setFollowing(List<String> following) { this.following = following; }

    public List<String> getSavedRecipes() { return savedRecipes; }
    public void setSavedRecipes(List<String> savedRecipes) { this.savedRecipes = savedRecipes; }

    public List<String> getLikedRecipes() { return likedRecipes; }
    public void setLikedRecipes(List<String> likedRecipes) { this.likedRecipes = likedRecipes; }
}
