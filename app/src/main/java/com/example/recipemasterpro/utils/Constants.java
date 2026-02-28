package com.example.recipemasterpro.utils;

public class Constants {
    // Firebase Collections
    public static final String USERS_COLLECTION = "users";
    public static final String RECIPES_COLLECTION = "recipes";
    public static final String RATINGS_COLLECTION = "ratings";
    public static final String COMMENTS_COLLECTION = "comments";

    // User Roles
    public static final String ROLE_ADMIN = "admin";
    public static final String ROLE_CHEF = "chef";
    public static final String ROLE_USER = "user";

    // Shared Preferences
    public static final String PREF_NAME = "RecipeMasterPrefs";
    public static final String KEY_USER_ID = "userId";
    public static final String KEY_USER_ROLE = "userRole";
    public static final String KEY_IS_LOGGED_IN = "isLoggedIn";

    // Intent Extras
    public static final String EXTRA_RECIPE_ID = "recipeId";
    public static final String EXTRA_USER_ID = "userId";
}