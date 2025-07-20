package com.digitaltolk.translation_management_service.util;

import com.digitaltolk.translation_management_service.cache.TranslationCacheManager;
import com.digitaltolk.translation_management_service.entity.Translation;
import com.digitaltolk.translation_management_service.repository.TranslationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);
    private final TranslationRepository translationRepository;
    private final Random random = new Random();
    private final String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private final char[] charArray = chars.toCharArray();

    private final String[] locales = {"en", "fr", "es", "de", "it", "pt", "ru", "ja", "ko", "zh"};
    private final String[] tags = {"mobile", "desktop", "web", "app", "ui", "button", "message", "error", "success"};
    private final String[] keyPrefixes = {"app", "common", "user", "product", "order", "payment", "profile", "settings"};

    public DataInitializer(TranslationRepository translationRepository) {
        this.translationRepository = translationRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (translationRepository.count() == 0) {
            logger.info("Populating database with test data...");
            populateDatabase(100000);
            logger.info("Database population completed!");
        }
    }

    public void populateDatabase(int recordCount) {
        List<Translation> translations = new ArrayList<>();

        for (int i = 0; i < recordCount; i++) {
            Translation translation = new Translation();
            translation.setTranslationKey(generateRandomKey());
            translation.setLocale(locales[random.nextInt(locales.length)]);
            translation.setContent(generateRandomContent());
            translation.setTags(generateRandomTags());

            translations.add(translation);

            // Batch save every 5000 records
            if (translations.size() >= 3000) {
                translationRepository.saveAll(translations);
                translations.clear();
            }
        }

        // Save remaining records
        if (!translations.isEmpty()) {
            translationRepository.saveAll(translations);
        }
    }

    private String generateRandomKey() {
        String prefix = keyPrefixes[random.nextInt(keyPrefixes.length)];
        return prefix + "." + generateRandomString(10);
    }

    private String generateRandomContent() {
        return "Sample translation content " + generateRandomString(20);
    }

    private Set<String> generateRandomTags() {
        int tagCount = random.nextInt(3) + 1;
        Set<String> selectedTags = new java.util.HashSet<>();

        for (int i = 0; i < tagCount; i++) {
            selectedTags.add(tags[random.nextInt(tags.length)]);
        }

        return selectedTags;
    }

    private String generateRandomString(int length) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < length; i++) {
            sb.append(charArray[random.nextInt(charArray.length)]);
        }

        return sb.toString();
    }
}
