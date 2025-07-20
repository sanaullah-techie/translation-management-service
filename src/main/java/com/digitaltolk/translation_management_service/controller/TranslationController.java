package com.digitaltolk.translation_management_service.controller;

import com.digitaltolk.translation_management_service.dto.TranslationRequestDto;
import com.digitaltolk.translation_management_service.dto.TranslationResponseDto;
import com.digitaltolk.translation_management_service.service.TranslationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/translations")
@Tag(name = "Translation Management", description = "API for managing translations")
public class TranslationController {

    private final TranslationService translationService;

    public TranslationController(TranslationService translationService) {
        this.translationService = translationService;
    }

    @PostMapping
    @Operation(summary = "Create a new translation")
    @ApiResponse(responseCode = "201", description = "Translation created successfully")
    public ResponseEntity<TranslationResponseDto> createTranslation(@Valid @RequestBody TranslationRequestDto requestDto) {
        TranslationResponseDto response = translationService.createTranslation(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing translation")
    @ApiResponse(responseCode = "200", description = "Translation updated successfully")
    public ResponseEntity<TranslationResponseDto> updateTranslation(
            @PathVariable Long id,
            @Valid @RequestBody TranslationRequestDto requestDto) {
        TranslationResponseDto response = translationService.updateTranslation(id, requestDto);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get translation by ID")
    @ApiResponse(responseCode = "200", description = "Translation found")
    public ResponseEntity<TranslationResponseDto> getTranslation(@PathVariable Long id) {
        TranslationResponseDto response = translationService.getTranslationById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search/by-tags")
    @Operation(summary = "Find translations by tags")
    @ApiResponse(responseCode = "200", description = "Translations found by tags")
    public ResponseEntity<Page<TranslationResponseDto>> findTranslationsByTags(
            @Parameter(description = "List of tags to search for") @Valid @RequestParam List<String> tags,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "50") int size,
            @Parameter(description = "Sort by field") @RequestParam(defaultValue = "updatedAt") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String sortDir) {

        Page<TranslationResponseDto> response = translationService.findTranslationsByTags(tags, page, size, sortBy, sortDir);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/search/by-key")
    @Operation(summary = "Find translations by translation key pattern")
    @ApiResponse(responseCode = "200", description = "Translations found by key pattern")
    public ResponseEntity<Page<TranslationResponseDto>> findTranslationsByKey(
            @Parameter(description = "Translation key pattern to search for") @Valid @RequestParam String key,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "50") int size,
            @Parameter(description = "Sort by field") @RequestParam(defaultValue = "updatedAt") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String sortDir) {

        Page<TranslationResponseDto> response = translationService.findTranslationsByKey(key, page, size, sortBy, sortDir);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/search/by-content")
    @Operation(summary = "Find translations by content pattern")
    @ApiResponse(responseCode = "200", description = "Translations found by content pattern")
    public ResponseEntity<Page<TranslationResponseDto>> findTranslationsByContent(
            @Parameter(description = "Content pattern to search for") @Valid @RequestParam String content,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "50") int size,
            @Parameter(description = "Sort by field") @RequestParam(defaultValue = "updatedAt") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String sortDir) {

        Page<TranslationResponseDto> response = translationService.findTranslationsByContent(content, page, size, sortBy, sortDir);

        return ResponseEntity.ok(response);
    }

     @GetMapping("/search/by-locale")
     @Operation(summary = "Find translations by locale")
     @ApiResponse(responseCode = "200", description = "Translations exported successfully")
      public ResponseEntity<List<TranslationResponseDto>> exportTranslations( @Parameter(description = "Content pattern to search for") @Valid @RequestParam String locale,
                                                                       @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
                                                                       @Parameter(description = "Page size") @RequestParam(defaultValue = "50") int size,
                                                                       @Parameter(description = "Sort by field") @RequestParam(defaultValue = "updatedAt") String sortBy,
                                                                       @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String sortDir) {

        List<TranslationResponseDto> translations = translationService.getTranslationsByLocale(locale, page, size, sortBy, sortDir);
        return ResponseEntity.ok(translations);
    }

    @GetMapping
    @Operation(summary = "Search translations with filters")
    @ApiResponse(responseCode = "200", description = "Translations found")
    public ResponseEntity<Page<TranslationResponseDto>> searchTranslations(
            @Parameter(description = "Filter by translation key") @RequestParam(required = false) String key,
            @Parameter(description = "Filter by locale") @RequestParam(required = false) String locale,
            @Parameter(description = "Filter by content") @RequestParam(required = false) String content,
            @Parameter(description = "Filter by tags") @RequestParam(required = false) List<String> tags,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "50") int size,
            @Parameter(description = "Sort by field") @RequestParam(defaultValue = "updatedAt") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String sortDir) {


        Page<TranslationResponseDto> response = translationService.searchTranslations(
                key, locale, content, tags, page, size, sortBy, sortDir );

        return ResponseEntity.ok(response);
    }


    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a translation")
    @ApiResponse(responseCode = "204", description = "Translation deleted successfully")
    public ResponseEntity<Void> deleteTranslation(@PathVariable Long id) {
        translationService.deleteTranslationById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/locales")
    @Operation(summary = "Get available locales")
    @ApiResponse(responseCode = "200", description = "Available locales")
    public ResponseEntity<List<String>> getAvailableLocales() {
        List<String> locales = translationService.getAvailableLocales();
        return ResponseEntity.ok(locales);
    }

}
