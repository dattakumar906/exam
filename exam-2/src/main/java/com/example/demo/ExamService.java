// File: src/main/java/com/example/demo/ExamService.java
package com.example.demo;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class ExamService {
    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private ExamResultRepository examResultRepository;

    public List<Question> getRandomQuestions() {
        return questionRepository.findAll();
    }

    public List<Question> getRandomQuestionsByTopic(String topic) {
        return questionRepository.findRandomQuestionsByTopic(topic);
    }

    public List<Question> getQuestionsByIds(List<Long> ids) {
        List<Question> questions = questionRepository.findAllById(ids);
        Map<Long, Question> questionMap = questions.stream()
                .collect(Collectors.toMap(Question::getId, q -> q));
        return ids.stream()
                .map(questionMap::get)
                .collect(Collectors.toList());
    }

    public void saveExamResult(ExamResult examResult) {
        examResultRepository.save(examResult);
    }

    public List<ExamResult> getTopExamResults(int limit) {
        return examResultRepository.findTopByOrderByScoreDescStartTimeAsc(PageRequest.of(0, limit));
    }
    
     
}
