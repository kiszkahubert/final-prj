package com.kiszka.kiddify.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.kiszka.kiddify.databinding.ItemTaskSuggestionBinding;
import com.kiszka.kiddify.models.Suggestion;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TaskSuggestionsAdapter extends RecyclerView.Adapter<TaskSuggestionsAdapter.ViewHolder> {
    private List<Suggestion> suggestions = new ArrayList<>();
    private LayoutInflater inflater;
    private OnDeleteClickListener deleteClickListener;

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
        String startStr = formatDateTimeWithDay(suggestion.getProposedStart());
        String endStr = formatTimeOnly(suggestion.getProposedEnd());
        holder.binding.tvDateTime.setText(startStr + " - " + endStr);
        String sentDate = "Wysłano: " + formatSentDate(suggestion.getCreatedAt());
        holder.binding.tvSentDate.setText(sentDate);
        holder.binding.btnDelete.setOnClickListener(v -> {
            if (deleteClickListener != null) {
                deleteClickListener.onDeleteClick(position);
            }
        });
    }

    private String formatDateTimeWithDay(String dateTimeStr) {
        if (dateTimeStr == null) return "";
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("EEEE, HH:mm", new Locale("pl", "PL"));
            Date date = inputFormat.parse(dateTimeStr);
            return outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return dateTimeStr;
        }
    }

    private String formatTimeOnly(String dateTimeStr) {
        if (dateTimeStr == null) return "";
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            Date date = inputFormat.parse(dateTimeStr);
            return outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return dateTimeStr;
        }
    }

    private String formatSentDate(String dateTimeStr) {
        if (dateTimeStr == null) return "";
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            Date date = inputFormat.parse(dateTimeStr);
            return outputFormat.format(date);
        } catch (ParseException e) {
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