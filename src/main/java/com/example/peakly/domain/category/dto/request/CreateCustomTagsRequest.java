package com.example.peakly.domain.category.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

//커스텀 태그 생성
public record CreateCustomTagsRequest(
        @NotNull(message = "대분류 ID가 필요합니다.")
        Long majorCategoryId,

        @NotEmpty(message = "태그 이름 목록이 필요합니다.")
        List<@NotBlank(message = "태그 이름은 비어 있을 수 없습니다.") String> names
) {
}
