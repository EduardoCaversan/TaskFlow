package com.eduardocaversan.taskflow;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.chip.ChipGroup;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    public static final String EXTRA_TASK_ID = "task_id";
    public static final String EXTRA_FEEDBACK = "feedback";
    private static final int REQUEST_TASK = 10;
    private LinearLayout taskList;
    private View emptyState;
    private TextView emptyTitle, emptyMessage, pendingCount, completedCount;
    private String selectedFilter;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        taskList = findViewById(R.id.task_list);
        emptyState = findViewById(R.id.empty_state);
        emptyTitle = findViewById(R.id.empty_state_title);
        emptyMessage = findViewById(R.id.empty_state_message);
        pendingCount = findViewById(R.id.text_pending_count);
        completedCount = findViewById(R.id.text_completed_count);
        selectedFilter = getString(R.string.filter_all);
        findViewById(R.id.button_add_task).setOnClickListener(view -> startActivityForResult(new Intent(this, TaskFormActivity.class), REQUEST_TASK));
        ChipGroup filters = findViewById(R.id.filter_group);
        filters.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            int id = checkedIds.get(0);
            selectedFilter = id == R.id.chip_pending ? getString(R.string.filter_pending) : id == R.id.chip_completed ? getString(R.string.filter_completed) : getString(R.string.filter_all);
            showTasks();
        });
    }

    @Override protected void onResume() { super.onResume(); if (taskList != null) showTasks(); }
    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_TASK && resultCode == RESULT_OK) {
            showTasks();
            if (data != null) Snackbar.make(taskList, data.getStringExtra(EXTRA_FEEDBACK), Snackbar.LENGTH_SHORT).show();
        }
    }
    private void showTasks() { TaskRepository.getInstance(this).getAll(this::renderTasks); }
    private void renderTasks(List<Task> tasks) {
        if (isFinishing() || isDestroyed()) return;
        int pending = 0;
        for (Task task : tasks) if (!task.isCompleted()) pending++;
        pendingCount.setText(String.valueOf(pending));
        completedCount.setText(String.valueOf(tasks.size() - pending));
        taskList.removeAllViews(); boolean hasVisibleTasks = false; LayoutInflater inflater = LayoutInflater.from(this);
        for (Task task : tasks) {
            if (!matchesFilter(task)) continue;
            hasVisibleTasks = true; View item = inflater.inflate(R.layout.item_task, taskList, false);
            TextView description = item.findViewById(R.id.text_task_description);
            description.setText(task.getDescription());
            description.setPaintFlags(task.isCompleted() ? description.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG : description.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
            description.setAlpha(task.isCompleted() ? 0.62f : 1f);
            TextView priority = item.findViewById(R.id.text_task_priority); priority.setText(task.getPriority().toUpperCase()); applyPriorityStyle(priority, task.getPriority());
            TextView status = item.findViewById(R.id.text_task_status);
            status.setText(task.isCompleted() ? R.string.status_completed : R.string.status_pending);
            status.setBackgroundResource(task.isCompleted() ? R.drawable.bg_status_completed : R.drawable.bg_status_pending);
            status.setTextColor(ContextCompat.getColor(this, task.isCompleted() ? R.color.status_completed_text : R.color.status_pending_text));
            ((ImageView) item.findViewById(R.id.image_task_status)).setImageResource(task.isCompleted() ? R.drawable.ic_check_circle_24 : R.drawable.ic_pending_24);
            CheckBox completed = item.findViewById(R.id.checkbox_completed);
            completed.setContentDescription(getString(task.isCompleted() ? R.string.mark_pending : R.string.mark_completed));
            completed.setChecked(task.isCompleted());
            completed.setOnCheckedChangeListener((button, checked) -> { if (checked != task.isCompleted()) TaskRepository.getInstance(this).updateStatus(task.getId(), checked, this::showTasks); });
            item.setOnClickListener(view -> openTaskDetails(task.getId())); taskList.addView(item);
        }
        emptyState.setVisibility(hasVisibleTasks ? View.GONE : View.VISIBLE);
        if (!hasVisibleTasks) updateEmptyState();
    }
    private void updateEmptyState() {
        if (selectedFilter.equals(getString(R.string.filter_pending))) { emptyTitle.setText(R.string.empty_pending_title); emptyMessage.setText(R.string.empty_pending_message); }
        else if (selectedFilter.equals(getString(R.string.filter_completed))) { emptyTitle.setText(R.string.empty_completed_title); emptyMessage.setText(R.string.empty_completed_message); }
        else { emptyTitle.setText(R.string.empty_tasks_title); emptyMessage.setText(R.string.empty_tasks_message); }
    }
    private boolean matchesFilter(Task task) { return selectedFilter.equals(getString(R.string.filter_all)) || (selectedFilter.equals(getString(R.string.filter_pending)) && !task.isCompleted()) || (selectedFilter.equals(getString(R.string.filter_completed)) && task.isCompleted()); }
    private void openTaskDetails(long taskId) { Intent intent = new Intent(this, TaskDetailActivity.class); intent.putExtra(EXTRA_TASK_ID, taskId); startActivityForResult(intent, REQUEST_TASK); }
    private void applyPriorityStyle(TextView priorityView, String priority) {
        int background = R.color.priority_low_container, foreground = R.color.priority_low_on_container;
        if (priority.equals(getString(R.string.priority_high))) { background = R.color.priority_high_container; foreground = R.color.priority_high_on_container; }
        else if (priority.equals(getString(R.string.priority_medium))) { background = R.color.priority_medium_container; foreground = R.color.priority_medium_on_container; }
        priorityView.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, background))); priorityView.setTextColor(ContextCompat.getColor(this, foreground));
    }
}
