package com.example.visualspider.dto;

import com.example.visualspider.entity.SpiderField;

public class FieldResponse {

    private Long id;
    private Long taskId;
    private String fieldName;
    private String fieldLabel;
    private SpiderField.FieldType fieldType;
    private String selector;
    private SpiderField.SelectorType selectorType;
    private SpiderField.ExtractType extractType;
    private String attrName;
    private Boolean required;
    private String defaultValue;
    private Integer displayOrder;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getFieldLabel() {
        return fieldLabel;
    }

    public void setFieldLabel(String fieldLabel) {
        this.fieldLabel = fieldLabel;
    }

    public SpiderField.FieldType getFieldType() {
        return fieldType;
    }

    public void setFieldType(SpiderField.FieldType fieldType) {
        this.fieldType = fieldType;
    }

    public String getSelector() {
        return selector;
    }

    public void setSelector(String selector) {
        this.selector = selector;
    }

    public SpiderField.SelectorType getSelectorType() {
        return selectorType;
    }

    public void setSelectorType(SpiderField.SelectorType selectorType) {
        this.selectorType = selectorType;
    }

    public SpiderField.ExtractType getExtractType() {
        return extractType;
    }

    public void setExtractType(SpiderField.ExtractType extractType) {
        this.extractType = extractType;
    }

    public String getAttrName() {
        return attrName;
    }

    public void setAttrName(String attrName) {
        this.attrName = attrName;
    }

    public Boolean getRequired() {
        return required;
    }

    public void setRequired(Boolean required) {
        this.required = required;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    public static FieldResponse from(SpiderField field) {
        FieldResponse response = new FieldResponse();
        response.setId(field.getId());
        response.setTaskId(field.getTaskId());
        response.setFieldName(field.getFieldName());
        response.setFieldLabel(field.getFieldLabel());
        response.setFieldType(field.getFieldType());
        response.setSelector(field.getSelector());
        response.setSelectorType(field.getSelectorType());
        response.setExtractType(field.getExtractType());
        response.setAttrName(field.getAttrName());
        response.setRequired(field.getRequired());
        response.setDefaultValue(field.getDefaultValue());
        response.setDisplayOrder(field.getDisplayOrder());
        return response;
    }
}