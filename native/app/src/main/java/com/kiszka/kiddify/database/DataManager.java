package com.kiszka.kiddify.database;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.lifecycle.LiveData;

import com.kiszka.kiddify.models.Kid;
import com.kiszka.kiddify.models.LoginResponse;
import com.kiszka.kiddify.models.Suggestion;
import com.kiszka.kiddify.models.TaskData;
import java.util.List;
import java.util.concurrent.Executors;

public class DataManager {
    private static final String PREF_NAME = "KiddifyPrefs";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_KID_ID = "kid_id";
    private static final String KEY_KID_NAME = "kid_name";
    private static final String KEY_KID_BIRTH_DATE = "kid_birth_date";
    private static final String KEY_KID_PARENTS = "kid_parents";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private final SharedPreferences sharedPreferences;
    private final TaskDao taskDao;
    private final SuggestionDao suggestionDao;
    private static DataManager instance;

    private DataManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        AppDatabase database = AppDatabase.getDatabase(context);
        taskDao = database.taskDao();
        suggestionDao = database.suggestionDao();
    }
    public static synchronized DataManager getInstance(Context context) {
        if (instance == null) {
            instance = new DataManager(context.getApplicationContext());
        }
        return instance;
    }
    public void saveLoginData(LoginResponse loginResponse) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_TOKEN, loginResponse.getToken());
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        if (loginResponse.getKid() != null) {
            Kid kid = loginResponse.getKid();
            editor.putInt(KEY_KID_ID, kid.getId());
            editor.putString(KEY_KID_NAME, kid.getName());
            editor.putString(KEY_KID_BIRTH_DATE, kid.getBirthDate());
            if (kid.getParents() != null && !kid.getParents().isEmpty()) {
                StringBuilder parentsStr = new StringBuilder();
                for (int i = 0; i < kid.getParents().size(); i++) {
                    if (i > 0) parentsStr.append(",");
                    parentsStr.append(kid.getParents().get(i));
                }
                editor.putString(KEY_KID_PARENTS, parentsStr.toString());
            }
        }
        editor.apply();
        if (loginResponse.getTasks() != null && !loginResponse.getTasks().isEmpty()) {
            saveTasks(loginResponse.getTasks());
        }
    }
    public String getToken() {
        return sharedPreferences.getString(KEY_TOKEN, null);
    }
    public int getKidId() {
        return sharedPreferences.getInt(KEY_KID_ID, -1);
    }
    public String getKidName() {
        return sharedPreferences.getString(KEY_KID_NAME, "");
    }
    public String getKidBirthDate() {
        return sharedPreferences.getString(KEY_KID_BIRTH_DATE, "");
    }
    public String getKidParents() {
        return sharedPreferences.getString(KEY_KID_PARENTS, "");
    }
    public boolean isLoggedIn() {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }
    public void logout() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
        Executors.newSingleThreadExecutor().execute(taskDao::deleteAllTasks);
    }

    public void saveTasks(List<TaskData> tasks) {
        Executors.newSingleThreadExecutor().execute(() -> {
            taskDao.insertTasks(tasks);
        });
    }
    public void saveTask(TaskData task) {
        Executors.newSingleThreadExecutor().execute(() -> {
            taskDao.insertTask(task);
        });
    }
    public LiveData<List<TaskData>> getAllTasks() {
        return taskDao.getAllTasks();
    }
    public List<TaskData> getAllTasksSync() {
        return taskDao.getAllTasksSync();
    }
    public void updateTaskStatus(int taskId, String status) {
        Executors.newSingleThreadExecutor().execute(() -> {
            taskDao.updateTaskStatus(taskId, status);
        });
    }
    public void updateTask(TaskData task) {
        Executors.newSingleThreadExecutor().execute(() -> {
            taskDao.updateTask(task);
        });
    }
    public void deleteTask(TaskData task) {
        Executors.newSingleThreadExecutor().execute(() -> {
            taskDao.deleteTask(task);
        });
    }
    public TaskData getTaskById(int taskId) {
        return taskDao.getTaskById(taskId);
    }
    public List<TaskData> getTasksByStatus(String status) {
        return taskDao.getTasksByStatus(status);
    }
    public void saveSuggestions(List<Suggestion> suggestions) {
        Executors.newSingleThreadExecutor().execute(() -> {
            for (Suggestion s : suggestions) {
                if (suggestionDao.getSuggestionById(s.getId()) == null) {
                    suggestionDao.insertSuggestion(s);
                } else {
                    suggestionDao.insertSuggestion(s);
                }
            }
        });
    }

    public void saveSuggestion(Suggestion suggestion) {
        Executors.newSingleThreadExecutor().execute(() -> {
            suggestionDao.insertSuggestion(suggestion);
        });
    }

    public void deleteSuggestion(Suggestion suggestion) {
        Executors.newSingleThreadExecutor().execute(() -> {
            suggestionDao.deleteSuggestion(suggestion);
        });
    }


    public LiveData<List<Suggestion>> getAllSuggestions() {
        return suggestionDao.getAllSuggestions();
    }
}