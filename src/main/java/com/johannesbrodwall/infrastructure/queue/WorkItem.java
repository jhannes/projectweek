package com.johannesbrodwall.infrastructure.queue;

import java.net.InetAddress;
import java.time.Instant;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.ToString;

@EqualsAndHashCode(of = "id")
@ToString
public class WorkItem {

    @Getter @Setter
    private Integer id;

    @Getter @NonNull
    private final Instant createdAt;

    @Getter @Setter
    private Instant startAfter;

    @Getter @NonNull
    private final String createdOnHost;

    @Getter @Setter
    private Instant startedAt;

    @Getter @Setter
    private String startedOnHost;

    @Getter @Setter
    private Instant completedAt;

    @Getter @Setter
    private String completedOnHost;

    @Getter @Setter @NonNull
    private WorkItemStatus status;

    @SneakyThrows
    public WorkItem() {
        createdAt = Instant.now();
        createdOnHost = InetAddress.getLocalHost().getHostAddress();
        startAfter = Instant.now();
        status = WorkItemStatus.QUEUED;
    }

    public WorkItem(Instant createdDate, String createdOnHost, int status) {
        this.createdAt = createdDate;
        this.createdOnHost = createdOnHost;
        this.status = WorkItemStatus.forCode(status);
    }

}
