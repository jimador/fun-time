package org.jimador.fun.time;

import org.junit.Test;

import java.time.LocalDate;
import java.time.Month;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class USFederalHolidaysTest {

    @Test
    public void testDateOf_fixed() {
        final LocalDate observance = USFederalHoliday.NEW_YEARS_DAY.getObservanceFor(2011);
        final LocalDate expected = LocalDate.of(2010, 12, 31);
        assertThat(observance, is(expected));
    }

    @Test
    public void testDateOf_varying() {
        final LocalDate observance = USFederalHoliday.BIRTHDAY_OF_MARTIN_LUTHER_KING_JR.getObservanceFor(2011);
        final LocalDate expected = LocalDate.of(2011, 1, 17);
        assertThat(observance, is(expected));
    }

    @Test
    public void testDateOf_varyingMemorialDay() {
        final LocalDate observance = USFederalHoliday.MEMORIAL_DAY.getObservanceFor(2011);
        final LocalDate expected = LocalDate.of(2011, Month.MAY, 30);
        assertThat(observance, is(expected));
    }
}
