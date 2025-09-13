package com.kiszka.kiddify;

import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.kiszka.kiddify.adapters.TaskAdapter;
import com.kiszka.kiddify.database.DataManager;
import com.kiszka.kiddify.databinding.ActivityMainPageBinding;

import java.util.ArrayList;

public class MainPageActivity extends AppCompatActivity {
    private ActivityMainPageBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        this.binding = ActivityMainPageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        String kidName = DataManager.getInstance(this).getKidName();
        binding.welcomeText.setText("Cześć, "+kidName+ "!");
        TaskAdapter taskAdapter = new TaskAdapter(this, new ArrayList<>());
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