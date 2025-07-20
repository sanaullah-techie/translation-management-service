package com.digitaltolk.translation_management_service.repository;

import com.digitaltolk.translation_management_service.entity.Translation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TranslationRepository extends JpaRepository<Translation, Long> {

    @Query("SELECT t FROM Translation t LEFT JOIN FETCH t.tags tag WHERE t.locale = :locale")
    List<Translation> findByLocale(@Param("locale") String locale, Pageable pageable);

    @Query("SELECT t FROM Translation t JOIN FETCH t.tags tag WHERE tag IN :tags")
    Page<Translation> findByTagsIn(@Param("tags") List<String> tags, Pageable pageable);

    @Query("SELECT t FROM Translation t LEFT JOIN FETCH t.tags tag WHERE t.translationKey LIKE %:key%")
    Page<Translation> findByKeyContaining(@Param("key") String key, Pageable pageable);

    @Query("SELECT t FROM Translation t LEFT JOIN FETCH t.tags tag WHERE LOWER(t.content) LIKE LOWER(CONCAT('%', :content, '%'))")
    Page<Translation> findByContentContainingIgnoreCase(@Param("content") String content, Pageable pageable);

    @Query("SELECT t FROM Translation t LEFT JOIN FETCH t.tags WHERE " +
            "(:key IS NULL OR t.translationKey LIKE %:key%) AND " +
            "(:locale IS NULL OR t.locale = :locale) AND " +
            "(:content IS NULL OR LOWER(t.content) LIKE LOWER(CONCAT('%', :content, '%')))")
    Page<Translation> findByFilters(@Param("key") String key,
                                    @Param("locale") String locale,
                                    @Param("content") String content,
                                    Pageable pageable);

    @Query("SELECT t FROM Translation t LEFT JOIN FETCH t.tags tag WHERE " +
            "tag IN :tags AND " +
            "(:key IS NULL OR t.translationKey LIKE %:key%) AND " +
            "(:locale IS NULL OR t.locale = :locale)")
    Page<Translation> findByTagsAndFilters(@Param("tags") List<String> tags,
                                           @Param("key") String key,
                                           @Param("locale") String locale,
                                           Pageable pageable);

    @Query("SELECT DISTINCT t.locale FROM Translation t ORDER BY t.locale")
    List<String> findDistinctLocales();
}
