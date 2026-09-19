package com.eduardocaversan.taskflow;

import java.util.ArrayList;
import java.util.List;

/** Repositório em memória, isolado para futura substituição por persistência local. */
public class TaskRepository {
    private static final TaskRepository INSTANCE = new TaskRepository();
    private final List<Task> tasks = new ArrayList<>();
    private long nextId = 1;
    private TaskRepository() { }
    public static TaskRepository getInstance() { return INSTANCE; }
    public Task add(String description, String priority) { Task task = new Task(nextId++, description, priority); tasks.add(task); return task; }
    public List<Task> getAll() { return new ArrayList<>(tasks); }
    public Task findById(long id) { for (Task task : tasks) if (task.getId() == id) return task; return null; }
    public void delete(long id) { Task task = findById(id); if (task != null) tasks.remove(task); }
}
