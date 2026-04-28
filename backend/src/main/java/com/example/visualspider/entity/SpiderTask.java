package com.example.visualspider.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 爬虫任务实体类
 */
@Entity
@Table(name = "spider_tasks")
public class SpiderTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "url_mode", nullable = false)
    private UrlMode urlMode;

    @Column(name = "list_page_url", length = 2048)
    private String listPageUrl;

    @Column(name = "list_page_rule", columnDefinition = "jsonb")
    private String listPageRule;

    @Column(name = "seed_urls", columnDefinition = "text[]")
    private String[] seedUrls;

    @Column(name = "content_page_rule", columnDefinition = "jsonb")
    private String contentPageRule;

    @Column(name = "schedule_cron", length = 100)
    private String scheduleCron;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskStatus status = TaskStatus.DRAFT;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public UrlMode getUrlMode() { return urlMode; }
    public void setUrlMode(UrlMode urlMode) { this.urlMode = urlMode; }

    public String getListPageUrl() { return listPageUrl; }
    public void setListPageUrl(String listPageUrl) { this.listPageUrl = listPageUrl; }

    public String getListPageRule() { return listPageRule; }
    public void setListPageRule(String listPageRule) { this.listPageRule = listPageRule; }

    public String[] getSeedUrls() { return seedUrls; }
    public void setSeedUrls(String[] seedUrls) { this.seedUrls = seedUrls; }

    public String getContentPageRule() { return contentPageRule; }
    public void setContentPageRule(String contentPageRule) { this.contentPageRule = contentPageRule; }

    public String getScheduleCron() { return scheduleCron; }
    public void setScheduleCron(String scheduleCron) { this.scheduleCron = scheduleCron; }

    public TaskStatus getStatus() { return status; }
    public void setStatus(TaskStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    /**
     * URL模式枚举
     */
    public enum UrlMode {
        LIST_PAGE,
        DIRECT_URL
    }

    /**
     * 任务状态枚举
     */
    public enum TaskStatus {
        DRAFT,
        ENABLED,
        DISABLED
    }
}
