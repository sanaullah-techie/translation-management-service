package com.digitaltolk.translation_management_service.entity;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "translations", indexes = {
        @Index(name = "idx_translation_key", columnList = "translationKey"),
        @Index(name = "idx_locale", columnList = "locale"),
        @Index(name = "idx_key_locale", columnList = "translationKey, locale"),
        @Index(name = "idx_content", columnList = "content")
})
@EntityListeners(AuditingEntityListener.class)
public class Translation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "translation_key", nullable = false, length = 500)
    private String translationKey;

    @Column(nullable = false, length = 10)
    private String locale;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "translation_tags",
            joinColumns = @JoinColumn(name = "translation_id"),
            indexes = @Index(name = "idx_tag", columnList = "tag"))
    @Column(name = "tag")
    private Set<String> tags;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;


    // Constructors
    public Translation() {}

    public Translation(String translationKey, String locale, String content, Set<String> tags) {
        this.translationKey = translationKey;
        this.locale = locale;
        this.content = content;
        this.tags = tags;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

}
