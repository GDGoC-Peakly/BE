package com.example.peakly.domain.category.dto.response;

import java.util.List;

public record MajorWithCustomTagsResponse(
        MajorCategoryItemDto majorCategory,
        List<CustomTagItemDto> customTags
) {
}
