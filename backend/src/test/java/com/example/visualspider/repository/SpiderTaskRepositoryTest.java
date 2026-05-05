package com.example.visualspider.repository;

import com.example.visualspider.entity.SpiderTask;
import com.example.visualspider.entity.SpiderTask.TaskStatus;
import com.example.visualspider.entity.SpiderTask.UrlMode;
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
class SpiderTaskRepositoryTest {

    @Autowired
    private SpiderTaskRepository spiderTaskRepository;

    @Test
    void save_newTask_returnsWithId() {
        SpiderTask task = new SpiderTask();
        task.setName("Test Task");
        task.setUrlMode(UrlMode.LIST_PAGE);
        task.setStatus(TaskStatus.DRAFT);

        SpiderTask saved = spiderTaskRepository.save(task);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("Test Task");
    }

    @Test
    void findById_existingId_returnsTask() {
        SpiderTask task = new SpiderTask();
        task.setName("Find Test");
        task.setUrlMode(UrlMode.DIRECT_URL);
        task.setStatus(TaskStatus.ENABLED);
        SpiderTask saved = spiderTaskRepository.save(task);

        Optional<SpiderTask> found = spiderTaskRepository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Find Test");
    }

    @Test
    void findById_nonExistingId_returnsEmpty() {
        Optional<SpiderTask> found = spiderTaskRepository.findById(99999L);

        assertThat(found).isEmpty();
    }

    @Test
    void delete_existingTask_removesFromDb() {
        SpiderTask task = new SpiderTask();
        task.setName("Delete Test");
        task.setUrlMode(UrlMode.LIST_PAGE);
        task.setStatus(TaskStatus.DRAFT);
        SpiderTask saved = spiderTaskRepository.save(task);
        Long id = saved.getId();

        spiderTaskRepository.delete(saved);

        Optional<SpiderTask> found = spiderTaskRepository.findById(id);
        assertThat(found).isEmpty();
    }

    @Test
    void findAll_withPagination_returnsPage() {
        // Create multiple tasks
        for (int i = 0; i < 5; i++) {
            SpiderTask task = new SpiderTask();
            task.setName("Task " + i);
            task.setUrlMode(UrlMode.LIST_PAGE);
            task.setStatus(TaskStatus.DRAFT);
            spiderTaskRepository.save(task);
        }

        Page<SpiderTask> page = spiderTaskRepository.findAll(PageRequest.of(0, 3));

        assertThat(page.getTotalElements()).isEqualTo(5);
        assertThat(page.getContent()).hasSize(3);
        assertThat(page.getNumber()).isZero();
    }

    @Test
    void findByStatus_withEnabledTasks_returnsOnlyEnabled() {
        SpiderTask draft = new SpiderTask();
        draft.setName("Draft");
        draft.setUrlMode(UrlMode.LIST_PAGE);
        draft.setStatus(TaskStatus.DRAFT);
        spiderTaskRepository.save(draft);

        SpiderTask enabled = new SpiderTask();
        enabled.setName("Enabled");
        enabled.setUrlMode(UrlMode.LIST_PAGE);
        enabled.setStatus(TaskStatus.ENABLED);
        spiderTaskRepository.save(enabled);

        var enabledTasks = spiderTaskRepository.findByStatus(TaskStatus.ENABLED);

        assertThat(enabledTasks).hasSize(1);
        assertThat(enabledTasks.get(0).getName()).isEqualTo("Enabled");
    }
}