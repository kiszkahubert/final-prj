package com.kiszka.kiddify.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.kiszka.kiddify.databinding.ItemTaskBinding;
import com.kiszka.kiddify.databinding.ItemTaskHeaderBinding;
import com.kiszka.kiddify.models.TaskData;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SectionedTaskAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;
    private final List<Object> items = new ArrayList<>();
    public interface OnTaskActionListener {
        void onMarkDone(TaskData task);
    }
    private OnTaskActionListener listener;
    public void setOnTaskActionListener(OnTaskActionListener l) {
        this.listener = l;
    }

    public void setTasks(List<TaskData> tasks) {
        items.clear();
        if (tasks == null || tasks.isEmpty()) {
            notifyDataSetChanged();
            return;
        }
        Map<String, List<TaskData>> grouped = new LinkedHashMap<>();
        for (TaskData t : tasks) {
            String start = t.getTaskStart();
            String key = "";
            if (start != null && start.length() >= 10) key = start.substring(0, 10);
            grouped.computeIfAbsent(key, k -> new ArrayList<>()).add(t);
        }
        DateTimeFormatter in = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter out = DateTimeFormatter.ofPattern("EEEE, dd.MM.yyyy", new Locale("en", "US"));
        for (Map.Entry<String, List<TaskData>> e : grouped.entrySet()) {
            String k = e.getKey();
            String header = k;
            try {
                LocalDate d = LocalDate.parse(k, in);
                header = d.format(out);
            } catch (Exception ignored) {}
            items.add(header);
            items.addAll(e.getValue());
        }
        notifyDataSetChanged();
    }
    @Override
    public int getItemViewType(int position) {
        return (items.get(position) instanceof String) ? TYPE_HEADER : TYPE_ITEM;
    }
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_HEADER) {
            ItemTaskHeaderBinding binding = ItemTaskHeaderBinding.inflate(inflater, parent, false);
            return new HeaderViewHolder(binding);
        } else {
            ItemTaskBinding binding = ItemTaskBinding.inflate(inflater, parent, false);
            return new ItemViewHolder(binding);
        }
    }
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == TYPE_HEADER) {
            ((HeaderViewHolder) holder).binding.tvHeader.setText((String) items.get(position));
        } else {
            TaskData t = (TaskData) items.get(position);
            ItemViewHolder vh = (ItemViewHolder) holder;
            vh.binding.taskTitle.setText(t.getTitle());
            vh.binding.taskTime.setText(formatTimeRange(t.getTaskStart(), t.getTaskEnd()));
            vh.binding.taskDescription.setText(t.getDescription() != null ? t.getDescription() : "");
            vh.binding.taskStatus.setText(t.getStatus() != null ? t.getStatus().toUpperCase(Locale.ENGLISH) : "");
            if (t.getStatus() != null) {
                String st = t.getStatus().toLowerCase();
                switch (st) {
                    case "missed":
                        vh.binding.taskStatus.setTextColor(0xFFFF4444);
                        break;
                    case "pending":
                        vh.binding.taskStatus.setTextColor(0xFFFFC107);
                        break;
                    case "done":
                        vh.binding.taskStatus.setTextColor(0xFF4CAF50);
                        break;
                    default:
                        vh.binding.taskStatus.setTextColor(0xFF3B82F6);
                        break;
                }
            } else {
                vh.binding.taskStatus.setTextColor(0xFF3B82F6);
            }
            boolean showMarkDone = t.getStatus() != null && "pending".equalsIgnoreCase(t.getStatus());
            vh.binding.btnMarkDone.setVisibility(showMarkDone ? View.VISIBLE : View.GONE);
            vh.binding.btnMarkDone.setOnClickListener(v -> {
                v.setEnabled(false);
                if (listener != null) listener.onMarkDone(t);
                v.postDelayed(() -> v.setEnabled(true), 200);
            });
        }
    }
    private String formatTimeRange(String start, String end) {
        if (start != null && end != null && start.length() >= 16 && end.length() >= 16) {
            try {
                String s = start.substring(11, 16);
                String e = end.substring(11, 16);
                return s + " - " + e;
            } catch (Exception ignored) {}
        }
        return "All day";
    }
    @Override
    public int getItemCount() {
        return items.size();
    }
    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        final ItemTaskHeaderBinding binding;
        public HeaderViewHolder(@NonNull ItemTaskHeaderBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
    static class ItemViewHolder extends RecyclerView.ViewHolder {
        final ItemTaskBinding binding;
        public ItemViewHolder(@NonNull ItemTaskBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}