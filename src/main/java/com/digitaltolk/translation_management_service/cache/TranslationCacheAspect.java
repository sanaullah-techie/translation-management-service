package com.digitaltolk.translation_management_service.cache;

import com.digitaltolk.translation_management_service.dto.TranslationResponseDto;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;
import java.util.Set;

@Aspect
@Component
public class TranslationCacheAspect {

    private final TranslationCacheManager cacheManager;
    private static final Logger log = LoggerFactory.getLogger(TranslationCacheAspect.class);
    public TranslationCacheAspect(TranslationCacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @AfterReturning(
            pointcut = "execution(* com.digitaltolk.translation_management_service.service.impl.TranslationServiceImpl.createTranslation(..))",
            returning = "result"
    )
    public void afterCreateTranslation(JoinPoint joinPoint, TranslationResponseDto result) {
        log.debug("Caching newly created translation with ID: {}", result.getId());
        cacheManager.cacheNewTranslation(result);
    }

    @Around("execution(* com.digitaltolk.translation_management_service.service.impl.TranslationServiceImpl.updateTranslation(..))")
    public Object aroundUpdateTranslation(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();
        Long id = (Long) args[0];

        // Try to get old values from cache first to avoid extra DB call
        TranslationResponseDto cachedTranslation = cacheManager.getCachedTranslationById(id);
        String oldLocale = cachedTranslation != null ? cachedTranslation.getLocale() : null;
        Set<String> oldTags = cachedTranslation != null ? cachedTranslation.getTags() : null;

        // Proceed with the update
        TranslationResponseDto result = (TranslationResponseDto) joinPoint.proceed();

        // Update cache with the result
        cacheManager.updateCachedTranslation(id, result, oldLocale, oldTags);
        log.debug("Updated cached translation with ID: {}", id);

        return result;
    }

    @Around("execution(* com.digitaltolk.translation_management_service.service.impl.TranslationServiceImpl.getTranslationById(..))")
    public Object aroundGetTranslationById(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();
        Long id = (Long) args[0];

        // Try cache first
        TranslationResponseDto cachedResult = cacheManager.getCachedTranslationById(id);
        if (Objects.nonNull(cachedResult)) {
            log.debug("Cache hit for translation ID: {}", id);
            return cachedResult;
        }

        // Proceed with database fetch
        TranslationResponseDto result = (TranslationResponseDto) joinPoint.proceed();

        // Cache the result
        cacheManager.cacheTranslationById(id, result);
        log.debug("Cached translation with ID: {}", id);

        return result;
    }

    @Around("execution(* com.digitaltolk.translation_management_service.service.impl.TranslationServiceImpl.getTranslationsByLocale(..))")
    public Object aroundGetTranslationsByLocale(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();
        String locale = (String) args[0];
        int page = (int) args[1];
        int size = (int) args[2];
        String sortBy = (String) args[3];
        String sortDir = (String) args[4];

        // Try cache first
        List<TranslationResponseDto> cachedResult = cacheManager.getCachedTranslationsByLocale(
                locale, page, size, sortBy, sortDir);
        if (!CollectionUtils.isEmpty(cachedResult)) {
            log.debug("Cache hit for locale translations: {}", locale);
            return cachedResult;
        }

        // Proceed with database fetch
        @SuppressWarnings("unchecked")
        List<TranslationResponseDto> result = (List<TranslationResponseDto>) joinPoint.proceed();

        // Cache the result
        cacheManager.cacheTranslationsByLocale(locale, page, size, sortBy, sortDir, result);
        log.debug("Cached translations for locale: {}", locale);

        return result;
    }

    @Around("execution(* com.digitaltolk.translation_management_service.service.impl.TranslationServiceImpl.getAvailableLocales(..))")
    public Object aroundGetAvailableLocales(ProceedingJoinPoint joinPoint) throws Throwable {
        // Try cache first
        List<String> cachedResult = cacheManager.getCachedAvailableLocales();
        if (!CollectionUtils.isEmpty(cachedResult)) {
            log.debug("Cache hit for available locales");
            return cachedResult;
        }

        // Proceed with database fetch
        @SuppressWarnings("unchecked")
        List<String> result = (List<String>) joinPoint.proceed();

        // Cache the result
        cacheManager.cacheAvailableLocales(result);
        log.debug("Cached available locales");

        return result;
    }

    @Around("execution(* com.digitaltolk.translation_management_service.service.impl.TranslationServiceImpl.deleteTranslationById(..))")
    public Object aroundDeleteTranslation(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();
        Long id = (Long) args[0];

        // Try to get translation info from cache before deletion
        TranslationResponseDto cachedTranslation = cacheManager.getCachedTranslationById(id);
        String locale = cachedTranslation != null ? cachedTranslation.getLocale() : null;
        Set<String> tags = cachedTranslation != null ? cachedTranslation.getTags() : null;

        // Proceed with deletion
        Object result = joinPoint.proceed();

        // Evict from cache
        cacheManager.evictDeletedTranslation(id, locale, tags);
        log.debug("Evicted deleted translation from cache with ID: {}", id);

        return result;
    }

}
