package com.example.visualspider.repository;

import com.example.visualspider.entity.ExecutionLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ExecutionLogRepository extends JpaRepository<ExecutionLog, Long> {
    Page<ExecutionLog> findByTaskIdOrderByStartedAtDesc(Long taskId, Pageable pageable);
    Optional<ExecutionLog> findById(Long id);
}
