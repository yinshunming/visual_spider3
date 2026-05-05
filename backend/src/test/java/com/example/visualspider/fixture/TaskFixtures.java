package com.example.visualspider.fixture;

import com.example.visualspider.dto.FieldRequest;
import com.example.visualspider.dto.SpiderTaskRequest;
import com.example.visualspider.entity.SpiderTask;
import com.example.visualspider.entity.SpiderTask.TaskStatus;
import com.example.visualspider.entity.SpiderTask.UrlMode;
import com.example.visualspider.entity.SpiderField;
import com.example.visualspider.entity.SpiderField.FieldType;
import com.example.visualspider.entity.SpiderField.SelectorType;
import com.example.visualspider.entity.SpiderField.ExtractType;

import java.util.Arrays;
import java.util.List;

/**
 * Test fixtures for SpiderTask entities
 */
public class TaskFixtures {

    private TaskFixtures() {
        // Utility class
    }

    public static SpiderTask draftTask() {
        SpiderTask task = new SpiderTask();
        task.setName("Draft Task");
        task.setUrlMode(UrlMode.LIST_PAGE);
        task.setListPageUrl("https://example.com/list");
        task.setStatus(TaskStatus.DRAFT);
        return task;
    }

    public static SpiderTask enabledTask() {
        SpiderTask task = new SpiderTask();
        task.setName("Enabled Task");
        task.setUrlMode(UrlMode.LIST_PAGE);
        task.setListPageUrl("https://example.com/list");
        task.setStatus(TaskStatus.ENABLED);
        return task;
    }

    public static SpiderTask disabledTask() {
        SpiderTask task = new SpiderTask();
        task.setName("Disabled Task");
        task.setUrlMode(UrlMode.DIRECT_URL);
        task.setSeedUrls(new String[]{"https://example.com/page1"});
        task.setStatus(TaskStatus.DISABLED);
        return task;
    }

    public static SpiderTask runningTask() {
        SpiderTask task = new SpiderTask();
        task.setName("Running Task");
        task.setUrlMode(UrlMode.LIST_PAGE);
        task.setListPageUrl("https://example.com/list");
        task.setStatus(TaskStatus.RUNNING);
        return task;
    }

    public static SpiderTask enabledTaskWithFields() {
        SpiderTask task = enabledTask();
        return task;
    }

    public static SpiderTaskRequest createRequest() {
        SpiderTaskRequest request = new SpiderTaskRequest();
        request.setName("New Task");
        request.setDescription("Test task description");
        request.setUrlMode(UrlMode.LIST_PAGE);
        request.setListPageUrl("https://example.com/list");
        request.setListPageRule("{\"containerSelector\": \"div.item\"}");
        request.setContentPageRule("{\"fields\": []}");
        return request;
    }

    public static SpiderTaskRequest createRequestWithFields() {
        SpiderTaskRequest request = createRequest();
        request.setFields(Arrays.asList(
            FieldFixtures.textFieldRequest(),
            FieldFixtures.imageFieldRequest()
        ));
        return request;
    }

    public static SpiderTaskRequest updateRequest() {
        SpiderTaskRequest request = new SpiderTaskRequest();
        request.setName("Updated Task");
        request.setDescription("Updated description");
        request.setUrlMode(UrlMode.DIRECT_URL);
        request.setSeedUrls(new String[]{"https://example.com/page1", "https://example.com/page2"});
        return request;
    }
}