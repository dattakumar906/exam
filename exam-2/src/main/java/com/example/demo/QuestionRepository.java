// File: src/main/java/com/example/demo/QuestionRepository.java
package com.example.demo;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
    @Query(value = "SELECT DISTINCT topic FROM question", nativeQuery = true)
    List<String> findAllTopics();

    @Query(value = "SELECT * FROM question WHERE topic = ?1 ORDER BY RAND() LIMIT 30", nativeQuery = true)
    List<Question> findRandomQuestionsByTopic(String topic);
}
