package com.kiszka.prj.DTOs;

import lombok.*;

import java.util.List;


@NoArgsConstructor @AllArgsConstructor @Getter @Setter
public class TaskWithKidsDTO extends TaskDTO{
    private List<String> kidNames;
}
