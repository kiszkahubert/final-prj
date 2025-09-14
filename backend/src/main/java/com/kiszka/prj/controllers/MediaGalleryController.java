package com.kiszka.prj.controllers;

import com.kiszka.prj.entities.MediaGallery;
import com.kiszka.prj.entities.Parent;
import com.kiszka.prj.services.JWTService;
import com.kiszka.prj.services.MediaGalleryService;
import com.kiszka.prj.services.MinioService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.Optional;

@RestController
@RequestMapping("/api/media")
public class MediaGalleryController {
    private final MediaGalleryService mediaGalleryService;
    private final MinioService minioService;
    private final JWTService jwtService;

    public MediaGalleryController(MediaGalleryService mediaGalleryService, MinioService minioService, JWTService jwtService){
        this.mediaGalleryService = mediaGalleryService;
        this.minioService = minioService;
        this.jwtService = jwtService;
    }
    @PostMapping("/upload")
    public ResponseEntity<?> uploadMedia(
            Authentication authentication,
            @RequestHeader("Authorization") String authHeader,
            @RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()){
                return ResponseEntity.badRequest().body("File is empty");
            }
            String token = authHeader.substring(7);
            Integer kidId = jwtService.extractKidId(token);
            if(kidId != null){
                MediaGallery savedMedia = mediaGalleryService.uploadMedia(file, Optional.empty(), Optional.of(kidId));
                return ResponseEntity.ok(savedMedia);
            }
            Parent parent = (Parent) authentication.getPrincipal();
            MediaGallery savedMedia = mediaGalleryService.uploadMedia(file, Optional.of(parent.getId()), Optional.empty());
            return ResponseEntity.ok(savedMedia);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }
    //TODO Delete afterwards only a debugging solution
    @GetMapping("/{id}")
    public ResponseEntity<MediaGallery> getMediaById(@PathVariable int id) {
        Optional<MediaGallery> media = mediaGalleryService.getMediaById(id);
        return media.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
    @GetMapping("/{id}/download")
    public ResponseEntity<InputStreamResource> downloadMedia(
            Authentication authentication,
            @RequestHeader("Authorization") String authHeader,
            @PathVariable int id) {
        try {
            Optional<MediaGallery> media = mediaGalleryService.getMediaById(id);
            if (media.isPresent()) {
                String token = authHeader.substring(7);
                Integer kidId = jwtService.extractKidId(token);
                if(kidId != null){
                    if(!media.get().getKidId().equals(kidId)){
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
                    }
                } else{
                    Parent parent = (Parent) authentication.getPrincipal();
                    if(!media.get().getParentId().equals(parent.getId())){
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
                    }
                }
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
    public ResponseEntity<?> deleteMedia(
            Authentication authentication,
            @RequestHeader("Authorization") String authHeader,
            @PathVariable int id) {
        try {
            Optional<MediaGallery> media = mediaGalleryService.getMediaById(id);
            if (media.isPresent()) {
                String token = authHeader.substring(7);
                Integer kidId = jwtService.extractKidId(token);
                if (kidId != null) {
                    if (!media.get().getKidId().equals(kidId)) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
                    }
                } else {
                    Parent parent = (Parent) authentication.getPrincipal();
                    if (!media.get().getParentId().equals(parent.getId())) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
                    }
                }
                mediaGalleryService.deleteMedia(id);
                return ResponseEntity.ok().body("Deleted OK");
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(e.getMessage());
        }
    }
}