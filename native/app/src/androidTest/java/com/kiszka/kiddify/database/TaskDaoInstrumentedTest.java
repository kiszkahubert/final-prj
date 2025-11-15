package com.kiszka.kiddify.database;

import android.content.Context;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.kiszka.kiddify.models.TaskData;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class TaskDaoInstrumentedTest {
    private AppDatabase appDatabase;
    private TaskDao taskDao;

    @Before
    public void setup() {
        Context context = ApplicationProvider.getApplicationContext();
        appDatabase = Room.inMemoryDatabaseBuilder(context, AppDatabase.class)
                .allowMainThreadQueries()
                .build();
        taskDao = appDatabase.taskDao();
    }
    @Test
    public void insert_and_query_tasks() {
        TaskData t = new TaskData(1, "Test title", "Test description", "2025-11-11 12:00:00", "2025-11-11 13:00:00", "PENDING", "", 1);
        taskDao.insertTasks(List.of(t));
        List<TaskData> all = taskDao.getAllTasksSync();
        assertEquals(1, all.size());
        assertEquals("Test title", all.get(0).getTitle());
        assertEquals("Test description", all.get(0).getDescription());
        assertEquals("PENDING", all.get(0).getStatus());
    }
    @After
    public void tearDown() {
        appDatabase.close();
    }
}