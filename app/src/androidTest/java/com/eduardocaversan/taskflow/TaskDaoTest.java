package com.eduardocaversan.taskflow;

import android.content.Context;
import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.util.List;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class TaskDaoTest {
    private TaskDatabase database;
    private TaskDao dao;

    @Before public void openDatabase() {
        database = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), TaskDatabase.class).build();
        dao = database.taskDao();
    }
    @After public void closeDatabase() { database.close(); }

    private Task insert(String description, String priority, boolean completed) {
        Task task = new Task(description, priority);
        task.setCompleted(completed);
        task.setId(dao.insert(task));
        return task;
    }

    @Test public void insertAndReadAllFields() {
        Task original = insert("Descrição com acentuação\ne nova linha", "Média", true);
        Task loaded = dao.findById(original.getId());
        assertNotNull(loaded);
        assertTrue(loaded.getId() > 0);
        assertEquals(original.getDescription(), loaded.getDescription());
        assertEquals("Média", loaded.getPriority());
        assertTrue(loaded.isCompleted());
        assertEquals(1, dao.getAll().size());
    }
    @Test public void updateChangesOnlySelectedTask() {
        Task first = insert("Primeira", "Baixa", false);
        Task second = insert("Segunda", "Alta", false);
        first.setDescription("Editada"); first.setPriority("Média"); first.setCompleted(true);
        assertEquals(1, dao.update(first));
        Task loaded = dao.findById(first.getId());
        assertEquals("Editada", loaded.getDescription());
        assertEquals("Média", loaded.getPriority());
        assertTrue(loaded.isCompleted());
        assertEquals("Segunda", dao.findById(second.getId()).getDescription());
        assertFalse(dao.findById(second.getId()).isCompleted());
    }
    @Test public void statusChangesInBothDirections() {
        Task task = insert("Status", "Alta", false);
        assertEquals(1, dao.updateStatus(task.getId(), true));
        assertTrue(dao.findById(task.getId()).isCompleted());
        assertEquals(1, dao.updateStatus(task.getId(), false));
        assertFalse(dao.findById(task.getId()).isCompleted());
        assertEquals("Alta", dao.findById(task.getId()).getPriority());
    }
    @Test public void deleteRemovesOnlySelectedTask() {
        Task first = insert("Excluir", "Baixa", false);
        Task second = insert("Manter", "Alta", true);
        assertEquals(1, dao.delete(first));
        assertNull(dao.findById(first.getId()));
        assertEquals(second.getId(), dao.getAll().get(0).getId());
        assertEquals(0, dao.delete(first));
    }
    @Test public void pendingFirstThenNewestIdWithinEachStatus() {
        Task oldPending = insert("Antiga", "Alta", false);
        Task completed = insert("Concluída", "Baixa", true);
        Task newPending = insert("Nova", "Média", false);
        List<Task> tasks = dao.getAll();
        assertEquals(newPending.getId(), tasks.get(0).getId());
        assertEquals(oldPending.getId(), tasks.get(1).getId());
        assertEquals(completed.getId(), tasks.get(2).getId());
    }
    @Test public void missingIdIsNullAndWritesReportNoRows() {
        assertNull(dao.findById(-1));
        Task missing = new Task("Ausente", "Baixa"); missing.setId(987);
        assertEquals(0, dao.update(missing));
        assertEquals(0, dao.updateStatus(987, true));
        assertEquals(0, dao.delete(missing));
    }
    @Test public void duplicateIdDoesNotReplaceExistingData() {
        Task first = insert("Original", "Alta", false);
        Task duplicate = new Task("Substituição", "Baixa"); duplicate.setId(first.getId());
        assertThrows(android.database.sqlite.SQLiteConstraintException.class, () -> dao.insert(duplicate));
        assertEquals("Original", dao.findById(first.getId()).getDescription());
    }
    @Test public void fileDatabaseSurvivesCloseAndReopen() {
        Context context = ApplicationProvider.getApplicationContext();
        String name = "taskflow-persistence-test.db";
        context.deleteDatabase(name);
        try {
            long id;
            TaskDatabase first = Room.databaseBuilder(context, TaskDatabase.class, name).build();
            try {
                id = first.taskDao().insert(new Task("Persistente", "Média"));
            } finally { first.close(); }
            TaskDatabase reopened = Room.databaseBuilder(context, TaskDatabase.class, name).build();
            try {
                assertEquals("Persistente", reopened.taskDao().findById(id).getDescription());
                assertEquals("Média", reopened.taskDao().findById(id).getPriority());
            } finally { reopened.close(); }
        } finally { context.deleteDatabase(name); }
    }
}
