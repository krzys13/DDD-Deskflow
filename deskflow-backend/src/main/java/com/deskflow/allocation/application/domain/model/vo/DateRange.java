package com.deskflow.allocation.application.domain.model.vo;

import java.time.LocalDate;
import java.util.Objects;

public final class DateRange {
    private final LocalDate dateFrom;
    private final LocalDate dateTo;

    public DateRange(LocalDate dateFrom, LocalDate dateTo) {
        Objects.requireNonNull(dateFrom, "dateFrom cannot be null");
        Objects.requireNonNull(dateTo, "dateTo cannot be null");
        if (!dateFrom.isBefore(dateTo)) {
            throw new IllegalArgumentException("dateFrom must be before dateTo");
        }
        this.dateFrom = dateFrom;
        this.dateTo = dateTo;
    }

    public LocalDate getDateFrom() { return dateFrom; }
    public LocalDate getDateTo() { return dateTo; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DateRange that)) return false;
        return dateFrom.equals(that.dateFrom) && dateTo.equals(that.dateTo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dateFrom, dateTo);
    }

    @Override
    public String toString() {
        return dateFrom + " — " + dateTo;
    }
}
