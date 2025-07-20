package com.digitaltolk.translation_management_service.exception;

public class TranslationNotFoundException extends RuntimeException {
    public TranslationNotFoundException(String message) {
        super(message);
    }
}
