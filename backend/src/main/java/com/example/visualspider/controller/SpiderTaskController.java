package com.example.visualspider.controller;

import com.example.visualspider.service.SpiderTaskService;
import com.example.visualspider.dto.SpiderTaskRequest;
import com.example.visualspider.entity.SpiderTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tasks")
public class SpiderTaskController {

    @Autowired
    private SpiderTaskService spiderTaskService;

    // GET /api/tasks - List all tasks with pagination
    @GetMapping
    public Page<SpiderTask> list(Pageable pageable) {
        // TODO: implement
        return null;
    }

    // GET /api/tasks/{id} - Get task by ID
    @GetMapping("/{id}")
    public SpiderTask getById(@PathVariable Long id) {
        // TODO: implement
        return null;
    }

    // POST /api/tasks - Create task
    @PostMapping
    public SpiderTask create(@RequestBody @Valid SpiderTaskRequest request) {
        // TODO: implement
        return null;
    }

    // PUT /api/tasks/{id} - Update task
    @PutMapping("/{id}")
    public SpiderTask update(@PathVariable Long id, @RequestBody @Valid SpiderTaskRequest request) {
        // TODO: implement
        return null;
    }

    // DELETE /api/tasks/{id} - Delete task
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        // TODO: implement
    }

    // POST /api/tasks/{id}/enable - Enable task
    @PostMapping("/{id}/enable")
    public SpiderTask enable(@PathVariable Long id) {
        // TODO: implement
        return null;
    }

    // POST /api/tasks/{id}/disable - Disable task
    @PostMapping("/{id}/disable")
    public SpiderTask disable(@PathVariable Long id) {
        // TODO: implement
        return null;
    }

    // POST /api/tasks/{id}/run - Run task
    @PostMapping("/{id}/run")
    public void run(@PathVariable Long id) {
        // TODO: implement
    }
}