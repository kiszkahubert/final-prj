package com.kiszka.prj.repositories;

import com.kiszka.prj.entities.MediaGallery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MediaGalleryRepository extends JpaRepository<MediaGallery, Integer> {
    List<MediaGallery> findByParentId(int parentId);
    List<MediaGallery> findByKidId(int kidId);
}
