package com.example.peakly.domain.category.repository;

import com.example.peakly.domain.category.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
    List<Category> findMyByMajor(@Param("userId") Long userId, @Param("majorCategoryId") Long majorCategoryId);

    @Query("""
    select c
    from Category c
    where c.user.id = :userId
      and c.majorCategory.id = :majorCategoryId
      and c.name in :names
""")
    List<Category> findMyByMajorAndNameIn(@Param("userId") Long userId,
                                          @Param("majorCategoryId") Long majorCategoryId,
                                          @Param("names") Collection<String> names);

    @Query("""
    select (count(c) > 0)
    from Category c
    where c.user.id = :userId
      and c.majorCategory.id = :majorCategoryId
      and c.name = :name
""")
    boolean existsMyByMajorAndName(@Param("userId") Long userId,
                                   @Param("majorCategoryId") Long majorCategoryId,
                                   @Param("name") String name);

    @Query("""
    select count(c)
    from Category c
    where c.user.id = :userId
      and c.majorCategory.id = :majorCategoryId
""")
    int countMyByMajor(@Param("userId") Long userId, @Param("majorCategoryId") Long majorCategoryId);

    @Query("""
    select c
    from Category c
    join fetch c.majorCategory m
    where c.user.id = :userId
""")
    List<Category> findAllMyWithMajor(@Param("userId") Long userId);

}
