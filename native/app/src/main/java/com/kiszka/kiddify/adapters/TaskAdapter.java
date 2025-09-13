package com.kiszka.kiddify.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.kiszka.kiddify.databinding.ItemTaskBinding;
import com.kiszka.kiddify.models.Task;
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
        holder.binding.taskTime.setText(task.getTaskStart() + " - " + task.getTaskEnd());
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