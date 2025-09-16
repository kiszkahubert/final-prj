package com.kiszka.kiddify.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.kiszka.kiddify.models.TaskData;

import java.util.List;

@Dao
public interface TaskDao {
    @Query("SELECT * FROM tasks ORDER BY taskStart ASC")
    LiveData<List<TaskData>> getAllTasks();
    @Query("SELECT * FROM tasks")
    List<TaskData> getAllTasksSync();
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertTasks(List<TaskData> tasks);
    @Delete
    void deleteTask(TaskData task);
    @Query("DELETE FROM tasks")
    void deleteAllTasks();
}
