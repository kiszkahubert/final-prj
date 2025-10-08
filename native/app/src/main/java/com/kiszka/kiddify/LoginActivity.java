package com.kiszka.kiddify;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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
    private final ActivityResultLauncher<Intent> qrScannerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    String qrCodeUrl = result.getData().getStringExtra("QR_CODE_URL");
                    if (qrCodeUrl != null) {
                        Uri uri = Uri.parse(qrCodeUrl);
                        String hash = uri.getQueryParameter("hash");
                        if (hash != null && !hash.isEmpty()) {
                            Toast.makeText(this, "QR Scanned! Logging in...", Toast.LENGTH_SHORT).show();
                            sendQrHashToServer(hash);
                        }
                    }
                }
            });
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
        binding.btnScanQr.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, QrScannerActivity.class);
            qrScannerLauncher.launch(intent);
        });
    }
    private void sendPinToServer(String pin){
        String url = "http://10.0.2.2:8080/auth/pin";
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
    private void sendQrHashToServer(String hash) {
        String url = "http://10.0.2.2:8080/auth/qr?hash=" + hash;
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> Toast.makeText(LoginActivity.this, "Failed to connect to server", Toast.LENGTH_LONG).show());
            }
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                final String responseBody = response.body().string();
                runOnUiThread(() -> {
                    if (response.isSuccessful()) {
                        LoginResponse loginResponse = gson.fromJson(responseBody, LoginResponse.class);
                        dataManager.saveLoginData(loginResponse);
                        Intent intent = new Intent(LoginActivity.this, MainPageActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(LoginActivity.this, "Invalid QR Code", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

}