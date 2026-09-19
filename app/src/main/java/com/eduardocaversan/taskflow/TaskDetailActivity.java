package com.eduardocaversan.taskflow;

import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.Spinner;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class TaskDetailActivity extends AppCompatActivity {
    private Task task;
    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_detail);
        task = TaskRepository.getInstance().findById(getIntent().getLongExtra(MainActivity.EXTRA_TASK_ID, -1));
        if (task == null) { finish(); return; }
        ((TextInputEditText) findViewById(R.id.input_description)).setText(task.getDescription());
        selectValue(findViewById(R.id.spinner_priority), task.getPriority());
        selectValue(findViewById(R.id.spinner_status), task.isCompleted() ? getString(R.string.status_completed) : getString(R.string.status_pending));
        findViewById(R.id.button_save).setOnClickListener(view -> saveTask());
        findViewById(R.id.button_delete).setOnClickListener(view -> confirmDelete());
        findViewById(R.id.button_cancel).setOnClickListener(view -> finish());
    }
    private void selectValue(Spinner spinner, String value) {
        for (int i = 0; i < spinner.getCount(); i++) if (value.equals(spinner.getItemAtPosition(i).toString())) { spinner.setSelection(i); return; }
    }
    private void saveTask() {
        TextInputLayout layout = findViewById(R.id.input_description_layout);
        TextInputEditText input = findViewById(R.id.input_description);
        String description = input.getText() == null ? "" : input.getText().toString().trim();
        if (description.isEmpty()) { layout.setError(getString(R.string.description_required)); return; }
        task.setDescription(description);
        task.setPriority(((Spinner) findViewById(R.id.spinner_priority)).getSelectedItem().toString());
        task.setCompleted(((Spinner) findViewById(R.id.spinner_status)).getSelectedItem().toString().equals(getString(R.string.status_completed)));
        finish();
    }
    private void confirmDelete() {
        new AlertDialog.Builder(this).setTitle(R.string.delete_task).setMessage(R.string.delete_confirmation)
                .setPositiveButton(R.string.delete, (DialogInterface dialog, int which) -> { TaskRepository.getInstance().delete(task.getId()); finish(); })
                .setNegativeButton(R.string.cancel, null).show();
    }
}
