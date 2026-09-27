package com.wac.autocore.gui.util;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class LanguageManager {

    private static final Logger logger = LoggerFactory.getLogger(LanguageManager.class);
    private static final ObjectProperty<ResourceBundle> bundle =
            new SimpleObjectProperty<>(loadBundle(new Locale("en")));


    private static ResourceBundle loadBundle(Locale locale) {
        try {
            return ResourceBundle.getBundle("i18n.messages", locale);
        } catch (MissingResourceException e) {
            logger.error("Varning: Hittade inte resursfilen 'i18n.messages' för locale: {}", locale, e);
            return null;
        }
    }

    public static void setLocale(Locale locale) {
        ResourceBundle rb = loadBundle(locale);
        if (rb != null) {
            bundle.set(rb);
        }
    }

    public static ResourceBundle getBundle() {
        return bundle.get();
    }

    public static String get(String key) {
        ResourceBundle rb = bundle.get();
        if (rb != null && rb.containsKey(key)) {
            return rb.getString(key);
        }
        return "!" + key + "!";
    }

    public static StringBinding bind(String key) {
        return Bindings.createStringBinding(() -> {
            ResourceBundle rb = bundle.get();
            if (rb != null && rb.containsKey(key)) {
                return rb.getString(key);
            }
            return "!" + key + "!";
        }, bundle);
    }
}