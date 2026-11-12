package com.eduardocaversan.taskflow;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

/** Mantém uma operação em curso durante rotação, sem guardar Activity ou View. */
public class TaskEditorState extends ViewModel {
    enum Status { READY, LOADING, SAVING, DELETING, CREATED, UPDATED, DELETED,
        LOAD_ERROR, SAVE_ERROR, DELETE_ERROR, MISSING }

    private final MutableLiveData<Status> status = new MutableLiveData<>(Status.READY);
    private Task task;

    LiveData<Status> status() { return status; }
    Task task() { return task; }
    boolean busy() { return status.getValue() == Status.LOADING || writing(); }
    boolean writing() {
        return status.getValue() == Status.SAVING || status.getValue() == Status.DELETING;
    }
    private boolean finished() {
        return status.getValue() == Status.CREATED || status.getValue() == Status.UPDATED
                || status.getValue() == Status.DELETED || status.getValue() == Status.MISSING;
    }

    void load(TaskRepository repository, long id) {
        if (busy() || task != null || finished()) return;
        status.setValue(Status.LOADING);
        repository.findById(id, (loaded, error) -> {
            task = loaded;
            status.setValue(error != null ? Status.LOAD_ERROR : loaded == null ? Status.MISSING : Status.READY);
        });
    }

    void save(TaskRepository repository, Task value, boolean create) {
        if (busy() || finished()) return;
        status.setValue(Status.SAVING);
        if (create) {
            repository.add(value, (id, error) -> status.setValue(error == null ? Status.CREATED : Status.SAVE_ERROR));
        } else {
            repository.update(value, (unused, error) -> status.setValue(error == null ? Status.UPDATED : Status.SAVE_ERROR));
        }
    }

    void delete(TaskRepository repository) {
        if (busy() || finished() || task == null) return;
        status.setValue(Status.DELETING);
        repository.delete(task, (unused, error) -> status.setValue(error == null ? Status.DELETED : Status.DELETE_ERROR));
    }
}
