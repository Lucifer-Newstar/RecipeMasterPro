package com.example.recipemasterpro.utils;

import com.example.recipemasterpro.models.Recipe;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

public class StatisticsHelper {

    private static final String TAG = "StatisticsHelper";
    private FirebaseFirestore db;

    public StatisticsHelper() {
        db = FirebaseFirestore.getInstance();
    }

    public void incrementViewCount(String recipeId, Recipe recipe) {
        if (recipe != null) {
            recipe.setViews(recipe.getViews() + 1);
        }
        db.collection(Constants.RECIPES_COLLECTION)
                .document(recipeId)
                .update("views", FieldValue.increment(1));
    }

    public void incrementLikeCount(String recipeId, Recipe recipe) {
        if (recipe != null) {
            recipe.setLikes(recipe.getLikes() + 1);
        }
        db.collection(Constants.RECIPES_COLLECTION)
                .document(recipeId)
                .update("likes", FieldValue.increment(1));
    }

    public void decrementLikeCount(String recipeId, Recipe recipe) {
        if (recipe != null) {
            recipe.setLikes(Math.max(0, recipe.getLikes() - 1));
        }
        db.collection(Constants.RECIPES_COLLECTION)
                .document(recipeId)
                .update("likes", FieldValue.increment(-1));
    }

    public void incrementShareCount(String recipeId, Recipe recipe) {
        if (recipe != null) {
            recipe.setShares(recipe.getShares() + 1);
        }
        db.collection(Constants.RECIPES_COLLECTION)
                .document(recipeId)
                .update("shares", FieldValue.increment(1));
    }

    public void incrementSaveCount(String recipeId, Recipe recipe) {
        if (recipe != null) {
            recipe.setSaves(recipe.getSaves() + 1);
        }
        db.collection(Constants.RECIPES_COLLECTION)
                .document(recipeId)
                .update("saves", FieldValue.increment(1));
    }

    public void decrementSaveCount(String recipeId, Recipe recipe) {
        if (recipe != null) {
            recipe.setSaves(Math.max(0, recipe.getSaves() - 1));
        }
        db.collection(Constants.RECIPES_COLLECTION)
                .document(recipeId)
                .update("saves", FieldValue.increment(-1));
    }
}
