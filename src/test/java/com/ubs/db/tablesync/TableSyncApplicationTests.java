package com.ubs.db.tablesync;

import com.ubs.db.tablesync.model.TaskDefinition;
import com.ubs.db.tablesync.persistence.BaseTaskDefinitionRepository;
import com.ubs.db.tablesync.persistence.MainTaskDefinitionRepository;
import com.ubs.db.tablesync.persistence.MirrorTaskDefinitionRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.Callable;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.given;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TableSyncApplicationTests {

    @Autowired
    private JdbcTemplate template;

    @Autowired
    private MainTaskDefinitionRepository mainRepo;

    @Autowired
    private MirrorTaskDefinitionRepository mirrorRepo;

    @Value("${tableSync.mainTableName}")
    private String mainTableName;

    @Value("${tableSync.mirrorTableName}")
    private String mirrorTableName;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        template.execute("TRUNCATE TABLE " + mainTableName);
        template.execute("TRUNCATE TABLE " + mirrorTableName);
    }

    @After
    public void tearDown() {
        template.execute("TRUNCATE TABLE " + mainTableName);
        template.execute("TRUNCATE TABLE " + mirrorTableName);
    }

    @Test
    public void addedTaskInMainEventuallySyncedInMirror() {
        TaskDefinition newTask = new TaskDefinition("New", "Task");
        mainRepo.insert(newTask);

        given()
                .ignoreExceptionsMatching(e -> e.getMessage().startsWith("Incorrect result size"))
                .await()
                .atMost(60, SECONDS)
                .until(syncedIn(newTask, mirrorRepo));
    }

    @Test
    public void addedTaskInMirrorEventuallySyncedInMain() {
        TaskDefinition newTask = new TaskDefinition("New", "Task");
        mirrorRepo.insert(newTask);

        given()
                .ignoreExceptionsMatching(e -> e.getMessage().startsWith("Incorrect result size"))
                .await()
                .atMost(60, SECONDS)
                .until(syncedIn(newTask, mainRepo));
    }

    @Test
    public void updatedTaskInMainEventuallySyncedInMirror() {
        TaskDefinition newTask = new TaskDefinition("New", "Task");
        mainRepo.insert(newTask);

        given()
                .ignoreExceptionsMatching(e -> e.getMessage().startsWith("Incorrect result size"))
                .await()
                .atMost(60, SECONDS)
                .until(syncedIn(newTask, mirrorRepo));

        TaskDefinition storedTask = mainRepo.findById(mainRepo.findAllIds().get(0));
        storedTask.setName("Changed name");
        mainRepo.update(storedTask);

        given()
                .ignoreExceptionsMatching(e -> e.getMessage().startsWith("Incorrect result size"))
                .await()
                .atMost(60, SECONDS)
                .until(syncedIn(storedTask, mirrorRepo));
    }

    @Ignore("Investigate why Awaitility gets stuck during this test")
    @Test
    public void updatedTaskInMirrorEventuallySyncedInMain() {
        TaskDefinition newTask = new TaskDefinition("New", "Task");
        mirrorRepo.insert(newTask);

        given()
                .ignoreExceptionsMatching(e -> e.getMessage().startsWith("Incorrect result size"))
                .await()
                .atMost(60, SECONDS)
                .until(syncedIn(newTask, mainRepo));

        TaskDefinition storedTask = mirrorRepo.findById(mirrorRepo.findAllIds().get(0));
        storedTask.setName("Changed name");
        mirrorRepo.update(storedTask);

        given()
                .ignoreExceptionsMatching(e -> e.getMessage().startsWith("Incorrect result size"))
                .await()
                .atMost(60, SECONDS)
                .until(syncedIn(storedTask, mainRepo));
    }

    @Test
    public void removedTaskInMainEventuallySyncedInMirror() {
        TaskDefinition newTask = new TaskDefinition("New", "Task");
        mainRepo.insert(newTask);

        given()
                .ignoreExceptionsMatching(e -> e.getMessage().startsWith("Incorrect result size"))
                .await()
                .atMost(60, SECONDS)
                .until(syncedIn(newTask, mirrorRepo));

        mainRepo.deleteById(mainRepo.findAllIds().get(0));

        given()
                .ignoreExceptionsMatching(e -> e.getMessage().startsWith("Incorrect result size"))
                .await()
                .atMost(60, SECONDS)
                .until(removedIn(mirrorRepo));
    }

    @Test
    public void removedTaskInMirrorEventuallySyncedInMain() {
        TaskDefinition newTask = new TaskDefinition("New", "Task");
        mirrorRepo.insert(newTask);

        given()
                .ignoreExceptionsMatching(e -> e.getMessage().startsWith("Incorrect result size"))
                .await()
                .atMost(60, SECONDS)
                .until(syncedIn(newTask, mainRepo));

        mirrorRepo.deleteById(mirrorRepo.findAllIds().get(0));

        given()
                .ignoreExceptionsMatching(e -> e.getMessage().startsWith("Incorrect result size"))
                .await()
                .atMost(60, SECONDS)
                .until(removedIn(mainRepo));
    }

    private Callable<Boolean> syncedIn(TaskDefinition task, BaseTaskDefinitionRepository repo) {
        return () -> isLooseEqual(task, repo.findByNameAndDescription(task));
    }

    private boolean isLooseEqual(TaskDefinition task, TaskDefinition syncedTask) {
        return task.getName().equals(syncedTask.getName())
                && task.getDescription().equals(syncedTask.getDescription());
    }

    private Callable<Boolean> removedIn(BaseTaskDefinitionRepository repo) {
        return () -> repo.findAllIds().size() == 0;
    }
}
