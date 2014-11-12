package com.johannesbrodwall.infrastructure.queue;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import lombok.SneakyThrows;

import org.junit.Before;
import org.junit.Test;

import com.johannesbrodwall.infrastructure.db.Database;
import com.johannesbrodwall.infrastructure.db.TestDatabase;

public class QueueTest {
    private Database database = TestDatabase.instance();
    private WorkItemRepository repository = new JdbcWorkItemRepository();

    @Before
    public void deleteAll() {
        database.executeInTransaction(() -> repository.deleteAll());
    }

    @Test
    public void shouldInsertJob() {
        WorkItem workItem = sampleWorkItem();
        database.executeInTransaction(() -> repository.insert(workItem));
        assertThat(database.executeInTransaction(() -> repository.findAll())).contains(workItem);

        assertThat(workItem.getCreatedOnHost()).isEqualTo(getLocalHostAddress());
        assertThat(workItem.getStatus()).isEqualTo(WorkItemStatus.QUEUED);
        assertThat(database.executeInTransaction(() -> repository.fetch(workItem.getId())))
            .isEqualToComparingFieldByField(workItem);
    }

    @Test
    public void shouldTakeOneJob() {
        WorkItem workItem = sampleWorkItem();

        database.executeInTransaction(() -> repository.insert(workItem));
        WorkItem taken = database.executeInTransaction(() -> repository.startWorking());
        assertThat(taken).isEqualTo(workItem);

        assertThat(database.executeInTransaction(() -> repository.startWorking()))
            .isNull();

        WorkItem startedWorkItem = database.executeInTransaction(() -> repository.fetch(workItem.getId()));
        assertThat(startedWorkItem.getStartedAt()).isNotNull();
        assertThat(startedWorkItem.getStartedOnHost()).isEqualTo(getLocalHostAddress());
        assertThat(startedWorkItem.getStatus()).isEqualTo(WorkItemStatus.STARTED);
    }

    @Test
    public void shouldPickupSeveralJobs() {
        List<WorkItem> workItems = new ArrayList<>();
        workItems.add(sampleWorkItem());
        workItems.add(sampleWorkItem());
        workItems.add(sampleWorkItem());
        workItems.add(sampleWorkItem());


        database.executeInTransaction(() -> {
            for (WorkItem workItem : workItems) {
                repository.insert(workItem);
            }
        });

        List<WorkItem> taken = database.executeInTransaction(() -> repository.startWorking(3));
        assertThat(taken).hasSize(3);

        for (WorkItem workItem : taken) {
            assertThat(workItem).isIn(workItems);
        }

        assertThat(database.executeInTransaction(() -> repository.startWorking(3))).hasSize(1);
        assertThat(database.executeInTransaction(() -> repository.startWorking(3))).isEmpty();
    }


    @Test
    public void shouldNotPickupFutureJob() {
        WorkItem workItem = sampleWorkItem();
        workItem.setStartAfter(Instant.now().plusSeconds(10));
        database.executeInTransaction(() -> repository.insert(workItem));

        assertThat(database.executeInTransaction(() -> repository.startWorking()))
                .isNull();
    }

    @Test
    public void shouldPickupOldJob() throws UnknownHostException {
        WorkItem workItem = startedWorkItem(Instant.now().minusSeconds(3601));
        database.executeInTransaction(() -> repository.insert(workItem));

        assertThat(database.executeInTransaction(() -> repository.startWorking()))
                .isEqualTo(workItem);
    }

    @Test
    public void shouldNotPickupNewJob() throws UnknownHostException {
        WorkItem workItem = startedWorkItem(Instant.now().minusSeconds(60));
        database.executeInTransaction(() -> repository.insert(workItem));

        assertThat(database.executeInTransaction(() -> repository.startWorking()))
                .isNull();
    }

    @Test
    public void shouldNotPickupOtherHostsJob() {
        WorkItem workItem = startedWorkItem(Instant.now().minusSeconds(3601));
        workItem.setStartedOnHost("8.8.8.8");
        database.executeInTransaction(() -> repository.insert(workItem));

        assertThat(database.executeInTransaction(() -> repository.startWorking()))
                .isNull();
    }

    @Test
    public void shouldCompleteJob() {
        WorkItem workItem = sampleWorkItem();

        database.executeInTransaction(() -> repository.insert(workItem));
        WorkItem taken = database.executeInTransaction(() -> repository.startWorking());

        database.executeInTransaction(() -> repository.complete(taken));

        WorkItem completed = database.executeInTransaction(() -> repository.fetch(workItem.getId()));
        assertThat(completed.getCompletedAt()).isNotNull();
        assertThat(completed.getCompletedOnHost()).isEqualTo(getLocalHostAddress());
    }

    @Test
    public void shouldNotPickupCompletedJobs() {
        WorkItem workItem = startedWorkItem(Instant.now().minusSeconds(13601));
        workItem.setCompletedAt(Instant.now());
        workItem.setCompletedOnHost(getLocalHostAddress());
        workItem.setStatus(WorkItemStatus.COMPLETED);
        database.executeInTransaction(() -> repository.insert(workItem));

        assertThat(database.executeInTransaction(() -> repository.startWorking()))
            .isNull();
    }

    @Test
    public void shouldDetectDuplicateCompletion() {
        WorkItem workItem = startedWorkItem(Instant.now().minusSeconds(13601));
        database.executeInTransaction(() -> repository.insert(workItem));
        WorkItem taken = database.executeInTransaction(() -> repository.startWorking());

        database.executeInTransaction(() -> repository.complete(taken));

        try {
            database.executeInTransaction(() -> repository.complete(taken));
            fail("Expected duplicate completion exception");
        } catch (OptimisticLockException expected) {
        }

    }


    @SneakyThrows
    private String getLocalHostAddress() {
        return InetAddress.getLocalHost().getHostAddress();
    }

    private WorkItem sampleWorkItem() {
        return new WorkItem();
    }

    private WorkItem startedWorkItem(Instant startedAt) {
        WorkItem workItem = sampleWorkItem();
        workItem.setStartedAt(startedAt);
        workItem.setStartedOnHost(getLocalHostAddress());
        workItem.setStatus(WorkItemStatus.STARTED);
        return workItem;
    }

    private WorkItem completedWorkItem(Instant completedAt) {
        WorkItem workItem = startedWorkItem(completedAt.minusSeconds(13601));
        workItem.setCompletedAt(completedAt);
        workItem.setCompletedOnHost(getLocalHostAddress());
        workItem.setStatus(WorkItemStatus.COMPLETED);
        return workItem;
    }
}
