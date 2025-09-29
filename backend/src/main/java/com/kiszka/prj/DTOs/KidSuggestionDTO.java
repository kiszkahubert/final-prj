package com.kiszka.prj.DTOs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

@Data
@AllArgsConstructor @NoArgsConstructor
public class KidSuggestionDTO {
    private Integer id;
    private String description;
    private String title;
    private LocalDateTime proposedStart;
    private LocalDateTime proposedEnd;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime reviewedAt;
    private Integer reviewedById;
    private Integer createdById;
}