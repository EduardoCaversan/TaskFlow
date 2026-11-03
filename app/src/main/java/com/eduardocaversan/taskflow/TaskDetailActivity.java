package com.eduardocaversan.taskflow;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class TaskDetailActivity extends AppCompatActivity {
    private Task task;
    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); setContentView(R.layout.activity_task_detail);
        findViewById(R.id.button_back).setOnClickListener(view -> finish());
        findViewById(R.id.button_cancel).setOnClickListener(view -> finish());
        findViewById(R.id.button_save).setOnClickListener(view -> saveTask());
        findViewById(R.id.button_delete).setOnClickListener(view -> confirmDelete());
        TaskRepository.getInstance(this).findById(getIntent().getLongExtra(MainActivity.EXTRA_TASK_ID, -1), loaded -> {
            if (loaded == null) { finish(); return; }
            task = loaded;
            ((TextInputEditText) findViewById(R.id.input_description)).setText(task.getDescription());
            selectValue(findViewById(R.id.spinner_priority), task.getPriority());
            selectValue(findViewById(R.id.spinner_status), task.isCompleted() ? getString(R.string.status_completed) : getString(R.string.status_pending));
        });
    }
    private void selectValue(Spinner spinner, String value) { for (int i = 0; i < spinner.getCount(); i++) if (value.equals(spinner.getItemAtPosition(i).toString())) { spinner.setSelection(i); return; } }
    private void saveTask() {
        if (task == null) return;
        TextInputLayout layout = findViewById(R.id.input_description_layout);
        TextInputEditText input = findViewById(R.id.input_description);
        String description = input.getText() == null ? "" : input.getText().toString().trim();
        String priority = ((Spinner) findViewById(R.id.spinner_priority)).getSelectedItem().toString();
        if (description.isEmpty()) { layout.setError(getString(R.string.description_required)); return; }
        if (!isValidPriority(priority)) { layout.setError(getString(R.string.priority_required)); return; }
        layout.setError(null); task.setDescription(description); task.setPriority(priority);
        task.setCompleted(((Spinner) findViewById(R.id.spinner_status)).getSelectedItem().toString().equals(getString(R.string.status_completed)));
        TaskRepository.getInstance(this).update(task, () -> finishWithFeedback(R.string.task_updated));
    }
    private boolean isValidPriority(String priority) { return priority.equals(getString(R.string.priority_low)) || priority.equals(getString(R.string.priority_medium)) || priority.equals(getString(R.string.priority_high)); }
    private void confirmDelete() {
        if (task == null) return;
        new MaterialAlertDialogBuilder(this).setTitle(R.string.delete_task).setMessage(R.string.delete_confirmation)
                .setPositiveButton(R.string.delete, (dialog, which) -> TaskRepository.getInstance(this).delete(task, () -> finishWithFeedback(R.string.task_deleted)))
                .setNegativeButton(R.string.cancel, null).show();
    }
    private void finishWithFeedback(int message) { Intent result = new Intent(); result.putExtra(MainActivity.EXTRA_FEEDBACK, getString(message)); setResult(RESULT_OK, result); finish(); }
}
