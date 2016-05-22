package com.example.android.sunshine.app;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by shamim on 5/16/16.
 */
public class DateFormatUtil {
    public static String generateDateString() {
        DateFormat dateFormat = new SimpleDateFormat("EEE, MMM d yyyy", Locale.US);
        return dateFormat.format(new Date()).toUpperCase();
    }

    // private constructor to prevent instantiation
    private DateFormatUtil() {
    }
}
