package com.example.peakly.domain.category.dto.request;

//커스텀 태그 수정
public record UpdateCustomTagRequest(
        String name,
        Integer sortOrder) {
}
