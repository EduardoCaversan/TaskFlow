package com.eduardocaversan.taskflow;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.runner.lifecycle.ActivityLifecycleCallback;
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import androidx.test.runner.lifecycle.Stage;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onIdle;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.assertion.ViewAssertions.*;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class TaskFlowUiTest {
    private Context context;
    private TaskDao dao;
    private RepositoryIdlingResource idle;
    private ActivityScenario<MainActivity> scenario;
    private String prefix;
    private String previousTheme;
    private int initialPending, initialCompleted;

    @Before public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        dao = TaskDatabase.getInstance(context).taskDao();
        prefix = "UI_TEST_" + System.nanoTime();
        previousTheme = ThemePreferences.getThemeMode(context);
        initialPending = TaskRules.countPending(dao.getAll());
        initialCompleted = dao.getAll().size() - initialPending;
        idle = new RepositoryIdlingResource(TaskRepository.getInstance(context));
        IdlingRegistry.getInstance().register(idle);
        scenario = ActivityScenario.launch(MainActivity.class);
    }
    @After public void tearDown() {
        onIdle();
        scenario.close();
        IdlingRegistry.getInstance().unregister(idle);
        idle.close();
        for (Task task : dao.getAll()) if (task.getDescription().startsWith(prefix)) dao.delete(task);
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> ThemePreferences.saveThemeMode(context, previousTheme));
    }
    private void createTask() {
        onView(withId(R.id.button_add_task)).perform(click());
        onView(withId(R.id.input_description)).perform(replaceText("  " + prefix + "  "), closeSoftKeyboard());
        onView(withId(R.id.spinner_priority)).perform(scrollTo(), click());
        onData(is("Alta")).perform(click());
        onView(withId(R.id.button_save)).perform(scrollTo(), click());
        onView(withText(prefix)).perform(scrollTo()).check(matches(isDisplayed()));
    }
    private Matcher<View> checkbox(String description) {
        return allOf(withId(R.id.checkbox_completed), isDescendantOfA(allOf(withId(R.id.task_card), hasDescendant(withText(description)))));
    }

    @Test public void completeCrudFiltersCountersAndDeleteConfirmation() {
        createTask();
        onView(withId(R.id.text_pending_count)).check(matches(withText(String.valueOf(initialPending + 1))));
        onView(withText(prefix)).perform(click());
        onView(withId(R.id.input_description)).check(matches(withText(prefix)));
        String edited = prefix + " editada";
        onView(withId(R.id.input_description)).perform(replaceText(edited), closeSoftKeyboard());
        onView(withId(R.id.spinner_priority)).perform(scrollTo(), click());
        onData(is("Média")).perform(click());
        onView(withId(R.id.button_save)).perform(scrollTo(), click());
        onView(withText(edited)).perform(scrollTo()).check(matches(isDisplayed()));
        onView(checkbox(edited)).perform(click());
        onView(withId(R.id.text_completed_count)).check(matches(withText(String.valueOf(initialCompleted + 1))));
        onView(withId(R.id.chip_pending)).perform(scrollTo(), click());
        onView(withText(edited)).check(doesNotExist());
        onView(withId(R.id.text_completed_count)).check(matches(withText(String.valueOf(initialCompleted + 1))));
        scenario.recreate();
        onView(withId(R.id.chip_pending)).check(matches(isChecked()));
        onView(withText(edited)).check(doesNotExist());
        onView(withId(R.id.chip_completed)).perform(scrollTo(), click());
        onView(withText(edited)).perform(scrollTo()).check(matches(isDisplayed()));
        onView(checkbox(edited)).perform(click());
        onView(withText(edited)).check(doesNotExist());
        onView(withId(R.id.chip_all)).perform(scrollTo(), click());
        onView(withText(edited)).perform(scrollTo(), click());
        onView(withId(R.id.button_delete)).perform(scrollTo(), click());
        onView(withId(android.R.id.button2)).perform(click());
        onView(withId(R.id.input_description)).check(matches(withText(edited)));
        onView(withId(R.id.button_delete)).perform(scrollTo(), click());
        onView(withId(android.R.id.button1)).perform(click());
        onView(withText(edited)).check(doesNotExist());
        onView(withId(R.id.text_pending_count)).check(matches(withText(String.valueOf(initialPending))));
        onView(withId(R.id.text_completed_count)).check(matches(withText(String.valueOf(initialCompleted))));
    }

    @Test public void whitespaceRejectedAndDoubleTapCreatesOneRecord() {
        onView(withId(R.id.button_add_task)).perform(click());
        onView(withId(R.id.input_description)).perform(replaceText("   "), closeSoftKeyboard());
        onView(withId(R.id.button_save)).perform(scrollTo(), click());
        onView(withText(R.string.description_required)).check(matches(isDisplayed()));
        onView(withId(R.id.input_description)).perform(replaceText(prefix), closeSoftKeyboard());
        onView(withId(R.id.button_save)).perform(scrollTo(), new ViewAction() {
            @Override public Matcher<View> getConstraints() { return isEnabled(); }
            @Override public String getDescription() { return "Dois envios no mesmo ciclo de UI"; }
            @Override public void perform(UiController controller, View view) { view.performClick(); view.performClick(); }
        });
        onView(withText(prefix)).perform(scrollTo()).check(matches(isDisplayed()));
        int count = 0;
        for (Task task : dao.getAll()) if (prefix.equals(task.getDescription())) count++;
        assertEquals(1, count);
    }

    @Test public void editDraftSurvivesActivityRecreationAndStatusSaves() {
        Task task = new Task(prefix, "Baixa"); task.setId(dao.insert(task));
        Intent intent = new Intent(context, TaskDetailActivity.class).putExtra(MainActivity.EXTRA_TASK_ID, task.getId());
        try (ActivityScenario<TaskDetailActivity> detail = ActivityScenario.launch(intent)) {
            onView(withId(R.id.input_description)).perform(replaceText(prefix + " rascunho"), closeSoftKeyboard());
            onView(withId(R.id.spinner_priority)).perform(scrollTo(), click());
            onData(is("Alta")).perform(click());
            onView(withId(R.id.spinner_status)).perform(scrollTo(), click());
            onData(is("Concluída")).perform(click());
            detail.recreate();
            onView(withId(R.id.input_description)).check(matches(withText(prefix + " rascunho")));
            onView(withId(R.id.spinner_priority)).check(matches(withSpinnerText("Alta")));
            onView(withId(R.id.spinner_status)).check(matches(withSpinnerText("Concluída")));
            onView(withId(R.id.button_save)).perform(scrollTo(), click());
            onIdle();
            assertEquals(prefix + " rascunho", dao.findById(task.getId()).getDescription());
            assertTrue(dao.findById(task.getId()).isCompleted());
        }
    }

    @Test public void themesPersistAndKeepFilterAndTasks() {
        createTask();
        onView(withId(R.id.chip_pending)).perform(scrollTo(), click());
        for (String mode : new String[]{"Escuro", "Claro", "Usar tema do sistema"}) {
            onView(withId(R.id.button_theme)).perform(scrollTo(), click());
            onView(withText(mode)).perform(click());
            onView(withId(R.id.chip_pending)).check(matches(isChecked()));
            onView(withText(prefix)).perform(scrollTo()).check(matches(isDisplayed()));
            assertEquals(mode.equals("Escuro") ? "dark" : mode.equals("Claro") ? "light" : "system", ThemePreferences.getThemeMode(context));
        }
    }

    @Test public void longTextCanBeSavedWithKeyboardOpen() {
        String description = prefix + " " + String.join(" ", java.util.Collections.nCopies(40, "Texto longo para verificar rolagem."));
        Task task = new Task(description, "Média"); task.setId(dao.insert(task));
        Intent intent = new Intent(context, TaskDetailActivity.class).putExtra(MainActivity.EXTRA_TASK_ID, task.getId());
        try (ActivityScenario<TaskDetailActivity> detail = ActivityScenario.launch(intent)) {
            onView(withId(R.id.input_description)).perform(scrollTo(), click());
            onView(isRoot()).perform(new ViewAction() {
                @Override public Matcher<View> getConstraints() { return isRoot(); }
                @Override public String getDescription() { return "Aguardar teclado e redimensionamento da janela"; }
                @Override public void perform(UiController controller, View view) {
                    long deadline = android.os.SystemClock.uptimeMillis() + 5000;
                    do {
                        androidx.core.view.WindowInsetsCompat insets = androidx.core.view.ViewCompat.getRootWindowInsets(view);
                        if (insets != null && insets.isVisible(androidx.core.view.WindowInsetsCompat.Type.ime())) {
                            controller.loopMainThreadUntilIdle();
                            return;
                        }
                        controller.loopMainThreadForAtLeast(50);
                    } while (android.os.SystemClock.uptimeMillis() < deadline);
                    throw new AssertionError("Teclado não abriu");
                }
            });
            // Não fecha o teclado: salvar deve permanecer acessível pela rolagem.
            onView(withId(R.id.button_save)).perform(scrollTo(), click());
            onIdle();
            assertEquals(description, dao.findById(task.getId()).getDescription());
        }
    }

    @Test public void missingIdFinishesSafely() throws Exception {
        CountDownLatch destroyed = new CountDownLatch(1);
        ActivityLifecycleCallback callback = (activity, stage) -> {
            if (activity instanceof TaskDetailActivity && stage == Stage.DESTROYED) destroyed.countDown();
        };
        ActivityLifecycleMonitorRegistry.getInstance().addLifecycleCallback(callback);
        Intent intent = new Intent(context, TaskDetailActivity.class).putExtra(MainActivity.EXTRA_TASK_ID, -1L);
        try (ActivityScenario<TaskDetailActivity> detail = ActivityScenario.launch(intent)) {
            onIdle();
            assertTrue("Tela ausente deve encerrar", destroyed.await(10, TimeUnit.SECONDS));
            assertEquals(androidx.lifecycle.Lifecycle.State.DESTROYED, detail.getState());
        } finally {
            ActivityLifecycleMonitorRegistry.getInstance().removeLifecycleCallback(callback);
        }
    }
}
