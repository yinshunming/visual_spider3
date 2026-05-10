package com.example.visualspider.service;

import com.example.visualspider.entity.SpiderTask;
import com.example.visualspider.entity.SpiderTask.TaskStatus;
import com.example.visualspider.exception.CrawlException;
import com.example.visualspider.repository.SpiderTaskRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 定时调度服务
 * 每 60 秒扫描 ENABLED + schedule_cron IS NOT NULL + status != RUNNING 的任务，
 * 用 CronExpression.nextExecution() 判断是否应触发
 */
@Service
public class SpiderSchedulerService {

    private static final Logger log = LoggerFactory.getLogger(SpiderSchedulerService.class);

    @Autowired
    private SpiderTaskRepository spiderTaskRepository;

    @Autowired
    private CrawlerEngine crawlerEngine;

    @Value("${spider.schedule.enabled:true}")
    private boolean scheduleEnabled;

    @Value("${spider.crawler.retry-times:2}")
    private int retryTimes;

    /**
     * 每 60 秒扫描一次定时任务
     */
    @Scheduled(fixedRate = 60000)
    public void scanAndTrigger() {
        if (!scheduleEnabled) {
            return;
        }

        log.debug("Scanning for scheduled tasks to trigger...");

        // 查询所有 ENABLED 任务，内存中过滤 schedule_cron 和 status
        List<SpiderTask> tasks = spiderTaskRepository.findByStatus(TaskStatus.ENABLED);
        for (SpiderTask task : tasks) {
            if (task.getScheduleCron() == null || task.getScheduleCron().isBlank()) {
                continue;
            }
            if (task.getStatus() == TaskStatus.RUNNING) {
                continue;
            }

            // 用 CronExpression 计算下次触发时间
            // 5字段 cron 需要转换为 6字段格式（秒 分 时 日 月 星期）
            try {
                String cronExpr = task.getScheduleCron().trim();
                String[] fields = cronExpr.split("\\s+");
                if (fields.length == 5) {
                    cronExpr = "0 " + cronExpr; // prepend second=0
                }
                CronExpression cronExpression = CronExpression.parse(cronExpr);
                LocalDateTime now = LocalDateTime.now();
                // Spring 6: CronExpression.next(Temporal) returns Temporal
                LocalDateTime nextTrigger = cronExpression.next(now);

                // 当前时间已超过（或等于）下次触发时间，则触发
                if (nextTrigger != null && !now.isBefore(nextTrigger)) {
                    triggerTask(task);
                }
            } catch (IllegalArgumentException e) {
                log.error("Invalid cron expression for task {}: {}", task.getId(), task.getScheduleCron(), e);
            } catch (Exception e) {
                log.error("Error checking cron for task {}: {}", task.getId(), e.getMessage(), e);
            }
        }
    }

    /**
     * 触发单个任务：乐观锁 + 重试 + 调用爬虫
     */
    private void triggerTask(SpiderTask task) {
        Long taskId = task.getId();
        log.info("Triggering scheduled task {}: {}", taskId, task.getName());

        // 乐观锁：检查 status != RUNNING 并更新为 RUNNING
        if (!tryAcquireLock(taskId)) {
            log.debug("Task {} is already running, skipping", taskId);
            return;
        }

        boolean crawlSuccess = false;

        // 重试循环：指数退避 1s → 2s → 4s
        for (int i = 0; i <= retryTimes; i++) {
            try {
                // 同步执行爬取
                crawlerEngine.execute(taskId, task);
                crawlSuccess = true;
                break; // 成功，退出重试循环
            } catch (CrawlException e) {
                if (i < retryTimes) {
                    long sleepMs = 1000L * (1 << i);
                    log.warn("Task {} crawl attempt {}/{} failed, retrying in {}ms: {}",
                            taskId, i + 1, retryTimes + 1, sleepMs, e.getMessage());
                    try {
                        Thread.sleep(sleepMs);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                } else {
                    log.error("Task {} failed after {} retries: {}",
                            taskId, retryTimes + 1, e.getMessage());
                }
            } catch (Exception e) {
                // 非网络异常（如配置错误、解析错误）不重试
                log.error("Task {} failed with non-retryable error: {}", taskId, e.getMessage());
                break;
            }
        }

        if (crawlSuccess) {
            // 爬取成功后，调用异步执行（后台保存结果）
            crawlerEngine.executeAsync(taskId, task);
            log.info("Task {} triggered successfully", taskId);
        } else {
            // 所有重试耗尽或非网络异常：回滚状态为 ENABLED
            rollbackStatus(taskId);
        }
    }

    /**
     * 乐观锁获取任务执行锁
     */
    @Transactional
    public boolean tryAcquireLock(Long taskId) {
        SpiderTask task = spiderTaskRepository.findById(taskId).orElse(null);
        if (task == null || task.getStatus() == TaskStatus.RUNNING) {
            return false;
        }
        task.setStatus(TaskStatus.RUNNING);
        spiderTaskRepository.save(task);
        return true;
    }

    /**
     * 回滚任务状态为 ENABLED（当所有重试失败时调用）
     */
    @Transactional
    public void rollbackStatus(Long taskId) {
        spiderTaskRepository.findById(taskId).ifPresent(task -> {
            task.setStatus(TaskStatus.ENABLED);
            spiderTaskRepository.save(task);
            log.info("Task {} status rolled back to ENABLED", taskId);
        });
    }
}
