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
}
