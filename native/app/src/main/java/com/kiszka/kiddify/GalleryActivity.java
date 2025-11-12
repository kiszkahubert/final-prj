package com.kiszka.kiddify;

import static android.util.Base64.NO_PADDING;
import static android.util.Base64.NO_WRAP;
import static android.util.Base64.URL_SAFE;
import static android.util.Base64.decode;
import static android.view.View.GONE;

import static java.nio.charset.StandardCharsets.UTF_8;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.kiszka.kiddify.adapters.GalleryAdapter;
import com.kiszka.kiddify.database.DataManager;
import com.kiszka.kiddify.databinding.ActivityGalleryBinding;
import com.kiszka.kiddify.databinding.DialogFullscreenImageBinding;
import com.kiszka.kiddify.models.Media;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class GalleryActivity extends AppCompatActivity implements GalleryAdapter.OnMediaClickListener {
    private ActivityGalleryBinding binding;
    private GalleryAdapter galleryAdapter;
    private DataManager dataManager;
    private ActivityResultLauncher<String> permissionLauncher;
    private ActivityResultLauncher<Intent> filePickerLauncher;
    private final List<Media> currentMedia = new ArrayList<>();
    private SortOption currentSort = SortOption.DATE_NEWEST;
    private enum SortOption {
        USERNAME_ASC,
        USERNAME_DESC,
        DATE_NEWEST,
        DATE_OLDEST
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityGalleryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        dataManager = DataManager.getInstance(this);
        setupActivityResultLaunchers();
        setupRecyclerView();
        loadMediaData();
        String formattedDate = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LocalDate today = LocalDate.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", new Locale("en","US"));
            formattedDate = today.format(formatter);
        }
        binding.tvTitle.setText("Gallery");
        binding.tvCurrentDate.setText(formattedDate);
        binding.btnAddMedia.setOnClickListener(v -> checkPermissionsAndOpenPicker());
        binding.btnSort.setOnClickListener(v -> showSortMenu());
        binding.navHome.setOnClickListener(v -> {
            Intent intent = new Intent(GalleryActivity.this, MainPageActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
        binding.navCalendar.setOnClickListener(v -> {
            Intent intent = new Intent(GalleryActivity.this, CalendarActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
        binding.navProfile.setOnClickListener(v -> {
            Intent intent = new Intent(GalleryActivity.this, ProfileActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
        dataSetup();
    }
    private void setupActivityResultLaunchers() {
        permissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        openFilePicker();
                    } else {
                        Toast.makeText(this, "File permissions are required", Toast.LENGTH_SHORT).show();
                    }
                }
        );
        filePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Intent data = result.getData();
                        if (data.getClipData() != null) {
                            int count = data.getClipData().getItemCount();
                            List<Uri> selectedUris = new ArrayList<>();
                            for (int i = 0; i < count; i++) {
                                Uri uri = data.getClipData().getItemAt(i).getUri();
                                selectedUris.add(uri);
                            }
                            uploadFiles(selectedUris);
                        }
                        else if (data.getData() != null) {
                            List<Uri> selectedUris = new ArrayList<>();
                            selectedUris.add(data.getData());
                            uploadFiles(selectedUris);
                        }
                    }
                }
        );
    }
    private void setupRecyclerView() {
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        binding.recyclerViewGallery.setLayoutManager(layoutManager);
        galleryAdapter = new GalleryAdapter(this, new ArrayList<>());
        galleryAdapter.setOnMediaClickListener(this);
        binding.recyclerViewGallery.setAdapter(galleryAdapter);
    }
    private void loadMediaData() {
        dataManager.getAllMedia().observe(this, mediaList -> {
            currentMedia.clear();
            if (mediaList != null) currentMedia.addAll(mediaList);
            if (!currentMedia.isEmpty()) {
                applySort();
                binding.emptyStateLayout.setVisibility(GONE);
                binding.recyclerViewGallery.setVisibility(View.VISIBLE);
            } else {
                showEmptyState();
            }
        });
    }
    private void showSortMenu() {
        PopupMenu popup = new PopupMenu(this, binding.btnSort);
        popup.getMenuInflater().inflate(R.menu.menu_gallery_sort, popup.getMenu());
        popup.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.action_sort_username_asc) currentSort = SortOption.USERNAME_ASC;
            else if (id == R.id.action_sort_username_desc) currentSort = SortOption.USERNAME_DESC;
            else if (id == R.id.action_sort_date_newest) currentSort = SortOption.DATE_NEWEST;
            else if (id == R.id.action_sort_date_oldest) currentSort = SortOption.DATE_OLDEST;
            else return false;
            applySort();
            return true;
        });
        popup.show();
    }
    private void applySort() {
        List<Media> sorted = new ArrayList<>(currentMedia);
        switch (currentSort) {
            case USERNAME_ASC:
                sorted.sort(Comparator.comparing(m -> safeLower(m.getUploadedByUsername())));
                break;
            case USERNAME_DESC:
                sorted.sort(Comparator.comparing((Media m) -> safeLower(m.getUploadedByUsername())).reversed());
                break;
            case DATE_NEWEST:
                sorted.sort((a, b) -> Long.compare(parseTime(b.getUploadedAt()), parseTime(a.getUploadedAt())));
                break;
            case DATE_OLDEST:
                sorted.sort((a, b) -> Long.compare(parseTime(a.getUploadedAt()), parseTime(b.getUploadedAt())));
                break;
        }
        galleryAdapter.updateMediaList(sorted);
    }
    private String safeLower(String s) {
        return s == null ? "" : s.toLowerCase(Locale.getDefault());
    }
    private long parseTime(String uploadedAt) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS", Locale.getDefault());
            Date date = inputFormat.parse(uploadedAt);
            if (date != null) return date.getTime();
        } catch (Exception ignored) {}
        try {
            SimpleDateFormat fallback = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            Date date = fallback.parse(uploadedAt);
            if (date != null) return date.getTime();
        } catch (Exception ignored) {}
        return 0L;
    }
    private String getLoggedInUsername() {
        String token = dataManager.getToken();
        if (token == null || token.isEmpty()) return null;
        try {
            String[] parts = token.split("\\.");
            if (parts.length < 2) return null;
            String payload = parts[1];
            byte[] decoded = decode(payload, URL_SAFE | NO_PADDING | NO_WRAP);
            String json = new String(decoded, UTF_8);
            try {
                Map map = new Gson().fromJson(json, Map.class);
                Object sub = map.get("sub");
                if (sub != null) {
                    String subject = String.valueOf(sub);
                    if (subject.startsWith("kid_")) {
                        String kidName = dataManager.getKidName();
                        return (kidName == null || kidName.isEmpty()) ? subject : kidName;
                    }
                    return subject;
                }
            } catch (Exception ignored) {}
        } catch (Exception ignored) {}
        return null;
    }
    private void showEmptyState() {
        binding.recyclerViewGallery.setVisibility(View.GONE);
        binding.emptyStateLayout.setVisibility(View.VISIBLE);
    }
    private void checkPermissionsAndOpenPicker() {
        String permission;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permission = Manifest.permission.READ_MEDIA_IMAGES;
        } else {
            permission = Manifest.permission.READ_EXTERNAL_STORAGE;
        }
        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            openFilePicker();
        } else {
            permissionLauncher.launch(permission);
        }
    }
    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        filePickerLauncher.launch(Intent.createChooser(intent, "Choose photos"));
    }
    private void uploadFiles(List<Uri> uris) {
        if (uris.isEmpty()) {
            Toast.makeText(this, "No files selected", Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(this, "Uploading " + uris.size() + " files...", Toast.LENGTH_SHORT).show();
        String token = dataManager.getToken();
        OkHttpClient client = new OkHttpClient();
        MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
        try {
            for (int i = 0; i < uris.size(); i++) {
                Uri uri = uris.get(i);
                byte[] fileBytes = getFileBytes(uri);
                if (fileBytes != null) {
                    String fileName = "image_" + System.currentTimeMillis() + "_" + i + ".png";
                    RequestBody fileBody = RequestBody.create(
                            MediaType.parse("image/*"),
                            fileBytes
                    );
                    builder.addFormDataPart("files", fileName, fileBody);
                }
            }
            RequestBody requestBody = builder.build();
            Request request = new Request.Builder()
                    .url("http://10.0.2.2:8080/api/media/upload")
                    .addHeader("Authorization", "Bearer " + token)
                    .post(requestBody)
                    .build();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    runOnUiThread(() -> {
                        Log.e("API", "Upload failed: " + e.getMessage());
                        Toast.makeText(GalleryActivity.this, "Upload error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                }
                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response){
                    runOnUiThread(() -> {
                        if (response.isSuccessful()) {
                            Toast.makeText(GalleryActivity.this, "Files uploaded successfully!", Toast.LENGTH_SHORT).show();
                            refreshGalleryData();
                        } else {
                            Toast.makeText(GalleryActivity.this, "Server error: " + response.code(), Toast.LENGTH_SHORT).show();
                        }
                    });
                    response.close();
                }
            });
        } catch (Exception e) {
            Log.e("Upload", "Error preparing upload: " + e.getMessage());
            Toast.makeText(this, "Error preparing files", Toast.LENGTH_SHORT).show();
        }
    }
    private byte[] getFileBytes(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            if (inputStream == null) return null;
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, length);
            }
            inputStream.close();
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            Log.e("FileRead", "Error reading file: " + e.getMessage());
            return null;
        }
    }
    private void refreshGalleryData() {
        String token = dataManager.getToken();
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("http://10.0.2.2:8080/api/media/kid/all")
                .addHeader("Authorization", "Bearer " + token)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("API", "Refresh media failed: " + e.getMessage());
            }
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String body = response.body().string();
                    Type listType = new TypeToken<List<Media>>(){}.getType();
                    List<Media> mediaList = new Gson().fromJson(body, listType);
                    if (mediaList == null) mediaList = List.of();
                    dataManager.saveMediaList(mediaList);
                    Log.d("API", "Media refreshed: " + mediaList.size());
                }
            }
        });
    }
    @Override
    public void onMediaClick(Media media, int position) {
        showFullScreenImage(media);
    }
    private void showFullScreenImage(Media media) {
        Dialog dialog = new Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        DialogFullscreenImageBinding dialogBinding = DialogFullscreenImageBinding.inflate(getLayoutInflater());
        dialog.setContentView(dialogBinding.getRoot());
        ImageView imageView = dialogBinding.ivFullscreenImage;
        ImageView closeButton = dialogBinding.ivClose;
        ImageView deleteButton = dialogBinding.ivDelete;
        ImageView saveButton = dialogBinding.ivSave;
        Glide.with(this)
                .load(media.getUrl())
                .fitCenter()
                .into(imageView);
        closeButton.setOnClickListener(v -> dialog.dismiss());
        imageView.setOnClickListener(v -> dialog.dismiss());
        deleteButton.setOnClickListener(v -> {
            dialog.dismiss();
            showDeleteConfirmation(media);
        });
        saveButton.setOnClickListener(v -> {
            downloadAndSaveImage(media);
            Toast.makeText(this, "Downloading image...", Toast.LENGTH_SHORT).show();
        });
        dialog.show();
    }
    private void showDeleteConfirmation(Media media) {
        new AlertDialog.Builder(this)
                .setTitle("Delete photo")
                .setMessage("Are you sure you want to delete this photo?")
                .setPositiveButton("Delete", (dialog, which) -> deleteMedia(media))
                .setNegativeButton("Cancel", null)
                .show();
    }
    private void deleteMedia(Media media) {
        if (media.getUploadedByUsername() != null && getLoggedInUsername() != null && !media.getUploadedByUsername().equals(getLoggedInUsername())) {
            runOnUiThread(() -> Toast.makeText(GalleryActivity.this, "You cannot delete another user's image", Toast.LENGTH_LONG).show());
            return;
        }
        String token = dataManager.getToken();
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("http://10.0.2.2:8080/api/media/" + media.getId())
                .addHeader("Authorization", "Bearer " + token)
                .delete()
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> {
                    Log.e("API", "Delete failed: " + e.getMessage());
                    Toast.makeText(GalleryActivity.this, "Delete error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                runOnUiThread(() -> {
                    if (response.isSuccessful()) {
                        Toast.makeText(GalleryActivity.this, "Photo deleted", Toast.LENGTH_SHORT).show();
                        refreshGalleryData();
                    } else {
                        Toast.makeText(GalleryActivity.this, "Delete error: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                });
                response.close();
            }
        });
    }
    private void downloadAndSaveImage(Media media) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            downloadImageToMediaStore(media);
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                permissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                return;
            }
            downloadImageToMediaStore(media);
        }
    }
    private void downloadImageToMediaStore(Media media) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(media.getUrl())
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> {
                    Log.e("Download", "Download failed: " + e.getMessage());
                    Toast.makeText(GalleryActivity.this, "Download error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    byte[] imageBytes = response.body().bytes();
                    runOnUiThread(() -> {
                        try {
                            saveImageToGallery(imageBytes);
                        } catch (Exception e) {
                            Log.e("Save", "Save failed: " + e.getMessage());
                            Toast.makeText(GalleryActivity.this, "Save error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    runOnUiThread(() -> Toast.makeText(GalleryActivity.this, "Error downloading image", Toast.LENGTH_SHORT).show());
                }
                response.close();
            }
        });
    }
    private void saveImageToGallery(byte[] imageBytes) {
        try {
            String fileName = "Kiddify_" + System.currentTimeMillis() + ".png";
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/Kiddify");
            }
            Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            if (uri != null) {
                try (var outputStream = getContentResolver().openOutputStream(uri)) {
                    if (outputStream != null) {
                        outputStream.write(imageBytes);
                        outputStream.flush();
                        Toast.makeText(this, "Photo saved to gallery", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                        intent.setData(uri);
                        sendBroadcast(intent);
                    }
                }
            } else {
                Toast.makeText(this, "Error saving to gallery", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e("Save", "Error saving to gallery: " + e.getMessage());
            Toast.makeText(this, "Save error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
    private void dataSetup() {
        String token = DataManager.getInstance(this).getToken();
        OkHttpClient client = new OkHttpClient();
        Request requestMedia = new Request.Builder()
                .url("http://10.0.2.2:8080/api/media/kid/all")
                .addHeader("Authorization", "Bearer " + token)
                .build();
        client.newCall(requestMedia).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("API", "Media failed: " + e.getMessage());
            }
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String body = response.body().string();
                    Type listType = new TypeToken<List<Media>>(){}.getType();
                    List<Media> mediaList = new Gson().fromJson(body, listType);
                    if (mediaList == null) mediaList = List.of();
                    DataManager.getInstance(GalleryActivity.this).saveMediaList(mediaList);
                    Log.d("API", "Media saved: " + mediaList.size());
                }
            }
        });
    }
}