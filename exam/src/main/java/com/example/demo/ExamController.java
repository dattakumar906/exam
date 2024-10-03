// File: src/main/java/com/example/demo/ExamController.java
package com.example.demo;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
// Import other necessary packages
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/exam")
public class ExamController {
    @Autowired
    private ExamService examService;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private ExamResultRepository examResultRepository;

    @Autowired
    private HttpSession httpSession; // Inject HttpSession

    private static final Logger logger = LoggerFactory.getLogger(ExamController.class);

    /**
     * Display the user registration form.
     */
    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("userDetails", new UserDetails());
        return "userDetails"; // Thymeleaf template for user registration
    }

    /**
     * Handle the submission of user details and redirect to topic selection.
     */
    @PostMapping("/register")
    public String registerUser(@ModelAttribute("userDetails") UserDetails userDetails,
                               BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            return "userDetails"; // Return to the registration form if validation fails
        }

        // Store user details in session for later use
        httpSession.setAttribute("currentUserDetails", userDetails);

        // Redirect to the topic selection page
        return "redirect:/exam/select-topic";
    }

    /**
     * Display topic selection form.
     */
    @GetMapping("/select-topic")
    public String selectTopicForm(Model model) {
        List<String> topics = questionRepository.findAllTopics();
        model.addAttribute("topics", topics);
        return "selectTopic"; // Thymeleaf template for topic selection
    }

    /**
     * Handle topic selection and redirect to the exam.
     */
    @PostMapping("/select-topic")
    public String selectTopic(@org.springframework.web.bind.annotation.RequestParam("topic") String topic, Model model) {
        httpSession.setAttribute("selectedTopic", topic);
        return "redirect:/exam/start";
    }

    /**
     * Display the exam questions with a timer.
     */
    @GetMapping("/start")
    public String startExam(Model model) {
        // Retrieve user details from session
        UserDetails userDetails = (UserDetails) httpSession.getAttribute("currentUserDetails");
        if (userDetails == null) {
            // If user details are not present, redirect to registration
            return "redirect:/exam/register";
        }

        // Retrieve selected topic from session
        String selectedTopic = (String) httpSession.getAttribute("selectedTopic");
        if (selectedTopic == null) {
            return "redirect:/exam/select-topic";
        }

        List<Question> questions = examService.getRandomQuestionsByTopic(selectedTopic);
        model.addAttribute("questions", questions);

        // Store question IDs in session to maintain order
        List<Long> questionIds = questions.stream()
                .map(Question::getId)
                .collect(Collectors.toList());
        httpSession.setAttribute("currentExamQuestionIds", questionIds);

        // Initialize ExamForm with question IDs
        ExamForm examForm = new ExamForm();
        List<ExamForm.QuestionAnswer> questionAnswers = new ArrayList<>();
        for (Long questionId : questionIds) {
            ExamForm.QuestionAnswer qa = new ExamForm.QuestionAnswer();
            qa.setQuestionId(questionId);
            questionAnswers.add(qa);
        }
        examForm.setQuestionAnswers(questionAnswers);
        model.addAttribute("examForm", examForm);

        // Calculate total exam time (1.5 minutes per question)
        int totalTimeInSeconds = questions.size() * 90; // 90 seconds = 1.5 minutes
        httpSession.setAttribute("examStartTime", System.currentTimeMillis());
        httpSession.setAttribute("examTotalTime", totalTimeInSeconds);
        model.addAttribute("totalTime", totalTimeInSeconds);

        return "index"; // Thymeleaf template for the exam
    }

    /**
     * Handle the submission of exam answers.
     */
    @PostMapping("/submit")
    public String submitExam(@ModelAttribute("examForm") ExamForm examForm,
                             BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            return "index"; // Return to the exam form if validation fails
        }

        // Retrieve user details from session
        UserDetails userDetails = (UserDetails) httpSession.getAttribute("currentUserDetails");
        if (userDetails == null) {
            // If user details are missing, redirect to registration
            return "redirect:/exam/register";
        }

        // Retrieve question IDs and exam time from session
        List<Long> questionIds = (List<Long>) httpSession.getAttribute("currentExamQuestionIds");
        Integer totalTime = (Integer) httpSession.getAttribute("examTotalTime");
        Long examStartTime = (Long) httpSession.getAttribute("examStartTime");

        if (questionIds == null || questionIds.isEmpty() || examStartTime == null || totalTime == null) {
            // Handle error: session expired or invalid
            model.addAttribute("error", "Session expired or invalid. Please start the exam again.");
            return "error"; // Create an error.html template
        }

        // Check if the exam is submitted within the allowed time
        long currentTime = System.currentTimeMillis();
        long elapsedTimeInSeconds = (currentTime - examStartTime) / 1000;
        boolean timeExceeded = false;
        if (elapsedTimeInSeconds > totalTime) {
            timeExceeded = true;
        }

        List<ExamForm.QuestionAnswer> questionAnswers = examForm.getQuestionAnswers();
        int score = 0;
        List<String> userAnswers = new ArrayList<>();

        // Collect user answers
        for (ExamForm.QuestionAnswer qa : questionAnswers) {
            userAnswers.add(qa.getAnswer());
        }

        // Logging for debugging
        logger.info("Submitted Question IDs: " + questionIds);
        logger.info("Submitted User Answers: " + userAnswers);
        logger.info("Exam Start Time: " + examStartTime);
        logger.info("Current Time: " + currentTime);
        logger.info("Elapsed Time (seconds): " + elapsedTimeInSeconds);
        logger.info("Time Exceeded: " + timeExceeded);

        // Retrieve the questions based on IDs in the exact order
        List<Question> questions = examService.getQuestionsByIds(questionIds);

        // Calculate score
        for (int i = 0; i < questions.size(); i++) {
            Question question = questions.get(i);
            String userAnswer = userAnswers.get(i);
            if (userAnswer != null && userAnswer.equalsIgnoreCase(question.getAnswer())) {
                score++;
            }
        }

        // Create and save ExamResult
        ExamResult examResult = new ExamResult();
        examResult.setName(userDetails.getName());
        examResult.setMobileNo(userDetails.getMobileNo());
        examResult.setEmailId(userDetails.getEmailId());
        examResult.setCollege(userDetails.getCollege());
        examResult.setScore(score);
        examResult.setTotalQuestions(questions.size());
        examResult.setQuestionIds(questionIds);
        examResult.setUserAnswers(userAnswers);
        examResult.setStartTime(examStartTime);
        examResult.setEndTime(currentTime);
        examService.saveExamResult(examResult);

        // Clear session attributes to prevent reuse
        httpSession.removeAttribute("currentUserDetails");
        httpSession.removeAttribute("currentExamQuestionIds");
        httpSession.removeAttribute("examStartTime");
        httpSession.removeAttribute("examTotalTime");
        httpSession.removeAttribute("selectedTopic");

        // Add attributes for the result view
        model.addAttribute("score", score);
        model.addAttribute("totalQuestions", questions.size());
        model.addAttribute("questions", questions);
        model.addAttribute("examResult", examResult); // To display detailed results
        return "result"; // Thymeleaf template for the result
    }

    /**
     * Display the leaderboard.
     */
    @GetMapping("/leaderboard")
    public String showLeaderboard(Model model) {
        List<ExamResult> topResults = examService.getTopExamResults(10); // Top 10
        model.addAttribute("topResults", topResults);
        return "leaderboard"; // Thymeleaf template for leaderboard
    }
}
