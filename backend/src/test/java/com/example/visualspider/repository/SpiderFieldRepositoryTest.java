package com.example.visualspider.repository;

import com.example.visualspider.entity.SpiderField;
import com.example.visualspider.entity.SpiderField.FieldType;
import com.example.visualspider.entity.SpiderField.SelectorType;
import com.example.visualspider.entity.SpiderField.ExtractType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class SpiderFieldRepositoryTest {

    @Autowired
    private SpiderFieldRepository spiderFieldRepository;

    @Autowired
    private SpiderTaskRepository spiderTaskRepository;

    private Long createTaskId() {
        var task = new com.example.visualspider.entity.SpiderTask();
        task.setName("Test Task");
        task.setUrlMode(com.example.visualspider.entity.SpiderTask.UrlMode.LIST_PAGE);
        task.setStatus(com.example.visualspider.entity.SpiderTask.TaskStatus.DRAFT);
        return spiderTaskRepository.save(task).getId();
    }

    @Test
    void save_newField_returnsWithId() {
        Long taskId = createTaskId();

        SpiderField field = new SpiderField();
        field.setTaskId(taskId);
        field.setFieldName("title");
        field.setFieldLabel("标题");
        field.setFieldType(FieldType.text);
        field.setSelector("h1");
        field.setSelectorType(SelectorType.CSS);
        field.setExtractType(ExtractType.text);

        SpiderField saved = spiderFieldRepository.save(field);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getFieldName()).isEqualTo("title");
    }

    @Test
    void findByTaskId_withMultipleFields_returnsAllOrdered() {
        Long taskId = createTaskId();

        SpiderField field1 = new SpiderField();
        field1.setTaskId(taskId);
        field1.setFieldName("title");
        field1.setFieldType(FieldType.text);
        field1.setDisplayOrder(2);
        spiderFieldRepository.save(field1);

        SpiderField field2 = new SpiderField();
        field2.setTaskId(taskId);
        field2.setFieldName("content");
        field2.setFieldType(FieldType.richText);
        field2.setDisplayOrder(1);
        spiderFieldRepository.save(field2);

        List<SpiderField> fields = spiderFieldRepository.findByTaskIdOrderByDisplayOrder(taskId);

        assertThat(fields).hasSize(2);
        assertThat(fields.get(0).getFieldName()).isEqualTo("content");
        assertThat(fields.get(1).getFieldName()).isEqualTo("title");
    }

    @Test
    void deleteById_existingField_removesFromDb() {
        Long taskId = createTaskId();

        SpiderField field = new SpiderField();
        field.setTaskId(taskId);
        field.setFieldName("toDelete");
        field.setFieldType(FieldType.text);
        SpiderField saved = spiderFieldRepository.save(field);
        Long id = saved.getId();

        spiderFieldRepository.deleteById(id);

        Optional<SpiderField> found = spiderFieldRepository.findById(id);
        assertThat(found).isEmpty();
    }

    @Test
    void countByTaskId_withFields_returnsCount() {
        Long taskId = createTaskId();

        SpiderField field1 = new SpiderField();
        field1.setTaskId(taskId);
        field1.setFieldName("field1");
        field1.setFieldType(FieldType.text);
        spiderFieldRepository.save(field1);

        SpiderField field2 = new SpiderField();
        field2.setTaskId(taskId);
        field2.setFieldName("field2");
        field2.setFieldType(FieldType.image);
        spiderFieldRepository.save(field2);

        long count = spiderFieldRepository.countByTaskId(taskId);

        assertThat(count).isEqualTo(2);
    }
}