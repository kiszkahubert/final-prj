package com.kiszka.prj.DTOs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PinLoginResponseDTO {
    private String token;
    private long expiresIn;
    private KidDTO kid;
    private List<TaskDTO> tasks;
}