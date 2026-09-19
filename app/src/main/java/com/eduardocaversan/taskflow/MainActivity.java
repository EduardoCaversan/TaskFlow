package com.eduardocaversan.taskflow;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

/** Tela inicial do aplicativo. A lista será conectada à persistência nas próximas entregas. */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.button_add_task).setOnClickListener(view ->
                startActivity(new Intent(this, TaskFormActivity.class)));
    }

    /** Ponto de navegação para quando os itens da lista forem implementados. */
    public void openTaskDetails(View view) {
        startActivity(new Intent(this, TaskDetailActivity.class));
    }
}
