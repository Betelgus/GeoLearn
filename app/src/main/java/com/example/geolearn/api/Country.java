package com.example.geolearn.api;

import java.util.List;
import java.util.Map;

public class Country {
    public Name name;
    public List<String> tld;
    public String cca2;
    public String ccn3;
    public String cca3;
    public String cioc;
    public boolean independent;
    public String status;
    public boolean unMember;
    public Map<String, Currency> currencies;
    public Idd idd;
    public List<String> capital;
    public List<String> altSpellings;
    public String region;
    public String subregion;
    public Map<String, String> languages;
    public Map<String, Translation> translations;
    public List<Double> latlng;
    public boolean landlocked;
    public Double area;
    public Demonyms demonyms;
    public String flag;
    public Maps maps;
    public int population;
    public Car car;
    public List<String> timezones;
    public List<String> continents;
    public Flags flags;
    public CoatOfArms coatOfArms;
    public String startOfWeek;
    public CapitalInfo capitalInfo;
    public PostalCode postalCode;

    // We add this ID field to store the document ID from Firestore manually
    public String id;
    public String category; // e.g., "easy", "medium", "hard"

    // --- REQUIRED: Empty Constructor for Firebase ---
    public Country() {
    }

    public static class Name {
        public String common;
        public String official;
        public Map<String, NativeName> nativeName;

        // --- REQUIRED: Empty Constructor for Firebase ---
        public Name() {
        }
    }

    public static class NativeName {
        public String official;
        public String common;

        public NativeName() {
        }
    }

    public static class Currency {
        public String name;
        public String symbol;

        public Currency() {
        }
    }

    public static class Idd {
        public String root;
        public List<String> suffixes;

        public Idd() {
        }
    }

    public static class Translation {
        public String official;
        public String common;

        public Translation() {
        }
    }

    public static class Demonyms {
        public Demonym eng;
        public Demonym fra;

        public Demonyms() {
        }
    }

    public static class Demonym {
        public String f;
        public String m;

        public Demonym() {
        }
    }

    public static class Maps {
        public String googleMaps;
        public String openStreetMaps;

        public Maps() {
        }
    }

    public static class Car {
        public List<String> signs;
        public String side;

        public Car() {
        }
    }

    public static class Flags {
        public String png;
        public String svg;
        public String alt;

        // --- REQUIRED: Empty Constructor for Firebase ---
        public Flags() {
        }
    }

    public static class CoatOfArms {
        public String png;
        public String svg;

        public CoatOfArms() {
        }
    }

    public static class CapitalInfo {
        public List<Double> latlng;

        public CapitalInfo() {
        }
    }

    public static class PostalCode {
        public String format;
        public String regex;

        public PostalCode() {
        }
    }
}