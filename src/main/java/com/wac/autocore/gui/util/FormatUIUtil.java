package com.wac.autocore.gui.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class FormatUIUtil {
    // Statis Util klass för återanvändingsbara UI-formaterings metoder.

    public static final String DEFAULT_PATTERN = "dd/MM/yyyy HH:mm";

    private FormatUIUtil() {}


    public static String formatTime(LocalDateTime dateTime) {
        return dateTime.format(DateTimeFormatter.ofPattern(DEFAULT_PATTERN));

    }


}
