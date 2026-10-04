package com.wac.autocore.gui.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class FormatUIUtil {
    // Statis Util klass för återanvändingsbara UI-formaterings metoder.

    public static final String DEFAULT_DATETIME = "dd/MM/yyyy HH:mm";
    public static final String DEFAULT_DATE = "dd/MM/yyyy";

    private FormatUIUtil() {}


    public static String formatTime(LocalDateTime dateTime) {
        return dateTime.format(DateTimeFormatter.ofPattern(DEFAULT_DATETIME));

    }
    public static String formatDate(LocalDate date){
        return date.format(DateTimeFormatter.ofPattern(DEFAULT_DATE));

    }


}
