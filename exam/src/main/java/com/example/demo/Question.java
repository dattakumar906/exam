package com.example.demo;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;

@Data
@Entity
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 1000)
    private String questionContent; // Stores question text or image path

    @Column(length = 255)
    private String optionA; // Stores option A text or image path

    @Column(length = 255)
    private String optionB; // Stores option B text or image path

    @Column(length = 255)
    private String optionC; // Stores option C text or image path

    @Column(length = 255)
    private String optionD; // Stores option D text or image path

    @Column(length = 100)
    private String correctAnswer;
    
    @Column(length = 255)
    private String topic;

    @Column(length = 255)
    private String purpose;
    
    private LocalDate examDate; 
    private int duration;
    private String collegeEmail;
    
    @ManyToOne
    @JoinColumn(name = "college_id")
    private College college;
}
