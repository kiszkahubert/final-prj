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

    @Query("SELECT * FROM tasks ORDER BY taskStart ASC")
    List<TaskData> getAllTasksSync();

    @Query("SELECT * FROM tasks WHERE taskId = :taskId")
    TaskData getTaskById(int taskId);

    @Query("SELECT * FROM tasks WHERE status = :status")
    List<TaskData> getTasksByStatus(String status);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertTask(TaskData task);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertTasks(List<TaskData> tasks);

    @Update
    void updateTask(TaskData task);

    @Delete
    void deleteTask(TaskData task);

    @Query("DELETE FROM tasks")
    void deleteAllTasks();

    @Query("UPDATE tasks SET status = :status WHERE taskId = :taskId")
    void updateTaskStatus(int taskId, String status);
}
