package com.eduardocaversan.taskflow;

import android.os.Handler;
import android.os.Looper;
import androidx.test.espresso.IdlingResource;

/** Sincroniza Espresso com o executor e com a entrega dos callbacks, sem sleeps. */
final class RepositoryIdlingResource implements IdlingResource {
    private final TaskRepository repository;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private ResourceCallback callback;
    private final Runnable check = new Runnable() {
        @Override public void run() {
            if (repository.isIdle()) {
                if (callback != null) callback.onTransitionToIdle();
            } else handler.postDelayed(this, 10);
        }
    };
    RepositoryIdlingResource(TaskRepository repository) { this.repository = repository; }
    @Override public String getName() { return "TaskRepository"; }
    @Override public boolean isIdleNow() {
        boolean idle = repository.isIdle();
        if (!idle) { handler.removeCallbacks(check); handler.post(check); }
        return idle;
    }
    @Override public void registerIdleTransitionCallback(ResourceCallback callback) { this.callback = callback; }
    void close() { handler.removeCallbacks(check); callback = null; }
}
