package com.johannesbrodwall.projectweek.issues;

import java.time.Instant;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@RequiredArgsConstructor
@ToString
@EqualsAndHashCode
public class Worklog {

    @Getter
    private final Instant workStarted;

    @Getter
    private final int secondsWorked;

    @Getter
    private final String author;

    public double getHours() {
        return secondsWorked/3600.0;
    }


}
