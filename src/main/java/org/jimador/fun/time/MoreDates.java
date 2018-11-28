package org.jimador.fun.time;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableSet;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Utility class for Dates
 *
 * @author James Dunnam
 * @implNote US Federal holidays are cached for convenience, as the dates are calculated. The cached dates are expired after 10 minutes or 1000 entries in an LRU fashion.
 */
public class MoreDates {

    private static final LoadingCache<HolidayKey, LocalDate> holidayCache = getCache();

    private MoreDates() {
    }

    /**
     * The total number of days {@link LocalDate} inclusive
     *
     * @param start the start date.
     * @param end   the end date.
     *
     * @return the number of days.
     */
    public static int totalDaysBetween(LocalDate start, LocalDate end) {
        return (int) ChronoUnit.DAYS.between(start, end) + 1;
    }

    /**
     * The total number of business days between 2 {@link LocalDate}.
     *
     * @param start the start date.
     * @param end   the end date.
     *
     * @return the number of days.
     *
     * @implNote Business days exclude weekends and the observance of US Federal holidays.
     */
    public static int totalBusinessDaysBetween(LocalDate start, LocalDate end) {
        Objects.requireNonNull(start, "Start date must not be null");
        Objects.requireNonNull(end, "End date must not be null");
        long daysBetweenWithoutWeekends = calculateDaysBetweenWithoutWeekends(start, end);
        final Set<LocalDate> holidayForYearRange = getUSFederalHolidayForYearRange(start.getYear(), end.getYear());
        for (LocalDate localDate : holidayForYearRange) {
            if (localDate.isAfter(start) && localDate.isBefore(end)) {
                daysBetweenWithoutWeekends--;
            }
        }
        return (int) daysBetweenWithoutWeekends;
    }

    /**
     * Set of all US Federal Holidays for a given year range.
     *
     * @param start the start year
     * @param end   the end year
     *
     * @return a {@link Set} of {@link LocalDate}
     */
    private static Set<LocalDate> getUSFederalHolidayForYearRange(int start, int end) {
        ImmutableSet.Builder<LocalDate> holidayDates = ImmutableSet.builder();
        for (int i = start; i <= end; i++) {
            holidayDates.addAll(getUSFederalHolidayDatesForYear(i));
        }

        return holidayDates.build();
    }

    /**
     * Set of all US Federal Holidays for a given year
     *
     * @param year the year
     *
     * @return a {@link Set} of {@link LocalDate}
     */
    private static Set<LocalDate> getUSFederalHolidayDatesForYear(int year) {
        ImmutableSet.Builder<LocalDate> holidayDates = ImmutableSet.builder();
        for (USFederalHoliday day : USFederalHoliday.US_FEDERAL_HOLIDAYS) {
            HolidayKey of = HolidayKey.of(day, year);
            LocalDate unchecked = holidayCache.getUnchecked(of);
            holidayDates.add(unchecked);
        }
        return holidayDates.build();
    }

    /**
     * Construct a {@link LoadingCache} for caching holiday dates as we come across them.
     *
     * @return a {@link LoadingCache} instance.
     */
    private static LoadingCache<HolidayKey, LocalDate> getCache() {
        return CacheBuilder.newBuilder()
                           .maximumSize(1000)
                           .expireAfterWrite(10, TimeUnit.MINUTES)
                           .build(new CacheLoader<HolidayKey, LocalDate>() {
                               @Override
                               public LocalDate load(HolidayKey key) throws Exception {
                                   return key.holiday.getObservanceFor(key.year);
                               }
                           });
    }

    /**
     * Helper method to calculate the number of days between 2 dates. Arbitraged from  http://stackoverflow.com/a/4600534/2918190
     *
     * @param start the start date
     * @param end   the end date
     *
     * @return the number of days between start and end excluding weekends
     */
    private static long calculateDaysBetweenWithoutWeekends(LocalDate start, LocalDate end) {

        Calendar calendar1 = Calendar.getInstance();
        calendar1.setTime(java.sql.Date.valueOf(start));
        int weekday1 = calendar1.get(Calendar.DAY_OF_WEEK);
        calendar1.add(Calendar.DAY_OF_WEEK, -weekday1);

        Calendar calendar2 = Calendar.getInstance();
        calendar2.setTime(java.sql.Date.valueOf(end));
        int weekday2 = calendar2.get(Calendar.DAY_OF_WEEK);
        calendar2.add(Calendar.DAY_OF_WEEK, -weekday2);

        //end Saturday to start Saturday
        long days = (calendar2.getTimeInMillis() - calendar1.getTimeInMillis()) / (1000 * 60 * 60 * 24);
        long daysWithoutWeekendDays = days - (days * 2 / 7);

        // Adjust days to add on (w2) and days to subtract (w1) so that Saturday
        // and Sunday are not included
        if (weekday1 == Calendar.SUNDAY && weekday2 != Calendar.SATURDAY) {
            weekday1 = Calendar.MONDAY;
        } else if (weekday1 == Calendar.SATURDAY && weekday2 != Calendar.SUNDAY) {
            weekday1 = Calendar.FRIDAY;
        }

        if (weekday2 == Calendar.SUNDAY) {
            weekday2 = Calendar.MONDAY;
        } else if (weekday2 == Calendar.SATURDAY) {
            weekday2 = Calendar.FRIDAY;
        }

        return daysWithoutWeekendDays - weekday1 + weekday2;
    }

    /**
     * Holder class for cache key data
     */
    private static class HolidayKey implements Serializable {
        private static final long serialVersionUID = -4601024994462669026L;

        private final USFederalHoliday holiday;
        private final int year;

        private HolidayKey(USFederalHoliday holiday, int year) {
            this.holiday = holiday;
            this.year = year;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            final HolidayKey that = (HolidayKey) o;
            return year == that.year &&
                   holiday == that.holiday;
        }

        @Override
        public int hashCode() {
            return Objects.hash(holiday, year);
        }

        static HolidayKey of(USFederalHoliday holiday, int year) {
            return new HolidayKey(holiday, year);
        }
    }
}
