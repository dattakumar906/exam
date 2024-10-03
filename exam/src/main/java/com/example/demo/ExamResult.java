// File: src/main/java/com/example/demo/ExamResult.java
package com.example.demo;

import java.util.List;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
public class ExamResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // User Details
    private String name;
    private String mobileNo;
    private String emailId;
    private String college;

    private int score;
    private int totalQuestions; // Total number of questions

    @ElementCollection
    private List<Long> questionIds; // List of question IDs

    @ElementCollection
    private List<String> userAnswers; // Store user's selected answers

    private Long startTime; // Epoch time in milliseconds
    private Long endTime; // Epoch time in milliseconds
}
