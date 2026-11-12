package com.eduardocaversan.taskflow;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Camada simples entre as telas e o Room; o banco nunca bloqueia a interface. */
public class TaskRepository {
    public interface Callback<T> { void onComplete(T value, Exception error); }

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
    public void add(Task task, Callback<Long> callback) {
        run(() -> {
            if (task.getId() != 0) throw new IllegalArgumentException("New tasks must not have an ID");
            return taskDao.insert(TaskRules.validatedCopy(task));
        }, callback);
    }
    public void update(Task task, Callback<Void> callback) { run(() -> { requireChanged(taskDao.update(TaskRules.validatedCopy(task))); return null; }, callback); }
    public void updateStatus(long id, boolean completed, Callback<Void> callback) { run(() -> { requireChanged(taskDao.updateStatus(id, completed)); return null; }, callback); }
    public void delete(Task task, Callback<Void> callback) { run(() -> { requireChanged(taskDao.delete(task)); return null; }, callback); }

    private static void requireChanged(int rows) {
        if (rows != 1) throw new IllegalStateException("Task no longer exists");
    }

    private <T> void run(java.util.concurrent.Callable<T> work, Callback<T> callback) {
        executor.execute(() -> {
            try {
                T result = work.call();
                mainHandler.post(() -> callback.onComplete(result, null));
            } catch (Exception error) {
                Log.e("TaskRepository", "Local database operation failed", error);
                mainHandler.post(() -> callback.onComplete(null, error));
            }
        });
    }
}
