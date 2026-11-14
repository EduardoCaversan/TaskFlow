package com.eduardocaversan.taskflow;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Spinner;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class TaskFormActivity extends AppCompatActivity {
    private TaskEditorState editor;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_form);
        ScreenInsets.apply(this);
        editor = new ViewModelProvider(this).get(TaskEditorState.class);
        OnBackPressedCallback waitForWrite = new OnBackPressedCallback(false) {
            @Override public void handleOnBackPressed() { /* Aguarda confirmação do banco. */ }
        };
        getOnBackPressedDispatcher().addCallback(this, waitForWrite);
        findViewById(R.id.button_back).setOnClickListener(view -> finish());
        findViewById(R.id.button_cancel).setOnClickListener(view -> finish());
        findViewById(R.id.button_save).setOnClickListener(view -> saveTask());
        editor.status().observe(this, status -> {
            boolean enabled = !editor.busy();
            for (int id : new int[]{R.id.button_save, R.id.button_back, R.id.button_cancel,
                    R.id.input_description, R.id.spinner_priority}) findViewById(id).setEnabled(enabled);
            waitForWrite.setEnabled(editor.writing());
            if (status == TaskEditorState.Status.CREATED) finishWithFeedback(R.string.task_created);
            else if (status == TaskEditorState.Status.SAVE_ERROR) {
                Snackbar.make(findViewById(R.id.button_save), R.string.save_failed, Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private void saveTask() {
        if (editor.busy()) return;
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
            Snackbar.make(input, R.string.priority_required, Snackbar.LENGTH_LONG).show();
            findViewById(R.id.spinner_priority).requestFocus();
            return;
        }
        layout.setError(null);
        editor.save(TaskRepository.getInstance(this), new Task(description, priority), true);
    }

    private void finishWithFeedback(int message) {
        Intent result = new Intent().putExtra(MainActivity.EXTRA_FEEDBACK, getString(message));
        setResult(RESULT_OK, result);
        finish();
    }
}
