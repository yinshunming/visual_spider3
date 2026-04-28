package com.example.visualspider.dto;

import com.example.visualspider.entity.SpiderField;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class FieldRequest {

    private Long id; // null for new field

    @NotBlank(message = "fieldName is required")
    private String fieldName;

    private String fieldLabel;

    @NotNull(message = "fieldType is required")
    private SpiderField.FieldType fieldType;

    private String selector;

    private SpiderField.SelectorType selectorType;

    private SpiderField.ExtractType extractType;

    private String attrName;

    private Boolean required = false;

    private String defaultValue;

    private Integer displayOrder;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
}