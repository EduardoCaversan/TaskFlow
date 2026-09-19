package com.eduardocaversan.taskflow;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

/** Estrutura inicial do formulário; o salvamento definitivo ainda não faz parte desta etapa. */
public class TaskFormActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_form);

        findViewById(R.id.button_cancel).setOnClickListener(view -> finish());
        findViewById(R.id.button_save).setOnClickListener(view -> {
            Toast.makeText(this, R.string.save, Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}
