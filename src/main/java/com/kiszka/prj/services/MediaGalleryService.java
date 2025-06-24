package com.kiszka.prj.services;

import com.kiszka.prj.entities.MediaGallery;
import com.kiszka.prj.repositories.MediaGalleryRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class MediaGalleryService {
    private final MediaGalleryRepository mediaGalleryRepository;
    private final MinioService minioService;
    public MediaGalleryService(MediaGalleryRepository mediaGalleryRepository, MinioService minioService) {
        this.mediaGalleryRepository = mediaGalleryRepository;
        this.minioService = minioService;
    }
    public MediaGallery uploadMedia(MultipartFile file, int parentId, int kidId) throws Exception {
        String fileName = minioService.uploadFile(file);
        String mediaType = determineMediaType(file.getContentType());
        MediaGallery mediaGallery = new MediaGallery();
        mediaGallery.setMediaType(mediaType);
        mediaGallery.setUrl(fileName);
        mediaGallery.setUploadedAt(LocalDateTime.now());
        mediaGallery.setParentId(parentId);
        mediaGallery.setKidId(kidId);

        return mediaGalleryRepository.save(mediaGallery);
    }
    public List<MediaGallery> getAllMedia() {
        return mediaGalleryRepository.findAll();
    }
    public Optional<MediaGallery> getMediaById(int id) {
        return mediaGalleryRepository.findById(id);
    }
    public List<MediaGallery> getMediaByParentId(int parentId) {
        return mediaGalleryRepository.findByParentId(parentId);
    }
    public List<MediaGallery> getMediaByKidId(int kidId) {
        return mediaGalleryRepository.findByKidId(kidId);
    }
    public String getMediaUrl(int mediaId) {
        Optional<MediaGallery> media = mediaGalleryRepository.findById(mediaId);
        if (media.isPresent()) {
            return minioService.getFileUrl(media.get().getUrl());
        }
        throw new RuntimeException("Media not found");
    }
    public void deleteMedia(int mediaId) throws Exception {
        Optional<MediaGallery> media = mediaGalleryRepository.findById(mediaId);
        if (media.isPresent()) {
            minioService.deleteFile(media.get().getUrl());
            mediaGalleryRepository.deleteById(mediaId);
        } else {
            throw new RuntimeException("Media not found");
        }
    }
    private String determineMediaType(String contentType) {
        if (contentType == null) {
            return "unknown";
        }
        if (contentType.startsWith("image/")) {
            return "image";
        } else if (contentType.startsWith("video/")) {
            return "video";
        } else {
            return "other";
        }
    }
}