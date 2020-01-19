package com.flair.bi.compiler.utils;

import java.util.Objects;

public class SqlTimeConverter {
    public static String toSingular(String letter) {
        if (Objects.equals(letter, "hours")) {
            return "hour";
        } else if (Objects.equals(letter, "days")) {
            return "day";
        } else if (Objects.equals(letter, "months")) {
            return "month";
        } else if (Objects.equals(letter, "years")) {
            return "year";
        }
        return letter;
    }

    public static String toPlural(String letter) {
        return letter;
    }

    public static long toMillis(String hourOrDays, String number) {
        long parsedLong = Long.parseLong(number);
        if (Objects.equals(hourOrDays, "hours")) {
            return parsedLong * 60 * 60 * 1000;
        } else if (Objects.equals(hourOrDays, "days")) {
            return parsedLong * 24 * 60 * 60 * 1000;
        }
        throw new IllegalArgumentException("Unknown hours or days parameter " + hourOrDays + " and number " + number);
    }
}
