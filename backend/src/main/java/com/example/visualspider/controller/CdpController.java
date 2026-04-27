package com.example.visualspider.controller;

import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/cdp")
public class CdpController {

    // POST /api/cdp/load-page - Load page and return DOM snapshot
    @PostMapping("/load-page")
    public Map<String, Object> loadPage(@RequestBody Map<String, String> request) {
        // TODO: implement (future)
        return Map.of("message", "Not implemented yet");
    }

    // POST /api/cdp/generate-selector - Generate selector from click position
    @PostMapping("/generate-selector")
    public Map<String, Object> generateSelector(@RequestBody Map<String, Object> request) {
        // TODO: implement (future)
        return Map.of("message", "Not implemented yet");
    }

    // POST /api/cdp/preview-selector - Preview selector result
    @PostMapping("/preview-selector")
    public Map<String, Object> previewSelector(@RequestBody Map<String, String> request) {
        // TODO: implement (future)
        return Map.of("message", "Not implemented yet");
    }
}