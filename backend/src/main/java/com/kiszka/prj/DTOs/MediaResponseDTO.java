package com.kiszka.prj.DTOs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor @NoArgsConstructor
public class MediaResponseDTO {
    private int mediaId;
    private String mediaType;
    private String url;
    private LocalDateTime uploadedAt;
    private String uploadByUsername;
}
