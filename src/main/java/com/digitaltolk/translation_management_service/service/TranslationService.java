package com.digitaltolk.translation_management_service.service;

import com.digitaltolk.translation_management_service.dto.TranslationRequestDto;
import com.digitaltolk.translation_management_service.dto.TranslationResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;

public interface TranslationService {

    TranslationResponseDto createTranslation(TranslationRequestDto requestDto);
    TranslationResponseDto updateTranslation(Long id, TranslationRequestDto requestDto);
    TranslationResponseDto getTranslationById(Long id);

    Page<TranslationResponseDto> findTranslationsByTags(List<String> tags, int page, int size, String sortBy, String sortDir);

    Page<TranslationResponseDto> findTranslationsByKey(String key, int page, int size, String sortBy, String sortDir);

    Page<TranslationResponseDto> findTranslationsByContent(String content, int page, int size, String sortBy, String sortDir);

    List<TranslationResponseDto>  getTranslationsByLocale(String locale,  int page, int size, String sortBy, String sortDir);

    void deleteTranslationById(Long id);
    List<String> getAvailableLocales();

    Page<TranslationResponseDto> searchTranslations(String key, String locale, String content, List<String> tags, int page, int size, String sortBy, String sortDir);
}
