package com.eduardocaversan.taskflow;

import android.os.Handler;
import android.os.Looper;
import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.util.ArrayDeque;
import java.util.Queue;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class TaskEditorStateTest {
    private final Queue<Runnable> queue = new ArrayDeque<>();
    private TaskDatabase database;
    private TaskRepository repository;
    private TaskEditorState editor;

    @Before public void setUp() {
        database = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), TaskDatabase.class).build();
        repository = new TaskRepository(database.taskDao(), queue::add, new Handler(Looper.getMainLooper()));
        editor = new TaskEditorState();
    }
    @After public void close() { database.close(); }
    private void onMain(Runnable action) { InstrumentationRegistry.getInstrumentation().runOnMainSync(action); }
    private void finishOperation() {
        queue.remove().run(); // Thread de instrumentação; nunca a main thread.
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
    }
    @Test public void duplicateSubmissionIsIgnoredWhileWritingAndAfterSuccess() {
        Task task = new Task("Uma vez", "Alta");
        onMain(() -> {
            editor.save(repository, task, true);
            editor.save(repository, task, true);
            assertTrue(editor.writing());
        });
        assertEquals(1, queue.size());
        finishOperation();
        assertEquals(TaskEditorState.Status.CREATED, editor.status().getValue());
        onMain(() -> editor.save(repository, task, true));
        assertTrue(queue.isEmpty());
        assertEquals(1, database.taskDao().getAll().size());
    }
    @Test public void failedSaveCanBeCorrectedAndRetried() {
        onMain(() -> editor.save(repository, new Task(" ", "Alta"), true));
        finishOperation();
        assertEquals(TaskEditorState.Status.SAVE_ERROR, editor.status().getValue());
        assertFalse(editor.busy());
        onMain(() -> editor.save(repository, new Task("Corrigida", "Média"), true));
        finishOperation();
        assertEquals(TaskEditorState.Status.CREATED, editor.status().getValue());
        assertEquals(1, database.taskDao().getAll().size());
    }
    @Test public void missingTaskHasExplicitState() {
        onMain(() -> editor.load(repository, -1));
        finishOperation();
        assertNull(editor.task());
        assertEquals(TaskEditorState.Status.MISSING, editor.status().getValue());
        assertFalse(editor.busy());
    }
}
