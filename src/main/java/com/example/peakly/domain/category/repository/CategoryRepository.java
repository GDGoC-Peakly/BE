package com.example.peakly.domain.category.repository;

import com.example.peakly.domain.category.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    @Query("""
    select c
    from Category c
    where c.user.id = :userId
      and c.majorCategory.id = :majorCategoryId
    order by c.sortOrder asc, c.id asc
""")
    List<Category> findMyByMajor(Long userId, Long majorCategoryId);

    @Query("""
    select c
    from Category c
    where c.user.id = :userId
      and c.majorCategory.id = :majorCategoryId
      and c.name in :names
""")
    List<Category> findMyByMajorAndNameIn(Long userId, Long majorCategoryId, Collection<String> names);

    @Query("""
    select (count(c) > 0)
    from Category c
    where c.user.id = :userId
      and c.majorCategory.id = :majorCategoryId
      and c.name = :name
""")
    boolean existsMyByMajorAndName(Long userId, Long majorCategoryId, String name);

    @Query("""
    select count(c)
    from Category c
    where c.user.id = :userId
      and c.majorCategory.id = :majorCategoryId
""")
    int countMyByMajor(Long userId, Long majorCategoryId);
}
