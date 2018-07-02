package com.ubs.db.tablesync.domain;

import com.ubs.db.tablesync.model.TaskDefinition;
import com.ubs.db.tablesync.service.ConflictHandler;
import com.ubs.db.tablesync.service.DataManager;
import com.ubs.db.tablesync.service.HashService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;

import static java.util.Collections.emptyList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assume.assumeTrue;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class TaskDefinitionSynchronizerTest {

    @InjectMocks
    private TaskDefinitionSynchronizer taskDefinitionSynchronizer;

    @Mock
    private HashService<TaskDefinition> hashService;

    @Mock
    private DataManager<Long, TaskDefinition> dataManager;

    @Mock
    private ConflictHandler conflictHandler;

    private TaskDefinition first = new TaskDefinition(1L, "first", "desc");
    private TaskDefinition second = new TaskDefinition(2L, "second", "desc");
    private TaskDefinition third = new TaskDefinition(3L, "third", "desc");

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        first = new TaskDefinition(1L, "first", "desc");
        second = new TaskDefinition(2L, "second", "desc");
        third = new TaskDefinition(3L, "third", "desc");

        when(hashService.hash(eq(first))).thenReturn("first-hash");
        when(hashService.hash(eq(second))).thenReturn("second-hash");
        when(hashService.hash(eq(third))).thenReturn("third-hash");

        when(dataManager.mainRow(eq(1L))).thenReturn(first);
        when(dataManager.mainRow(eq(2L))).thenReturn(second);
        when(dataManager.mainRow(eq(3L))).thenReturn(third);
        when(dataManager.mirrorRow(eq(1L))).thenReturn(first);
        when(dataManager.mirrorRow(eq(2L))).thenReturn(second);
        when(dataManager.mirrorRow(eq(3L))).thenReturn(third);
    }

    @After
    public void tearDown() {
        taskDefinitionSynchronizer = null;
        dataManager = null;
        hashService = null;
        conflictHandler = null;
    }

    @Test
    public void noSyncForEmptyLists() {
        when(dataManager.mainIdList()).thenReturn(emptyList());
        when(dataManager.mirrorIdList()).thenReturn(emptyList());

        taskDefinitionSynchronizer.sync();

        assertEquals(0, taskDefinitionSynchronizer.size());
        verify(dataManager, never()).mainRow(any());
        verify(dataManager, never()).mirrorRow(any());
        verify(dataManager, never()).addMainRow(any());
        verify(dataManager, never()).addMirrorRow(any());
        verify(dataManager, never()).updateMainRow(any());
        verify(dataManager, never()).updateMirrorRow(any());
        verify(dataManager, never()).removeMainRow(any());
        verify(dataManager, never()).removeMirrorRow(any());
    }

    @Test
    public void removeRowAndHashFromMirrorIfRowWasRemovedFromMainAndHashIsNotEmpty() {
        when(dataManager.mainIdList()).thenReturn(emptyList());
        when(dataManager.mirrorIdList()).thenReturn(Arrays.asList(1L, 2L));

        taskDefinitionSynchronizer.hashMap.put(1L, "first-hash");
        taskDefinitionSynchronizer.hashMap.put(2L, "second-hash");

        assumeTrue(taskDefinitionSynchronizer.size() == 2);
        taskDefinitionSynchronizer.sync();

        assertEquals(0, taskDefinitionSynchronizer.size());
        verify(dataManager, never()).mainRow(any());
        verify(dataManager, times(2)).mirrorRow(any());
        verify(dataManager, never()).addMainRow(any());
        verify(dataManager, never()).addMirrorRow(any());
        verify(dataManager, never()).updateMainRow(any());
        verify(dataManager, never()).updateMirrorRow(any());
        verify(dataManager, never()).removeMainRow(any());
        verify(dataManager, times(2)).removeMirrorRow(any());
    }

    @Test
    public void addRowAndHashToMainIfRowWasAddedToMirrorAndHashIsEmpty() {
        when(dataManager.mainIdList()).thenReturn(emptyList());
        when(dataManager.mirrorIdList()).thenReturn(Arrays.asList(1L, 2L));

        assumeTrue(taskDefinitionSynchronizer.size() == 0);
        taskDefinitionSynchronizer.sync();

        assertEquals(2, taskDefinitionSynchronizer.size());
        assertEquals("first-hash", taskDefinitionSynchronizer.hashMap.get(1L));
        assertEquals("second-hash", taskDefinitionSynchronizer.hashMap.get(2L));

        verify(dataManager, never()).mainRow(any());
        verify(dataManager, times(2)).mirrorRow(any());
        verify(dataManager, times(2)).addMainRow(any());
        verify(dataManager, never()).addMirrorRow(any());
        verify(dataManager, never()).updateMainRow(any());
        verify(dataManager, never()).updateMirrorRow(any());
        verify(dataManager, never()).removeMainRow(any());
        verify(dataManager, never()).removeMirrorRow(any());
    }

    @Test
    public void removeRowAndHashFromMainIfRowWasRemovedFromMirrorAndHashIsNotEmpty() {
        when(dataManager.mainIdList()).thenReturn(Arrays.asList(1L, 2L));
        when(dataManager.mirrorIdList()).thenReturn(emptyList());

        taskDefinitionSynchronizer.hashMap.put(1L, "first-hash");
        taskDefinitionSynchronizer.hashMap.put(2L, "second-hash");

        assumeTrue(taskDefinitionSynchronizer.size() == 2);
        taskDefinitionSynchronizer.sync();

        assertEquals(0, taskDefinitionSynchronizer.size());
        verify(dataManager, times(2)).mainRow(any());
        verify(dataManager, never()).mirrorRow(any());
        verify(dataManager, never()).addMainRow(any());
        verify(dataManager, never()).addMirrorRow(any());
        verify(dataManager, never()).updateMainRow(any());
        verify(dataManager, never()).updateMirrorRow(any());
        verify(dataManager, times(2)).removeMainRow(any());
        verify(dataManager, never()).removeMirrorRow(any());
    }

    @Test
    public void addRowAndHashToMirrorIfRowWasAddedToMainAndHashIsEmpty() {
        when(dataManager.mainIdList()).thenReturn(Arrays.asList(1L, 2L));
        when(dataManager.mirrorIdList()).thenReturn(emptyList());

        assumeTrue(taskDefinitionSynchronizer.size() == 0);
        taskDefinitionSynchronizer.sync();

        assertEquals(2, taskDefinitionSynchronizer.size());
        assertEquals("first-hash", taskDefinitionSynchronizer.hashMap.get(1L));
        assertEquals("second-hash", taskDefinitionSynchronizer.hashMap.get(2L));

        verify(dataManager, times(2)).mainRow(any());
        verify(dataManager, never()).mirrorRow(any());
        verify(dataManager, never()).addMainRow(any());
        verify(dataManager, times(2)).addMirrorRow(any());
        verify(dataManager, never()).updateMainRow(any());
        verify(dataManager, never()).updateMirrorRow(any());
        verify(dataManager, never()).removeMainRow(any());
        verify(dataManager, never()).removeMirrorRow(any());
    }

    @Test
    public void doNothingIfNoChanges() {
        when(dataManager.mainIdList()).thenReturn(Arrays.asList(1L, 2L));
        when(dataManager.mirrorIdList()).thenReturn(Arrays.asList(1L, 2L));

        taskDefinitionSynchronizer.hashMap.put(1L, "first-hash");
        taskDefinitionSynchronizer.hashMap.put(2L, "second-hash");

        assumeTrue(taskDefinitionSynchronizer.size() == 2);
        taskDefinitionSynchronizer.sync();

        assertEquals(2, taskDefinitionSynchronizer.size());
        assertEquals("first-hash", taskDefinitionSynchronizer.hashMap.get(1L));
        assertEquals("second-hash", taskDefinitionSynchronizer.hashMap.get(2L));

        verify(dataManager, times(2)).mainRow(any());
        verify(dataManager, times(2)).mirrorRow(any());
        verify(dataManager, never()).addMainRow(any());
        verify(dataManager, never()).addMirrorRow(any());
        verify(dataManager, never()).updateMainRow(any());
        verify(dataManager, never()).updateMirrorRow(any());
        verify(dataManager, never()).removeMainRow(any());
        verify(dataManager, never()).removeMirrorRow(any());
    }

    @Test
    public void updateFirstRowInMainIfHashIsDifferentForChangedRowInMirror() {
        when(dataManager.mainIdList()).thenReturn(Arrays.asList(1L, 2L));
        when(dataManager.mirrorIdList()).thenReturn(Arrays.asList(1L, 2L));

        TaskDefinition changed = new TaskDefinition(1L, "changed", "desc");
        when(dataManager.mirrorRow(eq(1L))).thenReturn(changed);

        when(hashService.hash(eq(changed))).thenReturn("changed-hash");

        taskDefinitionSynchronizer.hashMap.put(1L, "first-hash");
        taskDefinitionSynchronizer.hashMap.put(2L, "second-hash");

        assumeTrue(taskDefinitionSynchronizer.size() == 2);
        taskDefinitionSynchronizer.sync();

        assertEquals(2, taskDefinitionSynchronizer.size());
        assertEquals("changed-hash", taskDefinitionSynchronizer.hashMap.get(1L));
        assertEquals("second-hash", taskDefinitionSynchronizer.hashMap.get(2L));

        verify(dataManager, times(2)).mainRow(any());
        verify(dataManager, times(2)).mirrorRow(any());
        verify(dataManager, never()).addMainRow(any());
        verify(dataManager, never()).addMirrorRow(any());
        verify(dataManager, times(1)).updateMainRow(eq(changed));
        verify(dataManager, never()).updateMirrorRow(any());
        verify(dataManager, never()).removeMainRow(any());
        verify(dataManager, never()).removeMirrorRow(any());
    }

    @Test
    public void updateMiddleRowInMainIfHashIsDifferentForChangedRowInMirror() {
        when(dataManager.mainIdList()).thenReturn(Arrays.asList(1L, 2L, 3L));
        when(dataManager.mirrorIdList()).thenReturn(Arrays.asList(1L, 2L, 3L));

        TaskDefinition changed = new TaskDefinition(2L, "changed", "desc");
        when(dataManager.mirrorRow(eq(2L))).thenReturn(changed);

        when(hashService.hash(eq(changed))).thenReturn("changed-hash");

        taskDefinitionSynchronizer.hashMap.put(1L, "first-hash");
        taskDefinitionSynchronizer.hashMap.put(2L, "second-hash");
        taskDefinitionSynchronizer.hashMap.put(3L, "third-hash");

        assumeTrue(taskDefinitionSynchronizer.size() == 3);
        taskDefinitionSynchronizer.sync();

        assertEquals(3, taskDefinitionSynchronizer.size());
        assertEquals("first-hash", taskDefinitionSynchronizer.hashMap.get(1L));
        assertEquals("changed-hash", taskDefinitionSynchronizer.hashMap.get(2L));
        assertEquals("third-hash", taskDefinitionSynchronizer.hashMap.get(3L));

        verify(dataManager, times(3)).mainRow(any());
        verify(dataManager, times(3)).mirrorRow(any());
        verify(dataManager, never()).addMainRow(any());
        verify(dataManager, never()).addMirrorRow(any());
        verify(dataManager, times(1)).updateMainRow(eq(changed));
        verify(dataManager, never()).updateMirrorRow(any());
        verify(dataManager, never()).removeMainRow(any());
        verify(dataManager, never()).removeMirrorRow(any());
    }

    @Test
    public void updateLastRowInMainIfHashIsDifferentForChangedRowInMirror() {
        when(dataManager.mainIdList()).thenReturn(Arrays.asList(1L, 2L));
        when(dataManager.mirrorIdList()).thenReturn(Arrays.asList(1L, 2L));

        TaskDefinition changed = new TaskDefinition(2L, "changed", "desc");
        when(dataManager.mirrorRow(eq(2L))).thenReturn(changed);

        when(hashService.hash(eq(changed))).thenReturn("changed-hash");

        taskDefinitionSynchronizer.hashMap.put(1L, "first-hash");
        taskDefinitionSynchronizer.hashMap.put(2L, "second-hash");

        assumeTrue(taskDefinitionSynchronizer.size() == 2);
        taskDefinitionSynchronizer.sync();

        assertEquals(2, taskDefinitionSynchronizer.size());
        assertEquals("first-hash", taskDefinitionSynchronizer.hashMap.get(1L));
        assertEquals("changed-hash", taskDefinitionSynchronizer.hashMap.get(2L));

        verify(dataManager, times(2)).mainRow(any());
        verify(dataManager, times(2)).mirrorRow(any());
        verify(dataManager, never()).addMainRow(any());
        verify(dataManager, never()).addMirrorRow(any());
        verify(dataManager, times(1)).updateMainRow(eq(changed));
        verify(dataManager, never()).updateMirrorRow(any());
        verify(dataManager, never()).removeMainRow(any());
        verify(dataManager, never()).removeMirrorRow(any());
    }

    @Test
    public void updateFirstRowInMirrorIfHashIsDifferentForChangedRowInMain() {
        when(dataManager.mainIdList()).thenReturn(Arrays.asList(1L, 2L));
        when(dataManager.mirrorIdList()).thenReturn(Arrays.asList(1L, 2L));

        TaskDefinition changed = new TaskDefinition(1L, "changed", "desc");
        when(dataManager.mainRow(eq(1L))).thenReturn(changed);

        when(hashService.hash(eq(changed))).thenReturn("changed-hash");

        taskDefinitionSynchronizer.hashMap.put(1L, "first-hash");
        taskDefinitionSynchronizer.hashMap.put(2L, "second-hash");

        assumeTrue(taskDefinitionSynchronizer.size() == 2);
        taskDefinitionSynchronizer.sync();

        assertEquals(2, taskDefinitionSynchronizer.size());
        assertEquals("changed-hash", taskDefinitionSynchronizer.hashMap.get(1L));
        assertEquals("second-hash", taskDefinitionSynchronizer.hashMap.get(2L));

        verify(dataManager, times(2)).mainRow(any());
        verify(dataManager, times(2)).mirrorRow(any());
        verify(dataManager, never()).addMainRow(any());
        verify(dataManager, never()).addMirrorRow(any());
        verify(dataManager, never()).updateMainRow(any());
        verify(dataManager, times(1)).updateMirrorRow(eq(changed));
        verify(dataManager, never()).removeMainRow(any());
        verify(dataManager, never()).removeMirrorRow(any());
    }

    @Test
    public void updateMiddleRowInMirrorIfHashIsDifferentForChangedRowInMain() {
        when(dataManager.mainIdList()).thenReturn(Arrays.asList(1L, 2L, 3L));
        when(dataManager.mirrorIdList()).thenReturn(Arrays.asList(1L, 2L, 3L));

        TaskDefinition changed = new TaskDefinition(2L, "changed", "desc");
        when(dataManager.mainRow(eq(2L))).thenReturn(changed);

        when(hashService.hash(eq(changed))).thenReturn("changed-hash");

        taskDefinitionSynchronizer.hashMap.put(1L, "first-hash");
        taskDefinitionSynchronizer.hashMap.put(2L, "second-hash");
        taskDefinitionSynchronizer.hashMap.put(3L, "third-hash");

        assumeTrue(taskDefinitionSynchronizer.size() == 3);
        taskDefinitionSynchronizer.sync();

        assertEquals(3, taskDefinitionSynchronizer.size());
        assertEquals("first-hash", taskDefinitionSynchronizer.hashMap.get(1L));
        assertEquals("changed-hash", taskDefinitionSynchronizer.hashMap.get(2L));
        assertEquals("third-hash", taskDefinitionSynchronizer.hashMap.get(3L));

        verify(dataManager, times(3)).mainRow(any());
        verify(dataManager, times(3)).mirrorRow(any());
        verify(dataManager, never()).addMainRow(any());
        verify(dataManager, never()).addMirrorRow(any());
        verify(dataManager, never()).updateMainRow(any());
        verify(dataManager, times(1)).updateMirrorRow(eq(changed));
        verify(dataManager, never()).removeMainRow(any());
        verify(dataManager, never()).removeMirrorRow(any());
    }

    @Test
    public void updateLastRowInMirrorIfHashIsDifferentForChangedRowInMain() {
        when(dataManager.mainIdList()).thenReturn(Arrays.asList(1L, 2L));
        when(dataManager.mirrorIdList()).thenReturn(Arrays.asList(1L, 2L));

        TaskDefinition changed = new TaskDefinition(2L, "changed", "desc");
        when(dataManager.mainRow(eq(2L))).thenReturn(changed);

        when(hashService.hash(eq(changed))).thenReturn("changed-hash");

        taskDefinitionSynchronizer.hashMap.put(1L, "first-hash");
        taskDefinitionSynchronizer.hashMap.put(2L, "second-hash");

        assumeTrue(taskDefinitionSynchronizer.size() == 2);
        taskDefinitionSynchronizer.sync();

        assertEquals(2, taskDefinitionSynchronizer.size());
        assertEquals("first-hash", taskDefinitionSynchronizer.hashMap.get(1L));
        assertEquals("changed-hash", taskDefinitionSynchronizer.hashMap.get(2L));

        verify(dataManager, times(2)).mainRow(any());
        verify(dataManager, times(2)).mirrorRow(any());
        verify(dataManager, never()).addMainRow(any());
        verify(dataManager, never()).addMirrorRow(any());
        verify(dataManager, never()).updateMainRow(any());
        verify(dataManager, times(1)).updateMirrorRow(eq(changed));
        verify(dataManager, never()).removeMainRow(any());
        verify(dataManager, never()).removeMirrorRow(any());
    }

    @Test
    public void callConflictHandlerAndRemoveHashIfBothRowsAreChangedAndHashesAreDifferentForBothRows() {
        when(dataManager.mainIdList()).thenReturn(Arrays.asList(1L, 2L));
        when(dataManager.mirrorIdList()).thenReturn(Arrays.asList(1L, 2L));

        TaskDefinition changedMain = new TaskDefinition(2L, "changed-main", "desc");
        when(dataManager.mainRow(eq(2L))).thenReturn(changedMain);
        TaskDefinition changedMirror = new TaskDefinition(2L, "changed-mirror", "desc");
        when(dataManager.mirrorRow(eq(2L))).thenReturn(changedMirror);

        when(hashService.hash(eq(changedMain))).thenReturn("changed-main-hash");
        when(hashService.hash(eq(changedMirror))).thenReturn("changed-mirror-hash");

        taskDefinitionSynchronizer.hashMap.put(1L, "first-hash");
        taskDefinitionSynchronizer.hashMap.put(2L, "second-hash");

        assumeTrue(taskDefinitionSynchronizer.size() == 2);
        taskDefinitionSynchronizer.sync();

        assertEquals(1, taskDefinitionSynchronizer.size());
        assertEquals("first-hash", taskDefinitionSynchronizer.hashMap.get(1L));
        assertNull(taskDefinitionSynchronizer.hashMap.get(2L));    //hash removed

        verify(conflictHandler, times(1)).handle(eq(2L));
        verify(dataManager, times(2)).mainRow(any());
        verify(dataManager, times(2)).mirrorRow(any());
        verify(dataManager, never()).addMainRow(any());
        verify(dataManager, never()).addMirrorRow(any());
        verify(dataManager, never()).updateMainRow(any());
        verify(dataManager, never()).updateMirrorRow(any());
        verify(dataManager, never()).removeMainRow(any());
        verify(dataManager, never()).removeMirrorRow(any());
    }

    @Test
    public void removeFirstRowFromMirrorAndHashIfRemovedInMain() {
        when(dataManager.mainIdList()).thenReturn(Arrays.asList(2L, 3L));
        when(dataManager.mirrorIdList()).thenReturn(Arrays.asList(1L, 2L, 3L));

        taskDefinitionSynchronizer.hashMap.put(1L, "first-hash");
        taskDefinitionSynchronizer.hashMap.put(2L, "second-hash");
        taskDefinitionSynchronizer.hashMap.put(3L, "third-hash");

        assumeTrue(taskDefinitionSynchronizer.size() == 3);
        taskDefinitionSynchronizer.sync();

        assertEquals(2, taskDefinitionSynchronizer.size());
        assertNull(taskDefinitionSynchronizer.hashMap.get(1L));
        assertEquals("second-hash", taskDefinitionSynchronizer.hashMap.get(2L));
        assertEquals("third-hash", taskDefinitionSynchronizer.hashMap.get(3L));

        verify(dataManager, times(3)).mainRow(any()); //3 is correct, that's how iteration works
        verify(dataManager, times(3)).mirrorRow(any());
        verify(dataManager, never()).addMainRow(any());
        verify(dataManager, never()).addMirrorRow(any());
        verify(dataManager, never()).updateMainRow(any());
        verify(dataManager, never()).updateMirrorRow(any());
        verify(dataManager, never()).removeMainRow(any());
        verify(dataManager, times(1)).removeMirrorRow(eq(first));
    }

    @Test
    public void removeMiddleRowFromMirrorAndHashIfRemovedInMain() {
        when(dataManager.mainIdList()).thenReturn(Arrays.asList(1L, 3L));
        when(dataManager.mirrorIdList()).thenReturn(Arrays.asList(1L, 2L, 3L));

        taskDefinitionSynchronizer.hashMap.put(1L, "first-hash");
        taskDefinitionSynchronizer.hashMap.put(2L, "second-hash");
        taskDefinitionSynchronizer.hashMap.put(3L, "third-hash");

        assumeTrue(taskDefinitionSynchronizer.size() == 3);
        taskDefinitionSynchronizer.sync();

        assertEquals(2, taskDefinitionSynchronizer.size());
        assertNull(taskDefinitionSynchronizer.hashMap.get(2L));
        assertEquals("first-hash", taskDefinitionSynchronizer.hashMap.get(1L));
        assertEquals("third-hash", taskDefinitionSynchronizer.hashMap.get(3L));

        verify(dataManager, times(3)).mainRow(any()); //3 is correct, that's how iteration works
        verify(dataManager, times(3)).mirrorRow(any());
        verify(dataManager, never()).addMainRow(any());
        verify(dataManager, never()).addMirrorRow(any());
        verify(dataManager, never()).updateMainRow(any());
        verify(dataManager, never()).updateMirrorRow(any());
        verify(dataManager, never()).removeMainRow(any());
        verify(dataManager, times(1)).removeMirrorRow(eq(second));
    }

    @Test
    public void removeLastRowFromMirrorAndHashIfRemovedInMain() {
        when(dataManager.mainIdList()).thenReturn(Arrays.asList(1L, 2L));
        when(dataManager.mirrorIdList()).thenReturn(Arrays.asList(1L, 2L, 3L));

        taskDefinitionSynchronizer.hashMap.put(1L, "first-hash");
        taskDefinitionSynchronizer.hashMap.put(2L, "second-hash");
        taskDefinitionSynchronizer.hashMap.put(3L, "third-hash");

        assumeTrue(taskDefinitionSynchronizer.size() == 3);
        taskDefinitionSynchronizer.sync();

        assertEquals(2, taskDefinitionSynchronizer.size());
        assertEquals("first-hash", taskDefinitionSynchronizer.hashMap.get(1L));
        assertEquals("second-hash", taskDefinitionSynchronizer.hashMap.get(2L));
        assertNull(taskDefinitionSynchronizer.hashMap.get(3L));

        verify(dataManager, times(2)).mainRow(any()); //2 is correct, that's how iteration works
        verify(dataManager, times(3)).mirrorRow(any());
        verify(dataManager, never()).addMainRow(any());
        verify(dataManager, never()).addMirrorRow(any());
        verify(dataManager, never()).updateMainRow(any());
        verify(dataManager, never()).updateMirrorRow(any());
        verify(dataManager, never()).removeMainRow(any());
        verify(dataManager, times(1)).removeMirrorRow(eq(third));
    }

    @Test
    public void removeFirstRowFromMainAndHashIfRemovedInMirror() {
        when(dataManager.mainIdList()).thenReturn(Arrays.asList(1L, 2L, 3L));
        when(dataManager.mirrorIdList()).thenReturn(Arrays.asList(2L, 3L));

        taskDefinitionSynchronizer.hashMap.put(1L, "first-hash");
        taskDefinitionSynchronizer.hashMap.put(2L, "second-hash");
        taskDefinitionSynchronizer.hashMap.put(3L, "third-hash");

        assumeTrue(taskDefinitionSynchronizer.size() == 3);
        taskDefinitionSynchronizer.sync();

        assertEquals(2, taskDefinitionSynchronizer.size());
        assertNull(taskDefinitionSynchronizer.hashMap.get(1L));
        assertEquals("second-hash", taskDefinitionSynchronizer.hashMap.get(2L));
        assertEquals("third-hash", taskDefinitionSynchronizer.hashMap.get(3L));

        verify(dataManager, times(3)).mainRow(any());
        verify(dataManager, times(3)).mirrorRow(any()); //3 is correct, that's how iteration works
        verify(dataManager, never()).addMainRow(any());
        verify(dataManager, never()).addMirrorRow(any());
        verify(dataManager, never()).updateMainRow(any());
        verify(dataManager, never()).updateMirrorRow(any());
        verify(dataManager, times(1)).removeMainRow(first);
        verify(dataManager, never()).removeMirrorRow(any());
    }

    @Test
    public void removeMiddleRowFromMainAndHashIfRemovedInMirror() {
        when(dataManager.mainIdList()).thenReturn(Arrays.asList(1L, 2L, 3L));
        when(dataManager.mirrorIdList()).thenReturn(Arrays.asList(1L, 3L));

        taskDefinitionSynchronizer.hashMap.put(1L, "first-hash");
        taskDefinitionSynchronizer.hashMap.put(2L, "second-hash");
        taskDefinitionSynchronizer.hashMap.put(3L, "third-hash");

        assumeTrue(taskDefinitionSynchronizer.size() == 3);
        taskDefinitionSynchronizer.sync();

        assertEquals(2, taskDefinitionSynchronizer.size());
        assertNull(taskDefinitionSynchronizer.hashMap.get(2L));
        assertEquals("first-hash", taskDefinitionSynchronizer.hashMap.get(1L));
        assertEquals("third-hash", taskDefinitionSynchronizer.hashMap.get(3L));

        verify(dataManager, times(3)).mainRow(any());
        verify(dataManager, times(3)).mirrorRow(any()); //3 is correct, that's how iteration works
        verify(dataManager, never()).addMainRow(any());
        verify(dataManager, never()).addMirrorRow(any());
        verify(dataManager, never()).updateMainRow(any());
        verify(dataManager, never()).updateMirrorRow(any());
        verify(dataManager, times(1)).removeMainRow(second);
        verify(dataManager, never()).removeMirrorRow(any());
    }

    @Test
    public void removeLastRowFromMainAndHashIfRemovedInMirror() {
        when(dataManager.mainIdList()).thenReturn(Arrays.asList(1L, 2L, 3L));
        when(dataManager.mirrorIdList()).thenReturn(Arrays.asList(1L, 2L));

        taskDefinitionSynchronizer.hashMap.put(1L, "first-hash");
        taskDefinitionSynchronizer.hashMap.put(2L, "second-hash");
        taskDefinitionSynchronizer.hashMap.put(3L, "third-hash");

        assumeTrue(taskDefinitionSynchronizer.size() == 3);
        taskDefinitionSynchronizer.sync();

        assertEquals(2, taskDefinitionSynchronizer.size());
        assertEquals("first-hash", taskDefinitionSynchronizer.hashMap.get(1L));
        assertEquals("second-hash", taskDefinitionSynchronizer.hashMap.get(2L));
        assertNull(taskDefinitionSynchronizer.hashMap.get(3L));

        verify(dataManager, times(3)).mainRow(any());
        verify(dataManager, times(2)).mirrorRow(any()); //2 is correct, that's how iteration works
        verify(dataManager, never()).addMainRow(any());
        verify(dataManager, never()).addMirrorRow(any());
        verify(dataManager, never()).updateMainRow(any());
        verify(dataManager, never()).updateMirrorRow(any());
        verify(dataManager, times(1)).removeMainRow(third);
        verify(dataManager, never()).removeMirrorRow(any());
    }
}