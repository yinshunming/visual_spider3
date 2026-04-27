package com.example.visualspider.repository;

import com.example.visualspider.entity.SpiderTask;
import com.example.visualspider.entity.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpiderTaskRepository extends JpaRepository<SpiderTask, Long> {
    // Find by status
    List<SpiderTask> findByStatus(TaskStatus status);

    // Find by status with pagination
    Page<SpiderTask> findByStatus(TaskStatus status, Pageable pageable);

    // Find all with pagination (inherited from JpaRepository)
}