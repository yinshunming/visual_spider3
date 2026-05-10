package com.example.visualspider.service;

import com.example.visualspider.dto.ContentItemRequest;
import com.example.visualspider.dto.ContentResponse;
import com.example.visualspider.entity.ContentItem;
import com.example.visualspider.entity.ContentItem.ContentStatus;
import com.example.visualspider.repository.ContentItemRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 内容管理服务类
 */
@Service
public class ContentService {

    private static final Logger log = LoggerFactory.getLogger(ContentService.class);
    private static final int MAX_HTML_LENGTH = 65535;
    private static final int EXPORT_LIMIT = 10000;

    @Autowired
    private ContentItemRepository contentItemRepository;

    @Autowired
    private ObjectMapper objectMapper;

    // CRUD operations
    public Page<ContentItem> findAll(Pageable pageable) {
        return contentItemRepository.findAll(pageable);
    }

    public Page<ContentItem> findByTaskId(Long taskId, Pageable pageable) {
        return contentItemRepository.findByTaskId(taskId, pageable);
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
     * 更新内容
     */
    public ContentResponse updateContent(Long id, ContentItemRequest request) {
        ContentItem content = contentItemRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Content not found: " + id));

        if (request.getFields() != null) {
            content.setFields(request.getFields());
        }
        if (request.getRawHtml() != null) {
            content.setRawHtml(request.getRawHtml());
        }
        if (request.getStatus() != null) {
            content.setStatus(request.getStatus());
            if (request.getStatus() == ContentStatus.PUBLISHED) {
                content.setPublishedAt(LocalDateTime.now());
            }
        }

        return ContentResponse.from(contentItemRepository.save(content));
    }

    /**
     * 更新内容状态
     */
    public ContentItem updateStatus(Long id, ContentStatus status) {
        ContentItem content = contentItemRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Content not found: " + id));

        content.setStatus(status);
        if (status == ContentStatus.PUBLISHED) {
            content.setPublishedAt(LocalDateTime.now());
        }

        return contentItemRepository.save(content);
    }

    /**
     * 保存爬取的内容
     */
    public ContentItem saveContent(Long taskId, String sourceUrl, Map<String, Object> fields, String rawHtml) {
        log.info("save_content taskId={}, sourceUrl={}", taskId, sourceUrl);

        ContentItem content = new ContentItem();
        content.setTaskId(taskId);
        content.setSourceUrl(sourceUrl);
        content.setStatus(ContentStatus.PENDING);
        content.setPublishedAt(LocalDateTime.now());
        content.setFields(fields != null ? fields : Map.of());

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

    /**
     * 导出内容为 Excel 或 CSV
     */
    public void exportContent(Long taskId, String format, HttpServletResponse response) {
        List<ContentItem> items;
        if (taskId != null) {
            items = contentItemRepository.findByTaskId(taskId);
        } else {
            items = contentItemRepository.findAll();
        }

        // Apply export limit
        boolean truncated = items.size() > EXPORT_LIMIT;
        if (truncated) {
            items = items.subList(0, EXPORT_LIMIT);
            response.setHeader("X-Export-Truncated", "true");
        }

        if ("csv".equalsIgnoreCase(format)) {
            exportToCsv(items, response);
        } else {
            exportToExcel(items, response);
        }
    }

    /**
     * 导出为 Excel
     */
    private void exportToExcel(List<ContentItem> items, HttpServletResponse response) {
        try (Workbook workbook = new SXSSFWorkbook(100);
             OutputStream out = response.getOutputStream()) {

            Sheet sheet = workbook.createSheet("Content");

            // Collect all field keys for columns
            Set<String> fieldKeys = new LinkedHashSet<>();
            for (ContentItem item : items) {
                if (item.getFields() != null && !item.getFields().isEmpty()) {
                    fieldKeys.addAll(item.getFields().keySet());
                }
            }

            // Create header row
            Row headerRow = sheet.createRow(0);
            String[] headers = new String[4 + fieldKeys.size()];
            headers[0] = "source_url";
            headers[1] = "status";
            headers[2] = "published_at";
            headers[3] = "created_at";
            int idx = 4;
            for (String key : fieldKeys) {
                headers[idx++] = key;
            }
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }

            // Create data rows
            int rowNum = 1;
            for (ContentItem item : items) {
                Row row = sheet.createRow(rowNum++);
                int col = 0;

                row.createCell(col++).setCellValue(item.getSourceUrl() != null ? item.getSourceUrl() : "");
                row.createCell(col++).setCellValue(item.getStatus() != null ? item.getStatus().name() : "");
                row.createCell(col++).setCellValue(item.getPublishedAt() != null ? item.getPublishedAt().toString() : "");
                row.createCell(col++).setCellValue(item.getCreatedAt() != null ? item.getCreatedAt().toString() : "");

                Map<String, Object> fieldsMap = item.getFields() != null ? item.getFields() : Map.of();

                for (String key : fieldKeys) {
                    Object val = fieldsMap.get(key);
                    row.createCell(col++).setCellValue(val != null ? val.toString() : "");
                }
            }

            // Set response headers
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=content_export_" + timestamp + ".xlsx");

            workbook.write(out);
            out.flush();

        } catch (IOException e) {
            log.error("Failed to export to Excel", e);
            throw new RuntimeException("Failed to export to Excel", e);
        }
    }

    /**
     * 导出为 CSV
     */
    private void exportToCsv(List<ContentItem> items, HttpServletResponse response) {
        try (OutputStream out = response.getOutputStream();
             Writer writer = new OutputStreamWriter(out, StandardCharsets.UTF_8)) {

            // Write UTF-8 BOM for Excel compatibility
            out.write(0xEF);
            out.write(0xBB);
            out.write(0xBF);

            // Collect all field keys for columns
            Set<String> fieldKeys = new LinkedHashSet<>();
            for (ContentItem item : items) {
                if (item.getFields() != null && !item.getFields().isEmpty()) {
                    fieldKeys.addAll(item.getFields().keySet());
                }
            }

            // Write header row
            StringBuilder header = new StringBuilder();
            header.append("source_url").append(",");
            header.append("status").append(",");
            header.append("published_at").append(",");
            header.append("created_at");
            for (String key : fieldKeys) {
                header.append(",").append(escapeCsv(key));
            }
            writer.write(header.toString());
            writer.write("\n");

            // Write data rows
            for (ContentItem item : items) {
                StringBuilder row = new StringBuilder();
                row.append(escapeCsv(item.getSourceUrl())).append(",");
                row.append(escapeCsv(item.getStatus() != null ? item.getStatus().name() : "")).append(",");
                row.append(escapeCsv(item.getPublishedAt() != null ? item.getPublishedAt().toString() : "")).append(",");
                row.append(escapeCsv(item.getCreatedAt() != null ? item.getCreatedAt().toString() : ""));

                Map<String, Object> fieldsMap = item.getFields() != null ? item.getFields() : Map.of();

                for (String key : fieldKeys) {
                    row.append(",");
                    Object val = fieldsMap.get(key);
                    if (val != null) {
                        row.append(escapeCsv(val.toString()));
                    }
                }

                writer.write(row.toString());
                writer.write("\n");
            }

            writer.flush();

            // Set response headers
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            response.setContentType("text/csv");
            response.setHeader("Content-Disposition", "attachment; filename=content_export_" + timestamp + ".csv");

        } catch (IOException e) {
            log.error("Failed to export to CSV", e);
            throw new RuntimeException("Failed to export to CSV", e);
        }
    }

    /**
     * 转义 CSV 特殊字符
     */
    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
