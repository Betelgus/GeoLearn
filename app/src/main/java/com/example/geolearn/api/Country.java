package com.example.geolearn.api;

import java.util.List;

public class Country {
    public Name name;
    public List<String> capital; // JSON capital is an array ["..."]
    public String region;
    public Flags flags;

    // Inner class for "name": { "common": "..." }
    public static class Name {
        public String common;
    }

    // Inner class for "flags": { "png": "..." }
    public static class Flags {
        public String png; // Now stores the drawable name (e.g., "everest")
    }
}