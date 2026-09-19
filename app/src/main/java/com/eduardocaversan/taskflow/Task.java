package com.eduardocaversan.taskflow;

public class Task {
    private final long id;
    private String description;
    private String priority;
    private boolean completed;
    public Task(long id, String description, String priority) { this.id = id; this.description = description; this.priority = priority; }
    public long getId() { return id; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }
}
