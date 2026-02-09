package com.example.peakly.domain.category.dto.request;

import jakarta.validation.constraints.NotBlank;

//커스텀 태그 수정
public record UpdateCustomTagRequest(
        @NotBlank(message = "태그 이름은 비어 있을 수 없습니다.")
        String name,
        Integer sortOrder) {
}
