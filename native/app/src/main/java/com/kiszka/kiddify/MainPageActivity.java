package com.kiszka.kiddify;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.kiszka.kiddify.databinding.ActivityMainPageBinding;

public class MainPageActivity extends AppCompatActivity {
    private ActivityMainPageBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        this.binding = ActivityMainPageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }
}