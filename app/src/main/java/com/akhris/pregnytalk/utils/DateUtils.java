package com.akhris.pregnytalk.utils;

import android.app.DatePickerDialog;
import android.content.Context;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

/**
 * Util class for handling date/time operations
 */
public class DateUtils {

    /**
     * Get human-readable String from date in milliseconds.
     * @param millis - date in milliseconds.
     * @return Localized date String ("12/03/2015", for example)
     */
    public static String formatDateFromMillis(Long millis){
        if(millis==null){
            return getLocalizedPattern();
        }
        DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.SHORT, Locale.getDefault());
        Date date = new Date(millis);
        return dateFormat.format(date);
    }

    /**
     * Get human-readable String from time in milliseconds.
     * @param millis - time in milliseconds
     * @return time String like "16:05"
     */
    public static String formatTimeFromMillis(Long millis){
        if(millis==null){
            return getLocalizedPattern();
        }
        DateFormat dateFormat = new SimpleDateFormat( "HH:mm", Locale.getDefault());
        Date date = new Date(millis);
        return dateFormat.format(date);
    }

    /**
     * Used as a hint when formatDateFromMillis or formatTimeFromMillis is called with null parameter
     * @return localized pattern.
     */
    private static String getLocalizedPattern(){
        SimpleDateFormat dateFormat = (SimpleDateFormat) DateFormat.getDateInstance(DateFormat.SHORT, Locale.getDefault());
        return dateFormat.toLocalizedPattern();
    }

    /**
     * Make Calendar instance for given year, month and day
     */
    private static Calendar getCalendar(int year, int month, int dayOfMonth){
        Calendar calendar = GregorianCalendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        return calendar;
    }

    /**
     * Make Calendar instance for given time in milliseconds
     */
    private static Calendar getCalendar(Long timeInMillis){
    Calendar date = GregorianCalendar.getInstance();
    if(timeInMillis!=null) {
        date.setTimeInMillis(timeInMillis);
    }
    return date;
    }

    /**
     * Shows DatePickerDialog for given date in millisecons
     */
    public static void showDatePicker(Context context, final Long timeInMillis, final DatePickerCallback callback){
        final Calendar date = getCalendar(timeInMillis);
        final DatePickerDialog.OnDateSetListener dateSetListener = (view, year, month, dayOfMonth) -> callback.onDateChanged(DateUtils.getCalendar(year, month, dayOfMonth).getTimeInMillis());

        new DatePickerDialog(
                context,
                dateSetListener,
                date.get(Calendar.YEAR),
                date.get(Calendar.MONTH),
                date.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    public interface DatePickerCallback {
        void onDateChanged(long timeInMillis);
    }
}
