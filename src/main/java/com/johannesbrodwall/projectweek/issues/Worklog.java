package com.johannesbrodwall.projectweek.issues;

import java.time.Instant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
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
