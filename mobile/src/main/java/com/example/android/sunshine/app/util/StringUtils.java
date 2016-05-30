package com.example.android.sunshine.app.util;

/**
 * Created by shamim on 5/28/16.
 */
public class StringUtils {
    public static boolean isBlank(String inputStr) {
        return inputStr == null || inputStr.trim().equals("");
    }

    public static boolean isNotBlank(String inputStr) {
        return !isBlank(inputStr);
    }

    // private constructor to prevent instantiation
    private StringUtils() {
    }
}
