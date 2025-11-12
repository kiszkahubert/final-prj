package com.kiszka.kiddify.database;

import android.content.Context;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.kiszka.kiddify.models.Kid;
import com.kiszka.kiddify.models.LoginResponse;
import com.kiszka.kiddify.models.TaskData;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class DataManagerInstrumentedTest {
    @Rule
    public InstantTaskExecutorRule rule = new InstantTaskExecutorRule();
    private Context context;
    private DataManager dataManager;

    @Before
    public void setup() {
        context = ApplicationProvider.getApplicationContext();
        dataManager = DataManager.getInstance(context);
        dataManager.logout();
        try { Thread.sleep(300); } catch (InterruptedException ignored) {}
    }
    @Test
    public void saveLoginData_persists_token_and_tasks() throws InterruptedException {
        Kid kid = new Kid();
        kid.setId(1);
        kid.setName("Ala");
        kid.setBirthDate("2007-01-01");
        kid.setParents(List.of(1));

        TaskData taskData = new TaskData(1, "Test title", "Test description", "2025-11-11 12:00:00", "2025-11-11 13:00:00", "PENDING", "", 1);

        LoginResponse resp = new LoginResponse();
        resp.setToken("alamakota");
        resp.setKid(kid);
        resp.setTasks(List.of(taskData));

        dataManager.saveLoginData(resp);
        Thread.sleep(300);

        assertTrue(dataManager.isLoggedIn());
        assertEquals("alamakota", dataManager.getToken());

        List<TaskData> all = AppDatabase.getDatabase(context).taskDao().getAllTasksSync();
        assertEquals(1, all.size());
        assertEquals("Test title", all.get(0).getTitle());
    }
    @Test
    public void logout_clears_prefs_and_database() throws InterruptedException {
        AppDatabase.getDatabase(context).taskDao().insertTasks(List.of(new TaskData(2, "Title", "Description", "2025-11-11 14:00:00", "2025-11-11 15:00:00", "PENDING", "", 1)));

        dataManager.logout();
        Thread.sleep(300);

        assertFalse(dataManager.isLoggedIn());
        assertNull(dataManager.getToken());
        assertEquals(0, AppDatabase.getDatabase(context).taskDao().getAllTasksSync().size());
    }
}
