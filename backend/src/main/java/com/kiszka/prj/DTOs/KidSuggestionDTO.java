package com.kiszka.prj.DTOs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor @NoArgsConstructor
public class KidSuggestionDTO {
    private Integer id;
    private String description;
    private String title;
    private Date proposedStart;
    private Date proposedEnd;
    private String status;
    private Date createdAt;
    private Date reviewedAt;
    private Integer reviewedById;
    private Integer createdById;
}