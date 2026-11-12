package com.eduardocaversan.taskflow;

import org.junit.Test;
import java.util.Arrays;
import java.util.Collections;
import static org.junit.Assert.*;

public class TaskRulesTest {
    @Test public void nullDescriptionBecomesEmpty() {
        assertEquals("", TaskRules.normalizeDescription(null));
    }
    @Test public void rejectsWhitespaceIncludingUnicode() {
        for (String text : new String[]{"", "   ", "\t\r\n", "\u00a0\u2003"}) {
            assertEquals("", TaskRules.normalizeDescription(text));
        }
    }
    @Test public void trimsEdgesAndPreservesInternalText() {
        assertEquals("Estudar  Java\nHoje", TaskRules.normalizeDescription(" \tEstudar  Java\nHoje\u00a0 "));
    }
    @Test public void acceptsOnlySupportedPriorities() {
        for (String priority : new String[]{"Baixa", "Média", "Alta"}) assertTrue(TaskRules.isValidPriority(priority));
        for (String priority : new String[]{null, "", "Urgente", "alta", " Alta "}) assertFalse(TaskRules.isValidPriority(priority));
    }
    @Test public void filtersBothStatuses() {
        Task task = new Task("Teste", TaskRules.LOW);
        for (boolean completed : new boolean[]{false, true}) {
            task.setCompleted(completed);
            assertTrue(TaskRules.matches(task, TaskRules.Filter.ALL));
            assertEquals(!completed, TaskRules.matches(task, TaskRules.Filter.PENDING));
            assertEquals(completed, TaskRules.matches(task, TaskRules.Filter.COMPLETED));
        }
    }
    @Test public void countsGlobalTasksWithoutChangingOrder() {
        Task pending = new Task("Pendente", TaskRules.HIGH);
        Task completed = new Task("Concluída", TaskRules.LOW);
        completed.setCompleted(true);
        assertEquals(1, TaskRules.countPending(Arrays.asList(completed, pending)));
        assertEquals(0, TaskRules.countPending(Collections.emptyList()));
        assertEquals(0, TaskRules.countPending(Collections.singletonList(completed)));
        assertEquals(1, TaskRules.countPending(Collections.singletonList(pending)));
    }
    @Test public void copyPreservesIdentityAndStatusWithoutMutatingInput() {
        Task original = new Task("  Estudar  ", TaskRules.MEDIUM);
        original.setId(42);
        original.setCompleted(true);
        Task copy = TaskRules.validatedCopy(original);
        assertNotSame(original, copy);
        assertEquals(42, copy.getId());
        assertTrue(copy.isCompleted());
        assertEquals("Média", copy.getPriority());
        assertEquals("Estudar", copy.getDescription());
        assertEquals("  Estudar  ", original.getDescription());
    }
    @Test public void invalidDescriptionCannotCrossPersistenceBoundary() {
        assertThrows(IllegalArgumentException.class, () -> TaskRules.validatedCopy(new Task(" \n ", TaskRules.LOW)));
    }
    @Test public void invalidPriorityCannotCrossPersistenceBoundary() {
        assertThrows(IllegalArgumentException.class, () -> TaskRules.validatedCopy(new Task("Texto", null)));
    }
    @Test public void longDescriptionIsPreserved() {
        String text = String.join("", Collections.nCopies(2000, "Descrição longa. ")).trim();
        assertEquals(text, TaskRules.validatedCopy(new Task(text, TaskRules.HIGH)).getDescription());
    }
}
