package com.example.jobsearchapp.utils;

import java.util.Locale;

public class SalaryFormatter {
    public static String format(long salary) {
        if (salary <= 0) return "Thỏa thuận";
        if (salary >= 1000000) {
            double millions = salary / 1000000.0;
            if (millions == (long) millions) {
                return String.format(Locale.getDefault(), "%d triệu", (long) millions);
            } else {
                return String.format(Locale.getDefault(), "%.1f triệu", millions);
            }
        }
        return String.valueOf(salary);
    }

    public static String formatRange(long min, long max) {
        if (min <= 0 && max <= 0) return "Thỏa thuận";
        if (min == max) return format(min);
        return format(min) + " - " + format(max);
    }
}
