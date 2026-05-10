package com.example.visualspider.controller;

import com.example.visualspider.dto.ExecutionResponse;
import com.example.visualspider.entity.ExecutionLog;
import com.example.visualspider.service.ExecutionLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/executions")
public class ExecutionController {

    @Autowired
    private ExecutionLogService executionLogService;

    @GetMapping
    public Page<ExecutionResponse> list(
            @RequestParam(required = false) Long taskId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ExecutionLog> logs;
        if (taskId != null) {
            logs = executionLogService.findByTaskId(taskId, pageable);
        } else {
            logs = executionLogService.findAll(pageable);
        }
        return logs.map(ExecutionResponse::from);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExecutionResponse> getById(@PathVariable Long id) {
        return executionLogService.findById(id)
                .map(log -> ResponseEntity.ok(ExecutionResponse.from(log)))
                .orElse(ResponseEntity.notFound().build());
    }
}
