package com.example.visualspider.service;

import com.example.visualspider.entity.SpiderField;
import com.example.visualspider.entity.SpiderTask;
import com.example.visualspider.repository.SpiderFieldRepository;
import com.example.visualspider.repository.SpiderTaskRepository;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 爬虫引擎服务类
 */
@Service
public class CrawlerEngine {

    private static final Logger log = LoggerFactory.getLogger(CrawlerEngine.class);

    @Autowired
    private SpiderFieldRepository spiderFieldRepository;

    @Autowired
    private ListPageParser listPageParser;

    @Autowired
    private DirectUrlParser directUrlParser;

    @Autowired
    private ContentPageExtractor contentPageExtractor;

    @Autowired
    private ContentService contentService;

    @Autowired
    private SpiderTaskRepository spiderTaskRepository;

    /**
     * 执行爬虫任务
     * @param taskId 任务ID
     * @param task 任务实体
     */
    public void execute(Long taskId, SpiderTask task) {
        log.info("Starting crawl for task {}: {}", taskId, task.getName());

        List<String> urls = new ArrayList<>();

        // Route by URL mode
        if (task.getUrlMode() == SpiderTask.UrlMode.LIST_PAGE) {
            // Parse list page rule JSON
            ListPageParser.ListPageRule rule = listPageParser.parseListPageRule(task.getListPageRule());
            // Crawl all pages to get item URLs
            urls = listPageParser.crawlAllPages(task.getListPageUrl(), rule);
        } else {
            // DIRECT_URL mode - use seed URLs directly
            urls = directUrlParser.getUrls(task.getSeedUrls());
        }

        log.info("Found {} URLs to crawl for task {}", urls.size(), taskId);

        // Get field configurations for this task
        List<SpiderField> fields = spiderFieldRepository.findByTaskIdOrderByDisplayOrder(taskId);

        // Track success/failure counts
        int successCount = 0;
        int failedCount = 0;

        // Extract content from each URL
        for (String url : urls) {
            try {
                // Fetch content page
                Document doc = contentPageExtractor.fetchContentPage(url);

                if (doc != null) {
                    // Extract field values
                    Map<String, Object> extractedFields = contentPageExtractor.extract(doc, fields);

                    // Save content item using ContentService.saveContent()
                    contentService.saveContent(taskId, url, extractedFields, doc.html());
                    successCount++;
                    log.debug("Successfully crawled URL: {}", url);
                } else {
                    failedCount++;
                    log.warn("Failed to fetch document for URL: {}", url);
                }
            } catch (Exception e) {
                failedCount++;
                log.error("Error crawling URL {}: {}", url, e.getMessage());
                // Continue with next URL - don't stop the crawl
            }
        }

        log.info("Crawl completed for task {}: success={}, failed={}", taskId, successCount, failedCount);
    }

    /**
     * 异步执行爬虫任务
     * @param taskId 任务ID
     * @param task 任务实体
     */
    @Async("crawlTaskExecutor")
    public void executeAsync(Long taskId, SpiderTask task) {
        try {
            execute(taskId, task);
        } finally {
            // Restore task status to ENABLED
            SpiderTask existingTask = spiderTaskRepository.findById(taskId).orElse(null);
            if (existingTask != null) {
                existingTask.setStatus(SpiderTask.TaskStatus.ENABLED);
                spiderTaskRepository.save(existingTask);
                log.info("Task {} status restored to ENABLED", taskId);
            }
        }
    }

    // Crawl a single URL
    public Map<String, Object> crawl(String url, SpiderTask task) {
        // Legacy method - delegate to execute
        return Map.of("message", "Use execute() instead");
    }

    // Crawl list page
    public List<String> crawlListPage(String listPageUrl, SpiderTask task) {
        return List.of();
    }
}