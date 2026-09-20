package com.eduardocaversan.taskflow;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

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
        TaskRepository.getInstance(this).add(new Task(description, priority.getSelectedItem().toString()), id -> finishWithFeedback(R.string.task_created));
    }
    private boolean isValidPriority(String priority) { return priority.equals(getString(R.string.priority_low)) || priority.equals(getString(R.string.priority_medium)) || priority.equals(getString(R.string.priority_high)); }
    private void finishWithFeedback(int message) { Intent result = new Intent(); result.putExtra(MainActivity.EXTRA_FEEDBACK, getString(message)); setResult(RESULT_OK, result); finish(); }
}
