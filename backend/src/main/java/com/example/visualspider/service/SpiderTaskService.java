package com.example.visualspider.service;

import com.example.visualspider.dto.*;
import com.example.visualspider.entity.ExecutionLog.TriggerType;
import com.example.visualspider.entity.SpiderField;
import com.example.visualspider.entity.SpiderTask;
import com.example.visualspider.entity.SpiderTask.TaskStatus;
import com.example.visualspider.repository.SpiderFieldRepository;
import com.example.visualspider.repository.SpiderTaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SpiderTaskService {

    @Autowired
    private SpiderTaskRepository spiderTaskRepository;

    @Autowired
    private SpiderFieldRepository spiderFieldRepository;

    @Autowired
    private CrawlerEngine crawlerEngine;

    public Page<SpiderTask> findAll(Pageable pageable) {
        return spiderTaskRepository.findAll(pageable);
    }

    public Optional<SpiderTask> findById(Long id) {
        return spiderTaskRepository.findById(id);
    }

    @Transactional
    public SpiderTask save(SpiderTaskRequest request) {
        // 校验 scheduleCron 合法性
        validateScheduleCron(request.getScheduleCron());

        SpiderTask task = new SpiderTask();
        task.setName(request.getName());
        task.setDescription(request.getDescription());
        task.setUrlMode(request.getUrlMode());
        task.setListPageUrl(request.getListPageUrl());
        task.setListPageRule(request.getListPageRule());
        task.setSeedUrls(request.getSeedUrls());
        task.setContentPageRule(request.getContentPageRule());
        task.setScheduleCron(request.getScheduleCron());
        task.setStatus(TaskStatus.DRAFT);

        SpiderTask saved = spiderTaskRepository.save(task);

        if (request.getFields() != null && !request.getFields().isEmpty()) {
            saveFields(saved.getId(), request.getFields());
        }

        return saved;
    }

    @Transactional
    public SpiderTask update(Long id, SpiderTaskRequest request) {
        // 校验 scheduleCron 合法性
        validateScheduleCron(request.getScheduleCron());

        SpiderTask task = spiderTaskRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Task not found: " + id));

        task.setName(request.getName());
        task.setDescription(request.getDescription());
        task.setUrlMode(request.getUrlMode());
        task.setListPageUrl(request.getListPageUrl());
        task.setListPageRule(request.getListPageRule());
        task.setSeedUrls(request.getSeedUrls());
        task.setContentPageRule(request.getContentPageRule());
        task.setScheduleCron(request.getScheduleCron());

        SpiderTask saved = spiderTaskRepository.save(task);

        // Update fields: delete all and re-insert
        if (request.getFields() != null) {
            spiderFieldRepository.deleteByTaskId(id);
            saveFields(id, request.getFields());
        }

        return saved;
    }

    @Transactional
    public void delete(Long id) {
        spiderFieldRepository.deleteByTaskId(id);
        spiderTaskRepository.deleteById(id);
    }

    @Transactional
    public SpiderTask enable(Long id) {
        SpiderTask task = spiderTaskRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Task not found: " + id));

        // Validate field configuration before enabling
        long fieldCount = spiderFieldRepository.countByTaskId(id);
        if (fieldCount == 0) {
            throw new IllegalStateException("Cannot enable task without field configuration");
        }

        TaskStatus current = task.getStatus();
        if (current == TaskStatus.DRAFT) {
            task.setStatus(TaskStatus.ENABLED);
        } else if (current == TaskStatus.DISABLED) {
            task.setStatus(TaskStatus.ENABLED);
        } else {
            throw new IllegalStateException("Cannot enable task in status: " + current);
        }

        return spiderTaskRepository.save(task);
    }

    @Transactional
    public SpiderTask disable(Long id) {
        SpiderTask task = spiderTaskRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Task not found: " + id));

        TaskStatus current = task.getStatus();
        if (current == TaskStatus.ENABLED || current == TaskStatus.DRAFT) {
            task.setStatus(TaskStatus.DISABLED);
        } else {
            throw new IllegalStateException("Cannot disable task in status: " + current);
        }

        return spiderTaskRepository.save(task);
    }

    @Transactional
    public SpiderTask resetToDraft(Long id) {
        SpiderTask task = spiderTaskRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Task not found: " + id));

        if (task.getStatus() != TaskStatus.DISABLED) {
            throw new IllegalStateException("Only DISABLED tasks can be reset to draft");
        }

        task.setStatus(TaskStatus.DRAFT);
        return spiderTaskRepository.save(task);
    }

    @Transactional
    public void run(Long id) {
        SpiderTask task = spiderTaskRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Task not found: " + id));

        if (task.getStatus() != TaskStatus.ENABLED) {
            throw new IllegalStateException("Only ENABLED tasks can be run. Current status: " + task.getStatus());
        }

        task.setStatus(TaskStatus.RUNNING);
        spiderTaskRepository.save(task);

        crawlerEngine.executeAsync(id, task, TriggerType.MANUAL);
    }

    private void saveFields(Long taskId, List<FieldRequest> fieldRequests) {
        for (int i = 0; i < fieldRequests.size(); i++) {
            FieldRequest fr = fieldRequests.get(i);
            SpiderField field = new SpiderField();
            field.setTaskId(taskId);
            field.setFieldName(fr.getFieldName());
            field.setFieldLabel(fr.getFieldLabel());
            field.setFieldType(fr.getFieldType());
            field.setSelector(fr.getSelector());
            field.setSelectorType(fr.getSelectorType());
            field.setExtractType(fr.getExtractType());
            field.setAttrName(fr.getAttrName());
            field.setRequired(fr.getRequired());
            field.setDefaultValue(fr.getDefaultValue());
            field.setDisplayOrder(fr.getDisplayOrder() != null ? fr.getDisplayOrder() : i);
            spiderFieldRepository.save(field);
        }
    }

    public List<SpiderField> getFieldsByTaskId(Long taskId) {
        return spiderFieldRepository.findByTaskIdOrderByDisplayOrder(taskId);
    }

    /**
     * 校验 scheduleCron 表达式合法性
     * - 空值允许（表示不使用定时调度）
     * - 非法表达式抛 IllegalArgumentException
     * - 每秒级 cron 不允许，最小间隔 1 分钟
     */
    private void validateScheduleCron(String scheduleCron) {
        if (scheduleCron == null || scheduleCron.isBlank()) {
            return; // 允许为空
        }

        String trimmed = scheduleCron.trim();
        String[] fields = trimmed.split("\\s+");

        // 5字段 cron（标准 Unix 格式）：补充第6字段（秒），Spring 6 需要 6 字段
        // 格式: second minute hour day month weekday
        // "33 * * * *" -> "0 33 * * * *"
        String expressionToParse = trimmed;
        if (fields.length == 5) {
            expressionToParse = "0 " + trimmed; // 5-field: prepend second=0 at beginning
        } else if (fields.length == 6) {
            expressionToParse = trimmed;
        } else {
            throw new IllegalArgumentException(
                "Cron expression must have 5 or 6 fields, got " + fields.length + ": " + scheduleCron);
        }

        try {
            CronExpression.parse(expressionToParse);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid cron expression: " + scheduleCron, e);
        }

        // 禁止每秒级 cron（5字段 * * * * * 或 6字段 * * * * * *）
        if (trimmed.equals("* * * * *") || trimmed.equals("* * * * * *")) {
            throw new IllegalArgumentException(
                "Cron expression triggers too frequently. Minimum interval is 1 minute.");
        }
    }
}