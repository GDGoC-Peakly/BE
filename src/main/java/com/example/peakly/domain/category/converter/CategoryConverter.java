package com.example.peakly.domain.category.converter;

import com.example.peakly.domain.category.dto.response.CustomTagItemDto;
import com.example.peakly.domain.category.entity.Category;

public class CategoryConverter {
    private CategoryConverter() {}

    public static CustomTagItemDto toCustomTagItemDto(Category category) {
        return new CustomTagItemDto(category.getId(), category.getName(), category.getSortOrder());
    }
}
