package com.example.visualspider.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 爬取内容项实体类
 */
@Entity
@Table(name = "content_items")
@Getter
@Setter
public class ContentItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "task_id", nullable = false)
    private Long taskId;

    @Column(name = "source_url", length = 2048)
    private String sourceUrl;

    @Column(columnDefinition = "jsonb")
    private String fields;

    @Column(name = "raw_html", columnDefinition = "TEXT")
    private String rawHtml;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ContentStatus status = ContentStatus.PENDING;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    /**
     * 内容状态枚举
     */
    public enum ContentStatus {
        /** 已发布 */
        PUBLISHED,
        /** 待处理 */
        PENDING,
        /** 已删除 */
        DELETED
    }

    // Manual getters and setters (Lombok not working in build)
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

    public ContentStatus getStatus() { return status; }
    public void setStatus(ContentStatus status) { this.status = status; }

    public LocalDateTime getPublishedAt() { return publishedAt; }
    public void setPublishedAt(LocalDateTime publishedAt) { this.publishedAt = publishedAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}