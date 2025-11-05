package com.kiszka.kiddify.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.kiszka.kiddify.databinding.ItemTaskBinding;
import com.kiszka.kiddify.models.TaskData;

import java.util.List;
import java.util.Locale;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {
    private List<TaskData> tasks;
    private final LayoutInflater inflater;
    public TaskAdapter(Context context, List<TaskData> tasks) {
        this.inflater = LayoutInflater.from(context);
        this.tasks = tasks;
    }
    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemTaskBinding binding = ItemTaskBinding.inflate(inflater, parent, false);
        return new TaskViewHolder(binding);
    }
    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        TaskData task = tasks.get(position);
        holder.binding.taskTitle.setText(task.getTitle());
        holder.binding.taskTime.setText(formatTimeRange(task.getTaskStart(), task.getTaskEnd()));
        holder.binding.taskDescription.setText(task.getDescription() != null ? task.getDescription() : "");
        holder.binding.taskStatus.setText(task.getStatus() != null ? task.getStatus().toUpperCase(Locale.ENGLISH) : "");
        if (task.getStatus() != null) {
            switch (task.getStatus().toLowerCase()) {
                case "missed":
                    holder.binding.taskStatus.setTextColor(0xFFFF4444);
                    break;
                case "pending":
                    holder.binding.taskStatus.setTextColor(0xFFFFC107);
                    break;
                case "done":
                    holder.binding.taskStatus.setTextColor(0xFF4CAF50);
                    break;
                default:
                    holder.binding.taskStatus.setTextColor(0xFF3B82F6);
                    break;
            }
        } else {
            holder.binding.taskStatus.setTextColor(0xFF3B82F6);
        }
    }
    private String formatTimeRange(String start, String end) {
        if (start != null && end != null) {
            try {
                String startTime = start.substring(11, 16);
                String endTime = end.substring(11, 16);
                return startTime + " - " + endTime;
            } catch (Exception e) {
                return "Cały dzień";
            }
        }
        return "Cały dzień";
    }
    @Override
    public int getItemCount() {
        return tasks.size();
    }
    public void setTasks(List<TaskData> tasks) {
        this.tasks = tasks;
        notifyDataSetChanged();
    }
    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        public ItemTaskBinding binding;
        public TaskViewHolder(ItemTaskBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}