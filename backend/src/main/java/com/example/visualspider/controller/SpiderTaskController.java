package com.example.visualspider.controller;

import com.example.visualspider.dto.*;
import com.example.visualspider.entity.SpiderTask;
import com.example.visualspider.entity.SpiderField;
import com.example.visualspider.service.SpiderTaskService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tasks")
public class SpiderTaskController {

    @Autowired
    private SpiderTaskService spiderTaskService;

    @GetMapping
    public Page<SpiderTaskResponse> list(Pageable pageable) {
        return spiderTaskService.findAll(pageable)
            .map(task -> SpiderTaskResponse.from(task, getFieldResponses(task.getId())));
    }

    @GetMapping("/{id}")
    public SpiderTaskResponse getById(@PathVariable Long id) {
        SpiderTask task = spiderTaskService.findById(id)
            .orElseThrow(() -> new RuntimeException("Task not found: " + id));
        List<FieldResponse> fields = getFieldResponses(id);
        return SpiderTaskResponse.from(task, fields);
    }

    @PostMapping
    public SpiderTaskResponse create(@RequestBody @Valid SpiderTaskRequest request) {
        SpiderTask task = spiderTaskService.save(request);
        List<FieldResponse> fields = getFieldResponses(task.getId());
        return SpiderTaskResponse.from(task, fields);
    }

    @PutMapping("/{id}")
    public SpiderTaskResponse update(@PathVariable Long id, @RequestBody @Valid SpiderTaskRequest request) {
        SpiderTask task = spiderTaskService.update(id, request);
        List<FieldResponse> fields = getFieldResponses(id);
        return SpiderTaskResponse.from(task, fields);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        spiderTaskService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/enable")
    public SpiderTaskResponse enable(@PathVariable Long id) {
        SpiderTask task = spiderTaskService.enable(id);
        List<FieldResponse> fields = getFieldResponses(id);
        return SpiderTaskResponse.from(task, fields);
    }

    @PostMapping("/{id}/disable")
    public SpiderTaskResponse disable(@PathVariable Long id) {
        SpiderTask task = spiderTaskService.disable(id);
        List<FieldResponse> fields = getFieldResponses(id);
        return SpiderTaskResponse.from(task, fields);
    }

    @PostMapping("/{id}/run")
    public ResponseEntity<String> run(@PathVariable Long id) {
        spiderTaskService.run(id);
        return ResponseEntity.ok("Task started");
    }

    private List<FieldResponse> getFieldResponses(Long taskId) {
        return spiderTaskService.getFieldsByTaskId(taskId)
            .stream()
            .map(FieldResponse::from)
            .collect(Collectors.toList());
    }
}