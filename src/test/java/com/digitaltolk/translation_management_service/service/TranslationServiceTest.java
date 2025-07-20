package com.digitaltolk.translation_management_service.service;

import com.digitaltolk.translation_management_service.dto.TranslationRequestDto;
import com.digitaltolk.translation_management_service.dto.TranslationResponseDto;
import com.digitaltolk.translation_management_service.entity.Translation;
import com.digitaltolk.translation_management_service.exception.TranslationNotFoundException;
import com.digitaltolk.translation_management_service.mapper.TranslationMapper;
import com.digitaltolk.translation_management_service.repository.TranslationRepository;
import com.digitaltolk.translation_management_service.service.impl.TranslationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TranslationServiceTest {

    @Mock
    private TranslationRepository translationRepository;

    @Mock
    private TranslationMapper translationMapper;

    @InjectMocks
    private TranslationServiceImpl translationService;

    private Translation translation;
    private TranslationRequestDto requestDto;
    private TranslationResponseDto responseDto;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        translation = new Translation();
        translation.setId(1L);
        translation.setTranslationKey("test.key");
        translation.setLocale("en");
        translation.setContent("Test Content");

        requestDto = new TranslationRequestDto();
        requestDto.setTranslationKey("test.key");
        requestDto.setLocale("en");
        requestDto.setContent("Test Content");

        responseDto = new TranslationResponseDto();
        responseDto.setId(1L);
        responseDto.setTranslationKey("test.key");
        responseDto.setLocale("en");
        responseDto.setContent("Test Content");

        pageable = PageRequest.of(0, 10, Sort.by("id").ascending());
    }

    @Test
    void createTranslation_Success() {
        // Given
        when(translationMapper.toEntity(requestDto)).thenReturn(translation);
        when(translationRepository.save(translation)).thenReturn(translation);
        when(translationMapper.toDto(translation)).thenReturn(responseDto);

        // When
        TranslationResponseDto result = translationService.createTranslation(requestDto);

        // Then
        assertNotNull(result);
        assertEquals(responseDto.getId(), result.getId());
        assertEquals(responseDto.getTranslationKey(), result.getTranslationKey());
        verify(translationMapper).toEntity(requestDto);
        verify(translationRepository).save(translation);
        verify(translationMapper).toDto(translation);
    }

    @Test
    void updateTranslation_Success() {
        // Given
        Long id = 1L;
        when(translationRepository.findById(id)).thenReturn(Optional.of(translation));
        doNothing().when(translationMapper).updateEntity(requestDto, translation);
        when(translationRepository.save(translation)).thenReturn(translation);
        when(translationMapper.toDto(translation)).thenReturn(responseDto);

        // When
        TranslationResponseDto result = translationService.updateTranslation(id, requestDto);

        // Then
        assertNotNull(result);
        assertEquals(responseDto.getId(), result.getId());
        verify(translationRepository).findById(id);
        verify(translationMapper).updateEntity(requestDto, translation);
        verify(translationRepository).save(translation);
        verify(translationMapper).toDto(translation);
    }

    @Test
    void updateTranslation_NotFound() {
        // Given
        Long id = 1L;
        when(translationRepository.findById(id)).thenReturn(Optional.empty());

        // When & Then
        TranslationNotFoundException exception = assertThrows(
                TranslationNotFoundException.class,
                () -> translationService.updateTranslation(id, requestDto)
        );
        assertEquals("Translation not found with id: " + id, exception.getMessage());
        verify(translationRepository).findById(id);
        verifyNoMoreInteractions(translationMapper, translationRepository);
    }

    @Test
    void getTranslationById_Success() {
        // Given
        Long id = 1L;
        when(translationRepository.findById(id)).thenReturn(Optional.of(translation));
        when(translationMapper.toDto(translation)).thenReturn(responseDto);

        // When
        TranslationResponseDto result = translationService.getTranslationById(id);

        // Then
        assertNotNull(result);
        assertEquals(responseDto.getId(), result.getId());
        verify(translationRepository).findById(id);
        verify(translationMapper).toDto(translation);
    }

    @Test
    void getTranslationById_NotFound() {
        // Given
        Long id = 1L;
        when(translationRepository.findById(id)).thenReturn(Optional.empty());

        // When & Then
        TranslationNotFoundException exception = assertThrows(
                TranslationNotFoundException.class,
                () -> translationService.getTranslationById(id)
        );
        assertEquals("Translation not found with id: " + id, exception.getMessage());
        verify(translationRepository).findById(id);
        verifyNoInteractions(translationMapper);
    }

    @Test
    void findTranslationsByTags_Success() {
        // Given
        List<String> tags = Arrays.asList("tag1", "tag2");
        Page<Translation> translationPage = new PageImpl<>(Arrays.asList(translation));
        when(translationRepository.findByTagsIn(eq(tags), any(Pageable.class))).thenReturn(translationPage);
        when(translationMapper.toDto(translation)).thenReturn(responseDto);

        // When
        Page<TranslationResponseDto> result = translationService.findTranslationsByTags(tags, 0, 10, "id", "asc");

        // Then
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.getContent().size());
        verify(translationRepository).findByTagsIn(eq(tags), any(Pageable.class));
        verify(translationMapper).toDto(translation);
    }

    @Test
    void findTranslationsByTags_NullTags() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> translationService.findTranslationsByTags(null, 0, 10, "id", "asc")
        );
        assertEquals("Tags list cannot be null or empty", exception.getMessage());
        verifyNoInteractions(translationRepository, translationMapper);
    }

    @Test
    void findTranslationsByTags_EmptyTags() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> translationService.findTranslationsByTags(Collections.emptyList(), 0, 10, "id", "asc")
        );
        assertEquals("Tags list cannot be null or empty", exception.getMessage());
        verifyNoInteractions(translationRepository, translationMapper);
    }

    @Test
    void findTranslationsByTags_NoResults() {
        // Given
        List<String> tags = Arrays.asList("tag1", "tag2");
        Page<Translation> emptyPage = new PageImpl<>(Collections.emptyList());
        when(translationRepository.findByTagsIn(eq(tags), any(Pageable.class))).thenReturn(emptyPage);

        // When & Then
        TranslationNotFoundException exception = assertThrows(
                TranslationNotFoundException.class,
                () -> translationService.findTranslationsByTags(tags, 0, 10, "id", "asc")
        );
        assertEquals("No translations found with tags: " + tags, exception.getMessage());
        verify(translationRepository).findByTagsIn(eq(tags), any(Pageable.class));
    }

    @Test
    void findTranslationsByTags_DescendingSort() {
        // Given
        List<String> tags = Arrays.asList("tag1", "tag2");
        Page<Translation> translationPage = new PageImpl<>(Arrays.asList(translation));
        when(translationRepository.findByTagsIn(eq(tags), any(Pageable.class))).thenReturn(translationPage);
        when(translationMapper.toDto(translation)).thenReturn(responseDto);

        // When
        Page<TranslationResponseDto> result = translationService.findTranslationsByTags(tags, 0, 10, "id", "desc");

        // Then
        assertNotNull(result);
        verify(translationRepository).findByTagsIn(eq(tags), argThat(p ->
                p.getSort().equals(Sort.by("id").descending())));
    }

    @Test
    void findTranslationsByKey_Success() {
        // Given
        String key = "test.key";
        Page<Translation> translationPage = new PageImpl<>(Arrays.asList(translation));
        when(translationRepository.findByKeyContaining(eq(key), any(Pageable.class))).thenReturn(translationPage);
        when(translationMapper.toDto(translation)).thenReturn(responseDto);

        // When
        Page<TranslationResponseDto> result = translationService.findTranslationsByKey(key, 0, 10, "id", "asc");

        // Then
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.getContent().size());
        verify(translationRepository).findByKeyContaining(eq(key), any(Pageable.class));
    }

    @Test
    void findTranslationsByKey_EmptyKey() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> translationService.findTranslationsByKey("", 0, 10, "id", "asc")
        );
        assertEquals("Translation key cannot be null or empty", exception.getMessage());
        verifyNoInteractions(translationRepository);
    }

    @Test
    void findTranslationsByKey_NullKey() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> translationService.findTranslationsByKey(null, 0, 10, "id", "asc")
        );
        assertEquals("Translation key cannot be null or empty", exception.getMessage());
        verifyNoInteractions(translationRepository);
    }

    @Test
    void findTranslationsByKey_NoResults() {
        // Given
        String key = "nonexistent.key";
        Page<Translation> emptyPage = new PageImpl<>(Collections.emptyList());
        when(translationRepository.findByKeyContaining(eq(key), any(Pageable.class))).thenReturn(emptyPage);

        // When & Then
        TranslationNotFoundException exception = assertThrows(
                TranslationNotFoundException.class,
                () -> translationService.findTranslationsByKey(key, 0, 10, "id", "asc")
        );
        assertEquals("No translations found with key pattern: " + key, exception.getMessage());
    }

    @Test
    void findTranslationsByContent_Success() {
        // Given
        String content = "test content";
        Page<Translation> translationPage = new PageImpl<>(Arrays.asList(translation));
        when(translationRepository.findByContentContainingIgnoreCase(eq(content), any(Pageable.class)))
                .thenReturn(translationPage);
        when(translationMapper.toDto(translation)).thenReturn(responseDto);

        // When
        Page<TranslationResponseDto> result = translationService.findTranslationsByContent(content, 0, 10, "id", "asc");

        // Then
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.getContent().size());
        verify(translationRepository).findByContentContainingIgnoreCase(eq(content), any(Pageable.class));
    }

    @Test
    void findTranslationsByContent_NullContent() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> translationService.findTranslationsByContent(null, 0, 10, "id", "asc")
        );
        assertEquals("Content cannot be null or empty", exception.getMessage());
        verifyNoInteractions(translationRepository);
    }

    @Test
    void findTranslationsByContent_EmptyContent() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> translationService.findTranslationsByContent("   ", 0, 10, "id", "asc")
        );
        assertEquals("Content cannot be null or empty", exception.getMessage());
        verifyNoInteractions(translationRepository);
    }

    @Test
    void searchTranslations_WithTags() {
        // Given
        String key = "test.key";
        String locale = "en";
        String content = "content";
        List<String> tags = Arrays.asList("tag1", "tag2");
        Page<Translation> translationPage = new PageImpl<>(Arrays.asList(translation));

        when(translationRepository.findByTagsAndFilters(eq(tags), eq(key), eq(locale), any(Pageable.class)))
                .thenReturn(translationPage);
        when(translationMapper.toDto(translation)).thenReturn(responseDto);

        // When
        Page<TranslationResponseDto> result = translationService.searchTranslations(
                key, locale, content, tags, 0, 10, "id", "asc");

        // Then
        assertNotNull(result);
        assertFalse(result.isEmpty());
        verify(translationRepository).findByTagsAndFilters(eq(tags), eq(key), eq(locale), any(Pageable.class));
        verify(translationRepository, never()).findByFilters(anyString(), anyString(), anyString(), any(Pageable.class));
    }

    @Test
    void searchTranslations_WithoutTags() {
        // Given
        String key = "test.key";
        String locale = "en";
        String content = "content";
        Page<Translation> translationPage = new PageImpl<>(Arrays.asList(translation));

        when(translationRepository.findByFilters(eq(key), eq(locale), eq(content), any(Pageable.class)))
                .thenReturn(translationPage);
        when(translationMapper.toDto(translation)).thenReturn(responseDto);

        // When
        Page<TranslationResponseDto> result = translationService.searchTranslations(
                key, locale, content, null, 0, 10, "id", "asc");

        // Then
        assertNotNull(result);
        assertFalse(result.isEmpty());
        verify(translationRepository).findByFilters(eq(key), eq(locale), eq(content), any(Pageable.class));
        verify(translationRepository, never()).findByTagsAndFilters(any(), anyString(), anyString(), any(Pageable.class));
    }

    @Test
    void searchTranslations_NoResults() {
        // Given
        Page<Translation> emptyPage = new PageImpl<>(Collections.emptyList());
        when(translationRepository.findByFilters(anyString(), anyString(), anyString(), any(Pageable.class)))
                .thenReturn(emptyPage);

        // When & Then
        TranslationNotFoundException exception = assertThrows(
                TranslationNotFoundException.class,
                () -> translationService.searchTranslations("key", "en", "content", null, 0, 10, "id", "asc")
        );
        assertEquals("No translations found with defined criteria", exception.getMessage());
    }

    @Test
    void getTranslationsByLocale_Success() {
        // Given
        String locale = "en";
        List<Translation> translations = Arrays.asList(translation);
        when(translationRepository.findByLocale(eq(locale), any(Pageable.class))).thenReturn(translations);
        when(translationMapper.toDto(translation)).thenReturn(responseDto);

        // When
        List<TranslationResponseDto> result = translationService.getTranslationsByLocale(locale, 0, 10, "id", "asc");

        // Then
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        verify(translationRepository).findByLocale(eq(locale), any(Pageable.class));
        verify(translationMapper).toDto(translation);
    }

    @Test
    void getTranslationsByLocale_NoResults() {
        // Given
        String locale = "nonexistent";
        when(translationRepository.findByLocale(eq(locale), any(Pageable.class)))
                .thenReturn(Collections.emptyList());

        // When & Then
        TranslationNotFoundException exception = assertThrows(
                TranslationNotFoundException.class,
                () -> translationService.getTranslationsByLocale(locale, 0, 10, "id", "asc")
        );
        assertEquals("Translation not found with locale: " + locale, exception.getMessage());
        verify(translationRepository).findByLocale(eq(locale), any(Pageable.class));
    }

    @Test
    void deleteTranslationById_Success() {
        // Given
        Long id = 1L;
        when(translationRepository.existsById(id)).thenReturn(true);
        doNothing().when(translationRepository).deleteById(id);

        // When
        translationService.deleteTranslationById(id);

        // Then
        verify(translationRepository).existsById(id);
        verify(translationRepository).deleteById(id);
    }

    @Test
    void deleteTranslationById_NotFound() {
        // Given
        Long id = 1L;
        when(translationRepository.existsById(id)).thenReturn(false);

        // When & Then
        TranslationNotFoundException exception = assertThrows(
                TranslationNotFoundException.class,
                () -> translationService.deleteTranslationById(id)
        );
        assertEquals("Translation not found with id: " + id, exception.getMessage());
        verify(translationRepository).existsById(id);
        verify(translationRepository, never()).deleteById(id);
    }

    @Test
    void getAvailableLocales_Success() {
        // Given
        List<String> locales = Arrays.asList("en", "fr", "de");
        when(translationRepository.findDistinctLocales()).thenReturn(locales);

        // When
        List<String> result = translationService.getAvailableLocales();

        // Then
        assertNotNull(result);
        assertEquals(3, result.size());
        assertTrue(result.containsAll(locales));
        verify(translationRepository).findDistinctLocales();
    }

    @Test
    void getAvailableLocales_EmptyResult() {
        // Given
        when(translationRepository.findDistinctLocales()).thenReturn(Collections.emptyList());

        // When
        List<String> result = translationService.getAvailableLocales();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(translationRepository).findDistinctLocales();
    }
}
