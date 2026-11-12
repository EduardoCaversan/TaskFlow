package com.eduardocaversan.taskflow;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.snackbar.Snackbar;

public class TaskFormActivity extends AppCompatActivity {
    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); setContentView(R.layout.activity_task_form);
        findViewById(R.id.button_back).setOnClickListener(view -> finish());
        findViewById(R.id.button_cancel).setOnClickListener(view -> finish());
        findViewById(R.id.button_save).setOnClickListener(view -> saveTask());
    }
    private void saveTask() {
        TextInputLayout layout = findViewById(R.id.input_description_layout);
        TextInputEditText input = findViewById(R.id.input_description);
        String description = input.getText() == null ? "" : input.getText().toString().trim();
        Spinner priority = findViewById(R.id.spinner_priority);
        if (description.isEmpty()) { layout.setError(getString(R.string.description_required)); return; }
        if (!isValidPriority(priority.getSelectedItem().toString())) { layout.setError(getString(R.string.priority_required)); return; }
        layout.setError(null);
        findViewById(R.id.button_save).setEnabled(false);
        TaskRepository.getInstance(this).add(new Task(description, priority.getSelectedItem().toString()), (id, error) -> {
            if (isFinishing() || isDestroyed()) return;
            findViewById(R.id.button_save).setEnabled(true);
            if (error == null) finishWithFeedback(R.string.task_created);
            else Snackbar.make(input, R.string.save_failed, Snackbar.LENGTH_LONG).show();
        });
    }
    private boolean isValidPriority(String priority) { return priority.equals(getString(R.string.priority_low)) || priority.equals(getString(R.string.priority_medium)) || priority.equals(getString(R.string.priority_high)); }
    private void finishWithFeedback(int message) { Intent result = new Intent(); result.putExtra(MainActivity.EXTRA_FEEDBACK, getString(message)); setResult(RESULT_OK, result); finish(); }
}
