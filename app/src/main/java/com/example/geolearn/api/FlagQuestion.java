package com.example.geolearn.api;

import com.google.firebase.firestore.Exclude;

import java.util.List;
import java.util.Map;

public class FlagQuestion {

    private String id;
    private Map<String, String> name;
    private Map<String, String> flags;
    private String difficulty;
    private List<String> options;
    private String correctAnswer;

    // Analytics Fields
    private long timesAnswered;
    private long timesCorrect;

    // Required for Firestore deserialization
    public FlagQuestion() {}

    // Getters that map directly to Firestore fields
    public String getId() { return id; }
    public Map<String, String> getName() { return name; }
    public Map<String, String> getFlags() { return flags; }
    public String getDifficulty() { return difficulty; }
    public List<String> getOptions() { return options; }
    public String getCorrectAnswer() { return correctAnswer; }
    public long getTimesAnswered() { return timesAnswered; }
    public long getTimesCorrect() { return timesCorrect; }

    /**
     * Helper methods to easily access nested data.
     * Annotated with @Exclude so Firestore doesn't try to map them.
     */
    @Exclude
    public String getCommonName() {
        return (name != null && name.containsKey("common")) ? name.get("common") : "";
    }

    @Exclude
    public String getFlagUrl() {
        return (flags != null && flags.containsKey("png")) ? flags.get("png") : "";
    }
}
