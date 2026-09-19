package com.eduardocaversan.taskflow;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    public static final String EXTRA_TASK_ID = "task_id";
    private LinearLayout taskList;
    private View emptyState;
    private String selectedFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        taskList = findViewById(R.id.task_list);
        emptyState = findViewById(R.id.empty_state);
        Spinner filterSpinner = findViewById(R.id.spinner_filter);
        selectedFilter = getString(R.string.filter_all);
        findViewById(R.id.button_add_task).setOnClickListener(view -> startActivity(new Intent(this, TaskFormActivity.class)));
        filterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedFilter = parent.getItemAtPosition(position).toString();
                showTasks();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) { }
        });
    }

    @Override protected void onResume() {
        super.onResume();
        if (taskList != null) showTasks();
    }

    private void showTasks() {
        taskList.removeAllViews();
        boolean hasVisibleTasks = false;
        LayoutInflater inflater = LayoutInflater.from(this);
        for (Task task : TaskRepository.getInstance().getAll()) {
            if (!matchesFilter(task)) continue;
            hasVisibleTasks = true;
            View item = inflater.inflate(R.layout.item_task, taskList, false);
            ((TextView) item.findViewById(R.id.text_task_description)).setText(task.getDescription());
            ((TextView) item.findViewById(R.id.text_task_priority)).setText(getString(R.string.priority_label, task.getPriority()));
            ((TextView) item.findViewById(R.id.text_task_status)).setText(task.isCompleted() ? R.string.status_completed : R.string.status_pending);
            CheckBox completed = item.findViewById(R.id.checkbox_completed);
            completed.setChecked(task.isCompleted());
            completed.setOnCheckedChangeListener((button, checked) -> { task.setCompleted(checked); showTasks(); });
            item.setOnClickListener(view -> openTaskDetails(task.getId()));
            taskList.addView(item);
        }
        emptyState.setVisibility(hasVisibleTasks ? View.GONE : View.VISIBLE);
    }

    private boolean matchesFilter(Task task) {
        return selectedFilter.equals(getString(R.string.filter_all))
                || (selectedFilter.equals(getString(R.string.filter_pending)) && !task.isCompleted())
                || (selectedFilter.equals(getString(R.string.filter_completed)) && task.isCompleted());
    }

    private void openTaskDetails(long taskId) {
        Intent intent = new Intent(this, TaskDetailActivity.class);
        intent.putExtra(EXTRA_TASK_ID, taskId);
        startActivity(intent);
    }
}
