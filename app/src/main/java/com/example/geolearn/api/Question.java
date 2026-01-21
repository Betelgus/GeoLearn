package com.example.geolearn.api;

import java.util.List;

public class Question {
    private String id;
    private String questionText;
    private List<String> options;
    private String correctAnswer;
    private String difficulty;

    // Analytics Fields
    private long timesAnswered;
    private long timesCorrect;

    // Required for Firestore deserialization
    public Question() {}

    public String getId() { return id; }
    public String getQuestionText() { return questionText; }
    public List<String> getOptions() { return options; }
    public String getCorrectAnswer() { return correctAnswer; }
    public String getDifficulty() { return difficulty; }

    // Getters for analytics
    public long getTimesAnswered() { return timesAnswered; }
    public long getTimesCorrect() { return timesCorrect; }
}
