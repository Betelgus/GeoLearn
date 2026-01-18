package com.example.geolearn.profile;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class BookmarkManager {
    private static final String PREF_NAME = "GeoLearnBookmarks";
    private static final String KEY_BOOKMARKS = "saved_questions";

    // Save a question
    public static void addBookmark(Context context, String questionText) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        Set<String> bookmarks = prefs.getStringSet(KEY_BOOKMARKS, new HashSet<>());

        // Create a copy to modify (SharedPreferences sets are immutable)
        Set<String> newBookmarks = new HashSet<>(bookmarks);
        newBookmarks.add(questionText);

        prefs.edit().putStringSet(KEY_BOOKMARKS, newBookmarks).apply();
    }

    // Remove a question
    public static void removeBookmark(Context context, String questionText) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        Set<String> bookmarks = prefs.getStringSet(KEY_BOOKMARKS, new HashSet<>());

        Set<String> newBookmarks = new HashSet<>(bookmarks);
        newBookmarks.remove(questionText);

        prefs.edit().putStringSet(KEY_BOOKMARKS, newBookmarks).apply();
    }

    // Check if bookmarked
    public static boolean isBookmarked(Context context, String questionText) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        Set<String> bookmarks = prefs.getStringSet(KEY_BOOKMARKS, new HashSet<>());
        return bookmarks.contains(questionText);
    }

    // Get all bookmarks
    public static ArrayList<String> getBookmarks(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        Set<String> bookmarks = prefs.getStringSet(KEY_BOOKMARKS, new HashSet<>());
        return new ArrayList<>(bookmarks);
    }
}