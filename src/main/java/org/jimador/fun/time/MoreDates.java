package org.jimador.fun.time;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ForwardingSet;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import java.io.Serializable;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

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
        final Set<LocalDate> weekDaysForYearRange = new WeekDayRange(start, end);
        final Set<LocalDate> holidayForYearRange = getUSFederalHolidayForYearRange(start.getYear(), end.getYear());

        return Sets.difference(weekDaysForYearRange, holidayForYearRange).size();
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
     * Class representing a range of week days (Monday - Friday) between 2 dates.
     */
    private static class WeekDayRange extends ForwardingSet<LocalDate> {
        private final LocalDate startDate;
        private final LocalDate endDate;

        public WeekDayRange(LocalDate startDate, LocalDate endDate) {
            Objects.requireNonNull(startDate, "Start date must not be null!");
            Objects.requireNonNull(endDate, "End date must not be null");
            this.startDate = startDate;
            this.endDate = endDate;
        }

        @Override
        protected Set<LocalDate> delegate() {
            return Stream.iterate(startDate, d -> d.plusDays(1))
                         .limit(ChronoUnit.DAYS.between(startDate, endDate) + 1)
                         .filter(this::notWeekend)
                         .collect(ImmutableSet.toImmutableSet())
                    ;
        }

        private boolean notWeekend(LocalDate localDate) {
            return localDate.getDayOfWeek() != DayOfWeek.SATURDAY && localDate.getDayOfWeek() != DayOfWeek.SUNDAY;
        }

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
