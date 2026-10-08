package com.deskflow.resources.application.domain.model;

import com.deskflow.resources.application.domain.model.vo.Location;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class LocationTest {

    @Test
    void creates_location() {
        Location l = new Location("Building A", 2, "101");
        assertThat(l.getBuilding()).isEqualTo("Building A");
        assertThat(l.getFloor()).isEqualTo(2);
        assertThat(l.getNumber()).isEqualTo("101");
    }

    @Test
    void throws_when_building_blank() {
        assertThatThrownBy(() -> new Location("", 1, "1"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void throws_when_number_blank() {
        assertThatThrownBy(() -> new Location("Building A", 1, ""))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void value_object_equality() {
        Location l1 = new Location("A", 1, "101");
        Location l2 = new Location("A", 1, "101");
        assertThat(l1).isEqualTo(l2);
        assertThat(l1.hashCode()).isEqualTo(l2.hashCode());
    }

    @Test
    void not_equal_when_different_fields() {
        assertThat(new Location("A", 1, "101")).isNotEqualTo(new Location("B", 1, "101"));
        assertThat(new Location("A", 1, "101")).isNotEqualTo(new Location("A", 2, "101"));
        assertThat(new Location("A", 1, "101")).isNotEqualTo(new Location("A", 1, "102"));
    }
}
