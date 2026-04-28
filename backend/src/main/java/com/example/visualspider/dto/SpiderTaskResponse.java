package com.example.visualspider.dto;

import com.example.visualspider.entity.SpiderTask;
import java.time.LocalDateTime;
import java.util.List;

public class SpiderTaskResponse {

    private Long id;
    private String name;
    private String description;
    private SpiderTask.UrlMode urlMode;
    private String listPageUrl;
    private String listPageRule;
    private String[] seedUrls;
    private String contentPageRule;
    private String scheduleCron;
    private SpiderTask.TaskStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<FieldResponse> fields;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public SpiderTask.UrlMode getUrlMode() {
        return urlMode;
    }

    public void setUrlMode(SpiderTask.UrlMode urlMode) {
        this.urlMode = urlMode;
    }

    public String getListPageUrl() {
        return listPageUrl;
    }

    public void setListPageUrl(String listPageUrl) {
        this.listPageUrl = listPageUrl;
    }

    public String getListPageRule() {
        return listPageRule;
    }

    public void setListPageRule(String listPageRule) {
        this.listPageRule = listPageRule;
    }

    public String[] getSeedUrls() {
        return seedUrls;
    }

    public void setSeedUrls(String[] seedUrls) {
        this.seedUrls = seedUrls;
    }

    public String getContentPageRule() {
        return contentPageRule;
    }

    public void setContentPageRule(String contentPageRule) {
        this.contentPageRule = contentPageRule;
    }

    public String getScheduleCron() {
        return scheduleCron;
    }

    public void setScheduleCron(String scheduleCron) {
        this.scheduleCron = scheduleCron;
    }

    public SpiderTask.TaskStatus getStatus() {
        return status;
    }

    public void setStatus(SpiderTask.TaskStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<FieldResponse> getFields() {
        return fields;
    }

    public void setFields(List<FieldResponse> fields) {
        this.fields = fields;
    }

    public static SpiderTaskResponse from(SpiderTask task, List<FieldResponse> fields) {
        SpiderTaskResponse response = new SpiderTaskResponse();
        response.setId(task.getId());
        response.setName(task.getName());
        response.setDescription(task.getDescription());
        response.setUrlMode(task.getUrlMode());
        response.setListPageUrl(task.getListPageUrl());
        response.setListPageRule(task.getListPageRule());
        response.setSeedUrls(task.getSeedUrls());
        response.setContentPageRule(task.getContentPageRule());
        response.setScheduleCron(task.getScheduleCron());
        response.setStatus(task.getStatus());
        response.setCreatedAt(task.getCreatedAt());
        response.setUpdatedAt(task.getUpdatedAt());
        response.setFields(fields);
        return response;
    }
}