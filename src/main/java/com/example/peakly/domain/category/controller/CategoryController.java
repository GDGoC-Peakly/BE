package com.example.peakly.domain.category.controller;

import com.example.peakly.domain.category.dto.request.CreateCustomTagsRequest;
import com.example.peakly.domain.category.dto.request.UpdateCustomTagRequest;
import com.example.peakly.domain.category.dto.response.CreateCustomTagsResponse;
import com.example.peakly.domain.category.dto.response.CustomTagItemDto;
import com.example.peakly.domain.category.dto.response.MajorCategoryItemDto;
import com.example.peakly.domain.category.dto.response.MajorWithCustomTagsResponse;
import com.example.peakly.domain.category.service.CategoryService;
import com.example.peakly.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.example.peakly.global.security.SecurityUtil.currentUserId;

@RestController
@RequiredArgsConstructor
@RequestMapping("/categories")
@Tag(name = "Category", description = "대분류/커스텀 카테고리 조회 API")
public class CategoryController {

    public final CategoryService categoryService;
    Long userId = currentUserId();

    // 대분류만 조회
    @Operation(
            summary = "대분류 목록 조회",
            description = "대분류 목록을 조회합니다."
    )
    @GetMapping("/major")
    public ApiResponse<List<MajorCategoryItemDto>> getMajorCategories() {
        List<MajorCategoryItemDto> result = categoryService.getMajorCategories();
        return ApiResponse.onSuccess(result);
    }


    // 대분류 1개 + 해당 custom 태그 조회
    @Operation(
            summary = "대분류 + 커스텀 태그 목록 조회",
            description = "특정 대분류 정보와, 해당 대분류에 속한 커스텀 태그 목록을 함께 조회합니다."
    )
    @GetMapping("/major/{majorCategoryId}")
    public ApiResponse<MajorWithCustomTagsResponse> getMajorWithCustomTags(@PathVariable Long majorCategoryId) {
        Long userId = currentUserId();

        MajorCategoryItemDto major = categoryService.getMajorCategory(majorCategoryId);
        List<CustomTagItemDto> customTags = categoryService.getCustomTags(userId, majorCategoryId);

        MajorWithCustomTagsResponse data = new MajorWithCustomTagsResponse(major, customTags);
        return ApiResponse.onSuccess(data);
    }


    //custom 태그만 조회
    @Operation(
            summary = "커스텀 태그 목록 조회",
            description = "특정 대분류에 속한 커스텀 태그 목록만 조회합니다."
    )
    @GetMapping("/custom/{majorCategoryId}")
    public ApiResponse<List<CustomTagItemDto>> getCustomTags(@PathVariable Long majorCategoryId)
    {
        Long userId = currentUserId();

        List<CustomTagItemDto> customTags = categoryService.getCustomTags(userId, majorCategoryId);
        return ApiResponse.onSuccess(customTags);
    }


    // custom 태그 생성
    @Operation(
            summary = "커스텀 태그 생성",
            description = "특정 대분류에 대해 커스텀 태그를 생성합니다."
    )
    @PostMapping("/custom")
    public ApiResponse<CreateCustomTagsResponse> createCustomTags(@RequestBody CreateCustomTagsRequest request) {
        Long userId = currentUserId();

        CreateCustomTagsResponse createCustom = categoryService.createCustomTags(userId, request);
        return ApiResponse.onSuccess(createCustom);
    }


    // custom 태그 수정
    @Operation(
            summary = "커스텀 태그 수정",
            description = "커스텀 태그의 이름 또는 정렬순서를 수정합니다."
    )
    @PatchMapping("/custom/{customTagId}")
    public ApiResponse<String> updateCustomTag(@PathVariable Long customTagId, @RequestBody UpdateCustomTagRequest request
    ) {
        Long userId = currentUserId();

        categoryService.updateCustomTag(userId, customTagId, request);
        return ApiResponse.onSuccess("수정되었습니다.");
    }


    //custom 태그 삭제
    @Operation(
            summary = "커스텀 태그 삭제",
            description = "커스텀 태그를 삭제합니다."
    )
    @DeleteMapping("/custom/{customTagId}")
    public ApiResponse<String> deleteCustomTag(@PathVariable Long customTagId) {
        Long userId = currentUserId();

        categoryService.deleteCustomTag(userId, customTagId);
        return ApiResponse.onSuccess("삭제되었습니다.");
    }

}
