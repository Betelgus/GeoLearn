package com.example.geolearn.game;

import android.content.Context;
import android.util.Log;

import com.example.geolearn.api.Country;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class FlashcardData {

    private static final String FILE_NAME = "flashcard_data.json";

    /**
     * Reads the JSON file from assets and returns the list of countries
     * for the specific category (e.g., "mountains", "rivers").
     */
    public static List<Country> getCategoryData(Context context, String category) {
        // 1. Load the JSON String from the file
        String jsonString = loadJSONFromAsset(context);
        if (jsonString == null) {
            return new ArrayList<>();
        }

        try {
            // 2. Parse JSON using Gson
            Gson gson = new Gson();

            // The JSON structure is a Map: { "mountains": [List], "rivers": [List] }
            Type type = new TypeToken<Map<String, List<Country>>>(){}.getType();
            Map<String, List<Country>> allData = gson.fromJson(jsonString, type);

            // 3. Get the specific list based on the category key
            if (category.equals("random")) {
                // Special case: If user selected "random", combine all categories
                List<Country> allCountries = new ArrayList<>();
                for (List<Country> list : allData.values()) {
                    allCountries.addAll(list);
                }
                Collections.shuffle(allCountries);
                return allCountries;
            } else if (allData.containsKey(category)) {
                return allData.get(category);
            } else {
                Log.e("FlashcardData", "Category not found in JSON: " + category);
                return new ArrayList<>();
            }

        } catch (Exception e) {
            Log.e("FlashcardData", "Error parsing JSON", e);
            return new ArrayList<>();
        }
    }

    // Helper method to read file from Assets folder
    private static String loadJSONFromAsset(Context context) {
        String json = null;
        try {
            InputStream is = context.getAssets().open(FILE_NAME);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            Log.e("FlashcardData", "Error reading file from assets", ex);
            return null;
        }
        return json;
    }
}