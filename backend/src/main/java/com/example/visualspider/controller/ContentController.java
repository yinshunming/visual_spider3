package com.example.visualspider.controller;

import com.example.visualspider.service.ContentService;
import com.example.visualspider.dto.ContentItemRequest;
import com.example.visualspider.entity.ContentItem;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/contents")
public class ContentController {

    @Autowired
    private ContentService contentService;

    // GET /api/contents - List all contents with pagination
    @GetMapping
    public Page<ContentItem> list(Pageable pageable) {
        // TODO: implement
        return null;
    }

    // GET /api/contents/{id} - Get content by ID
    @GetMapping("/{id}")
    public ContentItem getById(@PathVariable Long id) {
        // TODO: implement
        return null;
    }

    // PUT /api/contents/{id} - Update content
    @PutMapping("/{id}")
    public ContentItem update(@PathVariable Long id, @RequestBody @Valid ContentItemRequest request) {
        // TODO: implement
        return null;
    }

    // DELETE /api/contents/{id} - Delete content
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        // TODO: implement
    }

    // GET /api/contents/export - Export contents
    @GetMapping("/export")
    public void export(HttpServletResponse response) {
        // TODO: implement
    }
}