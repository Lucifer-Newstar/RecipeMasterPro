package com.example.recipemasterpro.utils;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class SearchHistoryManager {

    private static final String PREF_NAME = "search_history";
    private static final String KEY_HISTORY = "recent_searches";
    private static final int MAX_HISTORY_SIZE = 10;

    private SharedPreferences preferences;
    private Gson gson;

    public SearchHistoryManager(Context context) {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    public void addSearch(String query) {
        List<String> history = getRecentSearches();

        // Remove if already exists
        history.remove(query);

        // Add to beginning
        history.add(0, query);

        // Keep only last MAX_HISTORY_SIZE
        if (history.size() > MAX_HISTORY_SIZE) {
            history = history.subList(0, MAX_HISTORY_SIZE);
        }

        saveHistory(history);
    }

    public List<String> getRecentSearches() {
        String json = preferences.getString(KEY_HISTORY, "");
        if (json.isEmpty()) {
            return new ArrayList<>();
        }

        Type type = new TypeToken<List<String>>(){}.getType();
        return gson.fromJson(json, type);
    }

    public void clearHistory() {
        preferences.edit().remove(KEY_HISTORY).apply();
    }

    public void removeSearch(String query) {
        List<String> history = getRecentSearches();
        history.remove(query);
        saveHistory(history);
    }

    private void saveHistory(List<String> history) {
        String json = gson.toJson(history);
        preferences.edit().putString(KEY_HISTORY, json).apply();
    }
}