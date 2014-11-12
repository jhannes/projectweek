package com.johannesbrodwall.infrastructure.queue;

import java.util.Collection;
import java.util.List;

public interface WorkItemRepository {
    void deleteAll();
    WorkItem fetch(int id);
    Collection<WorkItem> findAll();
    void insert(WorkItem entity);
    WorkItem startWorking();
    void complete(WorkItem workItem);
    List<WorkItem> startWorking(int maxItems);
}
