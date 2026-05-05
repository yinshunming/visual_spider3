package com.example.visualspider.repository;

import com.example.visualspider.entity.ContentItem;
import com.example.visualspider.entity.ContentItem.ContentStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class ContentItemRepositoryTest {

    @Autowired
    private ContentItemRepository contentItemRepository;

    @Autowired
    private SpiderTaskRepository spiderTaskRepository;

    private Long createTaskId() {
        var task = new com.example.visualspider.entity.SpiderTask();
        task.setName("Test Task");
        task.setUrlMode(com.example.visualspider.entity.SpiderTask.UrlMode.LIST_PAGE);
        task.setStatus(com.example.visualspider.entity.SpiderTask.TaskStatus.ENABLED);
        return spiderTaskRepository.save(task).getId();
    }

    @Test
    void save_newContentItem_returnsWithId() {
        Long taskId = createTaskId();

        ContentItem item = new ContentItem();
        item.setTaskId(taskId);
        item.setSourceUrl("https://example.com/article/1");
        item.setFields("{\"title\": \"Test\"}");
        item.setStatus(ContentStatus.PENDING);

        ContentItem saved = contentItemRepository.save(item);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getSourceUrl()).isEqualTo("https://example.com/article/1");
    }

    @Test
    void findByTaskId_withPagination_returnsPage() {
        Long taskId = createTaskId();

        for (int i = 0; i < 5; i++) {
            ContentItem item = new ContentItem();
            item.setTaskId(taskId);
            item.setSourceUrl("https://example.com/article/" + i);
            item.setStatus(ContentStatus.PENDING);
            contentItemRepository.save(item);
        }

        Page<ContentItem> page = contentItemRepository.findByTaskId(taskId, PageRequest.of(0, 3));

        assertThat(page.getTotalElements()).isEqualTo(5);
        assertThat(page.getContent()).hasSize(3);
    }

    @Test
    void findByStatus_withPendingItems_returnsPage() {
        Long taskId = createTaskId();

        ContentItem pending = new ContentItem();
        pending.setTaskId(taskId);
        pending.setSourceUrl("https://example.com/pending");
        pending.setStatus(ContentStatus.PENDING);
        contentItemRepository.save(pending);

        ContentItem published = new ContentItem();
        published.setTaskId(taskId);
        published.setSourceUrl("https://example.com/published");
        published.setStatus(ContentStatus.PUBLISHED);
        contentItemRepository.save(published);

        Page<ContentItem> page = contentItemRepository.findByStatus(ContentStatus.PENDING, PageRequest.of(0, 10));

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).getStatus()).isEqualTo(ContentStatus.PENDING);
    }

    @Test
    void deleteById_existingItem_removesFromDb() {
        Long taskId = createTaskId();

        ContentItem item = new ContentItem();
        item.setTaskId(taskId);
        item.setSourceUrl("https://example.com/toDelete");
        item.setStatus(ContentStatus.PENDING);
        ContentItem saved = contentItemRepository.save(item);
        Long id = saved.getId();

        contentItemRepository.deleteById(id);

        Optional<ContentItem> found = contentItemRepository.findById(id);
        assertThat(found).isEmpty();
    }

    @Test
    void findById_existingItem_returnsItem() {
        Long taskId = createTaskId();

        ContentItem item = new ContentItem();
        item.setTaskId(taskId);
        item.setSourceUrl("https://example.com/find");
        item.setStatus(ContentStatus.PUBLISHED);
        ContentItem saved = contentItemRepository.save(item);

        Optional<ContentItem> found = contentItemRepository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getSourceUrl()).isEqualTo("https://example.com/find");
    }
}