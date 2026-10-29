package com.wac.autocore.security;

import java.util.Optional;

/**
 * Håller den inloggade användaren. JavaFX kör allt på FX-tråden och appen har
 * en användare åt gången, så ett statiskt fält räcker (ingen ThreadLocal).
 */
public final class SecurityContext {

    private static volatile AuthUser currentUser;

    private SecurityContext() {
    }

    public static void setCurrentUser(AuthUser user) {
        currentUser = user;
    }

    public static Optional<AuthUser> getCurrentUser() {
        return Optional.ofNullable(currentUser);
    }

    public static String getUsername() {
        return getCurrentUser().map(AuthUser::username).orElse("unknown");
    }

    public static void clear() {
        currentUser = null;
    }
}
