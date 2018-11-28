package org.jimador.fun.time;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;

/**
 * The list of Federal Observances, as per section 6103(a) of title 5 of the United States Code.
 *
 * @see http://www.law.cornell.edu/uscode/text/5/6103
 */
public enum USFederalHoliday implements Holiday {
    NEW_YEARS_DAY(FixedObservance.of(Month.JANUARY, 1)),
    BIRTHDAY_OF_MARTIN_LUTHER_KING_JR(VaryingObservance.of(3, DayOfWeek.MONDAY, Month.JANUARY)),
    WASHINGTONS_BIRTHDAY(VaryingObservance.of(3, DayOfWeek.MONDAY, Month.FEBRUARY)),
    MEMORIAL_DAY(VaryingObservance.of(-1, DayOfWeek.MONDAY, Month.MAY)),
    INDEPENDENCE_DAY(FixedObservance.of(Month.JULY, 4)),
    LABOR_DAY(VaryingObservance.of(1, DayOfWeek.MONDAY, Month.SEPTEMBER)),
    COLUMBUS_DAY(VaryingObservance.of(2, DayOfWeek.MONDAY, Month.OCTOBER)),
    VETERANS_DAY(FixedObservance.of(Month.NOVEMBER, 11)),
    THANKSGIVING_DAY(VaryingObservance.of(4, DayOfWeek.THURSDAY, Month.NOVEMBER)),
    CHRISTMAS_DAY(FixedObservance.of(Month.DECEMBER, 25));

    private final Holiday delegate;
    static final USFederalHoliday[] US_FEDERAL_HOLIDAYS = USFederalHoliday.values();

    USFederalHoliday(Holiday delegate) {
        this.delegate = delegate;
    }

    @Override
    public LocalDate getObservanceFor(int year) {
        return delegate.getObservanceFor(year);
    }

    /**
     * Class representing a varying observance of a US federal holiday. e.g. Thanksgiving is celebrated on the fourth Thursday of November.
     *
     * @implNote A negative {@code ordinal} value represents the last week of the month. e.g. "The last Monday in May"
     */
    private static class VaryingObservance extends BaseHoliday {

        private final DayOfWeek dayOfWeek;
        private final int ordinal;

        private VaryingObservance(Month month, DayOfWeek dayOfWeek, int ordinal) {
            super(month);
            this.dayOfWeek = dayOfWeek;
            this.ordinal = ordinal;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public LocalDate getActualHolidayDate(int year) {
            return LocalDate.of(year, month, 1)
                            .with(TemporalAdjusters.dayOfWeekInMonth(ordinal, dayOfWeek));
        }

        static VaryingObservance of(int ordinal, DayOfWeek dayOfWeek, Month month) {
            return new VaryingObservance(month, dayOfWeek, ordinal);
        }

    }

    /**
     * Class representing a fixed observance of a US federal holiday. e.g New Years Day is always on January 1
     */
    private static class FixedObservance extends BaseHoliday {
        private final int dayOfMonth;

        private FixedObservance(Month month, int dayOfMonth) {
            super(month);
            this.dayOfMonth = dayOfMonth;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public LocalDate getActualHolidayDate(int year) {
            return LocalDate.of(year, month, dayOfMonth);
        }

        static FixedObservance of(Month month, int dayOfMonth) {
            return new FixedObservance(month, dayOfMonth);
        }
    }

    /**
     * Base class for the Federal Holidays. Subclasses should implement {@link #getActualHolidayDate(int)} and this class will adjust for weekends if
     * necessary.
     */
    private static abstract class BaseHoliday implements Holiday {
        final Month month;

        BaseHoliday(Month month) {
            this.month = month;
        }

        /**
         * Get the actual, unadjusted, day of the holiday
         *
         * @param year the year to query for
         *
         * @return the actual day the holiday is celebrated on.
         */
        abstract LocalDate getActualHolidayDate(int year);

        /**
         * {@inheritDoc}
         */
        @Override
        public LocalDate getObservanceFor(int year) {
            final LocalDate actualHolidayDate = getActualHolidayDate(year);
            return adjustForWeekendIfNecessary(actualHolidayDate);
        }

        /**
         * Helper method to adjust the observance day for a holiday that falls on a {@link DayOfWeek#SATURDAY} or {@link DayOfWeek#SUNDAY}.
         * See Executive order 11582, February 11, 1971.
         *
         * @param holiday the actual {@link LocalDate} that the holiday falls on.
         *
         * @return a possibly adjusted {@link LocalDate}
         */
        private LocalDate adjustForWeekendIfNecessary(LocalDate holiday) {
            switch (holiday.getDayOfWeek()) {
                case SATURDAY:
                    return holiday.minus(1, ChronoUnit.DAYS);
                case SUNDAY:
                    return holiday.plus(1, ChronoUnit.DAYS);
                default:
                    return holiday;
            }
        }
    }
}

