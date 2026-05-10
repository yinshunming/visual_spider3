package com.example.visualspider.dto;

import com.example.visualspider.entity.ContentItem.ContentStatus;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

/**
 * ContentItem Request DTO
 */
public class ContentItemRequest {

    private Long id;

    private Long taskId;

    private String sourceUrl;

    private Map<String, Object> fields;

    private String rawHtml;

    private ContentStatus status;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getTaskId() { return taskId; }
    public void setTaskId(Long taskId) { this.taskId = taskId; }

    public String getSourceUrl() { return sourceUrl; }
    public void setSourceUrl(String sourceUrl) { this.sourceUrl = sourceUrl; }

    public Map<String, Object> getFields() { return fields; }
    public void setFields(Map<String, Object> fields) { this.fields = fields; }

    public String getRawHtml() { return rawHtml; }
    public void setRawHtml(String rawHtml) { this.rawHtml = rawHtml; }

    public ContentStatus getStatus() { return status; }
    public void setStatus(ContentStatus status) { this.status = status; }
}
