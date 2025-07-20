package com.digitaltolk.translation_management_service.cache;

import com.digitaltolk.translation_management_service.dto.TranslationResponseDto;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TranslationCacheManager {

    private static final Logger logger = LoggerFactory.getLogger(TranslationCacheManager.class);

    // Cache names
    public static final String TRANSLATIONS_CACHE = "translations";
    public static final String LOCALE_TRANSLATIONS_CACHE = "localeTranslations";
    public static final String TRANSLATIONS_BY_TAGS_CACHE = "translationsByTags";
    public static final String TRANSLATIONS_BY_KEY_CACHE = "translationsByKey";
    public static final String TRANSLATIONS_BY_CONTENT_CACHE = "translationsByContent";
    public static final String AVAILABLE_LOCALES_CACHE = "availableLocales";

    private final CacheManager cacheManager;

    // Track cache keys for efficient eviction
    private final ConcurrentHashMap<String, Set<String>> localeKeysMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Set<String>> tagKeysMap = new ConcurrentHashMap<>();

    public TranslationCacheManager(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    /**
     * Cache a translation after creation
     */
    public void cacheNewTranslation(TranslationResponseDto translation) {
        try {
            // Cache the translation by ID
            Cache translationsCache = getCache(TRANSLATIONS_CACHE);
            if (Objects.nonNull(translationsCache)) {
                translationsCache.put(translation.getId(), translation);
                logger.debug("Cached new translation with ID: {}", translation.getId());
            }

            // Invalidate related caches that might be affected by new translation
            invalidateSearchCaches();
            invalidateLocaleCache(translation.getLocale());
            invalidateAvailableLocalesCache();

        } catch (Exception e) {
            logger.warn("Failed to cache new translation: {}", e.getMessage());
        }
    }

    /**
     * Update cached translation after modification
     */
    public void updateCachedTranslation(Long id, TranslationResponseDto updatedTranslation,
                                        String oldLocale, Set<String> oldTags) {
        try {
            // Update the main translation cache
            Cache translationsCache = getCache(TRANSLATIONS_CACHE);
            if (Objects.nonNull(translationsCache)) {
                translationsCache.put(id, updatedTranslation);
                logger.debug("Updated cached translation with ID: {}", id);
            }

            // Invalidate old locale cache if locale changed
            if (Objects.nonNull(oldLocale) && !oldLocale.equals(updatedTranslation.getLocale())) {
                invalidateLocaleCache(oldLocale);
            }

            // Invalidate new locale cache
            invalidateLocaleCache(updatedTranslation.getLocale());

            // Invalidate search caches as content might have changed
            invalidateSearchCaches();

            // If tags changed, invalidate tag-related caches
            if (!CollectionUtils.isEmpty(oldTags)) {
                invalidateTagCaches(oldTags);
            }
            if (updatedTranslation.getTags() != null) {
                invalidateTagCaches(updatedTranslation.getTags());
            }

        } catch (Exception e) {
            logger.warn("Failed to update cached translation: {}", e.getMessage());
        }
    }

    /**
     * Remove translation from cache after deletion
     */
    public void evictDeletedTranslation(Long id, String locale, Set<String> tags) {
        try {
            Cache translationsCache = getCache(TRANSLATIONS_CACHE);
            if (Objects.nonNull(translationsCache)) {
                translationsCache.evictIfPresent(id);
                logger.debug("Evicted translation with ID: {} from cache", id);
            }

            if (Objects.nonNull(locale)) {
                invalidateLocaleCache(locale);
            }

            if (!CollectionUtils.isEmpty(tags)) {
                invalidateTagCaches(tags);
            }

            invalidateSearchCaches();
            invalidateAvailableLocalesCache();

        } catch (Exception e) {
            logger.warn("Failed to evict deleted translation from cache: {}", e.getMessage());
        }
    }

    /**
     * Cache translation by ID
     */
    public void cacheTranslationById(Long id, TranslationResponseDto translation) {
        try {
            Cache cache = getCache(TRANSLATIONS_CACHE);
            if (Objects.nonNull(cache)) {
                cache.put(id, translation);
                logger.debug("Cached translation by ID: {}", id);
            }
        } catch (Exception e) {
            logger.warn("Failed to cache translation by ID: {}", e.getMessage());
        }
    }

    /**
     * Get cached translation by ID
     */
    public TranslationResponseDto getCachedTranslationById(Long id) {
        try {
            Cache cache = getCache(TRANSLATIONS_CACHE);
            if (Objects.nonNull(cache)) {
                Cache.ValueWrapper wrapper = cache.get(id);
                if (Objects.nonNull(wrapper)) {
                    logger.debug("Cache hit for translation ID: {}", id);
                    return (TranslationResponseDto) wrapper.get();
                }
            }
            logger.debug("Cache miss for translation ID: {}", id);
            return null;
        } catch (Exception e) {
            logger.warn("Error retrieving cached translation: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Cache translations by locale
     */
    public void cacheTranslationsByLocale(String locale, int page, int size,
                                          String sortBy, String sortDir,
                                          List<TranslationResponseDto> translations) {
        try {
            String cacheKey = buildLocaleKey(locale, page, size, sortBy, sortDir);
            Cache cache = getCache(LOCALE_TRANSLATIONS_CACHE);
            if (Objects.nonNull(cache)) {
                cache.put(cacheKey, translations);
                trackLocaleKey(locale, cacheKey);
                logger.debug("Cached translations for locale: {} with key: {}", locale, cacheKey);
            }
        } catch (Exception e) {
            logger.warn("Failed to cache translations by locale: {}", e.getMessage());
        }
    }

    /**
     * Get cached translations by locale
     */
    public List<TranslationResponseDto> getCachedTranslationsByLocale(String locale, int page, int size,
                                                                      String sortBy, String sortDir) {
        try {
            String cacheKey = buildLocaleKey(locale, page, size, sortBy, sortDir);
            Cache cache = getCache(LOCALE_TRANSLATIONS_CACHE);
            if (Objects.nonNull(cache)) {
                Cache.ValueWrapper wrapper = cache.get(cacheKey);
                if (Objects.nonNull(wrapper)) {
                    logger.debug("Cache hit for locale translations: {}", cacheKey);
                    return (List<TranslationResponseDto>) wrapper.get();
                }
            }
            logger.debug("Cache miss for locale translations: {}", cacheKey);
            return null;
        } catch (Exception e) {
            logger.warn("Error retrieving cached locale translations: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Cache available locales
     */
    public void cacheAvailableLocales(List<String> locales) {
        try {
            Cache cache = getCache(AVAILABLE_LOCALES_CACHE);
            if (Objects.nonNull(cache)) {
                cache.put("all", locales);
                logger.debug("Cached available locales");
            }
        } catch (Exception e) {
            logger.warn("Failed to cache available locales: {}", e.getMessage());
        }
    }

    /**
     * Get cached available locales
     */
    public List<String> getCachedAvailableLocales() {
        try {
            Cache cache = getCache(AVAILABLE_LOCALES_CACHE);
            if (Objects.nonNull(cache)) {
                Cache.ValueWrapper wrapper = cache.get("all");
                if (Objects.nonNull(wrapper)) {
                    logger.debug("Cache hit for available locales");
                    return (List<String>) wrapper.get();
                }
            }
            logger.debug("Cache miss for available locales");
            return null;
        } catch (Exception e) {
            logger.warn("Error retrieving cached available locales: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Invalidate all locale-related cache entries
     */
    public void invalidateLocaleCache(String locale) {
        if (Objects.isNull(locale)) return;

        try {
            Set<String> keysToEvict = localeKeysMap.get(locale);
            if (!CollectionUtils.isEmpty(keysToEvict)) {
                Cache cache = getCache(LOCALE_TRANSLATIONS_CACHE);
                if (Objects.nonNull(cache)) {
                    keysToEvict.forEach(cache::evictIfPresent);
                    logger.debug("Invalidated {} locale cache entries for locale: {}",
                            keysToEvict.size(), locale);
                }
                localeKeysMap.remove(locale);
            }
        } catch (Exception e) {
            logger.warn("Failed to invalidate locale cache: {}", e.getMessage());
        }
    }

    /**
     * Invalidate search-related caches
     */
    public void invalidateSearchCaches() {
        try {
            clearCache(TRANSLATIONS_BY_KEY_CACHE);
            clearCache(TRANSLATIONS_BY_CONTENT_CACHE);
            logger.debug("Invalidated search caches");
        } catch (Exception e) {
            logger.warn("Failed to invalidate search caches: {}", e.getMessage());
        }
    }

    /**
     * Invalidate tag-related caches
     */
    public void invalidateTagCaches(Set<String> tags) {

        if (CollectionUtils.isEmpty(tags)) return;

        try {
            clearCache(TRANSLATIONS_BY_TAGS_CACHE);
            logger.debug("Invalidated tag caches for tags: {}", tags);
        } catch (Exception e) {
            logger.warn("Failed to invalidate tag caches: {}", e.getMessage());
        }
    }

    /**
     * Invalidate available locales cache
     */
    public void invalidateAvailableLocalesCache() {
        try {
            clearCache(AVAILABLE_LOCALES_CACHE);
            logger.debug("Invalidated available locales cache");
        } catch (Exception e) {
            logger.warn("Failed to invalidate available locales cache: {}", e.getMessage());
        }
    }

    /**
     * Clear all translation-related caches
     */
    public void clearAllCaches() {
        try {
            clearCache(TRANSLATIONS_CACHE);
            clearCache(LOCALE_TRANSLATIONS_CACHE);
            clearCache(TRANSLATIONS_BY_TAGS_CACHE);
            clearCache(TRANSLATIONS_BY_KEY_CACHE);
            clearCache(TRANSLATIONS_BY_CONTENT_CACHE);
            clearCache(AVAILABLE_LOCALES_CACHE);

            // Clear tracking maps
            localeKeysMap.clear();
            tagKeysMap.clear();

            logger.info("Cleared all translation caches");
        } catch (Exception e) {
            logger.warn("Failed to clear all caches: {}", e.getMessage());
        }
    }

    // Helper methods

    private Cache getCache(String cacheName) {
        return cacheManager.getCache(cacheName);
    }

    private void clearCache(String cacheName) {
        Cache cache = getCache(cacheName);
        if (Objects.nonNull(cache)) {
            cache.clear();
        }
    }

    private String buildLocaleKey(String locale, int page, int size, String sortBy, String sortDir) {
        return String.format("%s_%d_%d_%s_%s", locale, page, size, sortBy, sortDir);
    }

    private void trackLocaleKey(String locale, String cacheKey) {
        localeKeysMap.computeIfAbsent(locale, k -> ConcurrentHashMap.newKeySet()).add(cacheKey);
    }

    /**
     * Get cache statistics for monitoring
     */
    public void logCacheStatistics() {
        try {
            logger.info("=== Translation Cache Statistics ===");
            logger.info("Tracked locale keys: {}", localeKeysMap.size());
            logger.info("Tracked tag keys: {}", tagKeysMap.size());

            // Log each cache's basic info
            for (String cacheName : List.of(TRANSLATIONS_CACHE, LOCALE_TRANSLATIONS_CACHE,
                    TRANSLATIONS_BY_TAGS_CACHE, TRANSLATIONS_BY_KEY_CACHE,
                    TRANSLATIONS_BY_CONTENT_CACHE, AVAILABLE_LOCALES_CACHE)) {
                Cache cache = getCache(cacheName);
                if (cache != null) {
                    logger.info("Cache '{}': Native cache type: {}", cacheName, cache.getNativeCache().getClass().getSimpleName());
                }
            }
        } catch (Exception e) {
            logger.warn("Failed to log cache statistics: {}", e.getMessage());
        }
    }
}