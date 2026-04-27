package com.example.visualspider.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 爬虫任务实体类
 */
@Entity
@Table(name = "spider_tasks")
@Getter
@Setter
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

    /**
     * URL模式枚举
     */
    public enum UrlMode {
        /** 列表页模式 */
        LIST_PAGE,
        /** 直接URL模式 */
        DIRECT_URL
    }

    /**
     * 任务状态枚举
     */
    public enum TaskStatus {
        /** 草稿 */
        DRAFT,
        /** 已启用 */
        ENABLED,
        /** 已禁用 */
        DISABLED
    }
}