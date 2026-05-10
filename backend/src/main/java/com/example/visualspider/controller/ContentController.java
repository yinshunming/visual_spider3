package com.example.visualspider.controller;

import com.example.visualspider.dto.ContentItemRequest;
import com.example.visualspider.dto.ContentResponse;
import com.example.visualspider.service.ContentService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/contents")
public class ContentController {

    @Autowired
    private ContentService contentService;

    // GET /api/contents - List all contents with pagination (optional taskId filter)
    @GetMapping
    public Page<ContentResponse> list(
            @RequestParam(required = false) Long taskId,
            Pageable pageable) {
        if (taskId != null) {
            return contentService.findByTaskId(taskId, pageable).map(ContentResponse::from);
        }
        return contentService.findAll(pageable).map(ContentResponse::from);
    }

    // GET /api/contents/{id} - Get content by ID
    @GetMapping("/{id}")
    public ResponseEntity<ContentResponse> getById(@PathVariable Long id) {
        return contentService.findById(id)
            .map(ContentResponse::from)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    // PUT /api/contents/{id} - Update content
    @PutMapping("/{id}")
    public ResponseEntity<ContentResponse> update(@PathVariable Long id, @RequestBody @Valid ContentItemRequest request) {
        if (contentService.findById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(contentService.updateContent(id, request));
    }

    // DELETE /api/contents/{id} - Delete content
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (contentService.findById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        contentService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // GET /api/contents/export - Export contents (format: xlsx or csv)
    @GetMapping("/export")
    public void export(
            @RequestParam(required = false) Long taskId,
            @RequestParam(defaultValue = "xlsx") String format,
            HttpServletResponse response) {
        contentService.exportContent(taskId, format, response);
    }
}
