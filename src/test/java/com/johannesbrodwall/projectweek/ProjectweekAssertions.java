package com.johannesbrodwall.projectweek;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;

public class ProjectweekAssertions extends Assertions {

    public static class InstantAssert extends AbstractAssert<InstantAssert, Instant> {

        public InstantAssert(Instant instant) {
            super(instant, InstantAssert.class);
        }

        public InstantAssert isAfter(ZonedDateTime zonedDateTime) {
            assertThat(actual.isAfter(zonedDateTime.toInstant())).as("%s.isAfter(%s)", actual, zonedDateTime)
                .isTrue();
            return this;
        }

        public InstantAssert isAfter(LocalDate localDate) {
            return isAfter(localDate.atStartOfDay());
        }

        public InstantAssert isAfter(LocalDateTime localDateTime) {
            return isAfter(localDateTime.atZone(ZoneId.systemDefault()));
        }

    }

    public static InstantAssert assertThat(Instant instant) {
        return new InstantAssert(instant);
    }


}
