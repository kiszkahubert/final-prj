package com.kiszka.kiddify;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.kiszka.kiddify.adapters.TaskSuggestionsAdapter;
import com.kiszka.kiddify.database.DataManager;
import com.kiszka.kiddify.databinding.ActivityPropositionBinding;
import com.kiszka.kiddify.models.Suggestion;

import java.io.IOException;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PropositionActivity extends AppCompatActivity {
    private static final String DISPLAY_DATE_TIME_PATTERN = "dd.MM.yyyy, HH:mm";
    private ActivityPropositionBinding binding;
    private TaskSuggestionsAdapter adapter;
    private final OkHttpClient client = new OkHttpClient();
    private final Gson gson = new Gson();

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
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", new Locale("en","EN"));
            formattedDate = LocalDate.now().format(formatter);
        }
        binding.tvTitle.setText("Task's Suggestions");
        binding.tvCurrentDate.setText(formattedDate);
        binding.btnBack.setOnClickListener(v -> finish());
        setSendButtonEnabled(false);
        binding.etActivityTitle.addTextChangedListener(new SimpleTextWatcher(this::checkFormFields));
        binding.etStartDateTime.addTextChangedListener(new SimpleTextWatcher(this::checkFormFields));
        binding.etEndDateTime.addTextChangedListener(new SimpleTextWatcher(this::checkFormFields));
        binding.etNotes.addTextChangedListener(new SimpleTextWatcher(this::checkFormFields));
        dataSetup();
    }
    private void setSendButtonEnabled(boolean enabled) {
        binding.btnSendSuggestion.setEnabled(enabled);
        binding.btnSendSuggestion.setTextColor(getResources().getColor(android.R.color.white));
        if (enabled) {
            binding.btnSendSuggestion.setBackgroundResource(R.drawable.rounded_button_blue);
        } else {
            binding.btnSendSuggestion.setBackgroundResource(R.drawable.rounded_button_gray);
        }
    }
    private void checkFormFields() {
        String title = binding.etActivityTitle.getText().toString().trim();
        String startDateTime = binding.etStartDateTime.getText().toString().trim();
        String endDateTime = binding.etEndDateTime.getText().toString().trim();
        String notes = binding.etNotes.getText().toString().trim();
        boolean allFilled = !title.isEmpty() && !startDateTime.isEmpty() && !endDateTime.isEmpty() && !notes.isEmpty();
        setSendButtonEnabled(allFilled);
    }
    private void validateEndDateAfterStartDate() {
        String startDateTimeStr = binding.etStartDateTime.getText().toString().trim();
        String endDateTimeStr = binding.etEndDateTime.getText().toString().trim();
        if (!startDateTimeStr.isEmpty() && !endDateTimeStr.isEmpty()) {
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat(DISPLAY_DATE_TIME_PATTERN, Locale.getDefault());
                Date startDate = inputFormat.parse(startDateTimeStr);
                Date endDate = inputFormat.parse(endDateTimeStr);
                long diffInMillis = endDate.getTime() - startDate.getTime();
                long diffInMinutes = diffInMillis / (60 * 1000);
                if (endDate.before(startDate) || diffInMinutes < 1) {
                    binding.etEndDateTime.setText("");
                    Toast.makeText(this, "End date has been cleared - please select a date later than start date", Toast.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    private static class SimpleTextWatcher implements android.text.TextWatcher {
        private final Runnable callback;
        SimpleTextWatcher(Runnable callback) { this.callback = callback; }
        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override public void onTextChanged(CharSequence s, int start, int before, int count) { callback.run(); }
        @Override public void afterTextChanged(android.text.Editable s) {}
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
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() ->
                    Toast.makeText(PropositionActivity.this, "Failed to delete", Toast.LENGTH_SHORT).show()
                );
            }
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response){
                runOnUiThread(() -> {
                    if (response.isSuccessful()) {
                        Toast.makeText(PropositionActivity.this, "Suggestion deleted successfully ", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(PropositionActivity.this, "Deletion rejected", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
    private void setupClickListeners() {
        binding.etStartDateTime.setOnClickListener(v -> {
            hideKeyboardAndClearFocus();
            showDateTimePicker(true);
        });
        binding.etEndDateTime.setOnClickListener(v -> {
            hideKeyboardAndClearFocus();
            showDateTimePicker(false);
        });
        binding.btnSendSuggestion.setOnClickListener(v -> {
            if (binding.btnSendSuggestion.isEnabled()) {
                createNewSuggestion();
            }
        });
    }
    private void hideKeyboardAndClearFocus() {
        View current = getCurrentFocus();
        if (current != null) {
            current.clearFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(current.getWindowToken(), 0);
            }
        }
    }
    private void showDateTimePicker(boolean isStart) {
        Calendar tempCal = Calendar.getInstance();
        Calendar now = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year, month, day) -> {
            tempCal.set(year, month, day);
            new TimePickerDialog(this, (timeView, hour, minute) -> {
                tempCal.set(Calendar.HOUR_OF_DAY, hour);
                tempCal.set(Calendar.MINUTE, minute);
                if (tempCal.before(now)) {
                    Toast.makeText(this, "You cannot select a date from the past", Toast.LENGTH_SHORT).show();
                    return;
                }
                SimpleDateFormat dtFormat = new SimpleDateFormat(DISPLAY_DATE_TIME_PATTERN, Locale.getDefault());
                if (isStart) {
                    binding.etStartDateTime.setText(dtFormat.format(tempCal.getTime()));
                    validateEndDateAfterStartDate();
                } else {
                    if (!binding.etStartDateTime.getText().toString().trim().isEmpty()) {
                        try {
                            SimpleDateFormat inputFormat = new SimpleDateFormat(DISPLAY_DATE_TIME_PATTERN, Locale.getDefault());
                            Date startDate = inputFormat.parse(binding.etStartDateTime.getText().toString().trim());
                            long diffInMillis = tempCal.getTime().getTime() - startDate.getTime();
                            long diffInMinutes = diffInMillis / (60 * 1000);
                            if (tempCal.getTime().before(startDate) || diffInMinutes < 1) {
                                Toast.makeText(this, "End date cannot be earlier than start date", Toast.LENGTH_LONG).show();
                                return;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    binding.etEndDateTime.setText(dtFormat.format(tempCal.getTime()));
                }
                checkFormFields();
            }, tempCal.get(Calendar.HOUR_OF_DAY), tempCal.get(Calendar.MINUTE), true).show();
        }, tempCal.get(Calendar.YEAR), tempCal.get(Calendar.MONTH), tempCal.get(Calendar.DAY_OF_MONTH));
        
        datePickerDialog.getDatePicker().setMinDate(now.getTimeInMillis());
        datePickerDialog.show();
    }
    private void createNewSuggestion() {
        String title = binding.etActivityTitle.getText().toString().trim();
        String startDateTime = binding.etStartDateTime.getText().toString().trim();
        String endDateTime = binding.etEndDateTime.getText().toString().trim();
        String note = binding.etNotes.getText().toString().trim();
        if (title.isEmpty() || startDateTime.isEmpty() || endDateTime.isEmpty()) {
            Toast.makeText(this, "All fields must be filled in", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat(DISPLAY_DATE_TIME_PATTERN, Locale.getDefault());
            inputFormat.setLenient(false);
            SimpleDateFormat apiFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
            Date startDate = inputFormat.parse(startDateTime);
            Date endDate = inputFormat.parse(endDateTime);
            Calendar now = Calendar.getInstance();
            if (startDate.before(now.getTime())) {
                Toast.makeText(this, "Start date cannot be in the past", Toast.LENGTH_SHORT).show();
                return;
            }
            long diffInMillis = endDate.getTime() - startDate.getTime();
            long diffInMinutes = diffInMillis / (60 * 1000);
            if (endDate.before(startDate) || diffInMinutes < 1) {
                Toast.makeText(this, "End date cannot be earlier than start date", Toast.LENGTH_SHORT).show();
                return;
            }
            String startDateTimeStr = apiFormat.format(startDate);
            String endDateTimeStr = apiFormat.format(endDate);
            int kidId = DataManager.getInstance(this).getKidId();
            String createdAt = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                createdAt = OffsetDateTime.now().toString();
            }
            Suggestion newSuggestion = new Suggestion(
                    title, note, startDateTimeStr, endDateTimeStr, "PENDING", createdAt, kidId
            );
            DataManager.getInstance(this).saveSuggestion(newSuggestion);
            clearForm();
            sendSuggestionToServer(newSuggestion);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error processing date", Toast.LENGTH_SHORT).show();
        }
    }
    private void clearForm() {
        binding.etActivityTitle.setText("");
        binding.etStartDateTime.setText("");
        binding.etEndDateTime.setText("");
        binding.etNotes.setText("");
    }
    private void updatePendingCount() {
        int count = adapter.getItemCount();
        binding.tvPendingCount.setText("Pending Suggestions (" + count + ")");
    }
    private void observeSuggestions() {
        DataManager.getInstance(this).getAllSuggestions().observe(this, suggestions -> {
            List<Suggestion> pendingSuggestions = new java.util.ArrayList<>();
            if (suggestions != null) {
                for (Suggestion s : suggestions) {
                    if ("PENDING".equalsIgnoreCase(s.getStatus())) {
                        pendingSuggestions.add(s);
                    }
                }
            }
            adapter.setSuggestions(pendingSuggestions);
            updatePendingCount();
        });
    }
    private void sendSuggestionToServer(Suggestion suggestion) {
        com.google.gson.JsonObject payload = new com.google.gson.JsonObject();
        payload.addProperty("title", suggestion.getTitle());
        payload.addProperty("description", suggestion.getDescription());
        payload.addProperty("proposedStart", suggestion.getProposedStart());
        payload.addProperty("proposedEnd", suggestion.getProposedEnd());
        String json = gson.toJson(payload);
        String token = DataManager.getInstance(this).getToken();
        RequestBody body = RequestBody.create(json, MediaType.get("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url("http://10.0.2.2:8080/api/suggestions/create")
                .addHeader("Authorization", "Bearer " + token)
                .post(body)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> Toast.makeText(PropositionActivity.this, "Failed to send suggestion", Toast.LENGTH_SHORT).show());
            }
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                runOnUiThread(() -> {
                    if (response.isSuccessful()) {
                        Toast.makeText(PropositionActivity.this, "Suggestion added successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(PropositionActivity.this, "Failed to add a suggestion", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
    private void dataSetup() {
        String token = DataManager.getInstance(this).getToken();
        OkHttpClient client = new OkHttpClient();
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
                    Type listType = new TypeToken<List<Suggestion>>() {}.getType();
                    List<Suggestion> suggestions = new Gson().fromJson(body, listType);
                    DataManager.getInstance(PropositionActivity.this).saveSuggestions(suggestions);
                }
            }
        });
    }
}