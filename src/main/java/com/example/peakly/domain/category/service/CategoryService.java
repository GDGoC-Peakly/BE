package com.example.peakly.domain.category.service;

import com.example.peakly.domain.category.dto.request.CreateCustomTagsRequest;
import com.example.peakly.domain.category.dto.request.UpdateCustomTagRequest;
import com.example.peakly.domain.category.dto.response.CreateCustomTagsResponse;
import com.example.peakly.domain.category.dto.response.CustomTagItemDto;
import com.example.peakly.domain.category.dto.response.MajorCategoryItemDto;
import com.example.peakly.domain.category.dto.response.MajorWithCustomTagsResponse;

import java.util.List;

public interface CategoryService {

    List<MajorCategoryItemDto> getMajorCategories();

    MajorCategoryItemDto getMajorCategory(Long majorCategoryId);

    List<CustomTagItemDto> getCustomTags(Long userId, Long majorCategoryId);

    CreateCustomTagsResponse createCustomTags(Long userId, CreateCustomTagsRequest request);

    Void updateCustomTag(Long userId, Long customTagId, UpdateCustomTagRequest request);

    Void deleteCustomTag(Long userId, Long customTagId);

    List<MajorWithCustomTagsResponse> getAllMajorWithMyCustomTags(Long userId);

}
