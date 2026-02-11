package com.example.peakly.domain.category.repository;

import com.example.peakly.domain.category.entity.MajorCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MajorCategoryRepository extends JpaRepository<MajorCategory, Long> {
    List<MajorCategory> findAllByOrderBySortOrderAscIdAsc();
}
