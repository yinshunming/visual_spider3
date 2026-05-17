package com.example.visualspider.dto;

import jakarta.validation.constraints.NotBlank;

public class TestSelectorRequest {

    @NotBlank(message = "selector is required")
    private String selector;

    @NotBlank(message = "type is required")
    private String type;

    public String getSelector() {
        return selector;
    }

    public void setSelector(String selector) {
        this.selector = selector;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}