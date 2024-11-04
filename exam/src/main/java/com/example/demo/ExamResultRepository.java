package com.example.demo;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ExamResultRepository extends JpaRepository<ExamResult, Long> {
    
    // This method fetches top 10 overall results sorted by score
    @Query(value = "SELECT er FROM ExamResult er ORDER BY er.score DESC, er.startTime ASC")
    List<ExamResult> findTop10Overall(Pageable pageable);

    // Fetch top 10 results by topic
    @Query(value = "SELECT er FROM ExamResult er WHERE er.topic = :topic ORDER BY er.score DESC, er.startTime ASC")
    List<ExamResult> findTop10ByTopic(@Param("topic") String topic, Pageable pageable);
}
