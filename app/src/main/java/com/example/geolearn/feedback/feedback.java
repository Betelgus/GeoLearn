package com.example.geolearn.feedback;

public class feedback {
    public String feedbackText;
    public float rating;
    public long timestamp;
    public String userId;
    public String documentId; // To store the Firestore ID for editing later

    // Empty constructor required for Firestore
    public feedback() {}

    public feedback(String feedbackText, float rating, long timestamp, String userId) {
        this.feedbackText = feedbackText;
        this.rating = rating;
        this.timestamp = timestamp;
        this.userId = userId;
    }
}