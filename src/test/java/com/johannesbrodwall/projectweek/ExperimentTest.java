package com.johannesbrodwall.projectweek;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class ExperimentTest {

    @Test
    public void shouldParseDate() {
        String dateString = "2014-09-05T10:21:19.012+0200";
        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSZ");
        ZonedDateTime date = ZonedDateTime.parse(dateString, format);
        assertThat(date)
            .isEqualTo(ZonedDateTime.of(2014, 9, 5, 10, 21, 19, 12000000, ZoneOffset.ofHours(2)));


    }

}
