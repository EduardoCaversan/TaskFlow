package com.eduardocaversan.taskflow;

import java.util.List;

/** Regras compartilhadas pela interface e pela fronteira de persistência. */
public final class TaskRules {
    public static final String LOW = "Baixa";
    public static final String MEDIUM = "Média";
    public static final String HIGH = "Alta";
    public enum Filter { ALL, PENDING, COMPLETED }

    private TaskRules() { }

    public static String normalizeDescription(String description) {
        if (description == null) return "";
        String value = description.trim();
        int start = 0, end = value.length();
        while (start < end && isSpace(value.charAt(start))) start++;
        while (end > start && isSpace(value.charAt(end - 1))) end--;
        return value.substring(start, end);
    }

    private static boolean isSpace(char value) {
        return Character.isWhitespace(value) || Character.isSpaceChar(value);
    }

    public static boolean isValidPriority(String priority) {
        return LOW.equals(priority) || MEDIUM.equals(priority) || HIGH.equals(priority);
    }

    public static boolean matches(Task task, Filter filter) {
        return filter == Filter.ALL || (filter == Filter.COMPLETED) == task.isCompleted();
    }

    public static int countPending(List<Task> tasks) {
        int count = 0;
        for (Task task : tasks) if (!task.isCompleted()) count++;
        return count;
    }

    public static Task validatedCopy(Task task) {
        String description = normalizeDescription(task.getDescription());
        if (description.isEmpty() || !isValidPriority(task.getPriority())) {
            throw new IllegalArgumentException("Invalid task description or priority");
        }
        Task copy = new Task(description, task.getPriority());
        copy.setId(task.getId());
        copy.setCompleted(task.isCompleted());
        return copy;
    }
}
