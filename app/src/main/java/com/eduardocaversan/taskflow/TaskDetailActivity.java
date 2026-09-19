package com.eduardocaversan.taskflow;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

/** Placeholder para visualização e edição de uma tarefa em uma entrega futura. */
public class TaskDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_detail);
        findViewById(R.id.button_back).setOnClickListener(view -> finish());
    }
}
