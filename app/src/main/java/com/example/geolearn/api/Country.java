package com.example.geolearn.api;

import java.util.List;

public class Country {
    public Name name;
    public List<String> capital;
    public String region;
    public Flags flags;

    public static class Name {
        public String common;
    }

    public static class Flags {
        public String png; // URL to the flag image
    }
}
