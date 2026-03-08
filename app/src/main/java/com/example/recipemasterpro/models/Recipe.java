package com.example.recipemasterpro.models;

import java.util.List;
import java.util.ArrayList;

public class Recipe {
    private String recipeId;
    private String title;
    private String description;
    private String chefId;
    private String chefName;
    private String category;
    private String cuisine;
    private String difficulty;
    private int prepTime;
    private int cookTime;
    private int servings;
    private List<Ingredient> ingredients;
    private List<Step> steps;
    private String thumbnailUrl;
    private List<String> imageUrls;
    private List<String> tags;
    private float averageRating;
    private int totalRatings;
    private int views;
    private int likes;
    private int shares;
    private int saves;
    private long createdAt;
    private long updatedAt;

    // NEW: Video fields
    private String fullVideoUrl;        // Full recipe video
    private List<String> stepVideoUrls; // Step-by-step videos
    private String reelVideoUrl;        // Short reel video
    private int reelDuration;            // Duration in seconds

    public Recipe() {
        this.ingredients = new ArrayList<>();
        this.steps = new ArrayList<>();
        this.imageUrls = new ArrayList<>();
        this.tags = new ArrayList<>();
        this.stepVideoUrls = new ArrayList<>();
    }

    // Getters and Setters for existing fields...

    public String getRecipeId() { return recipeId; }
    public void setRecipeId(String recipeId) { this.recipeId = recipeId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getChefId() { return chefId; }
    public void setChefId(String chefId) { this.chefId = chefId; }

    public String getChefName() { return chefName; }
    public void setChefName(String chefName) { this.chefName = chefName; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getCuisine() { return cuisine; }
    public void setCuisine(String cuisine) { this.cuisine = cuisine; }

    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }

    public int getPrepTime() { return prepTime; }
    public void setPrepTime(int prepTime) { this.prepTime = prepTime; }

    public int getCookTime() { return cookTime; }
    public void setCookTime(int cookTime) { this.cookTime = cookTime; }

    public int getServings() { return servings; }
    public void setServings(int servings) { this.servings = servings; }

    public List<Ingredient> getIngredients() { return ingredients; }
    public void setIngredients(List<Ingredient> ingredients) { this.ingredients = ingredients; }

    public List<Step> getSteps() { return steps; }
    public void setSteps(List<Step> steps) { this.steps = steps; }

    public String getThumbnailUrl() { return thumbnailUrl; }
    public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }

    public List<String> getImageUrls() { return imageUrls; }
    public void setImageUrls(List<String> imageUrls) { this.imageUrls = imageUrls; }

    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }

    public float getAverageRating() { return averageRating; }
    public void setAverageRating(float averageRating) { this.averageRating = averageRating; }

    public int getTotalRatings() { return totalRatings; }
    public void setTotalRatings(int totalRatings) { this.totalRatings = totalRatings; }

    public int getViews() { return views; }
    public void setViews(int views) { this.views = views; }

    public int getLikes() { return likes; }
    public void setLikes(int likes) { this.likes = likes; }

    public int getShares() { return shares; }
    public void setShares(int shares) { this.shares = shares; }

    public int getSaves() { return saves; }
    public void setSaves(int saves) { this.saves = saves; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }

    // NEW: Video getters and setters
    public String getFullVideoUrl() { return fullVideoUrl; }
    public void setFullVideoUrl(String fullVideoUrl) { this.fullVideoUrl = fullVideoUrl; }

    public List<String> getStepVideoUrls() { return stepVideoUrls; }
    public void setStepVideoUrls(List<String> stepVideoUrls) { this.stepVideoUrls = stepVideoUrls; }

    public String getReelVideoUrl() { return reelVideoUrl; }
    public void setReelVideoUrl(String reelVideoUrl) { this.reelVideoUrl = reelVideoUrl; }

    public int getReelDuration() { return reelDuration; }
    public void setReelDuration(int reelDuration) { this.reelDuration = reelDuration; }
}