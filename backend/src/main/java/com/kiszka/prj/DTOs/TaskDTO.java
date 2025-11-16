package com.kiszka.prj.DTOs;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor @AllArgsConstructor @Getter @Setter
public class TaskDTO {
    private Integer taskId;
    private String title;
    private String description;
    private LocalDateTime taskStart;
    private LocalDateTime taskEnd;
    private String status;
    private String note;
    private Integer parentId;
    private List<Integer> kidIds;
}