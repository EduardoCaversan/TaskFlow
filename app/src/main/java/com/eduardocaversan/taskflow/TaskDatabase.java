package com.eduardocaversan.taskflow;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Task.class}, version = 1, exportSchema = false)
public abstract class TaskDatabase extends RoomDatabase {
    private static volatile TaskDatabase instance;

    public abstract TaskDao taskDao();

    public static TaskDatabase getInstance(Context context) {
        if (instance == null) {
            synchronized (TaskDatabase.class) {
                if (instance == null) instance = Room.databaseBuilder(context.getApplicationContext(), TaskDatabase.class, "taskflow.db").build();
            }
        }
        return instance;
    }
}
