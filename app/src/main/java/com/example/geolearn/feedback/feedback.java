package com.example.geolearn.feedback;

// Update your feedback.java file
public class feedback {
    public String feedbackText;
    public float rating;
    public long timestamp;
    public String userId;
    public String documentId;

    // Add this field so the Adapter can show the name
    public String username;

    public feedback() {}

    public feedback(String feedbackText, float rating, long timestamp, String userId) {
        this.feedbackText = feedbackText;
        this.rating = rating;
        this.timestamp = timestamp;
        this.userId = userId;
    }
}