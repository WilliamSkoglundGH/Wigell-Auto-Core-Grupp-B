package com.wac.autocore.security;

/**
 * Kastas av SecurityProxy när användaren saknar inloggning eller rätt roll.
 */
public class AccessDeniedException extends RuntimeException {

    public AccessDeniedException(String message) {
        super(message);
    }
}
