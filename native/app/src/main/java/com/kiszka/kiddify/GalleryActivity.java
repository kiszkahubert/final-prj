package com.kiszka.kiddify;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.kiszka.kiddify.adapters.GalleryAdapter;
import com.kiszka.kiddify.database.DataManager;
import com.kiszka.kiddify.databinding.ActivityGalleryBinding;
import com.kiszka.kiddify.models.Media;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityGalleryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initializeComponents();
        setupActivityResultLaunchers();
        setupRecyclerView();
        loadMediaData();
        setupClickListeners();
    }
    private void initializeComponents() {
        dataManager = DataManager.getInstance(this);
    }
    private void setupActivityResultLaunchers() {
        permissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        openFilePicker();
                    } else {
                        Toast.makeText(this, "Uprawnienia do plików są wymagane", Toast.LENGTH_SHORT).show();
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
                });
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
            if (mediaList != null && !mediaList.isEmpty()) {
                galleryAdapter.updateMediaList(mediaList);
                binding.emptyStateLayout.setVisibility(android.view.View.GONE);
                binding.recyclerViewGallery.setVisibility(android.view.View.VISIBLE);
            } else {
                showEmptyState();
            }
        });
    }
    private void showEmptyState() {
        binding.recyclerViewGallery.setVisibility(android.view.View.GONE);
        binding.emptyStateLayout.setVisibility(android.view.View.VISIBLE);
    }
    private void setupClickListeners() {
        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnAddMedia.setOnClickListener(v -> checkPermissionsAndOpenPicker());
    }
    private void checkPermissionsAndOpenPicker() {
        String permission;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permission = Manifest.permission.READ_MEDIA_IMAGES;
        } else {
            permission = Manifest.permission.READ_EXTERNAL_STORAGE;
        }
        if (ContextCompat.checkSelfPermission(this, permission)
                == PackageManager.PERMISSION_GRANTED) {
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
        filePickerLauncher.launch(Intent.createChooser(intent, "Wybierz zdjęcia"));
    }
    private void uploadFiles(List<Uri> uris) {
        if (uris.isEmpty()) {
            Toast.makeText(this, "Nie wybrano plików", Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(this, "Przesyłanie " + uris.size() + " plików...", Toast.LENGTH_SHORT).show();
        String token = dataManager.getToken();
        OkHttpClient client = new OkHttpClient();
        MultipartBody.Builder builder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM);
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
            System.out.println(requestBody);
            Request request = new Request.Builder()
                    .url("http://10.0.2.2:8080/api/media/upload")
                    .addHeader("Authorization", "Bearer " + token)
                    .post(requestBody)
                    .build();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() -> {
                        Log.e("API", "Upload failed: " + e.getMessage());
                        Toast.makeText(GalleryActivity.this,
                                "Błąd przesyłania: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    });
                }
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    runOnUiThread(() -> {
                        if (response.isSuccessful()) {
                            Toast.makeText(GalleryActivity.this,
                                    "Pliki zostały przesłane pomyślnie!",
                                    Toast.LENGTH_SHORT).show();
                            refreshGalleryData();
                        } else {
                            Toast.makeText(GalleryActivity.this,
                                    "Błąd serwera: " + response.code(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                    response.close();
                }
            });
        } catch (Exception e) {
            Log.e("Upload", "Error preparing upload: " + e.getMessage());
            Toast.makeText(this, "Błąd przygotowania plików", Toast.LENGTH_SHORT).show();
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
            public void onFailure(Call call, IOException e) {
                Log.e("API", "Refresh media failed: " + e.getMessage());
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String body = response.body().string();
                    Type listType = new TypeToken<List<Media>>(){}.getType();
                    List<Media> mediaList = new Gson().fromJson(body, listType);
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
        dialog.setContentView(R.layout.dialog_fullscreen_image);
        ImageView imageView = dialog.findViewById(R.id.iv_fullscreen_image);
        ImageView closeButton = dialog.findViewById(R.id.iv_close);
        Glide.with(this)
                .load(media.getUrl())
                .fitCenter()
                .into(imageView);

        closeButton.setOnClickListener(v -> dialog.dismiss());
        imageView.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}