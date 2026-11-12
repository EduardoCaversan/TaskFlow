package com.eduardocaversan.taskflow;

import android.os.Handler;
import android.os.Looper;
import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.lang.reflect.Proxy;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class TaskRepositoryTest {
    private TaskDatabase database;
    private ExecutorService executor;
    private TaskRepository repository;
    @Before public void setUp() {
        database = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), TaskDatabase.class).build();
        executor = Executors.newSingleThreadExecutor();
        repository = new TaskRepository(database.taskDao(), executor, new Handler(Looper.getMainLooper()));
    }
    @After public void tearDown() throws Exception {
        executor.shutdown();
        assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS));
        database.close();
    }
    interface Operation<T> { void run(TaskRepository.Callback<T> callback); }
    private <T> T await(Operation<T> operation, boolean failure) throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<T> value = new AtomicReference<>();
        AtomicReference<Exception> error = new AtomicReference<>();
        AtomicReference<Looper> callbackLooper = new AtomicReference<>();
        operation.run((result, exception) -> {
            value.set(result); error.set(exception); callbackLooper.set(Looper.myLooper()); latch.countDown();
        });
        assertTrue("Callback não entregue", latch.await(10, TimeUnit.SECONDS));
        assertSame(Looper.getMainLooper(), callbackLooper.get());
        assertEquals(failure, error.get() != null);
        return value.get();
    }
    @Test public void addNormalizesAndReturnsPersistedIdOnMainThread() throws Exception {
        long id = this.<Long>await(callback -> repository.add(new Task("  Estudar  ", "Alta"), callback), false);
        assertEquals("Estudar", database.taskDao().findById(id).getDescription());
    }
    @Test public void invalidTaskReturnsErrorWithoutInserting() throws Exception {
        this.<Long>await(callback -> repository.add(new Task(" \t ", "Alta"), callback), true);
        this.<Long>await(callback -> repository.add(new Task("Teste", "Inválida"), callback), true);
        assertTrue(database.taskDao().getAll().isEmpty());
    }
    @Test public void missingWritesReturnFailure() throws Exception {
        Task missing = new Task("Ausente", "Baixa"); missing.setId(1234);
        this.<Void>await(callback -> repository.update(missing, callback), true);
        this.<Void>await(callback -> repository.updateStatus(1234, true, callback), true);
        this.<Void>await(callback -> repository.delete(missing, callback), true);
    }
    @Test public void databaseExceptionsReachEveryCallbackOffTheWorker() throws Exception {
        TaskDao failing = (TaskDao) Proxy.newProxyInstance(TaskDao.class.getClassLoader(), new Class[]{TaskDao.class}, (proxy, method, args) -> {
            assertNotSame(Looper.getMainLooper(), Looper.myLooper());
            throw new IllegalStateException("Simulated storage failure");
        });
        TaskRepository broken = new TaskRepository(failing, executor, new Handler(Looper.getMainLooper()));
        Task task = new Task("Teste", "Alta");
        await(broken::getAll, true);
        this.<Task>await(callback -> broken.findById(1, callback), true);
        this.<Long>await(callback -> broken.add(task, callback), true);
        task.setId(1);
        this.<Void>await(callback -> broken.update(task, callback), true);
        this.<Void>await(callback -> broken.updateStatus(1, true, callback), true);
        this.<Void>await(callback -> broken.delete(task, callback), true);
    }
}
