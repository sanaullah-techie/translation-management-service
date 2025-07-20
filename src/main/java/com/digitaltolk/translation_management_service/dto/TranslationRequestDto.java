package com.digitaltolk.translation_management_service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.Set;

public class TranslationRequestDto {

    @NotBlank(message = "Translation key is required")
    @Size(max = 500, message = "Translation key must not exceed 500 characters")
    @JsonProperty("key")
    private String translationKey;

    @NotBlank(message = "Locale is required")
    @Pattern(regexp = "^[a-z]{2}(-[A-Z]{2})?$", message = "Locale must be in format 'en' or 'en-US'")
    @JsonProperty("locale")
    private String locale;

    @NotBlank(message = "Content is required")
    @JsonProperty("content")
    private String content;

    @JsonProperty("tags")
    private Set<String> tags;

    // Constructors
    public TranslationRequestDto() {}

    public TranslationRequestDto(String translationKey, String locale, String content, Set<String> tags) {
        this.translationKey = translationKey;
        this.locale = locale;
        this.content = content;
        this.tags = tags;
    }

    // Getters and Setters
    public String getTranslationKey() {
        return translationKey;
    }

    public void setTranslationKey(String translationKey) {
        this.translationKey = translationKey;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Set<String> getTags() {
        return tags;
    }

    public void setTags(Set<String> tags) {
        this.tags = tags;
    }

}