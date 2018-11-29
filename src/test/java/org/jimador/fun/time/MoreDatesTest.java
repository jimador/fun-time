package org.jimador.fun.time;

import org.junit.Assert;
import org.junit.Test;

import java.time.LocalDate;

import static org.hamcrest.CoreMatchers.is;

public class MoreDatesTest {

    @Test
    public void totalDaysBetween() {
        final LocalDate start = LocalDate.of(2011, 11, 1);
        final LocalDate end = LocalDate.of(2011, 11, 30);
        Assert.assertThat(MoreDates.totalDaysBetween(start, end), is(29));
    }

    @Test
    public void totalBusinessDaysBetween() {
        LocalDate start = LocalDate.of(2018, 1, 1);
        LocalDate end = LocalDate.of(2018, 12, 31);

        Assert.assertThat(MoreDates.totalBusinessDaysBetween(start, end), is(251));
    }

    @Test
    public void federalWorkDayRangeTest() {
        LocalDate start = LocalDate.of(2018, 1, 1);
        LocalDate end = LocalDate.of(2018, 12, 31);

        Assert.assertThat(MoreDates.getFederalWorkDayRangeForYears(start, end).size(), is(251));
    }

    @Test
    public void workDayRangeTest() {
        LocalDate start = LocalDate.of(2018, 1, 1);
        LocalDate end = LocalDate.of(2018, 12, 31);

        Assert.assertThat(MoreDates.getWeekDayRangeForYears(start, end).size(), is(261));
    }

    @Test
    public void dayRangeTest() {
        LocalDate start = LocalDate.of(2020, 1, 1);
        LocalDate end = LocalDate.of(2020, 12, 31);

        Assert.assertThat(MoreDates.getDateRangeForYears(start, end).size(), is(366));
    }
}