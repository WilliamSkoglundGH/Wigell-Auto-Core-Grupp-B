package com.wac.autocore.gui.util;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.util.Locale;
import java.util.ResourceBundle;

public class LanguageManager {
    //Hjälper oss genom message_xx.properies filerna att toggla mellan språken och populera texter.

    private static final ObjectProperty<ResourceBundle> bundle =
            new SimpleObjectProperty<>(loadBundle(new Locale("sv")));

    private static ResourceBundle loadBundle(Locale locale) {
        return ResourceBundle.getBundle("i18n.messages", locale);
    }

    public static void setLocale(Locale locale) {
        bundle.set(loadBundle(locale));
    }

    public static ResourceBundle getBundle() {
        return bundle.get();
    }

    public static String get(String key) {
        return bundle.get().getString(key);
    }

    public static StringBinding bind(String key) {
        return Bindings.createStringBinding(() -> bundle.get().getString(key), bundle);
    }
}

