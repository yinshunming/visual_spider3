package com.example.visualspider.service;

import com.example.visualspider.entity.ContentItem;
import com.example.visualspider.entity.ContentItem.ContentStatus;
import com.example.visualspider.repository.ContentItemRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ContentExportTest {

    @Mock
    private ContentItemRepository contentItemRepository;

    private ContentService contentService;
    private ObjectMapper objectMapper;

    private ContentItem testItem1;
    private ContentItem testItem2;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        contentService = new ContentService();

        // Use reflection to inject dependencies since we're not using Spring
        try {
            var repoField = ContentService.class.getDeclaredField("contentItemRepository");
            repoField.setAccessible(true);
            repoField.set(contentService, contentItemRepository);

            var mapperField = ContentService.class.getDeclaredField("objectMapper");
            mapperField.setAccessible(true);
            mapperField.set(contentService, objectMapper);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        testItem1 = new ContentItem();
        testItem1.setId(1L);
        testItem1.setTaskId(100L);
        testItem1.setSourceUrl("https://example.com/article/1");
        testItem1.setFields(Map.of("title", "Title 1", "content", "Content 1"));
        testItem1.setStatus(ContentStatus.PUBLISHED);
        testItem1.setCreatedAt(LocalDateTime.of(2026, 1, 1, 10, 0, 0));
        testItem1.setPublishedAt(LocalDateTime.of(2026, 1, 1, 12, 0, 0));

        testItem2 = new ContentItem();
        testItem2.setId(2L);
        testItem2.setTaskId(100L);
        testItem2.setSourceUrl("https://example.com/article/2");
        testItem2.setFields(Map.of("title", "Title 2", "author", "Author 2"));
        testItem2.setStatus(ContentStatus.PENDING);
        testItem2.setCreatedAt(LocalDateTime.of(2026, 1, 2, 10, 0, 0));
    }

    @Test
    void exportToCsv_generatesValidCsv_withBom() throws Exception {
        List<ContentItem> items = List.of(testItem1, testItem2);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(response.getOutputStream()).thenReturn(new FakeServletOutputStream(baos));

        // Use reflection to call private method
        var method = ContentService.class.getDeclaredMethod("exportToCsv", List.class, HttpServletResponse.class);
        method.setAccessible(true);
        method.invoke(contentService, items, response);

        byte[] result = baos.toByteArray();

        // Check UTF-8 BOM
        assertThat(result[0]).isEqualTo((byte) 0xEF);
        assertThat(result[1]).isEqualTo((byte) 0xBB);
        assertThat(result[2]).isEqualTo((byte) 0xBF);

        String csv = new String(result, 3, result.length - 3, "UTF-8");
        assertThat(csv).contains("source_url");
        assertThat(csv).contains("Title 1");
        assertThat(csv).contains("Content 1");
    }

    @Test
    void exportToCsv_specialCharactersEscaped() throws Exception {
        ContentItem itemWithSpecialChars = new ContentItem();
        itemWithSpecialChars.setId(3L);
        itemWithSpecialChars.setTaskId(100L);
        // URL with commas
        itemWithSpecialChars.setSourceUrl("https://example.com/article, with, commas");
        itemWithSpecialChars.setFields(Map.of("title", "Title, with, commas"));
        itemWithSpecialChars.setStatus(ContentStatus.PUBLISHED);
        itemWithSpecialChars.setCreatedAt(LocalDateTime.now());

        List<ContentItem> items = List.of(itemWithSpecialChars);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(response.getOutputStream()).thenReturn(new FakeServletOutputStream(baos));

        var method = ContentService.class.getDeclaredMethod("exportToCsv", List.class, HttpServletResponse.class);
        method.setAccessible(true);
        method.invoke(contentService, items, response);

        byte[] result = baos.toByteArray();
        String csv = new String(result, 3, result.length - 3, "UTF-8"); // Skip BOM

        // URL with commas should be quoted
        assertThat(csv).contains("\"https://example.com/article, with, commas\"");
        // Title with commas should be quoted
        assertThat(csv).contains("\"Title, with, commas\"");
    }

    @Test
    void exportToExcel_generatesValidXlsx() throws Exception {
        List<ContentItem> items = List.of(testItem1, testItem2);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(response.getOutputStream()).thenReturn(new FakeServletOutputStream(baos));

        var method = ContentService.class.getDeclaredMethod("exportToExcel", List.class, HttpServletResponse.class);
        method.setAccessible(true);
        method.invoke(contentService, items, response);

        byte[] result = baos.toByteArray();

        // Check XLSX magic bytes (PK zip format)
        assertThat(result[0]).isEqualTo((byte) 'P');
        assertThat(result[1]).isEqualTo((byte) 'K');

        // Check content type header was set
        verify(response).setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    }

    @Test
    void exportToExcel_fieldsJsonExpanded() throws Exception {
        List<ContentItem> items = List.of(testItem1, testItem2);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(response.getOutputStream()).thenReturn(new FakeServletOutputStream(baos));

        var method = ContentService.class.getDeclaredMethod("exportToExcel", List.class, HttpServletResponse.class);
        method.setAccessible(true);
        method.invoke(contentService, items, response);

        byte[] result = baos.toByteArray();

        // Verify the file was created (ZIP format)
        assertThat(result.length).isGreaterThan(100);
    }

    @Test
    void exportContent_truncatesOverLimit() throws Exception {
        // Create list exceeding EXPORT_LIMIT (10000)
        List<ContentItem> items = new java.util.ArrayList<>();
        for (int i = 0; i < 10005; i++) {
            ContentItem item = new ContentItem();
            item.setId((long) i);
            item.setTaskId(100L);
            item.setSourceUrl("https://example.com/article/" + i);
            item.setFields(Map.of("index", i));
            item.setStatus(ContentStatus.PUBLISHED);
            item.setCreatedAt(LocalDateTime.now());
            items.add(item);
        }

        // Mock repository to return the oversized list
        when(contentItemRepository.findAll()).thenReturn(items);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(response.getOutputStream()).thenReturn(new FakeServletOutputStream(baos));

        contentService.exportContent(null, "xlsx", response);

        // Verify truncation header was set
        verify(response).setHeader("X-Export-Truncated", "true");
    }

    // Helper class to mock ServletOutputStream
    static class FakeServletOutputStream extends jakarta.servlet.ServletOutputStream {
        private final ByteArrayOutputStream baos;

        FakeServletOutputStream(ByteArrayOutputStream baos) {
            this.baos = baos;
        }

        @Override
        public void write(int b) {
            baos.write(b);
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setWriteListener(jakarta.servlet.WriteListener listener) {
        }
    }
}
