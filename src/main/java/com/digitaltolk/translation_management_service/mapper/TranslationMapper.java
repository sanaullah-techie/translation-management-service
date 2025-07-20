package com.digitaltolk.translation_management_service.mapper;

import com.digitaltolk.translation_management_service.dto.TranslationRequestDto;
import com.digitaltolk.translation_management_service.dto.TranslationResponseDto;
import com.digitaltolk.translation_management_service.entity.Translation;
import org.springframework.stereotype.Component;

@Component
public class TranslationMapper {

    public Translation toEntity(TranslationRequestDto dto) {
        return new Translation(
                dto.getTranslationKey(),
                dto.getLocale(),
                dto.getContent(),
                dto.getTags()
        );
    }

    public TranslationResponseDto toDto(Translation entity) {
        return new TranslationResponseDto(
                entity.getId(),
                entity.getTranslationKey(),
                entity.getLocale(),
                entity.getContent(),
                entity.getTags(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public void updateEntity(TranslationRequestDto dto, Translation entity) {
        entity.setTranslationKey(dto.getTranslationKey());
        entity.setLocale(dto.getLocale());
        entity.setContent(dto.getContent());
        entity.setTags(dto.getTags());
    }
}
