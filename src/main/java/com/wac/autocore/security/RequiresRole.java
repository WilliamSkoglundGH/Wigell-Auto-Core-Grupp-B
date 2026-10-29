package com.wac.autocore.security;

import com.wac.autocore.model.enums.UserRole;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Markerar vilka roller som får anropa en metod i ett interface som skyddas av SecurityProxy.
 * Tom lista = det räcker att vara inloggad (motsvarar isAuthenticated()).
 * Kan sättas på metod eller på hela interfacet; metodens annotation vinner.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface RequiresRole {
    UserRole[] value() default {};
}
