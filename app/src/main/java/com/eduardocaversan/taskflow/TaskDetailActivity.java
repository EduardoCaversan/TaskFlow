package com.eduardocaversan.taskflow;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Spinner;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class TaskDetailActivity extends AppCompatActivity {
    private static final String FORM_POPULATED = "form_populated";
    private TaskEditorState editor;
    private boolean formPopulated;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_detail);
        formPopulated = savedInstanceState != null && savedInstanceState.getBoolean(FORM_POPULATED);
        editor = new ViewModelProvider(this).get(TaskEditorState.class);
        OnBackPressedCallback waitForWrite = new OnBackPressedCallback(false) {
            @Override public void handleOnBackPressed() { /* Aguarda confirmação do banco. */ }
        };
        getOnBackPressedDispatcher().addCallback(this, waitForWrite);
        findViewById(R.id.button_back).setOnClickListener(view -> finish());
        findViewById(R.id.button_cancel).setOnClickListener(view -> finish());
        findViewById(R.id.button_save).setOnClickListener(view -> saveTask());
        findViewById(R.id.button_delete).setOnClickListener(view -> confirmDelete());
        editor.status().observe(this, status -> {
            boolean editable = editor.task() != null && !editor.busy();
            for (int id : new int[]{R.id.button_save, R.id.button_delete, R.id.input_description,
                    R.id.spinner_priority, R.id.spinner_status}) findViewById(id).setEnabled(editable);
            findViewById(R.id.button_back).setEnabled(!editor.writing());
            findViewById(R.id.button_cancel).setEnabled(!editor.writing());
            waitForWrite.setEnabled(editor.writing());
            if (editor.task() != null && !formPopulated) populateForm();
            switch (status) {
                case MISSING: finishWithFeedback(R.string.task_not_found); break;
                case UPDATED: finishWithFeedback(R.string.task_updated); break;
                case DELETED: finishWithFeedback(R.string.task_deleted); break;
                case LOAD_ERROR:
                    Snackbar.make(findViewById(R.id.button_save), R.string.load_failed, Snackbar.LENGTH_INDEFINITE)
                            .setAction(R.string.retry, view -> loadTask()).show();
                    break;
                case SAVE_ERROR: showError(R.string.save_failed); break;
                case DELETE_ERROR: showError(R.string.delete_failed); break;
                default: break;
            }
        });
        if (editor.status().getValue() == TaskEditorState.Status.READY) loadTask();
    }

    private void loadTask() {
        editor.load(TaskRepository.getInstance(this), getIntent().getLongExtra(MainActivity.EXTRA_TASK_ID, -1));
    }

    private void populateForm() {
        Task task = editor.task();
        ((TextInputEditText) findViewById(R.id.input_description)).setText(task.getDescription());
        Spinner priority = findViewById(R.id.spinner_priority);
        for (int i = 0; i < priority.getCount(); i++) {
            if (priority.getItemAtPosition(i).equals(task.getPriority())) priority.setSelection(i);
        }
        ((Spinner) findViewById(R.id.spinner_status)).setSelection(task.isCompleted() ? 1 : 0);
        formPopulated = true;
    }

    @Override protected void onSaveInstanceState(Bundle state) {
        state.putBoolean(FORM_POPULATED, formPopulated);
        super.onSaveInstanceState(state);
    }

    private void saveTask() {
        if (editor.task() == null || editor.busy()) return;
        TextInputLayout layout = findViewById(R.id.input_description_layout);
        TextInputEditText input = findViewById(R.id.input_description);
        String description = TaskRules.normalizeDescription(input.getText() == null ? null : input.getText().toString());
        Object selected = ((Spinner) findViewById(R.id.spinner_priority)).getSelectedItem();
        String priority = selected == null ? null : selected.toString();
        if (description.isEmpty()) {
            layout.setError(getString(R.string.description_required));
            input.requestFocus();
            return;
        }
        if (!TaskRules.isValidPriority(priority)) {
            showError(R.string.priority_required);
            findViewById(R.id.spinner_priority).requestFocus();
            return;
        }
        layout.setError(null);
        Task updated = new Task(description, priority);
        updated.setId(editor.task().getId());
        updated.setCompleted(((Spinner) findViewById(R.id.spinner_status)).getSelectedItemPosition() == 1);
        editor.save(TaskRepository.getInstance(this), updated, false);
    }

    private void confirmDelete() {
        if (editor.task() == null || editor.busy()) return;
        new MaterialAlertDialogBuilder(this).setTitle(R.string.delete_task).setMessage(R.string.delete_confirmation)
                .setPositiveButton(R.string.delete, (dialog, which) -> editor.delete(TaskRepository.getInstance(this)))
                .setNegativeButton(R.string.cancel, null).show();
    }

    private void showError(int message) {
        Snackbar.make(findViewById(R.id.button_save), message, Snackbar.LENGTH_LONG).show();
    }

    private void finishWithFeedback(int message) {
        Intent result = new Intent().putExtra(MainActivity.EXTRA_FEEDBACK, getString(message));
        setResult(RESULT_OK, result);
        finish();
    }
}