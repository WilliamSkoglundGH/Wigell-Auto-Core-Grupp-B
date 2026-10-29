package com.wac.autocore.security;

import com.wac.autocore.model.enums.UserRole;

import java.util.Arrays;
import java.util.Set;

/**
 * Den inloggade användaren, utan lösenord.
 */
public record AuthUser(String username, Set<UserRole> roles) {

    public AuthUser {
        roles = Set.copyOf(roles);
    }

    public boolean hasAnyRole(UserRole... required) {
        return Arrays.stream(required).anyMatch(roles::contains);
    }
}
