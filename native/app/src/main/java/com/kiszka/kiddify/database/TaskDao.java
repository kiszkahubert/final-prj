package com.kiszka.kiddify.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.kiszka.kiddify.models.TaskData;

import java.util.List;

@Dao
public interface TaskDao {
    @Query("SELECT * FROM tasks ORDER BY taskEnd")
    LiveData<List<TaskData>> getAllTasks();
    @Query("SELECT * FROM tasks WHERE taskStart LIKE :today || '%' ORDER BY taskEnd")
    LiveData<List<TaskData>> getTasksForToday(String today);
    @Query("SELECT * FROM tasks")
    List<TaskData> getAllTasksSync();
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertTasks(List<TaskData> tasks);
    @Delete
    void deleteTask(TaskData task);
    @Query("UPDATE tasks SET status = :status WHERE taskId = :taskId")
    void updateTaskStatus(int taskId, String status);
}
