package ru.demo.messenger.utils;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;

import org.threeten.bp.Duration;
import org.threeten.bp.Instant;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZonedDateTime;
import org.threeten.bp.format.DateTimeFormatter;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

import ru.demo.messenger.MainApp;
import ru.demo.messenger.R;

/**
 * Created by igor on 05.11.15.
 */
public class DateUtil {

    public static Locale getDefaultLocale() {
        final Locale currentLocale;
        final Configuration configuration = MainApp.getInstance().getResources().getConfiguration();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            currentLocale = configuration.getLocales().get(0);
        } else {
            currentLocale = configuration.locale;
        }
        if (currentLocale.getLanguage().equals(new Locale("ru").getLanguage()) ||
                currentLocale.getLanguage().equals(new Locale("de").getLanguage())) {
            return currentLocale;
        } else {
            return new Locale("en");
        }
    }

    public static String getDateStringFromTimestamp(Context context, long timestamp) {
        final ZonedDateTime now = ZonedDateTime.now();

        final ZonedDateTime date = Instant.ofEpochMilli(TimeUnit.SECONDS.toMillis(timestamp))
                .atZone(ZoneId.systemDefault());

        if (now.getYear() != date.getYear()) {
            return DateTimeFormatter.ofPattern("d'.'MM'.'yyyy", getDefaultLocale()).format(date);
        } else {
            int dayOfYearBefore = date.getDayOfYear();
            int dayOfYearNow = now.getDayOfYear();
            if (dayOfYearBefore == dayOfYearNow) {
                int hourBefore = date.getHour();
                int hourToday = now.getHour();
                if (hourBefore == hourToday) {
                    int minutesBefore = date.getMinute();
                    int minutesToday = now.getMinute();
                    int minutes = minutesToday - minutesBefore;
                    if (minutes <= 4) {
                        return context.getString(R.string.now);
                    } else {
                        return context.getResources().getQuantityString(R.plurals.minutes_ago, minutes, minutes);
                    }
                } else {
                    // FIXME: ET 21.12.16 crash when time zone different between devices
                    int mins = (int) Duration.between(date, now).toMinutes();
                    if (mins < 60) {
                        if (mins < 0) {
                            // TODO: ET 27.12.16 Is WA for ^
                            return context.getString(R.string.now);
                        }
                        return context.getResources().getQuantityString(R.plurals.minutes_ago, mins, mins);
                    } else {
                        int hours = hourToday - hourBefore;
                        return context.getResources().getQuantityString(R.plurals.hours_ago, hours, hours);
                    }
                }
            } else if (dayOfYearNow - 1 == dayOfYearBefore) {
                return context.getString(R.string.yesterday);
            } else if (dayOfYearNow - 2 == dayOfYearBefore) {
                return context.getString(R.string.preyesterday);
            } else {
                final String pattern;
                if (getDefaultLocale().getLanguage().equals(new Locale("de").getLanguage())) {
                    pattern = "d. MMMM";
                } else {
                    pattern = "d MMMM";
                }
                return DateTimeFormatter.ofPattern(pattern, getDefaultLocale()).format(date);
            }
        }
    }

}
