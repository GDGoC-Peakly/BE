package com.example.peakly.domain.category.dto.request;

import java.util.List;

//커스텀 태그 생성
public record CreateCustomTagsRequest(
        Long majorCategoryId,
        List<String> names
) {
}
