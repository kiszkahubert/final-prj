package com.kiszka.kiddify;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.kiszka.kiddify.adapters.TaskAdapter;
import com.kiszka.kiddify.database.DataManager;
import com.kiszka.kiddify.databinding.ActivityMainPageBinding;
import com.kiszka.kiddify.models.Media;
import com.kiszka.kiddify.models.Suggestion;
import com.kiszka.kiddify.models.TaskData;

import java.io.IOException;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

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
        binding.welcomeText.setText("Cześć, " + kidName + "!");
        String formattedDate = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LocalDate today = LocalDate.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, d MMMM", new Locale("pl","PL"));
            formattedDate = today.format(formatter);
        }
        pullCurrentData();
        binding.dateText.setText(formattedDate);
        TaskAdapter taskAdapter = new TaskAdapter(this, new ArrayList<>());
        binding.tasksRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.tasksRecyclerView.setAdapter(taskAdapter);
        DataManager.getInstance(this).getAllTasks().observe(this, taskList -> {
            if (taskList != null && !taskList.isEmpty()) {
                taskAdapter.setTasks(taskList);
                binding.emptyTasksText.setVisibility(View.GONE);
                binding.tasksRecyclerView.setVisibility(View.VISIBLE);
            } else {
                binding.emptyTasksText.setVisibility(View.VISIBLE);
                binding.tasksRecyclerView.setVisibility(View.GONE);
            }
        });
        binding.addSuggestionCard.setOnClickListener(v -> {
            startActivity(new Intent(MainPageActivity.this, PropositionActivity.class));
        });
        binding.galleryCard.setOnClickListener(v -> {
            startActivity(new Intent(MainPageActivity.this, GalleryActivity.class));
        });
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
        Request requestSuggestions = new Request.Builder()
                .url("http://10.0.2.2:8080/api/suggestions")
                .addHeader("Authorization", "Bearer " + token)
                .build();
        client.newCall(requestSuggestions).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("API", "Suggestions failed: " + e.getMessage());
            }
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String body = response.body().string();
                    Type listType = new TypeToken<List<Suggestion>>(){}.getType();
                    List<Suggestion> suggestions = new Gson().fromJson(body, listType);
                    DataManager.getInstance(MainPageActivity.this).saveSuggestions(suggestions);
                }
            }
        });
        Request requestMedia = new Request.Builder()
                .url("http://10.0.2.2:8080/api/media/kid/all")
                .addHeader("Authorization", "Bearer " + token)
                .build();
        client.newCall(requestMedia).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("API", "Media failed: " + e.getMessage());
            }
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String body = response.body().string();
                    Type listType = new TypeToken<List<Media>>(){}.getType();
                    List<Media> mediaList = new Gson().fromJson(body, listType);
                    DataManager.getInstance(MainPageActivity.this).saveMediaList(mediaList);
                    Log.d("API", "Media saved: " + mediaList.size());
                }
            }
        });
    }
}