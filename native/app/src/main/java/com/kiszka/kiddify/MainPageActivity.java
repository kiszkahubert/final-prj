package com.kiszka.kiddify;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.kiszka.kiddify.adapters.TaskAdapter;
import com.kiszka.kiddify.database.DataManager;
import com.kiszka.kiddify.databinding.ActivityMainPageBinding;
import com.kiszka.kiddify.models.TaskData;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

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
    }

}