package com.deskflow.allocation.application.domain.model;

import com.deskflow.allocation.application.domain.model.vo.DateRange;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

class DateRangeTest {

    private static final LocalDate MON = LocalDate.of(2025, 6, 2);
    private static final LocalDate WED = LocalDate.of(2025, 6, 4);
    private static final LocalDate FRI = LocalDate.of(2025, 6, 6);
    private static final LocalDate MON2 = LocalDate.of(2025, 6, 9);

    @Test
    void creates_date_range_when_from_before_to() {
        DateRange r = new DateRange(MON, FRI);
        assertThat(r.getDateFrom()).isEqualTo(MON);
        assertThat(r.getDateTo()).isEqualTo(FRI);
    }

    @Test
    void throws_when_date_from_equals_date_to() {
        assertThatThrownBy(() -> new DateRange(MON, MON))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("dateFrom must be before dateTo");
    }

    @Test
    void throws_when_date_from_after_date_to() {
        assertThatThrownBy(() -> new DateRange(FRI, MON))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void throws_when_date_from_null() {
        assertThatThrownBy(() -> new DateRange(null, FRI))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void value_object_equality() {
        DateRange r1 = new DateRange(MON, FRI);
        DateRange r2 = new DateRange(MON, FRI);
        assertThat(r1).isEqualTo(r2);
        assertThat(r1.hashCode()).isEqualTo(r2.hashCode());
    }

    @Test
    void not_equal_when_different_dates() {
        DateRange r1 = new DateRange(MON, FRI);
        DateRange r2 = new DateRange(WED, MON2);
        assertThat(r1).isNotEqualTo(r2);
    }
}
