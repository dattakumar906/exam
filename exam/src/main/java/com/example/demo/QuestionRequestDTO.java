package com.example.demo;

import lombok.Data;

@Data
public class QuestionRequestDTO {
    private Object questionContent; // Store text or image for the question
    private Object optionA;  // Store text or image for option A
    private Object optionB;  // Store text or image for option B
    private Object optionC;  // Store text or image for option C
    private Object optionD;  // Store text or image for option D
    private Object correctAnswer; // Updated to accept MultipartFile or String
    private String topic; // Added field for topic

    // Getters and Setters
}
