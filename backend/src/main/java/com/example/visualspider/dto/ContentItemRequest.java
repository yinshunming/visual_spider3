package com.example.visualspider.dto;

import jakarta.validation.constraints.NotNull;

/**
 * ContentItem Request DTO
 */
public class ContentItemRequest {

    private Long id;

    @NotNull(message = "taskId is required")
    private Long taskId;

    private String sourceUrl;

    private String fields;

    private String rawHtml;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getTaskId() { return taskId; }
    public void setTaskId(Long taskId) { this.taskId = taskId; }

    public String getSourceUrl() { return sourceUrl; }
    public void setSourceUrl(String sourceUrl) { this.sourceUrl = sourceUrl; }

    public String getFields() { return fields; }
    public void setFields(String fields) { this.fields = fields; }

    public String getRawHtml() { return rawHtml; }
    public void setRawHtml(String rawHtml) { this.rawHtml = rawHtml; }
}
