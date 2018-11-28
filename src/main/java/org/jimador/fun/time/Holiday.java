package org.jimador.fun.time;

import java.time.LocalDate;

/**
 * Local interface that represents the concept of a holiday that can be queried for the {@link LocalDate} it is observed on.
 *
 * @author James Dunnam
 * @see USFederalHoliday
 */
public interface Holiday {
    /**
     * Returns the {@link LocalDate} representing the day on which the holiday is actually observed.
     *
     * @param year the year to query for.
     *
     * @return a {@link LocalDate}
     */
    LocalDate getObservanceFor(int year);
}
