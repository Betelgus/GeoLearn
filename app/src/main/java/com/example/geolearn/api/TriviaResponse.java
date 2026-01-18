package com.example.geolearn.api;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class TriviaResponse {
    @SerializedName("response_code")
    private int responseCode;

    @SerializedName("results")
    private List<Question> results;

    public int getResponseCode() {
        return responseCode;
    }

    public List<Question> getResults() {
        return results;
    }

    public static class Question {
        private String category;
        private String type;
        private String difficulty;
        private String question;

        @SerializedName("correct_answer")
        private String correctAnswer;

        @SerializedName("incorrect_answers")
        private List<String> incorrectAnswers;

        public String getCategory() {
            return category;
        }

        public String getType() {
            return type;
        }

        public String getDifficulty() {
            return difficulty;
        }

        public String getQuestion() {
            return question;
        }

        public String getCorrectAnswer() {
            return correctAnswer;
        }

        public List<String> getIncorrectAnswers() {
            return incorrectAnswers;
        }
    }
}