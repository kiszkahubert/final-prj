package com.kiszka.prj.DTOs;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor @AllArgsConstructor @Getter @Setter
public class TaskResponseDTO {
    private Integer taskId;
    private String title;
    private String description;
    private LocalDateTime taskStart;
    private LocalDateTime taskEnd;
    private String status;
    private String note;
    private Integer parentId;
    private List<AssignedKidDTO> assignedKids;
}

@NoArgsConstructor @AllArgsConstructor @Getter @Setter
class AssignedKidDTO{
    private Integer kidId;
    private String isSynced;
}