package org.jimador.fun.time;

import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Class representing a lazy {@link java.util.Collection} of Work Day (Monday - Friday minus US Federal Holidays)
 *
 * @author James Dunnam
 */
final class FederalWorkDayRange extends WeekDateRange {
    private final Set<LocalDate> holidays;

    FederalWorkDayRange(LocalDate startDate, LocalDate endDate) {
        super(startDate, endDate);
        this.holidays = MoreDates.getUSFederalHolidayForYearRange(startDate.getYear(), endDate.getYear());
    }

    @Override
    public Stream<LocalDate> stream() {
        return super.stream().filter(localDate -> !holidays.contains(localDate));
    }
}
