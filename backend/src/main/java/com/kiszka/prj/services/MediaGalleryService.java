package com.kiszka.prj.services;

import com.kiszka.prj.DTOs.MediaResponseDTO;
import com.kiszka.prj.entities.Kid;
import com.kiszka.prj.entities.MediaGallery;
import com.kiszka.prj.entities.Parent;
import com.kiszka.prj.repositories.KidRepository;
import com.kiszka.prj.repositories.MediaGalleryRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class MediaGalleryService {
    private final MediaGalleryRepository mediaGalleryRepository;
    private final MinioService minioService;
    private final KidService kidService;
    private final ParentService parentService;

    public MediaGalleryService(MediaGalleryRepository mediaGalleryRepository,
                               MinioService minioService,
                               KidService kidService,
                               ParentService parentService) {
        this.mediaGalleryRepository = mediaGalleryRepository;
        this.minioService = minioService;
        this.kidService = kidService;
        this.parentService = parentService;
    }
    public List<MediaResponseDTO> getAllRelatedMediaForKid(int kidId) {
        List<MediaResponseDTO> result = new ArrayList<>();
        kidService.getKidById(kidId).ifPresent(kid ->
                mediaGalleryRepository.findByKidId(kidId).forEach(media ->{
                    String presignedUrl = minioService.getFileUrl(media.getUrl());
                    result.add(new MediaResponseDTO(media.getMediaId(), media.getMediaType(), presignedUrl, media.getUploadedAt(), kid.getName()));
                })
        );
        Set<Parent> parents = kidService.getKidById(kidId)
                .map(Kid::getParents)
                .orElse(Set.of());
        for (Parent parent : parents) {
            mediaGalleryRepository.findByParentId(parent.getId()).forEach(media ->
                    result.add(new MediaResponseDTO(media.getMediaId(), media.getMediaType(), media.getUrl(), media.getUploadedAt(), parent.getUsername()))
            );
            for (Kid sibling : parent.getKids()) {
                if (sibling.getId() != kidId) {
                    mediaGalleryRepository.findByKidId(sibling.getId()).forEach(media ->
                            result.add(new MediaResponseDTO(media.getMediaId(), media.getMediaType(), media.getUrl(), media.getUploadedAt(), sibling.getName()))
                    );
                }
            }
        }
        return result;
    }
    public MediaGallery uploadMedia(MultipartFile file, Optional<Integer> parentId, Optional<Integer> kidId) throws Exception {
        String fileName = minioService.uploadFile(file);
        String mediaType = determineMediaType(file.getContentType());
        MediaGallery mediaGallery = new MediaGallery();
        mediaGallery.setMediaType(mediaType);
        mediaGallery.setUrl(fileName);
        mediaGallery.setUploadedAt(LocalDateTime.now());
        if(parentId.isPresent()){
            mediaGallery.setParentId(parentId.get());
        } else kidId.ifPresent(mediaGallery::setKidId);
        return mediaGalleryRepository.save(mediaGallery);
    }
    public Optional<MediaGallery> getMediaById(int id) {
        return mediaGalleryRepository.findById(id);
    }
    public List<MediaGallery> getMediaByParentId(int parentId) {
        return mediaGalleryRepository.findByParentId(parentId);
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