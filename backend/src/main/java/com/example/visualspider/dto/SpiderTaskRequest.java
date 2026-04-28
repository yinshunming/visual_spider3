package com.example.visualspider.dto;

import com.example.visualspider.entity.SpiderTask;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class SpiderTaskRequest {

    private Long id; // null for create, set for update

    @NotBlank(message = "name is required")
    private String name;

    private String description;

    @NotNull(message = "urlMode is required")
    private SpiderTask.UrlMode urlMode;

    private String listPageUrl;

    private String listPageRule;

    private String[] seedUrls;

    private String contentPageRule;

    private String scheduleCron;

    @Valid
    private List<FieldRequest> fields;

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

    public List<FieldRequest> getFields() {
        return fields;
    }

    public void setFields(List<FieldRequest> fields) {
        this.fields = fields;
    }
}