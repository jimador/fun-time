package org.jimador.fun.time;

import com.google.common.base.Preconditions;
import com.google.common.primitives.Ints;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.AbstractCollection;
import java.util.Iterator;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Class representing a Lazy Range of {@link LocalDate}.
 *
 * @author James Dunnam
 */
class DateRange extends AbstractCollection<LocalDate> {
    protected final LocalDate startDate;
    protected final LocalDate endDate;

    DateRange(LocalDate startDate, LocalDate endDate) {
        Objects.requireNonNull(startDate, "Start date must not be null");
        Objects.requireNonNull(endDate, "End date must not be null");
        Preconditions.checkArgument(startDate.isBefore(endDate), "Start date must be before end date");
        this.startDate = startDate;
        this.endDate = endDate;
    }

    @Override
    public Iterator<LocalDate> iterator() {
        return stream().iterator();
    }

    @Override
    public int size() {
        return Ints.saturatedCast(stream().count());
    }

    @Override
    public boolean contains(@Nullable Object object) {
        return stream().anyMatch(o -> o.equals(object));
    }

    @Override
    public Stream<LocalDate> stream() {
        return Stream.iterate(startDate, d -> d.plusDays(1))
                     .limit(ChronoUnit.DAYS.between(startDate, endDate) + 1);
    }
}
