package com.example.visualspider.repository;

import com.example.visualspider.entity.SpiderField;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpiderFieldRepository extends JpaRepository<SpiderField, Long> {
    // Find by task ID, ordered by display_order
    List<SpiderField> findByTaskIdOrderByDisplayOrder(Long taskId);

    // Find by task ID
    List<SpiderField> findByTaskId(Long taskId);

    // Delete by task ID
    void deleteByTaskId(Long taskId);

    // Count by task ID
    long countByTaskId(Long taskId);
}