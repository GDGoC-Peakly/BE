package com.example.peakly.domain.category.service;

import com.example.peakly.domain.category.converter.CategoryConverter;
import com.example.peakly.domain.category.converter.MajorCategoryConverter;
import com.example.peakly.domain.category.dto.request.CreateCustomTagsRequest;
import com.example.peakly.domain.category.dto.request.UpdateCustomTagRequest;
import com.example.peakly.domain.category.dto.response.CreateCustomTagsResponse;
import com.example.peakly.domain.category.dto.response.CustomTagItemDto;
import com.example.peakly.domain.category.dto.response.MajorCategoryItemDto;
import com.example.peakly.domain.category.dto.response.MajorWithCustomTagsResponse;
import com.example.peakly.domain.category.entity.Category;
import com.example.peakly.domain.category.entity.MajorCategory;
import com.example.peakly.domain.category.repository.CategoryRepository;
import com.example.peakly.domain.category.repository.MajorCategoryRepository;
import com.example.peakly.domain.user.entity.User;
import com.example.peakly.domain.user.repository.UserRepository;
import com.example.peakly.global.apiPayload.code.status.CategoryErrorCode;
import com.example.peakly.global.apiPayload.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {

    private final MajorCategoryRepository majorCategoryRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;


    // 조회
    @Override
    public List<MajorCategoryItemDto> getMajorCategories() {
        return majorCategoryRepository.findAllByOrderBySortOrderAscIdAsc()
                .stream()
                .map(MajorCategoryConverter::toItemDto)
                .toList();
    }

    @Override
    public MajorCategoryItemDto getMajorCategory(Long majorCategoryId) {
        MajorCategory major = majorCategoryRepository.findById(require(majorCategoryId))
                .orElseThrow(() -> new GeneralException(CategoryErrorCode.MAJOR_CATEGORY_NOT_FOUND));
        return MajorCategoryConverter.toItemDto(major);
    }

    @Override
    public List<CustomTagItemDto> getCustomTags(Long userId, Long majorCategoryId) {
        require(userId);
        require(majorCategoryId);

        return categoryRepository.findMyByMajor(userId, majorCategoryId)
                .stream()
                .map(CategoryConverter::toCustomTagItemDto)
                .toList();
    }


    // 생성
    @Override
    @Transactional
    public CreateCustomTagsResponse createCustomTags(Long userId, CreateCustomTagsRequest request) {
        require(userId);
        if (request == null) throw new GeneralException(CategoryErrorCode.INVALID_TAG_NAMES);

        Long majorCategoryId = request.majorCategoryId();
        if (majorCategoryId == null) throw new GeneralException(CategoryErrorCode.MAJOR_CATEGORY_NOT_FOUND);

        MajorCategory major = majorCategoryRepository.findById(majorCategoryId)
                .orElseThrow(() -> new GeneralException(CategoryErrorCode.MAJOR_CATEGORY_NOT_FOUND));

        // names 정규화
        List<String> normalized = normalizeNames(request.names());
        if (normalized.isEmpty()) {
            throw new GeneralException(CategoryErrorCode.INVALID_TAG_NAMES);
        }

        // 이미 존재하는 태그 제외
        Set<String> already = categoryRepository.findMyByMajorAndNameIn(userId, majorCategoryId, normalized)
                .stream()
                .map(Category::getName)
                .collect(Collectors.toSet());

        List<String> toCreate = normalized.stream()
                .filter(n -> !already.contains(n))
                .toList();

        List<Category> saved = List.of();

        if (!toCreate.isEmpty()) {
            // sortOrder는 기존 목록 뒤에 이어붙이기
            int baseSortOrder = categoryRepository.countMyByMajor(userId, majorCategoryId);

            User userRef = userRepository.getReferenceById(userId);

            List<Category> entities = new ArrayList<>(toCreate.size());
            for (int i = 0; i < toCreate.size(); i++) {
                entities.add(Category.create(userRef, major, toCreate.get(i), baseSortOrder + i));
            }

            try {
                saved = categoryRepository.saveAll(entities);
            } catch (DataIntegrityViolationException e) {
                // 동시성 등으로 유니크 충돌 가능
                throw new GeneralException(CategoryErrorCode.CUSTOM_TAG_NAME_DUPLICATE);
            }
        }

        List<CustomTagItemDto> createdTags = saved.stream()
                .sorted(Comparator.comparing(Category::getSortOrder).thenComparing(Category::getId))
                .map(CategoryConverter::toCustomTagItemDto)
                .toList();

        return new CreateCustomTagsResponse(majorCategoryId, createdTags);
    }


    // 수정/삭제
    @Override
    @Transactional
    public Void updateCustomTag(Long userId, Long customTagId, UpdateCustomTagRequest request) {
        require(userId);
        require(customTagId);
        if (request == null) throw new GeneralException(CategoryErrorCode.INVALID_TAG_NAMES);

        Category category = categoryRepository.findById(customTagId)
                .orElseThrow(() -> new GeneralException(CategoryErrorCode.CUSTOM_TAG_NOT_FOUND));

        // 소유권 체크
        if (category.getUser() == null || !Objects.equals(category.getUser().getId(), userId)) {
            throw new GeneralException(CategoryErrorCode.CUSTOM_TAG_FORBIDDEN);
        }

        // name 변경
        if (request.name() != null && !request.name().isBlank()) {
            String newName = request.name().trim();
            Long majorCategoryId = category.getMajorCategory().getId();

            if (!newName.equals(category.getName())
                    && categoryRepository.existsMyByMajorAndName(userId, majorCategoryId, newName)) {
                throw new GeneralException(CategoryErrorCode.CUSTOM_TAG_NAME_DUPLICATE);
            }

            category.updateName(newName);
        }

        // sortOrder 변경
        if (request.sortOrder() != null) {
            category.updateSortOrder(request.sortOrder());
        }

        return null;
    }


    @Override
    @Transactional
    public Void deleteCustomTag(Long userId, Long customTagId) {
        require(userId);
        require(customTagId);

        Category category = categoryRepository.findById(customTagId)
                .orElseThrow(() -> new GeneralException(CategoryErrorCode.CUSTOM_TAG_NOT_FOUND));

        if (category.getUser() == null || !Objects.equals(category.getUser().getId(), userId)) {
            throw new GeneralException(CategoryErrorCode.CUSTOM_TAG_FORBIDDEN);
        }

        categoryRepository.delete(category);
        return null;
    }

    @Override
    @Transactional
    public List<MajorWithCustomTagsResponse> getAllMajorWithMyCustomTags(Long userId) {
        if (userId == null) throw new GeneralException(CategoryErrorCode.CUSTOM_TAG_FORBIDDEN);

        List<MajorCategory> majors = majorCategoryRepository.findAllByOrderBySortOrderAscIdAsc();

        // 유저의 커스텀 태그를 한 번에 조회 (N+1 방지)
        List<Category> categories = categoryRepository.findAllMyWithMajor(userId);

        Map<Long, List<Category>> grouped = categories.stream()
                .collect(Collectors.groupingBy(c -> c.getMajorCategory().getId()));

        return majors.stream()
                .map(m -> {
                    List<CustomTagItemDto> tags = grouped.getOrDefault(m.getId(), List.of()).stream()
                            .sorted(Comparator.comparing(Category::getSortOrder).thenComparing(Category::getId))
                            .map(CategoryConverter::toCustomTagItemDto)
                            .toList();

                    return new MajorWithCustomTagsResponse(
                            new MajorCategoryItemDto(m.getId(), m.getName()),
                            tags);
                })
                .toList();
    }



    private static Long require(Long value) {
        if (value == null) throw new GeneralException(CategoryErrorCode.REQUIRED_FIELD_MISSING);
        return value;
    }

    private static List<String> normalizeNames(List<String> names) {
        if (names == null) return List.of();
        LinkedHashSet<String> set = new LinkedHashSet<>();
        for (String n : names) {
            if (n == null) continue;
            String t = n.trim();
            if (t.isBlank()) continue;
            set.add(t);
        }
        return new ArrayList<>(set);
    }
}
