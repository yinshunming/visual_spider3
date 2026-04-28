package com.example.visualspider.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 爬虫字段配置实体类
 */
@Entity
@Table(name = "spider_fields")
public class SpiderField {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "task_id", nullable = false)
    private Long taskId;

    @Column(name = "field_name", nullable = false, length = 100)
    private String fieldName;

    @Column(name = "field_label", length = 255)
    private String fieldLabel;

    @Enumerated(EnumType.STRING)
    @Column(name = "field_type", nullable = false)
    private FieldType fieldType;

    @Column(length = 500)
    private String selector;

    @Enumerated(EnumType.STRING)
    @Column(name = "selector_type")
    private SelectorType selectorType;

    @Enumerated(EnumType.STRING)
    @Column(name = "extract_type")
    private ExtractType extractType;

    @Column(name = "attr_name", length = 100)
    private String attrName;

    @Column
    private Boolean required = false;

    @Column(name = "default_value", length = 500)
    private String defaultValue;

    @Column(name = "display_order")
    private Integer displayOrder;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getTaskId() { return taskId; }
    public void setTaskId(Long taskId) { this.taskId = taskId; }

    public String getFieldName() { return fieldName; }
    public void setFieldName(String fieldName) { this.fieldName = fieldName; }

    public String getFieldLabel() { return fieldLabel; }
    public void setFieldLabel(String fieldLabel) { this.fieldLabel = fieldLabel; }

    public FieldType getFieldType() { return fieldType; }
    public void setFieldType(FieldType fieldType) { this.fieldType = fieldType; }

    public String getSelector() { return selector; }
    public void setSelector(String selector) { this.selector = selector; }

    public SelectorType getSelectorType() { return selectorType; }
    public void setSelectorType(SelectorType selectorType) { this.selectorType = selectorType; }

    public ExtractType getExtractType() { return extractType; }
    public void setExtractType(ExtractType extractType) { this.extractType = extractType; }

    public String getAttrName() { return attrName; }
    public void setAttrName(String attrName) { this.attrName = attrName; }

    public Boolean getRequired() { return required; }
    public void setRequired(Boolean required) { this.required = required; }

    public String getDefaultValue() { return defaultValue; }
    public void setDefaultValue(String defaultValue) { this.defaultValue = defaultValue; }

    public Integer getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }

    /**
     * 字段类型枚举
     */
    public enum FieldType {
        text,
        image,
        link,
        richText
    }

    /**
     * 选择器类型枚举
     */
    public enum SelectorType {
        CSS,
        XPATH
    }

    /**
     * 提取类型枚举
     */
    public enum ExtractType {
        text,
        attr,
        html
    }
}
