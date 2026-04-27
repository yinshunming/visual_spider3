package com.example.visualspider.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * 爬虫字段配置实体类
 */
@Entity
@Table(name = "spider_fields")
@Getter
@Setter
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

    /**
     * 字段类型枚举
     */
    public enum FieldType {
        /** 文本 */
        text,
        /** 图片 */
        image,
        /** 链接 */
        link,
        /** 富文本 */
        richText
    }

    /**
     * 选择器类型枚举
     */
    public enum SelectorType {
        /** CSS选择器 */
        CSS,
        /** XPath选择器 */
        XPATH
    }

    /**
     * 提取类型枚举
     */
    public enum ExtractType {
        /** 提取文本 */
        text,
        /** 提取属性 */
        attr,
        /** 提取HTML */
        html
    }
}