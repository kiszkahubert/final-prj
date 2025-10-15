package com.kiszka.prj.DTOs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KidDTO {
    private int id;
    private String name;
    private LocalDate birthDate;
    private List<Integer> parents;
}
