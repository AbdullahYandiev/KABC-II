package com.kabc.desktop.date;

import java.time.LocalDate;
import java.util.Arrays;

public class TextDate {

    public static boolean isValidBirthDate(String date) {
        if (date.contains("_")) {
            return false;
        }

        String[] tokens = date.split("\\.");
        int year = Integer.parseInt(tokens[2]);
        int month = Integer.parseInt(tokens[1]);
        int day = Integer.parseInt(tokens[0]);

        return isValidDate(year, month, day) && !isDateInFuture(year, month, day);
    }

    private static boolean isValidDate(int year, int month, int day) {
        return month <= 12 && day <= 31 && (!Arrays.asList(4, 6, 9, 11).contains(month) || day <= 30)
                && (month != 2 || ((year % 4 != 0 || day <= 29) && (year % 4 == 0 || day <= 28)));
    }

    private static boolean isDateInFuture(int year, int month, int day) {
        LocalDate todayDate = LocalDate.now();
        int todayYear = todayDate.getYear();
        int todayMonth = todayDate.getMonthValue();
        int todayDay = todayDate.getDayOfMonth();

        return year > todayYear || year == todayYear && (month > todayMonth || month == todayMonth && day > todayDay);
    }
}