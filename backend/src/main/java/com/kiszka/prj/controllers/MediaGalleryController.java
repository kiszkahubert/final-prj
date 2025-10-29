package com.kiszka.prj.controllers;

import com.kiszka.prj.DTOs.MediaResponseDTO;
import com.kiszka.prj.entities.MediaGallery;
import com.kiszka.prj.entities.Parent;
import com.kiszka.prj.services.JWTService;
import com.kiszka.prj.services.MediaGalleryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/media")
public class MediaGalleryController {
    private final MediaGalleryService mediaGalleryService;
    private final JWTService jwtService;

    public MediaGalleryController(MediaGalleryService mediaGalleryService, JWTService jwtService){
        this.mediaGalleryService = mediaGalleryService;
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
    @GetMapping("/parent/all")
    public ResponseEntity<List<MediaResponseDTO>> getAllMediaForParent(Authentication authentication) {
        try {
            Parent parent = (Parent) authentication.getPrincipal();
            List<MediaResponseDTO> medias = mediaGalleryService.getAllRelatedMediaForParent(parent.getId());
            return ResponseEntity.ok(medias);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    @PostMapping("/upload")
    public ResponseEntity<?> uploadMedia(
            Authentication authentication,
            @RequestHeader("Authorization") String authHeader,
            @RequestParam("files") MultipartFile[] files
    ) {
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
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
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