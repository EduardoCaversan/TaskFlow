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
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;

import com.google.android.material.chip.ChipGroup;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;
import java.util.Locale;
import java.util.HashSet;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    public static final String EXTRA_TASK_ID = "task_id";
    public static final String EXTRA_FEEDBACK = "feedback";
    private static final String FILTER_STATE = "selected_filter";
    private LinearLayout taskList;
    private View emptyState;
    private TextView emptyTitle, emptyMessage, pendingCount, completedCount;
    private TaskRules.Filter selectedFilter = TaskRules.Filter.ALL;
    private final Set<Long> changingStatus = new HashSet<>();
    private int loadGeneration;
    private boolean navigating;
    private final ActivityResultLauncher<Intent> taskLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                Intent data = result.getData();
                String message = data == null ? null : data.getStringExtra(EXTRA_FEEDBACK);
                if (result.getResultCode() == RESULT_OK && message != null) {
                    Snackbar.make(taskList, message, Snackbar.LENGTH_SHORT).show();
                }
            });

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        taskList = findViewById(R.id.task_list);
        emptyState = findViewById(R.id.empty_state);
        emptyTitle = findViewById(R.id.empty_state_title);
        emptyMessage = findViewById(R.id.empty_state_message);
        pendingCount = findViewById(R.id.text_pending_count);
        completedCount = findViewById(R.id.text_completed_count);
        if (savedInstanceState != null) selectedFilter = TaskRules.Filter.valueOf(
                savedInstanceState.getString(FILTER_STATE, TaskRules.Filter.ALL.name()));
        findViewById(R.id.button_add_task).setOnClickListener(view -> openEditor(new Intent(this, TaskFormActivity.class)));
        MaterialButton themeButton = findViewById(R.id.button_theme);
        updateThemeButton(themeButton);
        themeButton.setOnClickListener(view -> showThemeSelector());
        ChipGroup filters = findViewById(R.id.filter_group);
        filters.check(selectedFilter == TaskRules.Filter.PENDING ? R.id.chip_pending
                : selectedFilter == TaskRules.Filter.COMPLETED ? R.id.chip_completed : R.id.chip_all);
        filters.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            int id = checkedIds.get(0);
            selectedFilter = id == R.id.chip_pending ? TaskRules.Filter.PENDING
                    : id == R.id.chip_completed ? TaskRules.Filter.COMPLETED : TaskRules.Filter.ALL;
            showTasks();
        });
    }

    @Override protected void onResume() { super.onResume(); navigating = false; showTasks(); }
    @Override protected void onSaveInstanceState(Bundle state) {
        state.putString(FILTER_STATE, selectedFilter.name());
        super.onSaveInstanceState(state);
    }
    private void showTasks() {
        int generation = ++loadGeneration;
        TaskRepository.getInstance(this).getAll((tasks, error) -> {
            if (isFinishing() || isDestroyed() || generation != loadGeneration) return;
            if (error != null) {
                Snackbar.make(taskList, R.string.load_failed, Snackbar.LENGTH_INDEFINITE)
                        .setAction(R.string.retry, view -> showTasks()).show();
                return;
            }
            renderTasks(tasks);
        });
    }
    private void renderTasks(List<Task> tasks) {
        if (isFinishing() || isDestroyed()) return;
        int pending = TaskRules.countPending(tasks);
        pendingCount.setText(String.valueOf(pending));
        completedCount.setText(String.valueOf(tasks.size() - pending));
        taskList.removeAllViews(); boolean hasVisibleTasks = false; LayoutInflater inflater = LayoutInflater.from(this);
        for (Task task : tasks) {
            if (!TaskRules.matches(task, selectedFilter)) continue;
            hasVisibleTasks = true; View item = inflater.inflate(R.layout.item_task, taskList, false);
            TextView description = item.findViewById(R.id.text_task_description);
            description.setText(task.getDescription());
            description.setPaintFlags(task.isCompleted() ? description.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG : description.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
            description.setAlpha(task.isCompleted() ? 0.62f : 1f);
            TextView priority = item.findViewById(R.id.text_task_priority);
            String priorityText = task.getPriority() == null ? getString(R.string.priority_required) : task.getPriority();
            priority.setText(priorityText.toUpperCase(Locale.getDefault()));
            applyPriorityStyle(priority, priorityText);
            TextView status = item.findViewById(R.id.text_task_status);
            status.setText(task.isCompleted() ? R.string.status_completed : R.string.status_pending);
            status.setBackgroundResource(task.isCompleted() ? R.drawable.bg_status_completed : R.drawable.bg_status_pending);
            status.setTextColor(ContextCompat.getColor(this, task.isCompleted() ? R.color.status_completed_text : R.color.status_pending_text));
            ((ImageView) item.findViewById(R.id.image_task_status)).setImageResource(task.isCompleted() ? R.drawable.ic_check_circle_24 : R.drawable.ic_pending_24);
            CheckBox completed = item.findViewById(R.id.checkbox_completed);
            completed.setContentDescription(getString(task.isCompleted() ? R.string.mark_pending : R.string.mark_completed));
            completed.setChecked(task.isCompleted());
            completed.setEnabled(!changingStatus.contains(task.getId()));
            completed.setOnCheckedChangeListener((button, checked) -> {
                if (checked == task.isCompleted() || !button.isEnabled()) return;
                button.setEnabled(false);
                changingStatus.add(task.getId());
                TaskRepository.getInstance(this).updateStatus(task.getId(), checked, (unused, error) -> {
                    changingStatus.remove(task.getId());
                    if (isFinishing() || isDestroyed()) return;
                    if (error != null) {
                        button.setChecked(task.isCompleted());
                        button.setEnabled(true);
                        Snackbar.make(taskList, R.string.save_failed, Snackbar.LENGTH_LONG).show();
                    } else {
                        Snackbar.make(taskList, R.string.task_updated, Snackbar.LENGTH_SHORT).show();
                        showTasks();
                    }
                });
            });
            item.setOnClickListener(view -> openTaskDetails(task.getId())); taskList.addView(item);
        }
        emptyState.setVisibility(hasVisibleTasks ? View.GONE : View.VISIBLE);
        if (!hasVisibleTasks) updateEmptyState();
    }
    private void updateEmptyState() {
        if (selectedFilter == TaskRules.Filter.PENDING) { emptyTitle.setText(R.string.empty_pending_title); emptyMessage.setText(R.string.empty_pending_message); }
        else if (selectedFilter == TaskRules.Filter.COMPLETED) { emptyTitle.setText(R.string.empty_completed_title); emptyMessage.setText(R.string.empty_completed_message); }
        else { emptyTitle.setText(R.string.empty_tasks_title); emptyMessage.setText(R.string.empty_tasks_message); }
    }
    private void openTaskDetails(long taskId) {
        if (!changingStatus.contains(taskId)) openEditor(new Intent(this, TaskDetailActivity.class).putExtra(EXTRA_TASK_ID, taskId));
    }
    private void openEditor(Intent intent) {
        if (navigating) return;
        navigating = true;
        taskLauncher.launch(intent);
    }
    private void showThemeSelector() {
        String currentMode = ThemePreferences.getThemeMode(this);
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.theme_selector_title)
                .setSingleChoiceItems(R.array.theme_options, ThemePreferences.toSelection(currentMode), (dialog, which) -> {
                    ThemePreferences.saveThemeMode(this, ThemePreferences.fromSelection(which));
                    dialog.dismiss();
                })
                .show();
    }
    private void updateThemeButton(MaterialButton button) {
        String mode = ThemePreferences.getThemeMode(this);
        if (ThemePreferences.MODE_LIGHT.equals(mode)) {
            button.setIconResource(R.drawable.ic_light_mode_24);
            button.setContentDescription(getString(R.string.theme_current_light));
        } else if (ThemePreferences.MODE_DARK.equals(mode)) {
            button.setIconResource(R.drawable.ic_dark_mode_24);
            button.setContentDescription(getString(R.string.theme_current_dark));
        } else {
            button.setIconResource(R.drawable.ic_brightness_auto_24);
            button.setContentDescription(getString(R.string.theme_current_system));
        }
    }
    private void applyPriorityStyle(TextView priorityView, String priority) {
        int background = R.color.priority_low_container, foreground = R.color.priority_low_on_container;
        if (priority.equals(getString(R.string.priority_high))) { background = R.color.priority_high_container; foreground = R.color.priority_high_on_container; }
        else if (priority.equals(getString(R.string.priority_medium))) { background = R.color.priority_medium_container; foreground = R.color.priority_medium_on_container; }
        priorityView.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, background))); priorityView.setTextColor(ContextCompat.getColor(this, foreground));
    }
}
