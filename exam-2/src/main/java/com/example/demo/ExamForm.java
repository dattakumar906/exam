package com.example.demo;

import java.util.List;
import lombok.Data;

@Data
public class ExamForm {
    private List<QuestionAnswer> questionAnswers;

    @Data
    public static class QuestionAnswer {
        private Long questionId;
        private String answer;
    }
}
