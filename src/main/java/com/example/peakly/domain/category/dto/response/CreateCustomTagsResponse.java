package com.example.peakly.domain.category.dto.response;

import java.util.List;

public record CreateCustomTagsResponse(
        Long majorCategoryId,
        List<CustomTagItemDto> tags
) {
}
