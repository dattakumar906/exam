// File: src/main/java/com/example/demo/Question.java
package com.example.demo;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
public class Question {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String topic; // New field for topic

    private String questionText;
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;
    private String answer; // Correct answer
}
