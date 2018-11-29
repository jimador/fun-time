package org.jimador.fun.time;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.stream.Stream;

/**
 * Class representing a lazy range of Week Days (Monday - Friday)
 *
 * @author James Dunnam
 */
class WeekDateRange extends DateRange {

    WeekDateRange(LocalDate startDate, LocalDate endDate) {
        super(startDate, endDate);
    }

    @Override
    public Stream<LocalDate> stream() {
        return super.stream().filter(this::notWeekend);
    }

    private boolean notWeekend(LocalDate localDate) {
        return localDate.getDayOfWeek() != DayOfWeek.SATURDAY && localDate.getDayOfWeek() != DayOfWeek.SUNDAY;
    }
}
