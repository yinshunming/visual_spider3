package com.example.visualspider.service;

import com.example.visualspider.entity.ExecutionLog;
import com.example.visualspider.entity.ExecutionLog.ExecutionStatus;
import com.example.visualspider.entity.ExecutionLog.TriggerType;
import com.example.visualspider.repository.ExecutionLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class ExecutionLogService {

    private static final Logger log = LoggerFactory.getLogger(ExecutionLogService.class);
    private static final int MAX_ERROR_MESSAGE_LENGTH = 65535;

    @Autowired
    private ExecutionLogRepository executionLogRepository;

    public ExecutionLog createLog(Long taskId, TriggerType type) {
        ExecutionLog logEntry = new ExecutionLog();
        logEntry.setTaskId(taskId);
        logEntry.setTriggerType(type);
        logEntry.setStartedAt(LocalDateTime.now());
        logEntry.setStatus(ExecutionStatus.RUNNING);
        return executionLogRepository.save(logEntry);
    }

    public void updateLog(Long logId, ExecutionStatus status, int itemsCrawled, String errorMessage, long durationMs) {
        Optional<ExecutionLog> optionalLog = executionLogRepository.findById(logId);
        if (optionalLog.isEmpty()) {
            log.warn("ExecutionLog not found for update: logId={}", logId);
            return;
        }
        ExecutionLog logEntry = optionalLog.get();
        logEntry.setStatus(status);
        logEntry.setItemsCrawled(itemsCrawled);
        logEntry.setFinishedAt(LocalDateTime.now());
        logEntry.setDurationMs(durationMs);
        if (errorMessage != null && errorMessage.length() > MAX_ERROR_MESSAGE_LENGTH) {
            logEntry.setErrorMessage(errorMessage.substring(0, MAX_ERROR_MESSAGE_LENGTH));
        } else {
            logEntry.setErrorMessage(errorMessage);
        }
        executionLogRepository.save(logEntry);
    }

    public Page<ExecutionLog> findByTaskId(Long taskId, Pageable pageable) {
        if (taskId == null) {
            return executionLogRepository.findAll(pageable);
        }
        return executionLogRepository.findByTaskIdOrderByStartedAtDesc(taskId, pageable);
    }

    public Page<ExecutionLog> findAll(Pageable pageable) {
        return executionLogRepository.findAll(pageable);
    }

    public Optional<ExecutionLog> findById(Long id) {
        return executionLogRepository.findById(id);
    }
}
