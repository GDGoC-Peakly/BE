package com.example.peakly.domain.category.converter;

import com.example.peakly.domain.category.dto.response.MajorCategoryItemDto;
import com.example.peakly.domain.category.entity.MajorCategory;

public class MajorCategoryConverter {
    private MajorCategoryConverter() {}

    public static MajorCategoryItemDto toItemDto(MajorCategory majorCategory) {
        return new MajorCategoryItemDto(majorCategory.getId(), majorCategory.getName());
    }
}
