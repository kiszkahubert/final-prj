package com.kiszka.kiddify.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.kiszka.kiddify.databinding.ItemTaskSuggestionBinding;
import com.kiszka.kiddify.models.Suggestion;
import com.kiszka.kiddify.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TaskSuggestionsAdapter extends RecyclerView.Adapter<TaskSuggestionsAdapter.ViewHolder> {
    private List<Suggestion> suggestions = new ArrayList<>();
    private final LayoutInflater inflater;
    private final OnDeleteClickListener deleteClickListener;

    public interface OnDeleteClickListener {
        void onDeleteClick(int position);
    }
    public TaskSuggestionsAdapter(Context context, OnDeleteClickListener listener) {
        this.inflater = LayoutInflater.from(context);
        this.deleteClickListener = listener;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemTaskSuggestionBinding binding = ItemTaskSuggestionBinding.inflate(inflater, parent, false);
        return new ViewHolder(binding);
    }
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Suggestion suggestion = suggestions.get(position);
        holder.binding.tvTitle.setText(suggestion.getTitle());
        holder.binding.tvDescription.setText(suggestion.getDescription());
        String status = suggestion.getStatus();
        if (status != null && !status.isEmpty()) {
            holder.binding.tvStatus.setText(status.toUpperCase());
            int backgroundResource;
            switch (status.toUpperCase()) {
                case "ACCEPTED":
                    backgroundResource = com.kiszka.kiddify.R.drawable.status_badge_accepted;
                    break;
                case "REJECTED":
                    backgroundResource = com.kiszka.kiddify.R.drawable.status_badge_rejected;
                    break;
                case "PENDING":
                default:
                    backgroundResource = com.kiszka.kiddify.R.drawable.status_badge_pending;
                    break;
            }
            holder.binding.tvStatus.setBackgroundResource(backgroundResource);
            holder.binding.tvStatus.setVisibility(android.view.View.VISIBLE);
        } else {
            holder.binding.tvStatus.setVisibility(android.view.View.GONE);
        }
        String startStr = formatDateTimeFull(suggestion.getProposedStart());
        String endStr = formatDateTimeFull(suggestion.getProposedEnd());
        holder.binding.tvStartDate.setText("Start date: " + startStr);
        holder.binding.tvEndDate.setText("End date: " + endStr);
        String sentDate = "Sent at: " + formatDateTimeFull(suggestion.getCreatedAt());
        holder.binding.tvSentDate.setText(sentDate);
        holder.binding.btnDelete.setOnClickListener(v -> {
            if (deleteClickListener != null) {
                deleteClickListener.onDeleteClick(position);
            }
        });
    }

    private String formatDateTimeFull(String dateTimeStr) {
        if (dateTimeStr == null) return "";
        try {
            SimpleDateFormat inputFormat1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.getDefault());
            SimpleDateFormat inputFormat2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd.MM.yyyy, HH:mm", Locale.getDefault());
            Date date;
            try {
                date = inputFormat1.parse(dateTimeStr);
            } catch (ParseException e) {
                date = inputFormat2.parse(dateTimeStr);
            }
            return outputFormat.format(date);
        } catch (Exception e) {
            e.printStackTrace();
            return dateTimeStr;
        }
    }

    @Override
    public int getItemCount() {
        return suggestions.size();
    }

    public void setSuggestions(List<Suggestion> newSuggestions) {
        this.suggestions = newSuggestions != null ? newSuggestions : new ArrayList<>();
        notifyDataSetChanged();
    }

    public Suggestion getSuggestionAt(int position) {
        return suggestions.get(position);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ItemTaskSuggestionBinding binding;

        public ViewHolder(@NonNull ItemTaskSuggestionBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}