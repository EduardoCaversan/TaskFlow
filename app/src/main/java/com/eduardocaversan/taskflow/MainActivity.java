package com.eduardocaversan.taskflow;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

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
            TextView description = item.findViewById(R.id.text_task_description);
            description.setText(task.getDescription());
            description.setPaintFlags(task.isCompleted()
                    ? description.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG
                    : description.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
            TextView priority = item.findViewById(R.id.text_task_priority);
            priority.setText(getString(R.string.priority_label, task.getPriority()));
            applyPriorityStyle(priority, task.getPriority());
            ((TextView) item.findViewById(R.id.text_task_status)).setText(task.isCompleted() ? R.string.status_completed : R.string.status_pending);
            ((ImageView) item.findViewById(R.id.image_task_status)).setImageResource(task.isCompleted()
                    ? R.drawable.ic_check_circle_24 : R.drawable.ic_pending_24);
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

    private void applyPriorityStyle(TextView priorityView, String priority) {
        int background = R.color.priority_low_container;
        int foreground = R.color.priority_low_on_container;
        if (priority.equals(getString(R.string.priority_high))) {
            background = R.color.priority_high_container;
            foreground = R.color.priority_high_on_container;
        } else if (priority.equals(getString(R.string.priority_medium))) {
            background = R.color.priority_medium_container;
            foreground = R.color.priority_medium_on_container;
        }
        priorityView.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, background)));
        priorityView.setTextColor(ContextCompat.getColor(this, foreground));
    }
}
