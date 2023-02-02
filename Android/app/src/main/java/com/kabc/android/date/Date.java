package com.kabc.android.date;

import static java.util.Calendar.DATE;
import static java.util.Calendar.MONTH;
import static java.util.Calendar.YEAR;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class Date {
    public static int getAge(String strDateOfBirth) {
        try {
            SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
            java.util.Date birthDate = format.parse(strDateOfBirth);
            java.util.Date currentDate = new java.util.Date();
            return getYearsDifference(birthDate, currentDate);
        } catch (ParseException e) {
            e.printStackTrace();
            return -1;
        }
    }

    private static int getYearsDifference(java.util.Date firstDate, java.util.Date secondDate) {
        Calendar firstCalendar = getCalendar(firstDate);
        Calendar secondCalendar = getCalendar(secondDate);
        int yearsDifference = secondCalendar.get(YEAR) - firstCalendar.get(YEAR);
        int monthDifference = firstCalendar.get(MONTH) - secondCalendar.get(MONTH);
        int dayDifference = firstCalendar.get(DATE) - secondCalendar.get(DATE);
        if (monthDifference > 0 || monthDifference == 0 && dayDifference > 0) {
            --yearsDifference;
        }
        return yearsDifference;
    }

    private static Calendar getCalendar(java.util.Date date) {
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTime(date);
        return calendar;
    }
}
