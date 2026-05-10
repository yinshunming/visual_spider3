package com.example.visualspider.dto;

import com.example.visualspider.entity.ContentItem;
import com.example.visualspider.entity.ContentItem.ContentStatus;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * ContentItem Response DTO
 */
public class ContentResponse {

    private Long id;
    private Long taskId;
    private String sourceUrl;
    private Map<String, Object> fields;
    private String rawHtml;
    private ContentStatus status;
    private LocalDateTime publishedAt;
    private LocalDateTime createdAt;

    public ContentResponse() {
    }

    public static ContentResponse from(ContentItem item) {
        ContentResponse response = new ContentResponse();
        response.setId(item.getId());
        response.setTaskId(item.getTaskId());
        response.setSourceUrl(item.getSourceUrl());
        response.setRawHtml(item.getRawHtml());
        response.setStatus(item.getStatus());
        response.setPublishedAt(item.getPublishedAt());
        response.setCreatedAt(item.getCreatedAt());
        response.setFields(item.getFields() != null ? item.getFields() : Map.of());
        return response;
    }

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

    public LocalDateTime getPublishedAt() { return publishedAt; }
    public void setPublishedAt(LocalDateTime publishedAt) { this.publishedAt = publishedAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
