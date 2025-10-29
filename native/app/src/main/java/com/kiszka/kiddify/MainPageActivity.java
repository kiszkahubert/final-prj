package com.kiszka.kiddify;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.kiszka.kiddify.adapters.TaskAdapter;
import com.kiszka.kiddify.database.DataManager;
import com.kiszka.kiddify.databinding.ActivityMainPageBinding;
import com.kiszka.kiddify.models.Media;
import com.kiszka.kiddify.models.Message;
import com.kiszka.kiddify.models.Suggestion;
import com.kiszka.kiddify.models.TaskData;

import java.io.IOException;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainPageActivity extends AppCompatActivity {
    private ActivityMainPageBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        this.binding = ActivityMainPageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        String kidName = DataManager.getInstance(this).getKidName();
        binding.welcomeText.setText("Hi, " + kidName + "!");
        String formattedDate = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LocalDate today = LocalDate.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, d MMMM YYYY", new Locale("en","EN"));
            formattedDate = today.format(formatter);
        }
        pullCurrentData();
        binding.dateText.setText(formattedDate);
        TaskAdapter taskAdapter = new TaskAdapter(this, new ArrayList<>());
        binding.tasksRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.tasksRecyclerView.setAdapter(taskAdapter);
        String todayDate = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LocalDate today = LocalDate.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            todayDate = today.format(formatter);
        }
        if (todayDate != null) {
            todayDate = todayDate.trim();
            DataManager.getInstance(this).getTasksForToday(todayDate).observe(this, taskList -> {
                if (taskList != null && !taskList.isEmpty()) {
                    taskAdapter.setTasks(taskList);
                    binding.emptyTasksText.setVisibility(View.GONE);
                    binding.tasksRecyclerView.setVisibility(View.VISIBLE);
                } else {
                    binding.emptyTasksText.setVisibility(View.VISIBLE);
                    binding.tasksRecyclerView.setVisibility(View.GONE);
                }
            });
        }
        binding.addSuggestionCard.setOnClickListener(v -> {
            Intent intent = new Intent(MainPageActivity.this, PropositionActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });
        binding.chatCard.setOnClickListener(v -> {
            Intent intent = new Intent(MainPageActivity.this, ChatActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });
        binding.navChat.setOnClickListener(v -> {
            Intent intent = new Intent(MainPageActivity.this, GalleryActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });
        binding.navProfile.setOnClickListener(v -> {
            Intent intent = new Intent(MainPageActivity.this, ProfileActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });
        binding.navCalendar.setOnClickListener(v -> {
            Intent intent = new Intent(MainPageActivity.this, CalendarActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {

            }
        });
    }
    @Override
    protected void onResume() {
        super.onResume();
        pullCurrentData();
    }
    private void pullCurrentData(){
        String token = DataManager.getInstance(this).getToken();
        OkHttpClient client = new OkHttpClient();
        Request requestTasks = new Request.Builder()
                .url("http://10.0.2.2:8080/api/tasks")
                .addHeader("Authorization", "Bearer " + token)
                .build();

        client.newCall(requestTasks).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("API", "Tasks failed: " + e.getMessage());
            }
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String body = response.body().string();
                    Type listType = new TypeToken<List<TaskData>>(){}.getType();
                    List<TaskData> tasks = new Gson().fromJson(body, listType);
                    DataManager.getInstance(MainPageActivity.this).saveTasks(tasks);
                }
            }
        });
    }
}