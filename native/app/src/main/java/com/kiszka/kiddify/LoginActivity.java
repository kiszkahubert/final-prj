package com.kiszka.kiddify;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.gson.Gson;
import com.kiszka.kiddify.database.DataManager;
import com.kiszka.kiddify.databinding.ActivityLoginBinding;
import com.kiszka.kiddify.models.LoginResponse;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity {
    private ActivityLoginBinding binding;
    private final OkHttpClient client = new OkHttpClient();
    private DataManager dataManager;
    private Gson gson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        this.binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        dataManager = DataManager.getInstance(this);
        gson = new Gson();
        binding.btnLogin.setOnClickListener(v -> {
            String pin = binding.inputPin.getText().toString();
            if(pin.length() != 8){
                binding.inputPin.setError("PIN must be exactly 8 characters");
                return;
            }
            sendPinToServer(pin);
        });
    }
    private void sendPinToServer(String pin){
        String url = "http://192.168.100.206:8080/auth/pin";
        RequestBody body = RequestBody.create(pin, MediaType.get("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(()->{
                    binding.inputPin.setError("Failed to connect to server");
                    System.out.println(e.getMessage());
                });
            }
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                final String responseBody = response.body().string();
                runOnUiThread(() -> {
                    if(response.isSuccessful()){
                        LoginResponse loginResponse = gson.fromJson(responseBody, LoginResponse.class);
                        dataManager.saveLoginData(loginResponse);
                        Intent intent = new Intent(LoginActivity.this, MainPageActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        binding.inputPin.setError("Invalid PIN");
                    }
                });
            }
        });
    }

}