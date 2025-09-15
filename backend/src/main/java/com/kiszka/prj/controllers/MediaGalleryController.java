package com.kiszka.prj.controllers;

import com.kiszka.prj.DTOs.MediaResponseDTO;
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
import java.util.ArrayList;
import java.util.List;
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
    @GetMapping("/kid/all")
    public ResponseEntity<List<MediaResponseDTO>> getAllMediaForKid(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7);
            Integer kidId = jwtService.extractKidId(token);
            List<MediaResponseDTO> medias = mediaGalleryService.getAllRelatedMediaForKid(kidId);
            return ResponseEntity.ok(medias);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    @PostMapping("/upload")
    public ResponseEntity<?> uploadMedia(
            Authentication authentication,
            @RequestHeader("Authorization") String authHeader,
            @RequestParam("files") MultipartFile[] files) {
        try {
            if (files == null || files.length == 0) {
                return ResponseEntity.badRequest().body("Files are empty");
            }
            List<MediaGallery> savedMedias = new ArrayList<>();
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    String token = authHeader.substring(7);
                    Integer kidId = jwtService.extractKidId(token);
                    if(kidId != null){
                        savedMedias.add(mediaGalleryService.uploadMedia(file, Optional.empty(), Optional.of(kidId)));
                    } else {
                        Parent parent = (Parent) authentication.getPrincipal();
                        savedMedias.add(mediaGalleryService.uploadMedia(file, Optional.of(parent.getId()), Optional.empty()));
                    }
                }
            }
            return ResponseEntity.ok(savedMedias);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }
    @GetMapping("/{id}/url")
    public ResponseEntity<String> getMediaUrl(
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
                String presignedUrl = mediaGalleryService.getMediaUrl(id);
                return ResponseEntity.ok(presignedUrl);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
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