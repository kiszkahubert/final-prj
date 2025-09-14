package com.kiszka.kiddify.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.kiszka.kiddify.databinding.ItemTaskBinding;
import com.kiszka.kiddify.models.TaskData;

import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {
    private List<TaskData> tasks;
    private LayoutInflater inflater;

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