package com.eduardocaversan.taskflow;

import android.os.Bundle;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class TaskFormActivity extends AppCompatActivity {
    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_form);
        findViewById(R.id.button_cancel).setOnClickListener(view -> finish());
        findViewById(R.id.button_save).setOnClickListener(view -> saveTask());
    }

    private void saveTask() {
        TextInputLayout layout = findViewById(R.id.input_description_layout);
        TextInputEditText input = findViewById(R.id.input_description);
        String description = input.getText() == null ? "" : input.getText().toString().trim();
        if (description.isEmpty()) { layout.setError(getString(R.string.description_required)); return; }
        layout.setError(null);
        Spinner priority = findViewById(R.id.spinner_priority);
        TaskRepository.getInstance().add(description, priority.getSelectedItem().toString());
        finish();
    }
}
