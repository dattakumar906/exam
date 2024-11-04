/*package com.example.demo;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/questions")
public class QuestionController {

    @Autowired
    private QuestionService questionService;
    
    @PostMapping("/add")
    public ResponseEntity<String> addQuestion(
            @RequestParam("collegeId") Long collegeId,
            @RequestPart(value = "question", required = false) MultipartFile questionFile,
            @RequestParam(value = "questionText", required = false) String questionText,
            @RequestPart(value = "optionA", required = false) MultipartFile optionAFile,
            @RequestParam(value = "optionAText", required = false) String optionAText,
            @RequestPart(value = "optionB", required = false) MultipartFile optionBFile,
            @RequestParam(value = "optionBText", required = false) String optionBText,
            @RequestPart(value = "optionC", required = false) MultipartFile optionCFile,
            @RequestParam(value = "optionCText", required = false) String optionCText,
            @RequestPart(value = "optionD", required = false) MultipartFile optionDFile,
            @RequestParam(value = "optionDText", required = false) String optionDText,
            @RequestPart(value = "correctAnswerFile", required = false) MultipartFile correctAnswerFile,
            @RequestParam(value = "correctAnswerText", required = false) String correctAnswerText,
            @RequestParam(value = "topic") String topic) throws IOException { // Added topic parameter

        // Create a new question object
        QuestionRequestDTO questionDTO = new QuestionRequestDTO();

        // Set question and options as text or image based on input
        questionDTO.setQuestionContent(questionFile != null ? questionFile : questionText);
        questionDTO.setOptionA(optionAFile != null ? optionAFile : optionAText);
        questionDTO.setOptionB(optionBFile != null ? optionBFile : optionBText);
        questionDTO.setOptionC(optionCFile != null ? optionCFile : optionCText);
        questionDTO.setOptionD(optionDFile != null ? optionDFile : optionDText);
        questionDTO.setCorrectAnswer(correctAnswerFile != null ? correctAnswerFile : correctAnswerText); // Handle correct answer
        questionDTO.setTopic(topic); // Set the topic

        // Call service to save question using collegeId
        questionService.saveQuestion(questionDTO, collegeId);

        return new ResponseEntity<>("Question added successfully", HttpStatus.CREATED);
    }
}*/


package com.example.demo;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/questions")
public class QuestionController {

    @Autowired
    private QuestionService questionService;

    // Endpoint for adding a single question
@PostMapping("/add")
public ResponseEntity<String> addQuestion(
        @RequestParam("collegeId") Long collegeId,
        @RequestPart(value = "question", required = false) MultipartFile questionFile,
        @RequestParam(value = "questionText", required = false) String questionText,
        @RequestPart(value = "optionA", required = false) MultipartFile optionAFile,
        @RequestParam(value = "optionAText", required = false) String optionAText,
        @RequestPart(value = "optionB", required = false) MultipartFile optionBFile,
        @RequestParam(value = "optionBText", required = false) String optionBText,
        @RequestPart(value = "optionC", required = false) MultipartFile optionCFile,
        @RequestParam(value = "optionCText", required = false) String optionCText,
        @RequestPart(value = "optionD", required = false) MultipartFile optionDFile,
        @RequestParam(value = "optionDText", required = false) String optionDText,
        @RequestPart(value = "correctAnswerFile", required = false) MultipartFile correctAnswerFile,
        @RequestParam(value = "correctAnswerText", required = false) String correctAnswerText,
        @RequestParam(value = "topic") String topic) throws IOException {

    // Validate inputs
    boolean isTextProvided = (questionText != null || questionFile != null) &&
                             (optionAText != null || optionAFile != null) &&
                             (optionBText != null || optionBFile != null) &&
                             (optionCText != null || optionCFile != null) &&
                             (optionDText != null || optionDFile != null) &&
                             (correctAnswerText != null || correctAnswerFile != null);

    if (!isTextProvided) {
        return ResponseEntity.badRequest().body("At least one input for each field must be provided as text or a file.");
    }

    // Create a new QuestionRequestDTO object
    QuestionRequestDTO questionDTO = new QuestionRequestDTO();

    // Set question and options as text or image based on input
    questionDTO.setQuestionContent(questionFile != null ? questionFile : questionText);
    questionDTO.setOptionA(optionAFile != null ? optionAFile : optionAText);
    questionDTO.setOptionB(optionBFile != null ? optionBFile : optionBText);
    questionDTO.setOptionC(optionCFile != null ? optionCFile : optionCText);
    questionDTO.setOptionD(optionDFile != null ? optionDFile : optionDText);
    questionDTO.setCorrectAnswer(correctAnswerFile != null ? correctAnswerFile : correctAnswerText);
    questionDTO.setTopic(topic);

    // Call the service to save the question using collegeId
    questionService.saveQuestion(questionDTO, collegeId);

    return new ResponseEntity<>("Question added successfully", HttpStatus.CREATED);
}

    // New endpoint for adding bulk questions from PDF, Excel, or Text files
    @PostMapping("/bulk-add")
    public ResponseEntity<String> addBulkQuestions(
            @RequestParam("collegeId") Long collegeId,
            @RequestPart("file") MultipartFile file) throws IOException {

        // Call the service to process and save bulk questions
        questionService.saveBulkQuestions(file, collegeId);

        return new ResponseEntity<>("Bulk questions added successfully", HttpStatus.CREATED);
    }
}
