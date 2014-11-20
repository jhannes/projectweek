package com.johannesbrodwall.infrastructure.queue;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import lombok.SneakyThrows;

import com.johannesbrodwall.infrastructure.db.Database;

public class Worker {

    private final Executor executor;

    private Database database;
    private WorkItemRepository repository;

    public Worker() {
        executor = Executors.newFixedThreadPool(2);

        executor.execute(this::runWorker);
    }

    @SneakyThrows(InterruptedException.class)
    public void runWorker() {
        while (true) {
            WorkItem workItem = database.executeInTransaction(() -> repository.startWorking());
            if (workItem == null) {
                Thread.sleep(5000); // TODO: 10 times the last "startWorking" call
                continue;
            }

            try {
                database.executeInTransaction(() -> {
                    executeJob(workItem);
                    repository.complete(workItem);
                });
            } catch (Exception e) {
                repository.retryOrAbort(workItem, e);
            }
        }
    }


    private void executeJob(WorkItem workItem) {
        // TODO Auto-generated method stub

    }

    public void start() {

    }


}
