package com.example.visualspider.service;

import com.example.visualspider.entity.ContentItem;
import com.example.visualspider.entity.ContentItem.ContentStatus;
import com.example.visualspider.repository.ContentItemRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

/**
 * 内容管理服务类
 */
@Service
public class ContentService {

    private static final Logger log = LoggerFactory.getLogger(ContentService.class);
    private static final int MAX_HTML_LENGTH = 65535;

    @Autowired
    private ContentItemRepository contentItemRepository;

    @Autowired
    private ObjectMapper objectMapper;

    // CRUD operations
    public Page<ContentItem> findAll(Pageable pageable) {
        return contentItemRepository.findAll(pageable);
    }

    public Optional<ContentItem> findById(Long id) {
        return contentItemRepository.findById(id);
    }

    public ContentItem save(ContentItem content) {
        return contentItemRepository.save(content);
    }

    public void delete(Long id) {
        contentItemRepository.deleteById(id);
    }

    /**
     * 保存爬取的内容
     *
     * @param taskId   任务ID
     * @param sourceUrl 来源URL
     * @param fields    提取的字段数据
     * @param rawHtml   原始HTML
     * @return 保存的内容项
     */
    public ContentItem saveContent(Long taskId, String sourceUrl, Map<String, Object> fields, String rawHtml) {
        log.info("save_content taskId={}, sourceUrl={}", taskId, sourceUrl);

        ContentItem content = new ContentItem();
        content.setTaskId(taskId);
        content.setSourceUrl(sourceUrl);
        content.setStatus(ContentStatus.PENDING);
        content.setPublishedAt(LocalDateTime.now());

        // Serialize fields map to JSON
        try {
            content.setFields(objectMapper.writeValueAsString(fields));
        } catch (JsonProcessingException e) {
            log.error("failed_to_serialize_fields taskId={}", taskId, e);
            content.setFields("{}");
        }

        // Truncate rawHtml if exceeds PostgreSQL TEXT limit
        if (rawHtml != null && rawHtml.length() > MAX_HTML_LENGTH) {
            log.warn("raw_html_truncated taskId={}, originalLength={}, maxLength={}",
                    taskId, rawHtml.length(), MAX_HTML_LENGTH);
            content.setRawHtml(rawHtml.substring(0, MAX_HTML_LENGTH));
        } else {
            content.setRawHtml(rawHtml);
        }

        return contentItemRepository.save(content);
    }

    // Export (placeholder)
    public void exportToExcel(HttpServletResponse response) {
        // TODO: implement
    }
}