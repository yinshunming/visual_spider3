package com.example.visualspider.service;

import com.example.visualspider.dto.ContentItemRequest;
import com.example.visualspider.dto.ContentResponse;
import com.example.visualspider.entity.ContentItem;
import com.example.visualspider.entity.ContentItem.ContentStatus;
import com.example.visualspider.repository.ContentItemRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContentServiceTest {

    @Mock
    private ContentItemRepository contentItemRepository;

    private ContentService contentService;
    private ObjectMapper objectMapper;

    private ContentItem testItem;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        contentService = new ContentService();

        // Inject dependencies via reflection
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

        testItem = new ContentItem();
        testItem.setId(1L);
        testItem.setTaskId(100L);
        testItem.setSourceUrl("https://example.com/article/1");
        testItem.setFields(Map.of("title", "Test Title", "content", "Test Content"));
        testItem.setStatus(ContentStatus.PENDING);
        testItem.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void findById_existingItem_returnsParsedFields() {
        when(contentItemRepository.findById(1L)).thenReturn(Optional.of(testItem));

        Optional<ContentItem> result = contentService.findById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getFields()).isEqualTo(Map.of("title", "Test Title", "content", "Test Content"));
    }

    @Test
    void findById_nonExistingItem_returnsEmpty() {
        when(contentItemRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<ContentItem> result = contentService.findById(999L);

        assertThat(result).isEmpty();
    }

    @Test
    void updateContent_success_updatesFields() {
        ContentItemRequest request = new ContentItemRequest();
        request.setFields(Map.of("title", "Updated Title"));

        when(contentItemRepository.findById(1L)).thenReturn(Optional.of(testItem));
        when(contentItemRepository.save(any(ContentItem.class))).thenAnswer(inv -> inv.getArgument(0));

        ContentResponse result = contentService.updateContent(1L, request);

        assertThat(result).isNotNull();
        verify(contentItemRepository).save(any(ContentItem.class));
    }

    @Test
    void updateContent_notFound_throwsException() {
        ContentItemRequest request = new ContentItemRequest();
        request.setFields(Map.of("title", "Updated Title"));

        when(contentItemRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> contentService.updateContent(999L, request))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Content not found");
    }

    @Test
    void updateStatus_toPublished_setsPublishedAt() {
        when(contentItemRepository.findById(1L)).thenReturn(Optional.of(testItem));
        when(contentItemRepository.save(any(ContentItem.class))).thenAnswer(inv -> inv.getArgument(0));

        ContentItem result = contentService.updateStatus(1L, ContentStatus.PUBLISHED);

        assertThat(result.getStatus()).isEqualTo(ContentStatus.PUBLISHED);
        assertThat(result.getPublishedAt()).isNotNull();
    }

    @Test
    void updateStatus_toPending_doesNotChangePublishedAt() {
        testItem.setPublishedAt(LocalDateTime.of(2026, 1, 1, 12, 0, 0));
        when(contentItemRepository.findById(1L)).thenReturn(Optional.of(testItem));
        when(contentItemRepository.save(any(ContentItem.class))).thenAnswer(inv -> inv.getArgument(0));

        ContentItem result = contentService.updateStatus(1L, ContentStatus.PENDING);

        assertThat(result.getStatus()).isEqualTo(ContentStatus.PENDING);
        assertThat(result.getPublishedAt()).isEqualTo(LocalDateTime.of(2026, 1, 1, 12, 0, 0));
    }

    @Test
    void updateStatus_toDeleted_keepsOriginalPublishedAt() {
        testItem.setPublishedAt(LocalDateTime.of(2026, 1, 1, 12, 0, 0));
        when(contentItemRepository.findById(1L)).thenReturn(Optional.of(testItem));
        when(contentItemRepository.save(any(ContentItem.class))).thenAnswer(inv -> inv.getArgument(0));

        ContentItem result = contentService.updateStatus(1L, ContentStatus.DELETED);

        assertThat(result.getStatus()).isEqualTo(ContentStatus.DELETED);
        assertThat(result.getPublishedAt()).isEqualTo(LocalDateTime.of(2026, 1, 1, 12, 0, 0));
    }

    @Test
    void delete_existingItem_deletesSuccessfully() {
        doNothing().when(contentItemRepository).deleteById(1L);

        contentService.delete(1L);

        verify(contentItemRepository).deleteById(1L);
    }
}
