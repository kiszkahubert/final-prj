package com.kiszka.kiddify.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.kiszka.kiddify.databinding.ItemMediaBinding;
import com.kiszka.kiddify.models.Media;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.MediaViewHolder> {
    private List<Media> mediaList;
    private LayoutInflater inflater;
    private OnMediaClickListener onMediaClickListener;
    public interface OnMediaClickListener {
        void onMediaClick(Media media, int position);
    }
    public GalleryAdapter(Context context, List<Media> mediaList) {
        this.inflater = LayoutInflater.from(context);
        this.mediaList = mediaList;
    }
    public void setOnMediaClickListener(OnMediaClickListener listener) {
        this.onMediaClickListener = listener;
    }
    @NonNull
    @Override
    public MediaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemMediaBinding binding = ItemMediaBinding.inflate(inflater, parent, false);
        return new MediaViewHolder(binding);
    }
    @Override
    public void onBindViewHolder(@NonNull MediaViewHolder holder, int position) {
        Media media = mediaList.get(position);
        Glide.with(holder.itemView.getContext())
                .load(media.getAndroidUrl())
                .centerCrop()
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_report_image)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(holder.binding.ivMediaImage);
        String formattedDate = formatDate(media.getUploadedAt());
        holder.binding.tvMediaDate.setText(formattedDate);
        holder.itemView.setOnClickListener(v -> {
            if (onMediaClickListener != null) {
                onMediaClickListener.onMediaClick(media, position);
            }
        });
    }
    @Override
    public int getItemCount() {
        return mediaList != null ? mediaList.size() : 0;
    }
    public void updateMediaList(List<Media> newMediaList) {
        this.mediaList = newMediaList;
        notifyDataSetChanged();
    }
    private String formatDate(String uploadedAt) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
            Date date = inputFormat.parse(uploadedAt);
            return outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            try {
                SimpleDateFormat fallbackFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                SimpleDateFormat outputFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
                Date date = fallbackFormat.parse(uploadedAt);
                return outputFormat.format(date);
            } catch (ParseException ex) {
                ex.printStackTrace();
                return uploadedAt;
            }
        }
    }
    public static class MediaViewHolder extends RecyclerView.ViewHolder {
        private final ItemMediaBinding binding;

        public MediaViewHolder(ItemMediaBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
