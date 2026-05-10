package com.example.visualspider.dto;

import com.example.visualspider.entity.ExecutionLog;
import com.example.visualspider.entity.ExecutionLog.ExecutionStatus;
import com.example.visualspider.entity.ExecutionLog.TriggerType;

import java.time.LocalDateTime;

public class ExecutionResponse {

    private Long id;
    private Long taskId;
    private TriggerType triggerType;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
    private ExecutionStatus status;
    private Integer itemsCrawled;
    private String errorMessage;
    private Long durationMs;

    public ExecutionResponse() {
    }

    public static ExecutionResponse from(ExecutionLog log) {
        ExecutionResponse response = new ExecutionResponse();
        response.setId(log.getId());
        response.setTaskId(log.getTaskId());
        response.setTriggerType(log.getTriggerType());
        response.setStartedAt(log.getStartedAt());
        response.setFinishedAt(log.getFinishedAt());
        response.setStatus(log.getStatus());
        response.setItemsCrawled(log.getItemsCrawled());
        response.setErrorMessage(log.getErrorMessage());
        response.setDurationMs(log.getDurationMs());
        return response;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getTaskId() { return taskId; }
    public void setTaskId(Long taskId) { this.taskId = taskId; }

    public TriggerType getTriggerType() { return triggerType; }
    public void setTriggerType(TriggerType triggerType) { this.triggerType = triggerType; }

    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }

    public LocalDateTime getFinishedAt() { return finishedAt; }
    public void setFinishedAt(LocalDateTime finishedAt) { this.finishedAt = finishedAt; }

    public ExecutionStatus getStatus() { return status; }
    public void setStatus(ExecutionStatus status) { this.status = status; }

    public Integer getItemsCrawled() { return itemsCrawled; }
    public void setItemsCrawled(Integer itemsCrawled) { this.itemsCrawled = itemsCrawled; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public Long getDurationMs() { return durationMs; }
    public void setDurationMs(Long durationMs) { this.durationMs = durationMs; }
}
