package com.example.visualspider.controller;

import com.example.visualspider.dto.ContentItemRequest;
import com.example.visualspider.entity.ContentItem;
import com.example.visualspider.entity.ContentItem.ContentStatus;
import com.example.visualspider.entity.SpiderTask;
import com.example.visualspider.entity.SpiderTask.UrlMode;
import com.example.visualspider.repository.ContentItemRepository;
import com.example.visualspider.repository.SpiderTaskRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:postgresql://localhost:5432/postgres",
    "spring.datasource.username=postgres",
    "spring.datasource.password=123456",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect"
})
class ContentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ContentItemRepository contentItemRepository;

    @Autowired
    private SpiderTaskRepository spiderTaskRepository;

    private SpiderTask testTask;
    private ContentItem testContent;

    @BeforeEach
    void setUp() {
        contentItemRepository.deleteAll();
        spiderTaskRepository.deleteAll();

        testTask = new SpiderTask();
        testTask.setName("Test Task for Content");
        testTask.setUrlMode(UrlMode.LIST_PAGE);
        testTask.setStatus(SpiderTask.TaskStatus.ENABLED);
        testTask = spiderTaskRepository.save(testTask);

        testContent = new ContentItem();
        testContent.setTaskId(testTask.getId());
        testContent.setSourceUrl("https://example.com/article/1");
        testContent.setFields(Map.of("title", "原始标题", "content", "正文内容"));
        testContent.setStatus(ContentStatus.PENDING);
        testContent.setRawHtml("<html><body>Test HTML</body></html>");
        testContent = contentItemRepository.save(testContent);
    }

    // ========== API-001~006: 内容列表分页查询 ==========

    @Test
    void listContents_defaultPagination_returnsFirstPage() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/contents")
                .param("page", "0")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andReturn();

        String json = result.getResponse().getContentAsString();
        assertThat(json).contains("\"content\"");
        assertThat(json).contains("\"totalElements\"");
    }

    @Test
    void listContents_withPagination_returnsSecondPage() throws Exception {
        for (int i = 0; i < 5; i++) {
            ContentItem item = new ContentItem();
            item.setTaskId(testTask.getId());
            item.setSourceUrl("https://example.com/article/" + i);
            item.setStatus(ContentStatus.PENDING);
            contentItemRepository.save(item);
        }

        MvcResult result = mockMvc.perform(get("/api/contents")
                .param("page", "1")
                .param("size", "3"))
                .andExpect(status().isOk())
                .andReturn();

        String json = result.getResponse().getContentAsString();
        assertThat(json).contains("\"totalPages\"");
    }

    @Test
    void listContents_filterByTaskId_returnsFilteredResults() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/contents")
                .param("taskId", String.valueOf(testTask.getId())))
                .andExpect(status().isOk())
                .andReturn();

        String json = result.getResponse().getContentAsString();
        assertThat(json).contains("https://example.com/article/1");
    }

    @Test
    void listContents_filterByStatus_returnsFilteredResults() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/contents")
                .param("status", "PENDING"))
                .andExpect(status().isOk())
                .andReturn();

        String json = result.getResponse().getContentAsString();
        assertThat(json).contains("PENDING");
    }

    @Test
    void listContents_emptyResult_returnsEmptyPage() throws Exception {
        contentItemRepository.deleteAll();

        MvcResult result = mockMvc.perform(get("/api/contents"))
                .andExpect(status().isOk())
                .andReturn();

        String json = result.getResponse().getContentAsString();
        assertThat(json).contains("\"totalElements\":0");
    }

    // ========== API-010~012: 内容详情查询 ==========

    @Test
    void getContent_existingId_returnsContent() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/contents/{id}", testContent.getId()))
                .andExpect(status().isOk())
                .andReturn();

        String json = result.getResponse().getContentAsString();
        assertThat(json).contains("\"id\":" + testContent.getId());
        assertThat(json).contains("https://example.com/article/1");
        assertThat(json).contains("title");
    }

    @Test
    void getContent_nonExistingId_returns404() throws Exception {
        mockMvc.perform(get("/api/contents/{id}", 99999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void getContent_nullFields_returnsEmptyMap() throws Exception {
        ContentItem itemWithNullFields = new ContentItem();
        itemWithNullFields.setTaskId(testTask.getId());
        itemWithNullFields.setSourceUrl("https://example.com/null-fields");
        itemWithNullFields.setFields(null);
        itemWithNullFields.setStatus(ContentStatus.PENDING);
        itemWithNullFields = contentItemRepository.save(itemWithNullFields);

        MvcResult result = mockMvc.perform(get("/api/contents/{id}", itemWithNullFields.getId()))
                .andExpect(status().isOk())
                .andReturn();

        String json = result.getResponse().getContentAsString();
        assertThat(json).contains("\"fields\":{}");
    }

    // ========== API-020~024: 内容更新 ==========

    @Test
    void updateContent_onlyFields_updatesFieldsOnly() throws Exception {
        ContentItemRequest request = new ContentItemRequest();
        request.setFields(Map.of("title", "新标题"));

        mockMvc.perform(put("/api/contents/{id}", testContent.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        ContentItem updated = contentItemRepository.findById(testContent.getId()).orElseThrow();
        assertThat(updated.getFields()).isEqualTo(Map.of("title", "新标题"));
        assertThat(updated.getStatus()).isEqualTo(ContentStatus.PENDING);
    }

    @Test
    void updateContent_statusToPublished_setsPublishedAt() throws Exception {
        ContentItemRequest request = new ContentItemRequest();
        request.setStatus(ContentStatus.PUBLISHED);

        mockMvc.perform(put("/api/contents/{id}", testContent.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        ContentItem updated = contentItemRepository.findById(testContent.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(ContentStatus.PUBLISHED);
        assertThat(updated.getPublishedAt()).isNotNull();
    }

    @Test
    void updateContent_statusToDeleted_keepsPublishedAt() throws Exception {
        testContent.setPublishedAt(java.time.LocalDateTime.now());
        contentItemRepository.save(testContent);

        ContentItemRequest request = new ContentItemRequest();
        request.setStatus(ContentStatus.DELETED);

        mockMvc.perform(put("/api/contents/{id}", testContent.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        ContentItem updated = contentItemRepository.findById(testContent.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(ContentStatus.DELETED);
    }

    @Test
    void updateContent_bothFieldsAndStatus_updatesBoth() throws Exception {
        ContentItemRequest request = new ContentItemRequest();
        request.setFields(Map.of("title", "更新的标题"));
        request.setStatus(ContentStatus.PUBLISHED);

        mockMvc.perform(put("/api/contents/{id}", testContent.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        ContentItem updated = contentItemRepository.findById(testContent.getId()).orElseThrow();
        assertThat(updated.getFields()).isEqualTo(Map.of("title", "更新的标题"));
        assertThat(updated.getStatus()).isEqualTo(ContentStatus.PUBLISHED);
        assertThat(updated.getPublishedAt()).isNotNull();
    }

    @Test
    void updateContent_nonExistingId_returns404() throws Exception {
        ContentItemRequest request = new ContentItemRequest();
        request.setFields(Map.of("title", "新标题"));

        mockMvc.perform(put("/api/contents/{id}", 99999L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    // ========== API-030~031: 内容删除 ==========

    @Test
    void deleteContent_existingId_returns204AndRemovesFromDb() throws Exception {
        Long contentId = testContent.getId();

        mockMvc.perform(delete("/api/contents/{id}", contentId))
                .andExpect(status().isNoContent());

        assertThat(contentItemRepository.findById(contentId)).isEmpty();
    }

    @Test
    void deleteContent_nonExistingId_returns404() throws Exception {
        mockMvc.perform(delete("/api/contents/{id}", 99999L))
                .andExpect(status().isNotFound());
    }

    // ========== API-040~045: 导出功能 ==========

    @Test
    void exportContent_xlsx_returnsExcelFile() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/contents/export")
                .param("format", "xlsx"))
                .andExpect(status().isOk())
                .andReturn();

        assertThat(result.getResponse().getContentType())
                .contains("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    }

    @Test
    void exportContent_csv_returnsCsvFile() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/contents/export")
                .param("format", "csv"))
                .andExpect(status().isOk())
                .andReturn();

        assertThat(result.getResponse().getContentType())
                .contains("text/csv");
    }

    @Test
    void exportContent_withTaskId_filtersResults() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/contents/export")
                .param("taskId", String.valueOf(testTask.getId()))
                .param("format", "xlsx"))
                .andExpect(status().isOk())
                .andReturn();

        assertThat(result.getResponse().getContentType())
                .contains("spreadsheetml");
    }

    @Test
    void exportContent_emptyResult_generatesEmptyFile() throws Exception {
        contentItemRepository.deleteAll();

        MvcResult result = mockMvc.perform(get("/api/contents/export")
                .param("format", "csv"))
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        assertThat(content).contains("source_url");
    }

    @Test
    void exportContent_csvSpecialCharacters_escapesCorrectly() throws Exception {
        ContentItem specialItem = new ContentItem();
        specialItem.setTaskId(testTask.getId());
        specialItem.setSourceUrl("https://example.com/special\"chars");
        specialItem.setFields(Map.of("title", "Hello, \"World\"\nNew Line"));
        specialItem.setStatus(ContentStatus.PENDING);
        contentItemRepository.save(specialItem);

        MvcResult result = mockMvc.perform(get("/api/contents/export")
                .param("format", "csv"))
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        assertThat(content).contains("\"\"");
    }

    @Test
    void exportContent_xlsxFields_expandsToColumns() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/contents/export")
                .param("format", "xlsx"))
                .andExpect(status().isOk())
                .andReturn();

        assertThat(result.getResponse().getContentType())
                .contains("spreadsheetml");
    }
}
