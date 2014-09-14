package com.johannesbrodwall.projectweek.issues;

import java.time.Instant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@RequiredArgsConstructor
@ToString
public class IssueStatus {

    @Getter
    private final Instant createdAt;

    @Getter
    private final String status;
}
