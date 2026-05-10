package com.example.visualspider.service;

import com.example.visualspider.entity.ExecutionLog;
import com.example.visualspider.entity.ExecutionLog.ExecutionStatus;
import com.example.visualspider.entity.ExecutionLog.TriggerType;
import com.example.visualspider.entity.SpiderField;
import com.example.visualspider.entity.SpiderTask;
import com.example.visualspider.repository.ExecutionLogRepository;
import com.example.visualspider.repository.SpiderFieldRepository;
import com.example.visualspider.repository.SpiderTaskRepository;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

    @Autowired
    private ExecutionLogRepository executionLogRepository;

    public void execute(Long taskId, SpiderTask task, TriggerType triggerType) {
        ExecutionLog executionLog = null;
        long startTime = System.currentTimeMillis();
        int itemsCrawled = 0;
        String errorMessage = null;

        try {
            log.info("Starting crawl for task {}: {}", taskId, task.getName());

            try {
                executionLog = new ExecutionLog();
                executionLog.setTaskId(taskId);
                executionLog.setTriggerType(triggerType);
                executionLog.setStartedAt(LocalDateTime.now());
                executionLog.setStatus(ExecutionStatus.RUNNING);
                executionLog = executionLogRepository.save(executionLog);
            } catch (Exception e) {
                log.warn("Failed to create execution log for task {}: {}", taskId, e.getMessage());
            }

            List<String> urls = new ArrayList<>();

            if (task.getUrlMode() == SpiderTask.UrlMode.LIST_PAGE) {
                ListPageParser.ListPageRule rule = listPageParser.parseListPageRule(task.getListPageRule());
                urls = listPageParser.crawlAllPages(task.getListPageUrl(), rule);
            } else {
                urls = directUrlParser.getUrls(task.getSeedUrls());
            }

            log.info("Found {} URLs to crawl for task {}", urls.size(), taskId);

            List<SpiderField> fields = spiderFieldRepository.findByTaskIdOrderByDisplayOrder(taskId);

            int successCount = 0;
            int failedCount = 0;

            for (String url : urls) {
                try {
                    Document doc = contentPageExtractor.fetchContentPage(url);

                    if (doc != null) {
                        Map<String, Object> extractedFields = contentPageExtractor.extract(doc, fields);
                        contentService.saveContent(taskId, url, extractedFields, doc.html());
                        successCount++;
                        itemsCrawled = successCount;
                        log.debug("Successfully crawled URL: {}", url);
                    } else {
                        failedCount++;
                        log.warn("Failed to fetch document for URL: {}", url);
                    }
                } catch (Exception e) {
                    failedCount++;
                    log.error("Error crawling URL {}: {}", url, e.getMessage());
                }
            }

            log.info("Crawl completed for task {}: success={}, failed={}", taskId, successCount, failedCount);

            long durationMs = System.currentTimeMillis() - startTime;
            updateExecutionLog(executionLog, ExecutionStatus.SUCCESS, itemsCrawled, null, durationMs);

        } catch (Exception e) {
            long durationMs = System.currentTimeMillis() - startTime;
            errorMessage = e.getMessage();
            log.error("Crawl failed for task {}: {}", taskId, e.getMessage(), e);
            updateExecutionLog(executionLog, ExecutionStatus.FAILED, itemsCrawled, errorMessage, durationMs);
            throw e;
        }
    }

    private void updateExecutionLog(ExecutionLog logEntry, ExecutionStatus status, int itemsCrawled, String errorMessage, long durationMs) {
        if (logEntry == null) {
            return;
        }
        try {
            logEntry.setStatus(status);
            logEntry.setFinishedAt(LocalDateTime.now());
            logEntry.setItemsCrawled(itemsCrawled);
            logEntry.setDurationMs(durationMs);
            if (errorMessage != null && errorMessage.length() > 65535) {
                logEntry.setErrorMessage(errorMessage.substring(0, 65535));
            } else {
                logEntry.setErrorMessage(errorMessage);
            }
            executionLogRepository.save(logEntry);
        } catch (Exception e) {
            CrawlerEngine.log.warn("Failed to update execution log {}: {}", logEntry.getId(), e.getMessage());
        }
    }

    public void execute(Long taskId, SpiderTask task) {
        execute(taskId, task, TriggerType.MANUAL);
    }

    @Async("crawlTaskExecutor")
    public void executeAsync(Long taskId, SpiderTask task, TriggerType triggerType) {
        try {
            execute(taskId, task, triggerType);
        } finally {
            SpiderTask existingTask = spiderTaskRepository.findById(taskId).orElse(null);
            if (existingTask != null) {
                existingTask.setStatus(SpiderTask.TaskStatus.ENABLED);
                spiderTaskRepository.save(existingTask);
                log.info("Task {} status restored to ENABLED", taskId);
            }
        }
    }

    public void executeAsync(Long taskId, SpiderTask task) {
        executeAsync(taskId, task, TriggerType.MANUAL);
    }

    public Map<String, Object> crawl(String url, SpiderTask task) {
        return Map.of("message", "Use execute() instead");
    }

    public List<String> crawlListPage(String listPageUrl, SpiderTask task) {
        return List.of();
    }
}