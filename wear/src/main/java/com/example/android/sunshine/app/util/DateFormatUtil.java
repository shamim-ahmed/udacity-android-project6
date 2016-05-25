package com.example.android.sunshine.app.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by shamim on 5/16/16.
 */
public class DateFormatUtil {
    public static String generateDateString(Date date) {
        DateFormat dateFormat = new SimpleDateFormat("EEE, MMM d yyyy", Locale.US);
        return dateFormat.format(date).toUpperCase();
    }

    // private constructor to prevent instantiation
    private DateFormatUtil() {
    }
}
