package com.example.visualspider.fixture;

import com.example.visualspider.dto.FieldRequest;
import com.example.visualspider.entity.SpiderField;
import com.example.visualspider.entity.SpiderField.ExtractType;
import com.example.visualspider.entity.SpiderField.FieldType;
import com.example.visualspider.entity.SpiderField.SelectorType;

/**
 * Test fixtures for SpiderField entities
 */
public class FieldFixtures {

    private FieldFixtures() {
        // Utility class
    }

    public static SpiderField textField() {
        SpiderField field = new SpiderField();
        field.setFieldName("title");
        field.setFieldLabel("标题");
        field.setFieldType(FieldType.text);
        field.setSelector("h1.title");
        field.setSelectorType(SelectorType.CSS);
        field.setExtractType(ExtractType.text);
        field.setRequired(true);
        field.setDisplayOrder(1);
        return field;
    }

    public static SpiderField imageField() {
        SpiderField field = new SpiderField();
        field.setFieldName("image");
        field.setFieldLabel("图片");
        field.setFieldType(FieldType.image);
        field.setSelector("img.content-image");
        field.setSelectorType(SelectorType.CSS);
        field.setExtractType(ExtractType.attr);
        field.setAttrName("src");
        field.setRequired(false);
        field.setDisplayOrder(2);
        return field;
    }

    public static SpiderField linkField() {
        SpiderField field = new SpiderField();
        field.setFieldName("link");
        field.setFieldLabel("链接");
        field.setFieldType(FieldType.link);
        field.setSelector("a.content-link");
        field.setSelectorType(SelectorType.CSS);
        field.setExtractType(ExtractType.attr);
        field.setAttrName("href");
        field.setRequired(false);
        field.setDisplayOrder(3);
        return field;
    }

    public static SpiderField richTextField() {
        SpiderField field = new SpiderField();
        field.setFieldName("content");
        field.setFieldLabel("正文内容");
        field.setFieldType(FieldType.richText);
        field.setSelector("div.article-content");
        field.setSelectorType(SelectorType.CSS);
        field.setExtractType(ExtractType.html);
        field.setRequired(true);
        field.setDisplayOrder(4);
        return field;
    }

    public static FieldRequest textFieldRequest() {
        FieldRequest request = new FieldRequest();
        request.setFieldName("title");
        request.setFieldLabel("标题");
        request.setFieldType(FieldType.text);
        request.setSelector("h1.title");
        request.setSelectorType(SelectorType.CSS);
        request.setExtractType(ExtractType.text);
        request.setRequired(true);
        return request;
    }

    public static FieldRequest imageFieldRequest() {
        FieldRequest request = new FieldRequest();
        request.setFieldName("image");
        request.setFieldLabel("图片");
        request.setFieldType(FieldType.image);
        request.setSelector("img.content-image");
        request.setSelectorType(SelectorType.CSS);
        request.setExtractType(ExtractType.attr);
        request.setAttrName("src");
        request.setRequired(false);
        return request;
    }
}