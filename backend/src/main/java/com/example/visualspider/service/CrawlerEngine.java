package com.example.visualspider.service;

import com.example.visualspider.entity.SpiderTask;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 爬虫引擎服务类
 */
@Service
public class CrawlerEngine {

    // Crawl a single URL
    public Map<String, Object> crawl(String url, SpiderTask task) {
        // TODO: implement (M3)
        return Map.of("message", "Not implemented yet");
    }

    // Crawl list page
    public List<String> crawlListPage(String listPageUrl, SpiderTask task) {
        // TODO: implement (M3)
        return List.of();
    }
}