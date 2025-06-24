package com.kiszka.prj.repositories;

import com.kiszka.prj.entities.MediaGallery;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MediaGalleryRepository extends JpaRepository<MediaGallery, Integer> {
    List<MediaGallery> findByParentId(int parentId);
    List<MediaGallery> findByKidId(int kidId);
    List<MediaGallery> findByMediaType(String mediaType);
}
