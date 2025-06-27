package com.kiszka.prj.controllers;

import com.kiszka.prj.entities.MediaGallery;
import com.kiszka.prj.services.MediaGalleryService;
import com.kiszka.prj.services.MinioService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/media")
public class MediaGalleryController {
    private final MediaGalleryService mediaGalleryService;
    private final MinioService minioService;
    public MediaGalleryController(MediaGalleryService mediaGalleryService, MinioService minioService){
        this.mediaGalleryService = mediaGalleryService;
        this.minioService = minioService;
    }
    @PostMapping("/upload")
    public ResponseEntity<?> uploadMedia(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "parentId", required = false) Integer parentId,
            @RequestParam(value = "kidId", required = false) Integer kidId) {
        try {
            if ((parentId == null && kidId == null) || (parentId != null && kidId != null))
                return ResponseEntity.badRequest().body("Either kid or parent not both");
            if (file.isEmpty())
                return ResponseEntity.badRequest().body("File is empty");
            MediaGallery savedMedia = mediaGalleryService.uploadMedia(file, Optional.ofNullable(parentId), Optional.ofNullable(kidId));
            return ResponseEntity.ok(savedMedia);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }
    @GetMapping
    public ResponseEntity<List<MediaGallery>> getAllMedia() {
        List<MediaGallery> mediaList = mediaGalleryService.getAllMedia();
        return ResponseEntity.ok(mediaList);
    }
    @GetMapping("/{id}")
    public ResponseEntity<MediaGallery> getMediaById(@PathVariable int id) {
        Optional<MediaGallery> media = mediaGalleryService.getMediaById(id);
        return media.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    @GetMapping("/parent/{parentId}")
    public ResponseEntity<List<MediaGallery>> getMediaByParentId(@PathVariable Integer parentId) {
        List<MediaGallery> mediaList = mediaGalleryService.getMediaByParentId(parentId);
        return ResponseEntity.ok(mediaList);
    }
    @GetMapping("/kid/{kidId}")
    public ResponseEntity<List<MediaGallery>> getMediaByKidId(@PathVariable Integer kidId) {
        List<MediaGallery> mediaList = mediaGalleryService.getMediaByKidId(kidId);
        return ResponseEntity.ok(mediaList);
    }
    @GetMapping("/{id}/url")
    public ResponseEntity<String> getMediaUrl(@PathVariable int id) {
        try {
            String url = mediaGalleryService.getMediaUrl(id);
            return ResponseEntity.ok(url);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    @GetMapping("/{id}/download")
    public ResponseEntity<InputStreamResource> downloadMedia(@PathVariable int id) {
        try {
            Optional<MediaGallery> media = mediaGalleryService.getMediaById(id);
            if (media.isPresent()) {
                InputStream inputStream = minioService.downloadFile(media.get().getUrl());
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION,
                                "attachment; filename=\"" + media.get().getUrl() + "\"")
                        .contentType(MediaType.APPLICATION_OCTET_STREAM)
                        .body(new InputStreamResource(inputStream));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMedia(@PathVariable int id) {
        try {
            mediaGalleryService.deleteMedia(id);
            return ResponseEntity.ok().body("Deleted OK");
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(e.getMessage());
        }
    }
}