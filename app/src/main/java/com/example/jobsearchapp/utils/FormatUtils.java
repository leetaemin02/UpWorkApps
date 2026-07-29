package com.example.jobsearchapp.utils;

public class FormatUtils {
    public static String formatSalary(long salary) {
        if (salary >= 1000000) {
            double million = salary / 1000000.0;
            if (million == (long) million) {
                return String.format("%d Triệu", (long) million);
            } else {
                return String.format("%.1f Triệu", million).replace(".", ",");
            }
        }
        return String.valueOf(salary);
    }

    public static String formatSalaryRange(long min, long max) {
        if (min == 0 && max == 0) return "Thỏa thuận";
        return formatSalary(min) + " - " + formatSalary(max);
    }
}
