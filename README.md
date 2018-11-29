# fun with java.time
### Convenience code for handling date counting and including counting business days and US federal holidays.
#### Sample
```java
    @Test
    public void totalBuisnessDaysBetween() {
        LocalDate start = LocalDate.of(2018, 1, 1);
        LocalDate end = LocalDate.of(2018, 12, 31);

        Assert.assertThat(MoreDates.totalBusinessDaysBetween(start, end), is(251));
    }
```

#### TODO:
 * Add more tests
 * Error handling when startDate > endDate, nulls, etc...
 * add more docs
 * add JMH benchmarks
