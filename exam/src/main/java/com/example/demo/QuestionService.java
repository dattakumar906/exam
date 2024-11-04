/*package com.example.demo;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;

@Service
public class QuestionService {

    private static final Logger logger = LoggerFactory.getLogger(QuestionService.class);
    private final String bucketName = "rjayb2b";

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private CollegeRepository collegeRepository;

    @Autowired
    private AmazonS3 amazonS3;

    public void saveQuestion(QuestionRequestDTO questionDTO, Long collegeId) throws IOException {
        Question question = new Question();

        // Retrieve the College object using collegeId
        College college = collegeRepository.findById(collegeId)
            .orElseThrow(() -> new RuntimeException("College not found with id: " + collegeId));

        // Set question content as text or image URL
        question.setQuestionContent(handleTextOrImage(questionDTO.getQuestionContent(), "question"));

        // Set options A, B, C, and D as text or image URLs
        question.setOptionA(handleTextOrImage(questionDTO.getOptionA(), "optionA"));
        question.setOptionB(handleTextOrImage(questionDTO.getOptionB(), "optionB"));
        question.setOptionC(handleTextOrImage(questionDTO.getOptionC(), "optionC"));
        question.setOptionD(handleTextOrImage(questionDTO.getOptionD(), "optionD"));

        // Handle correct answer as text or image URL
        question.setCorrectAnswer(handleTextOrImage(questionDTO.getCorrectAnswer(), "correctAnswer"));

        // Set topic and additional fields
        question.setTopic(questionDTO.getTopic());
        question.setCollege(college);

        // Save question to database
        questionRepository.save(question);
        logger.info("Saved question for college ID: {}", collegeId);
    }

private String handleTextOrImage(Object input, String prefix) throws IOException {
    if (input instanceof MultipartFile) {
        MultipartFile file = (MultipartFile) input;

        // File type validation
        if (!isValidImage(file)) {
            throw new IOException("Invalid file type for " + file.getOriginalFilename());
        }

        // Use the original filename without adding the prefix
        String fileName = file.getOriginalFilename();

        // Upload file to S3 bucket and return the file URL
        amazonS3.putObject(new PutObjectRequest(bucketName, fileName, file.getInputStream(), null));
        String fileUrl = amazonS3.getUrl(bucketName, fileName).toString();

        logger.info("Image uploaded to S3: {}", fileUrl); // Log the S3 URL
        return fileUrl;
    } else if (input instanceof String) {
        return (String) input;
    }
    return null;
}

    private boolean isValidImage(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null && contentType.startsWith("image/"); // Basic image check
    }
}*/



package com.example.demo;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;

@Service
public class QuestionService {

    private static final Logger logger = LoggerFactory.getLogger(QuestionService.class);
    private final String bucketName = "rjayb2b";

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private CollegeRepository collegeRepository;

    @Autowired
    private AmazonS3 amazonS3;

    public void saveQuestion(QuestionRequestDTO questionDTO, Long collegeId) throws IOException {
        try {
            logger.info("Saving question for college ID: {}", collegeId);

            // Retrieve the College object using collegeId
            College college = collegeRepository.findById(collegeId)
                    .orElseThrow(() -> new RuntimeException("College not found with id: " + collegeId));

            // Initialize a new Question entity
            Question question = new Question();
            
            // Set question content, options, and correct answer as text or image URLs
            question.setQuestionContent(handleTextOrImage(questionDTO.getQuestionContent(), "question"));
            question.setOptionA(handleTextOrImage(questionDTO.getOptionA(), "optionA"));
            question.setOptionB(handleTextOrImage(questionDTO.getOptionB(), "optionB"));
            question.setOptionC(handleTextOrImage(questionDTO.getOptionC(), "optionC"));
            question.setOptionD(handleTextOrImage(questionDTO.getOptionD(), "optionD"));
            question.setCorrectAnswer(handleTextOrImage(questionDTO.getCorrectAnswer(), "correctAnswer"));

            // Set additional fields
            question.setTopic(questionDTO.getTopic());
            question.setCollege(college);

            // Save question to database
            questionRepository.save(question);
            logger.info("Saved question for college ID: {}", collegeId);
        } catch (Exception e) {
            logger.error("Error saving question: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to save question");
        }
    }

    /**
     * Handle text or image input for a question field.
     * @param input The input object, which can be a String (text) or MultipartFile (image).
     * @param fieldType The field type (e.g., "question", "optionA") to be used in the file name.
     * @return A String containing either the text content or the URL of the uploaded image.
     * @throws IOException if an invalid file type is provided.
     */
public String handleTextOrImage(Object input, String fieldType) throws IOException {
    if (input instanceof MultipartFile) {
        MultipartFile file = (MultipartFile) input;

        if (file.isEmpty()) {
            logger.error("No file provided for field: {}", fieldType);
            throw new IOException("No file provided for " + fieldType);
        }

        String contentType = file.getContentType();
        if (contentType == null || (!contentType.startsWith("image/") && !contentType.equals("text/plain"))) {
            logger.error("Invalid file type for field: {} with content type: {}", fieldType, contentType);
            throw new IOException("Invalid file type for " + fieldType + ": " + contentType);
        }

        // Handle valid image upload
        if (contentType.startsWith("image/")) {
            String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(contentType);
            amazonS3.putObject(new PutObjectRequest(bucketName, fileName, file.getInputStream(), metadata));
            return amazonS3.getUrl(bucketName, fileName).toString();
        }

        // Handle valid text upload
        if (contentType.equals("text/plain")) {
            return new String(file.getBytes(), StandardCharsets.UTF_8);
        }

    } else if (input instanceof String) {
        // If input is a String, assume it is text content and return it directly
        return (String) input;
    }

    // Log and throw error for unsupported types
    logger.error("Unsupported input type for field: {}", fieldType);
    throw new IOException("Unsupported input type for " + fieldType);
}

    /**
     * Check if the file is a valid image format (e.g., JPEG, PNG).
     * @param file MultipartFile to check.
     * @return true if file is a valid image, otherwise false.
     */
    private boolean isValidImage(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null && contentType.startsWith("image/");
    }

    /**
     * Check if the file is a valid text format (e.g., PDF, DOCX, plain text).
     * @param file MultipartFile to check.
     * @return true if file is a valid text file, otherwise false.
     */
    private boolean isValidText(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null &&
               (contentType.equals("text/plain") ||
                contentType.equals("application/pdf") ||
                contentType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document") ||
                contentType.equals("application/msword")); // Support for .doc and .docx
    }



    @Transactional
    public void saveBulkQuestions(MultipartFile file, Long collegeId) throws IOException {
        List<Question> questions = new ArrayList<>();

        // Retrieve the College object
        College college = collegeRepository.findById(collegeId)
                .orElseThrow(() -> new RuntimeException("College not found with id: " + collegeId));

        String fileType = file.getContentType();

        if ("application/pdf".equals(fileType)) {
            questions = processPDF(file, college);
        } else if ("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet".equals(fileType)) {
            questions = processExcel(file, college);
        } else if ("text/plain".equals(fileType)) {
            questions = processTextFile(file, college);
        } else {
            throw new IOException("Unsupported file type");
        }

        // Save all questions to the database in bulk
        questionRepository.saveAll(questions);
        logger.info("Bulk questions saved for college ID: {}", collegeId);
    }

    private List<Question> processPDF(MultipartFile file, College college) throws IOException {
        List<Question> questions = new ArrayList<>();
        try (PDDocument document = PDDocument.load(file.getInputStream())) {
            PDFTextStripper pdfStripper = new PDFTextStripper();
            String pdfText = pdfStripper.getText(document);

            // Basic example: split questions based on some delimiter
            String[] questionBlocks = pdfText.split("Question \\d+:");

            for (String block : questionBlocks) {
                Question question = parseQuestionFromText(block, college);
                questions.add(question);
            }
        }
        return questions;
    }

    private List<Question> processExcel(MultipartFile file, College college) throws IOException {
        List<Question> questions = new ArrayList<>();
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);

            for (Row row : sheet) {
                Question question = new Question();
                question.setQuestionContent(row.getCell(0).getStringCellValue());
                question.setOptionA(row.getCell(1).getStringCellValue());
                question.setOptionB(row.getCell(2).getStringCellValue());
                question.setOptionC(row.getCell(3).getStringCellValue());
                question.setOptionD(row.getCell(4).getStringCellValue());
                question.setCorrectAnswer(row.getCell(5).getStringCellValue());
                question.setTopic(row.getCell(6).getStringCellValue());
                question.setCollege(college);

                questions.add(question);
            }
        }
        return questions;
    }

    private List<Question> processTextFile(MultipartFile file, College college) throws IOException {
        List<Question> questions = new ArrayList<>();
        String content = new String(file.getInputStream().readAllBytes());

        String[] questionBlocks = content.split("Question \\d+:");

        for (String block : questionBlocks) {
            Question question = parseQuestionFromText(block, college);
            questions.add(question);
        }
        return questions;
    }

    private Question parseQuestionFromText(String text, College college) {
        Question question = new Question();
        question.setCollege(college);

        // Example: Parse question text and options using regex or other parsing methods
        question.setQuestionContent(extractContent(text, "Content"));
        question.setOptionA(extractContent(text, "Option A"));
        question.setOptionB(extractContent(text, "Option B"));
        question.setOptionC(extractContent(text, "Option C"));
        question.setOptionD(extractContent(text, "Option D"));
        question.setCorrectAnswer(extractContent(text, "Answer"));

        return question;
    }

    private String extractContent(String text, String label) {
        Pattern pattern = Pattern.compile(label + ":\\s*(.*)");
        Matcher matcher = pattern.matcher(text);
        return matcher.find() ? matcher.group(1).trim() : null;
    }
}
