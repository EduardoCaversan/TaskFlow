package com.eduardocaversan.taskflow;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Camada simples entre as telas e o Room; o banco nunca bloqueia a interface. */
public class TaskRepository {
    public interface Callback<T> { void onComplete(T value); }

    private static volatile TaskRepository instance;
    private final TaskDao taskDao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private TaskRepository(Context context) { taskDao = TaskDatabase.getInstance(context).taskDao(); }

    public static TaskRepository getInstance(Context context) {
        if (instance == null) {
            synchronized (TaskRepository.class) {
                if (instance == null) instance = new TaskRepository(context.getApplicationContext());
            }
        }
        return instance;
    }

    public void getAll(Callback<List<Task>> callback) { run(() -> taskDao.getAll(), callback); }
    public void findById(long id, Callback<Task> callback) { run(() -> taskDao.findById(id), callback); }
    public void add(Task task, Callback<Long> callback) { run(() -> taskDao.insert(task), callback); }
    public void update(Task task, Runnable callback) { run(() -> { taskDao.update(task); return null; }, ignored -> callback.run()); }
    public void updateStatus(long id, boolean completed, Runnable callback) { run(() -> { taskDao.updateStatus(id, completed); return null; }, ignored -> callback.run()); }
    public void delete(Task task, Runnable callback) { run(() -> { taskDao.delete(task); return null; }, ignored -> callback.run()); }

    private <T> void run(java.util.concurrent.Callable<T> work, Callback<T> callback) {
        executor.execute(() -> {
            try {
                T result = work.call();
                mainHandler.post(() -> callback.onComplete(result));
            } catch (Exception ignored) { }
        });
    }
}
