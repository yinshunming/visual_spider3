package com.example.visualspider.dto;

import java.util.List;

public class TestSelectorResponse {

    private boolean unique;
    private int count;
    private List<ElementInfoResponse> elements;

    public TestSelectorResponse() {}

    public TestSelectorResponse(boolean unique, int count, List<ElementInfoResponse> elements) {
        this.unique = unique;
        this.count = count;
        this.elements = elements;
    }

    public boolean isUnique() {
        return unique;
    }

    public void setUnique(boolean unique) {
        this.unique = unique;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public List<ElementInfoResponse> getElements() {
        return elements;
    }

    public void setElements(List<ElementInfoResponse> elements) {
        this.elements = elements;
    }
}