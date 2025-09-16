package com.kiszka.kiddify;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.gson.Gson;
import com.kiszka.kiddify.adapters.TaskSuggestionsAdapter;
import com.kiszka.kiddify.database.DataManager;
import com.kiszka.kiddify.databinding.ActivityPropositionBinding;
import com.kiszka.kiddify.models.Suggestion;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PropositionActivity extends AppCompatActivity {
    private ActivityPropositionBinding binding;
    private TaskSuggestionsAdapter adapter;
    private Calendar calendar = Calendar.getInstance();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE", Locale.getDefault());
    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    private OkHttpClient client = new OkHttpClient();
    private Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityPropositionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initRecyclerView();
        setupClickListeners();
        observeSuggestions();
        String formattedDate = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LocalDate today = LocalDate.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, d MMMM", new Locale("pl","PL"));
            formattedDate = today.format(formatter);
        }
        binding.tvTitle.setText("Propozycja zadań");
        binding.tvCurrentDate.setText(formattedDate);
        binding.btnBack.setOnClickListener(v -> finish());

    }
    private void initRecyclerView() {
        adapter = new TaskSuggestionsAdapter(this, position -> {
            Suggestion suggestion = adapter.getSuggestionAt(position);
            DataManager.getInstance(this).deleteSuggestion(suggestion);
            deleteSuggestionFromServer(suggestion);
            updatePendingCount();
        });
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(adapter);
    }
    private void deleteSuggestionFromServer(Suggestion suggestion) {
        String token = DataManager.getInstance(this).getToken();
        String url = "http://10.0.2.2:8080/api/suggestions/" + suggestion.getId();
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + token)
                .delete()
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(PropositionActivity.this, "Failed to delete on server", Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onResponse(Call call, Response response){
                runOnUiThread(() -> {
                    if (response.isSuccessful()) {
                        Toast.makeText(PropositionActivity.this, "Deleted on server", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(PropositionActivity.this, "Server rejected delete", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
    private void setupClickListeners() {
        binding.etPickDate.setOnClickListener(v -> showDatePicker());
        binding.etStartTime.setOnClickListener(v -> showTimePicker(true));
        binding.etEndTime.setOnClickListener(v -> showTimePicker(false));
        binding.btnSendSuggestion.setOnClickListener(v -> createNewSuggestion());
    }
    private void showDatePicker() {
        new DatePickerDialog(this, (view, year, month, day) -> {
            calendar.set(year, month, day);
            binding.etPickDate.setText(dateFormat.format(calendar.getTime()));
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)).show();
    }
    private void showTimePicker(boolean isStartTime) {
        new TimePickerDialog(this, (view, hour, minute) -> {
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
            String timeString = timeFormat.format(calendar.getTime());
            if (isStartTime) binding.etStartTime.setText(timeString);
            else binding.etEndTime.setText(timeString);
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show();
    }
    private void createNewSuggestion() {
        String title = binding.etActivityTitle.getText().toString().trim();
        String startTime = binding.etStartTime.getText().toString().trim();
        String endTime = binding.etEndTime.getText().toString().trim();
        String note = binding.etNotes.getText().toString().trim();
        if (title.isEmpty() || startTime.isEmpty() || endTime.isEmpty()) {
            Toast.makeText(this, "Uzupełnij wszystkie pola", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            String dateInput = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.getTime());
            String startDateTimeStr = dateInput + "T" + startTime + ":00.000+00:00";
            String endDateTimeStr = dateInput + "T" + endTime + ":00.000+00:00";
            int kidId = DataManager.getInstance(this).getKidId();
            String createdAt = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                createdAt = OffsetDateTime.now().toString();
            }
            Suggestion newSuggestion = new Suggestion(
                    title,
                    note,
                    startDateTimeStr,
                    endDateTimeStr,
                    "PENDING",
                    createdAt,
                    kidId
            );
            DataManager.getInstance(this).saveSuggestion(newSuggestion);
            clearForm();
            sendSuggestionToServer(newSuggestion);
            Toast.makeText(this, "Suggestion saved locally and sent to server!", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to parse date/time", Toast.LENGTH_SHORT).show();
        }
    }

    private void clearForm() {
        binding.etActivityTitle.setText("");
        binding.etPickDate.setText("");
        binding.etStartTime.setText("");
        binding.etEndTime.setText("");
        binding.etNotes.setText("");
    }
    private void updatePendingCount() {
        int count = adapter.getItemCount();
        binding.tvPendingCount.setText("Pending Suggestions (" + count + ")");
    }
    private void observeSuggestions() {
        DataManager.getInstance(this).getAllSuggestions().observe(this, suggestions -> {
            adapter.setSuggestions(suggestions);
            updatePendingCount();
        });
    }
    private void sendSuggestionToServer(Suggestion suggestion) {
        String json = gson.toJson(suggestion);
        String token = DataManager.getInstance(this).getToken();
        RequestBody body = RequestBody.create(json, MediaType.get("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url("http://10.0.2.2:8080/api/suggestions/create")
                .addHeader("Authorization", "Bearer " + token)
                .post(body)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(PropositionActivity.this, "Failed to send suggestion", Toast.LENGTH_SHORT).show());
            }
            @Override
            public void onResponse(Call call, Response response) {
                runOnUiThread(() -> {
                    if (response.isSuccessful()) {
                        Toast.makeText(PropositionActivity.this, "Suggestion successfully sent to server", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(PropositionActivity.this, "Server rejected the suggestion", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}