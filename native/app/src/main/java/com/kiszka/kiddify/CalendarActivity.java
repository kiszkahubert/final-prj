package com.kiszka.kiddify;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.kiszka.kiddify.database.DataManager;
import com.kiszka.kiddify.databinding.ActivityCalendarBinding;
import com.kiszka.kiddify.models.TaskData;
import com.kiszka.kiddify.adapters.SectionedTaskAdapter;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CalendarActivity extends AppCompatActivity {
    private ActivityCalendarBinding binding;
    private DataManager dataManager;
    private SectionedTaskAdapter sectionedAdapter;
    private boolean showingEarlier = false;
    private List<TaskData> cachedUpcoming = new ArrayList<>();
    private List<TaskData> cachedEarlier = new ArrayList<>();
    private List<TaskData> cachedEarlierAll = new ArrayList<>();
    private void sortByStartAsc(java.util.List<TaskData> list) {
        if (list == null || list.size() <= 1) return;
        list.sort((a, b) -> {
            String sa = a != null && a.getTaskStart() != null ? a.getTaskStart() : "";
            String sb = b != null && b.getTaskStart() != null ? b.getTaskStart() : "";
            int cmp = sa.compareTo(sb);
            if (cmp != 0) return cmp;
            return Integer.compare(a.getTaskId(), b.getTaskId());
        });
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityCalendarBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.tvTitle.setText("Calendar");
        String formattedDate = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            LocalDate today = LocalDate.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", new java.util.Locale("en","US"));
            formattedDate = today.format(formatter);
        }
        binding.tvCurrentDate.setText(formattedDate);
        dataManager = DataManager.getInstance(this);
        sectionedAdapter = new SectionedTaskAdapter();
        sectionedAdapter.setOnTaskActionListener(task -> {
            String token = DataManager.getInstance(CalendarActivity.this).getToken();
            String url = "http://10.0.2.2:8080/api/tasks/" + task.getTaskId() + "/complete";
            OkHttpClient client = new OkHttpClient();
            RequestBody emptyBody = RequestBody.create("", MediaType.get("application/json; charset=utf-8"));
            Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + token)
                .patch(emptyBody)
                .build();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    runOnUiThread(() -> Toast.makeText(CalendarActivity.this, "Failed to update task", Toast.LENGTH_SHORT).show());
                }
                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response){
                    if (response.isSuccessful()) {
                        DataManager.getInstance(CalendarActivity.this).markTaskAsDone(task.getTaskId());
                        runOnUiThread(() -> Toast.makeText(CalendarActivity.this, "Marked as done", Toast.LENGTH_SHORT).show());
                    } else {
                        runOnUiThread(() -> Toast.makeText(CalendarActivity.this, "Update failed", Toast.LENGTH_SHORT).show());
                    }
                }
            });
        });
        binding.rvAllTasks.setLayoutManager(new LinearLayoutManager(this));
        binding.rvAllTasks.setAdapter(sectionedAdapter);
        binding.btnLoadEarlier.setOnClickListener(v -> {
            showingEarlier = true;
            binding.btnLoadEarlier.setVisibility(View.GONE);
            binding.rvAllTasks.setPadding(binding.rvAllTasks.getPaddingLeft(), 0, binding.rvAllTasks.getPaddingRight(), binding.rvAllTasks.getPaddingBottom());
            List<TaskData> merged = new ArrayList<>();
            merged.addAll(cachedEarlierAll);
            merged.addAll(cachedUpcoming);
            sortByStartAsc(merged);
            binding.rvAllTasks.animate().alpha(0f).setDuration(120).withEndAction(() -> {
                sectionedAdapter.setTasks(merged);
                binding.rvAllTasks.animate().alpha(1f).setDuration(240).start();
            }).start();
        });
        dataManager.getAllTasks().observe(this, tasks -> {
            cachedEarlier.clear();
            cachedEarlierAll.clear();
            cachedUpcoming.clear();
            if (tasks == null || tasks.isEmpty()) {
                binding.placeholderCalendarText.setVisibility(View.VISIBLE);
                binding.rvAllTasks.setVisibility(View.GONE);
                binding.btnLoadEarlier.setVisibility(View.GONE);
                binding.rvAllTasks.setPadding(binding.rvAllTasks.getPaddingLeft(), 0, binding.rvAllTasks.getPaddingRight(), binding.rvAllTasks.getPaddingBottom());
                return;
            }
            LocalDate todayDate = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                todayDate = LocalDate.now();
            }
            for (TaskData t : tasks) {
                String start = t.getTaskStart();
                boolean isEarlier = false;
                if (start != null && start.length() >= 10 && todayDate != null) {
                    try {
                        LocalDate d = LocalDate.parse(start.substring(0,10));
                        isEarlier = d.isBefore(todayDate);
                    } catch (Exception ex) {
                        isEarlier = false;
                    }
                }
                if (isEarlier) {
                    cachedEarlierAll.add(t);
                    cachedEarlier.add(t);
                } else {
                    cachedUpcoming.add(t);
                }
            }
            binding.placeholderCalendarText.setVisibility(View.GONE);
            binding.rvAllTasks.setVisibility(View.VISIBLE);
            sortByStartAsc(cachedEarlierAll);
            sortByStartAsc(cachedEarlier);
            sortByStartAsc(cachedUpcoming);
            if (showingEarlier) {
                List<TaskData> merged = new ArrayList<>();
                merged.addAll(cachedEarlierAll);
                merged.addAll(cachedUpcoming);
                sortByStartAsc(merged);
                sectionedAdapter.setTasks(merged);
            } else {
                sectionedAdapter.setTasks(cachedUpcoming);
            }
            if (!cachedEarlierAll.isEmpty()) {
                binding.btnLoadEarlier.setVisibility(View.VISIBLE);
                int padTop = (int) (48 * getResources().getDisplayMetrics().density);
                binding.rvAllTasks.setPadding(binding.rvAllTasks.getPaddingLeft(), padTop, binding.rvAllTasks.getPaddingRight(), binding.rvAllTasks.getPaddingBottom());
            } else {
                binding.btnLoadEarlier.setVisibility(View.GONE);
                binding.rvAllTasks.setPadding(binding.rvAllTasks.getPaddingLeft(), 0, binding.rvAllTasks.getPaddingRight(), binding.rvAllTasks.getPaddingBottom());
            }
        });
        binding.navHome.setOnClickListener(v -> {
            Intent intent = new Intent(CalendarActivity.this, MainPageActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
        binding.navChat.setOnClickListener(v -> {
            Intent intent = new Intent(CalendarActivity.this, GalleryActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
        binding.navProfile.setOnClickListener(v -> {
            Intent intent = new Intent(CalendarActivity.this, ProfileActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}