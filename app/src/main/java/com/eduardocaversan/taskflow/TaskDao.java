package com.eduardocaversan.taskflow;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface TaskDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Task task);

    @Query("SELECT * FROM tasks ORDER BY completed ASC, id DESC")
    List<Task> getAll();

    @Query("SELECT * FROM tasks WHERE id = :id LIMIT 1")
    Task findById(long id);

    @Update
    void update(Task task);

    @Query("UPDATE tasks SET completed = :completed WHERE id = :id")
    void updateStatus(long id, boolean completed);

    @Delete
    void delete(Task task);
}
