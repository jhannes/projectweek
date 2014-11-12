package com.johannesbrodwall.infrastructure.queue;

public enum WorkItemStatus {

    QUEUED(0), STARTED(1), COMPLETED(2);

    private int value;

    private WorkItemStatus(int value) {
        this.value = value;
    }

    public static WorkItemStatus forCode(int statusCode) {
        for (WorkItemStatus status : values()) {
            if (status.getCode() == statusCode) return status;
        }
        throw new IllegalArgumentException("Unknown " + WorkItemStatus.class + " code " + statusCode);
    }

    public int getCode() {
        return value;
    }

}
