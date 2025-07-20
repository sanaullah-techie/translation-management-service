package com.digitaltolk.translation_management_service.service.impl;

import com.digitaltolk.translation_management_service.dto.TranslationRequestDto;
import com.digitaltolk.translation_management_service.dto.TranslationResponseDto;
import com.digitaltolk.translation_management_service.entity.Translation;
import com.digitaltolk.translation_management_service.exception.TranslationNotFoundException;
import com.digitaltolk.translation_management_service.mapper.TranslationMapper;
import com.digitaltolk.translation_management_service.repository.TranslationRepository;
import com.digitaltolk.translation_management_service.service.TranslationService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TranslationServiceImpl implements TranslationService {

    private final TranslationRepository translationRepository;
    private final TranslationMapper translationMapper;

    public TranslationServiceImpl(TranslationRepository translationRepository, TranslationMapper translationMapper) {
        this.translationRepository = translationRepository;
        this.translationMapper = translationMapper;
    }

    public TranslationResponseDto createTranslation(TranslationRequestDto requestDto) {

        Translation translation = translationMapper.toEntity(requestDto);
        Translation savedTranslation = translationRepository.save(translation);
        return translationMapper.toDto(savedTranslation);
    }

    public TranslationResponseDto updateTranslation(Long id, TranslationRequestDto requestDto) {

        Translation existingTranslation = translationRepository.findById(id)
                .orElseThrow(() -> new TranslationNotFoundException("Translation not found with id: " + id));

        translationMapper.updateEntity(requestDto, existingTranslation);
        existingTranslation = translationRepository.save(existingTranslation);
        return translationMapper.toDto(existingTranslation);
    }

    public TranslationResponseDto getTranslationById(Long id) {

        Translation translation = translationRepository.findById(id)
                .orElseThrow(() -> new TranslationNotFoundException("Translation not found with id: " + id));
        return translationMapper.toDto(translation);
    }

    public Page<TranslationResponseDto> findTranslationsByTags(List<String> tags, int page, int size, String sortBy, String sortDir) {
        if (tags == null || tags.isEmpty()) {
            throw new IllegalArgumentException("Tags list cannot be null or empty");
        }

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() :
                Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Translation> translations = translationRepository.findByTagsIn(tags, pageable);

        if (translations.isEmpty()) {
            throw new TranslationNotFoundException("No translations found with tags: " + tags);
        }

        return translations.map(translationMapper::toDto);
    }

    public Page<TranslationResponseDto> findTranslationsByKey(String key, int page, int size, String sortBy, String sortDir) {

        if (StringUtils.isEmpty(key)) {
            throw new IllegalArgumentException("Translation key cannot be null or empty");
        }

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() :
                Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Translation> translations = translationRepository.findByKeyContaining(key, pageable);

        if (translations.isEmpty()) {
            throw new TranslationNotFoundException("No translations found with key pattern: " + key);
        }

        return translations.map(translationMapper::toDto);
    }

    public Page<TranslationResponseDto> findTranslationsByContent(String content, int page, int size, String sortBy, String sortDir) {
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Content cannot be null or empty");
        }

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() :
                Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Translation> translations = translationRepository.findByContentContainingIgnoreCase(content, pageable);

        if (translations.isEmpty()) {
            throw new TranslationNotFoundException("No translations found with content pattern: " + content);
        }

        return translations.map(translationMapper::toDto);
    }

    public Page<TranslationResponseDto> searchTranslations(String key, String locale, String content, List<String> tags, int page, int size, String sortBy, String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() :
                Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Translation> translations;

        if (tags != null && !tags.isEmpty()) {
            translations = translationRepository.findByTagsAndFilters(tags, key, locale, pageable);
        } else {
            translations = translationRepository.findByFilters(key, locale, content, pageable);
        }

        if (translations.isEmpty()) {
            throw new TranslationNotFoundException("No translations found with defined criteria");
        }

        return translations.map(translationMapper::toDto);
    }

    public List<TranslationResponseDto> getTranslationsByLocale(String locale,  int page, int size, String sortBy, String sortDir) {

       Sort sort = sortDir.equalsIgnoreCase("desc") ?
               Sort.by(sortBy).descending() :
               Sort.by(sortBy).ascending();

       Pageable pageable = PageRequest.of(page, size, sort);
        List<Translation> translations = translationRepository.findByLocale(locale, pageable);
        if(translations.isEmpty()){
            throw new TranslationNotFoundException("Translation not found with locale: " + locale);
        }
        return translations.stream().map(translationMapper::toDto).toList();
    }

    public void deleteTranslationById(Long id) {
        if (!translationRepository.existsById(id)) {
            throw new TranslationNotFoundException("Translation not found with id: " + id);
        }
        translationRepository.deleteById(id);
    }

    public List<String> getAvailableLocales() {
        return translationRepository.findDistinctLocales();
    }
}
