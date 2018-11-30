package org.jimador.fun.time;

import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableSet;
import com.google.common.primitives.Ints;

import java.io.Serializable;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Objects;
import java.util.Set;

/**
 * Utility class for Dates
 *
 * @author James Dunnam
 * @implNote US Federal holidays are cached for convenience, as the dates are calculated. The cached dates are expired after 10 minutes or 1000 entries in an LRU fashion.
 */
public final class MoreDates {

    private static final LoadingCache<HolidayKey, LocalDate> holidayCache = getCache();

    private MoreDates() {
    }

    /**
     * The total number of days between 2 {@link LocalDate}, start inclusive, end exclusive.
     *
     * @param start the start date.
     * @param end   the end date.
     *
     * @return the number of days.
     *
     * @throws NullPointerException if {@code start} <b>or</b> {@code end} are {@code null}
     * @implNote max returned from the method is {@link Integer#MAX_VALUE}
     */
    public static int totalDaysBetween(LocalDate start, LocalDate end) {
        Objects.requireNonNull(start, "Start date must not be null");
        Objects.requireNonNull(end, "End date must not be null");
        return Ints.saturatedCast(ChronoUnit.DAYS.between(start, end));
    }

    /**
     * The total number of days between 2 {@link Date}
     *
     * @param start the start date.
     * @param end   the end date.
     *
     * @return the number of days.
     *
     * @implNote self-use: this calls {@link #totalDaysBetween(LocalDate, LocalDate)}
     */
    public static int totalDaysBetween(Date start, Date end) {
        return totalDaysBetween(toLocalDate(start), toLocalDate(end));
    }

    /**
     * The total number of business days between 2 {@link LocalDate}.
     *
     * @param start the start date.
     * @param end   the end date.
     *
     * @return the number of days.
     *
     * @throws NullPointerException     if {@code start} <b>or</b> {@code end} is {@code null}
     * @throws IllegalArgumentException if {@code start} <b>is after</b> {@code end}
     * @implNote Business days exclude weekends and the observance of US Federal holidays.
     */
    public static int totalBusinessDaysBetween(LocalDate start, LocalDate end) {
        Objects.requireNonNull(start, "Start date must not be null");
        Objects.requireNonNull(end, "End date must not be null");

        if (start.equals(end)) {
            return 0;
        }

        Preconditions.checkArgument(start.isBefore(end), "Start must be before end");
        long daysBetweenWithoutWeekends = calculateNumberOfDaysBetweenMinusWeekends(start, end);
        final Set<LocalDate> holidayForYearRange = getUSFederalHolidayForYearRange(start.getYear(), end.getYear());
        for (LocalDate localDate : holidayForYearRange) {
            if (localDate.isAfter(start) && localDate.isBefore(end)) {
                daysBetweenWithoutWeekends--;
            }
        }
        return (int) daysBetweenWithoutWeekends;
    }

    /**
     * The total number of business days between 2 {@link Date}
     *
     * @param start the start date
     * @param end   the end date
     *
     * @return the number of days
     *
     * @implNote self use: this method calls {@link #totalBusinessDaysBetween(LocalDate, LocalDate)}
     */
    public static int totalBusinessDaysBetween(Date start, Date end) {
        return totalBusinessDaysBetween(toLocalDate(start), toLocalDate(end));
    }

    /**
     * Set of all US Federal Holidays for a given year range.
     *
     * @param start the start year
     * @param end   the end year
     *
     * @return a {@link Set} of {@link LocalDate}
     */
    public static Set<LocalDate> getUSFederalHolidayForYearRange(int start, int end) {
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
    public static Set<LocalDate> getUSFederalHolidayDatesForYear(int year) {
        ImmutableSet.Builder<LocalDate> holidayDates = ImmutableSet.builder();
        for (USFederalHoliday day : USFederalHoliday.US_FEDERAL_HOLIDAYS) {
            HolidayKey of = HolidayKey.of(day, year);
            LocalDate unchecked = holidayCache.getUnchecked(of);
            holidayDates.add(unchecked);
        }
        return holidayDates.build();
    }

    /**
     * Get a Collection of all {@link LocalDate} between start and end
     *
     * @param startDate the start date
     * @param endDate   the end date
     *
     * @return a Collection of {@link LocalDate}
     */
    public static Collection<LocalDate> getDateRangeForYears(LocalDate startDate, LocalDate endDate) {
        return Collections.unmodifiableCollection(new DateRange(startDate, endDate));
    }

    /**
     * Get a Collection of week days between start and end
     *
     * @param startDate the start date
     * @param endDate   the end date
     *
     * @return a Collection of {@link LocalDate}
     */
    public static Collection<LocalDate> getWeekDayRangeForYears(LocalDate startDate, LocalDate endDate) {
        return Collections.unmodifiableCollection(new WeekDateRange(startDate, endDate));
    }

    /**
     * Get a Collection of all {@link LocalDate} between start and end
     *
     * @param startDate the start date
     * @param endDate   the end date
     *
     * @return a Collection of {@link LocalDate}
     */
    public static Collection<LocalDate> getFederalWorkDayRangeForYears(LocalDate startDate, LocalDate endDate) {
        return Collections.unmodifiableCollection(new FederalWorkDayRange(startDate, endDate));
    }

    /**
     * Convert a {@link Date} to a {@link LocalDate} with a specific {@link ZoneId}
     *
     * @param date   the date
     * @param zoneId the zone
     *
     * @return a {@link LocalDate}
     */
    public static LocalDate toLocalDate(Date date, ZoneId zoneId) {
        return date.toInstant().atZone(zoneId).toLocalDate();
    }

    /**
     * Convert a {@link Date} to a {@link LocalDate}
     *
     * @param date the date
     *
     * @return a {@link LocalDate}
     *
     * @implNote self-use: this calls {@link #toLocalDate(Date, ZoneId)} with {@link ZoneId#systemDefault()}
     */
    public static LocalDate toLocalDate(Date date) {
        return toLocalDate(date, ZoneId.systemDefault());
    }

    /**
     * Create a {@link Date} from a {@link LocalDate}
     *
     * @param date the date
     *
     * @return a {@link LocalDate}
     */
    public static Date toDate(LocalDate date) {
        return java.sql.Date.valueOf(date);
    }

    /**
     * Construct a {@link LoadingCache} for caching holiday dates as we come across them.
     *
     * @return a {@link LoadingCache} instance.
     */
    private static LoadingCache<HolidayKey, LocalDate> getCache() {
        return CacheBuilder.newBuilder()
                           .maximumSize(1000)
                           .expireAfterWrite(Duration.of(10, ChronoUnit.MINUTES))
                           .build(new CacheLoader<HolidayKey, LocalDate>() {
                               @Override
                               public LocalDate load(HolidayKey key) throws Exception {
                                   return key.holiday.getObservanceFor(key.year);
                               }
                           });
    }

    /**
     * Helper method to calculate the number of days between 2 dates. Shamelessly arbitraged from:
     * {@link https://stackoverflow.com/questions/4600034/calculate-number-of-weekdays-between-two-dates-in-java/44942039#44942039}
     *
     * @param start the start date
     * @param end   the end date
     *
     * @return the number of days between start and end excluding weekends
     */
    private static long calculateNumberOfDaysBetweenMinusWeekends(LocalDate start, LocalDate end) {

        final DayOfWeek startW = start.getDayOfWeek();
        final DayOfWeek endW = end.getDayOfWeek();

        final long days = ChronoUnit.DAYS.between(start, end);
        final long daysWithoutWeekends = days - 2 * ((days + startW.getValue()) / 7);

        //adjust for starting and ending on a Sunday:
        return daysWithoutWeekends + (startW == DayOfWeek.SUNDAY ? 1 : 0) + (endW == DayOfWeek.SUNDAY ? 1 : 0);
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
