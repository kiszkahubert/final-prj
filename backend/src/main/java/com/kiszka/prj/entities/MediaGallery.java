package com.kiszka.prj.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "media_gallery")
@NoArgsConstructor @AllArgsConstructor @Getter @Setter
public class MediaGallery {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "media_id")
    private int mediaId;
    @Column(name = "media_type", nullable = false, length = 50)
    private String mediaType;
    @Column(name = "url", length = 255)
    private String url;
    @Column(name = "uploaded_at", nullable = false)
    private LocalDateTime uploadedAt;
    @Column(name = "parent_id")
    private Integer parentId;
    @Column(name = "kid_id")
    private Integer kidId;
}
