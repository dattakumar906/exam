package com.example.demo;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpServletRequest;
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
    private HttpSession httpSession;
    @Autowired
    private HttpServletRequest request;
    
    @Autowired
    private S3Service s3Service;
    
    private static final Logger logger = LoggerFactory.getLogger(ExamController.class);
    
    @GetMapping("/college/AddQuestion")
    public String showAddQuestionPage() {
        return "AddQuestion";  // Returns the AddQuestion.html file in the templates folder
    }
    @GetMapping("/exam/chat")
    public String showAddQuestionPage1() {
        return "chat";  // Returns the AddQuestion.html file in the templates folder
    }


    @GetMapping("/register/{uniqueLink}")
    public String showRegistrationForm(@PathVariable String uniqueLink, Model model) {
        String sessionLink = (String) httpSession.getAttribute("uniqueExamLink");
        if (!uniqueLink.equals(sessionLink)) {
            return "error"; // Display an error page if the link is invalid
        }

        model.addAttribute("userDetails", new UserDetails());
        return "userDetails"; // Thymeleaf template for user registration
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute("userDetails") UserDetails userDetails,
                               BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            return "userDetails"; // Return to the registration form if validation fails
        }

        // Store user details in session for later use
        httpSession.setAttribute("currentUserDetails", userDetails);

        // Redirect to the exam start page
        return "redirect:/exam/start";
    }

    @GetMapping("/select-topic")
    public String selectTopicForm(Model model) {
        List<String> topics = questionRepository.findAllTopics();
        model.addAttribute("topics", topics);
        return "selectTopic"; // Thymeleaf template for topic selection
    }
@PostMapping("/select-topic")
    public String selectTopic(@RequestParam("topic") String topic,
                              @RequestParam("numberOfQuestions") int numberOfQuestions,
                              @RequestParam("duration") int duration,
                              Model model) {
        // Check available questions
        int availableQuestions = questionRepository.countByTopic(topic);
        
        // Ensure the user does not select more questions than available
        if (numberOfQuestions > availableQuestions) {
            model.addAttribute("error", "You cannot select more questions than available. Maximum is " + availableQuestions);
            return "selectTopic";
        }

        // Store selected topic, number of questions, and duration in session
        httpSession.setAttribute("selectedTopic", topic);
        httpSession.setAttribute("numberOfQuestions", numberOfQuestions);
        httpSession.setAttribute("examDuration", duration); // Store duration in minutes

        logger.info("Stored duration in session: " + duration); // Log the duration

        // Generate a unique link for the exam registration
        String uniqueExamLink = UUID.randomUUID().toString();
        httpSession.setAttribute("uniqueExamLink", uniqueExamLink);

        // Construct the full exam registration URL
        String scheme = request.getScheme(); // http or https
        String serverName = request.getServerName(); // localhost or domain name
        int serverPort = request.getServerPort(); // 8080 or 80
        String contextPath = request.getContextPath(); // /your-context-path

        // Create the complete URL
        String examRegistrationUrl = scheme + "://" + serverName + (serverPort != 80 && serverPort != 443 ? ":" + serverPort : "") + contextPath + "/exam/register/" + uniqueExamLink;

        model.addAttribute("examLink", examRegistrationUrl);
        model.addAttribute("numberOfQuestions", numberOfQuestions);
        return "examLink"; // Thymeleaf page that displays the unique exam link
    }

@GetMapping("/start")
public String startExam(Model model, HttpSession httpSession) {
    UserDetails userDetails = (UserDetails) httpSession.getAttribute("currentUserDetails");
    if (userDetails == null) {
        return "redirect:/exam/select-topic";
    }

    // Retrieve selected topic, number of questions, and duration
    String selectedTopic = (String) httpSession.getAttribute("selectedTopic");
    Integer numberOfQuestions = (Integer) httpSession.getAttribute("numberOfQuestions");
    Integer duration = (Integer) httpSession.getAttribute("examDuration");

    if (selectedTopic == null || numberOfQuestions == null || duration == null) {
        return "redirect:/exam/select-topic";
    }

    logger.info("Retrieved duration from session: {}", duration); // Log retrieved duration

    // Set the total exam time in seconds (duration in minutes * 60)
    int totalTimeInSeconds = duration * 60;
    model.addAttribute("totalTimeInSeconds", totalTimeInSeconds);

    // Fetch questions based on the selected topic and number of questions
    List<Question> questions = examService.getRandomQuestionsByTopic(selectedTopic, numberOfQuestions);
    model.addAttribute("questions", questions);

    // Create a list of question IDs to store in session
    List<Long> questionIds = questions.stream()
            .map(Question::getId)
            .collect(Collectors.toList());
    httpSession.setAttribute("currentExamQuestionIds", questionIds);

    // Prepare the exam form
    ExamForm examForm = new ExamForm();
    List<ExamForm.QuestionAnswer> questionAnswers = new ArrayList<>();
    for (Long questionId : questionIds) {
        ExamForm.QuestionAnswer qa = new ExamForm.QuestionAnswer();
        qa.setQuestionId(questionId);
        questionAnswers.add(qa);
    }
    examForm.setQuestionAnswers(questionAnswers);
    model.addAttribute("examForm", examForm);

    // Store the exam start time and total time in the session
    httpSession.setAttribute("examStartTime", System.currentTimeMillis());
    httpSession.setAttribute("examTotalTime", totalTimeInSeconds);
    model.addAttribute("totalTime", totalTimeInSeconds);

    return "index"; // Thymeleaf template for the exam
}
    

@PostMapping("/submit")
    public String submitExam(@ModelAttribute("examForm") ExamForm examForm,
                             BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            return "index"; // Return to the exam form if validation fails
        }

        UserDetails userDetails = (UserDetails) httpSession.getAttribute("currentUserDetails");
        if (userDetails == null) {
            return "redirect:/exam/select-topic";
        }

        List<Long> questionIds = (List<Long>) httpSession.getAttribute("currentExamQuestionIds");
        Integer totalTime = (Integer) httpSession.getAttribute("examTotalTime");
        Long examStartTimeMillis = (Long) httpSession.getAttribute("examStartTime");

        if (questionIds == null || questionIds.isEmpty() || examStartTimeMillis == null || totalTime == null) {
            model.addAttribute("error", "Session expired or invalid. Please start the exam again.");
            return "error"; // Create an error.html template
        }

        long currentTimeMillis = System.currentTimeMillis();
        long elapsedTimeInSeconds = (currentTimeMillis - examStartTimeMillis) / 1000;

        boolean timeExceeded = elapsedTimeInSeconds > totalTime;

        List<ExamForm.QuestionAnswer> questionAnswers = examForm.getQuestionAnswers();
        int score = 0;
        List<String> userAnswers = new ArrayList<>();

        for (ExamForm.QuestionAnswer qa : questionAnswers) {
            userAnswers.add(qa.getAnswer());
        }

        logger.info("Submitted Question IDs: " + questionIds);
        logger.info("Submitted User Answers: " + userAnswers);
        logger.info("Exam Start Time (ms): " + examStartTimeMillis);
        logger.info("Current Time (ms): " + currentTimeMillis);
        logger.info("Elapsed Time (seconds): " + elapsedTimeInSeconds);
        logger.info("Time Exceeded: " + timeExceeded);

        List<Question> questions = examService.getQuestionsByIds(questionIds);

        for (int i = 0; i < questions.size(); i++) {
            Question question = questions.get(i);
            String userAnswer = userAnswers.get(i);
            if (userAnswer != null && userAnswer.equalsIgnoreCase(question.getCorrectAnswer())) {
                score++;
            }
        }

        // Create an ExamResult object and set its fields
        ExamResult examResult = new ExamResult();
        examResult.setName(userDetails.getName());
        examResult.setMobileNo(userDetails.getMobileNo());
        examResult.setEmailId(userDetails.getEmailId());
        examResult.setCollege(userDetails.getCollege());
        examResult.setScore(score);
        examResult.setTotalQuestions(questions.size());
        examResult.setQuestionIds(questionIds);
        examResult.setUserAnswers(userAnswers);
        examResult.setTopic((String) httpSession.getAttribute("selectedTopic")); // Set topic

        // Convert Long to LocalDateTime
        LocalDateTime examStartTime = LocalDateTime.ofEpochSecond(examStartTimeMillis / 1000, 0, ZoneOffset.UTC);
        LocalDateTime currentTime = LocalDateTime.ofEpochSecond(currentTimeMillis / 1000, 0, ZoneOffset.UTC);

        // Set the LocalDateTime fields
        examResult.setStartTime(examStartTime);
        examResult.setEndTime(currentTime);

        // Set the LocalDate field for the exam date
        examResult.setExamDate(examStartTime.toLocalDate()); // Store LocalDate directly

        // Set total time taken
        examResult.setTotalTimeTaken((int) elapsedTimeInSeconds); // Store the total time taken in seconds

        // Save the exam result
        examService.saveExamResult(examResult);

        // Set session attributes for download
        httpSession.setAttribute("score", score);
        httpSession.setAttribute("userAnswers", userAnswers);
        httpSession.setAttribute("currentExamQuestions", questions); // Store questions if needed

        // Clear the session attributes after submission
        httpSession.removeAttribute("currentUserDetails");
        httpSession.removeAttribute("currentExamQuestionIds");
        httpSession.removeAttribute("examStartTime");
        httpSession.removeAttribute("examTotalTime");
        httpSession.removeAttribute("selectedTopic");

        model.addAttribute("score", score);
        model.addAttribute("totalQuestions", questions.size());
        model.addAttribute("questions", questions);
        model.addAttribute("examResult", examResult); 
        return "result"; // Thymeleaf template for the result
    }

    @GetMapping("/leaderboard")
    public String showLeaderboard(Model model) {
        String selectedTopic = (String) httpSession.getAttribute("selectedTopic");
        List<ExamResult> results;

        if (selectedTopic != null) {
            // Fetch top 10 results by topic
            Pageable pageable = PageRequest.of(0, 10); // Page 0 with size 10
            results = examResultRepository.findTop10ByTopic(selectedTopic, pageable);
        } else {
            // Fetch top 10 overall results sorted by score
            Pageable pageable = PageRequest.of(0, 10); // Page 0 with size 10
            results = examResultRepository.findTop10Overall(pageable);
        }

        if (results.isEmpty()) {
            model.addAttribute("message", "No exam results available."); // Handle no results scenario
        } else {
            model.addAttribute("results", results); // Add results to the model for the view
        }

        return "leaderboard"; // Return the leaderboard view
    }



}