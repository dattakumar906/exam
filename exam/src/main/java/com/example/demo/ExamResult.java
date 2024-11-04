// File: src/main/java/com/example/demo/ExamResult.java
package com.example.demo;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
    private Integer totalTimeTaken; // Total time taken to complete the exam

    private int score;
    private int totalQuestions; // Total number of questions

    @ElementCollection
    private List<Long> questionIds; // List of question IDs

    @ElementCollection
    private List<String> userAnswers; // Store user's selected answers

    private LocalDate examDate; // Local date for the exam date

    private LocalDateTime startTime; // Local date and time
    private LocalDateTime endTime; // Local date and time
    
    private String topic;
    
}
