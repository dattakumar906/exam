// File: src/main/java/com/example/demo/ExamResultRepository.java
package com.example.demo;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ExamResultRepository extends JpaRepository<ExamResult, Long> {
    List<ExamResult> findTopByOrderByScoreDescStartTimeAsc(Pageable pageable);

 
}
