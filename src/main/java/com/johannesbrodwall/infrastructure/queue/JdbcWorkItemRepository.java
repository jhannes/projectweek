package com.johannesbrodwall.infrastructure.queue;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Collection;
import java.util.List;

import lombok.SneakyThrows;

import com.johannesbrodwall.infrastructure.db.Database;
import com.johannesbrodwall.projectweek.ProjectweekDatabase;

public class JdbcWorkItemRepository implements WorkItemRepository {

    private Database database = new ProjectweekDatabase();

    private String tableName = "Work_Items";

    @Override
    public Collection<WorkItem> findAll() {
        // TODO generate statistics for datetime interval with median and 90% times
        //   for max(created,start_after) -> completed and started -> completed
        return database.queryForList("select * from " + tableName, this::mapToEntity);
    }

    @Override
    public WorkItem fetch(int id) {
        return database.queryForSingle("select * from work_items where id = ?",
                this::mapToEntity, id);
    }

    @Override
    public void insert(WorkItem entity) {
        // TODO create a performance test that inserts 1 million rows with status COMPLETE/RETRY/FAILED
        //     and a few hundred with QUEUED and verify performance
        // TODO correlation id
        database.executeOperation(
                "INSERT INTO " + tableName +
                " (created_at, created_on_host, started_at, started_on_host, completed_at, completed_on_host, start_after, status) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                entity.getCreatedAt(), entity.getCreatedOnHost(),
                entity.getStartedAt(), entity.getStartedOnHost(),
                entity.getCompletedAt(), entity.getCompletedOnHost(),
                entity.getStartAfter(), entity.getStatus().getCode());
        entity.setId(database.queryForPrimaryInt("SELECT last_value FROM "+ tableName + "_Id_Seq"));
    }

    @Override
    public List<WorkItem> startWorking(int maxItems) {
        // TODO order by priority, weight, filter by priority and weight
        // TODO when PostgreSQL supports it, use "FOR UPDATE SKIP LOCKED"
        List<WorkItem> workItems = database.queryForList(
                "select * from " + tableName
                + " where started_at is null and completed_at is null and status = ? and start_after <= ?"
                + " for update limit ?",
                this::mapToEntity, WorkItemStatus.QUEUED.getCode(), Instant.now(), maxItems);

        if (workItems.isEmpty()) {
            workItems = database.queryForList(
                    "select * from " + tableName
                    + " where started_at < ? and started_on_host = ? and completed_at is null and status = ? and start_after <= ?"
                    + " for update limit ?",
                    this::mapToEntity,
                    Instant.now().minusSeconds(3600), getLocalHostAddress(), WorkItemStatus.STARTED.getCode(), Instant.now(), maxItems);
        }

        for (WorkItem workItem : workItems) {
            updateStarted(workItem);
        }
        return workItems;
    }

    @Override
    public WorkItem startWorking() {
        List<WorkItem> working = startWorking(1);
        return !working.isEmpty() ? working.get(0) : null;
    }

    private WorkItem updateStarted(WorkItem workItem) {
        if (workItem == null) return null;

        workItem.setStartedAt(Instant.now());
        workItem.setStartedOnHost(getLocalHostAddress());
        workItem.setStatus(WorkItemStatus.STARTED);

        database.executeOperation(
                "update " + tableName + " set started_at = ?, started_on_host = ?, status = ? where id = ?",
                workItem.getStartedAt(), workItem.getStartedOnHost(), workItem.getStatus().getCode(), workItem.getId());

        return workItem;
    }

    @Override
    public void complete(WorkItem workItem) {
        workItem.setCompletedAt(Instant.now());
        workItem.setCompletedOnHost(getLocalHostAddress());
        workItem.setStatus(WorkItemStatus.COMPLETED);

        int updateCount = database.executeOperation(
                "update work_items set completed_at = ?, completed_on_host = ?, status = ? "
                + "where id = ? and started_at = ? and started_on_host = ? and status = ?",
                workItem.getCompletedAt(), workItem.getCompletedOnHost(), workItem.getStatus().getCode(),
                workItem.getId(), workItem.getStartedAt(), workItem.getStartedOnHost(), WorkItemStatus.STARTED.getCode());
        if (updateCount == 0) {
            throw new OptimisticLockException();
        }
    }

    private WorkItem mapToEntity(ResultSet rs) throws SQLException {
        WorkItem workItem = new WorkItem(database.getInstant(rs, "created_at"), rs.getString("created_on_host"), rs.getInt("status"));
        workItem.setStartAfter(database.getInstant(rs, "start_after"));
        workItem.setStartedAt(database.getInstant(rs, "started_at"));
        workItem.setStartedOnHost(rs.getString("started_on_host"));
        workItem.setCompletedAt(database.getInstant(rs, "completed_at"));
        workItem.setCompletedOnHost(rs.getString("completed_on_host"));
        workItem.setId(rs.getInt("id"));
        return workItem;
    }

    @SneakyThrows(UnknownHostException.class)
    private String getLocalHostAddress()  {
        return InetAddress.getLocalHost().getHostAddress();
    }

    @Override
    public void deleteAll() {
        database.executeOperation("delete from " + tableName);
    }

    @Override
    public void retryOrAbort(WorkItem workItem, Exception e) {
        //workItem.setException(e);
        workItem.setStatus(WorkItemStatus.FAILED);

        if (shouldRetry(e) && workItem.hasMoreRetries()) {
            WorkItem retryItem = cloneWorkItem(workItem);
//            retryItem.setRetryNumber(workItem.getRetryNumber() + 1);
//            retryItem.setRetryOf(workItem.getId());
            workItem.setStatus(WorkItemStatus.RETRIED);
            insert(retryItem);
        }

        // TODO save original work item
    }

    private WorkItem cloneWorkItem(WorkItem workItem) {
        // TODO Auto-generated method stub
        return null;
    }

    private boolean shouldRetry(Exception e) {
        // TODO Auto-generated method stub
        return false;
    }

}
