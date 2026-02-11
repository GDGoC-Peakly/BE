package com.example.peakly.domain.category.dto.response;

//커스텀 태그 목록 조회
public record CustomTagItemDto(
        Long id,
        String name,
        Integer sortOrder
) {
}
