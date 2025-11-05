package com.kiszka.prj.DTOs;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor @NoArgsConstructor
public class ParentDTO {
    @NotBlank(message="username cant be empty")
    @Size(min=6,max=20,message="6-20 chars")
    private String username;
    @NotBlank(message="password cant be empty")
    @Size(min=6,max=20,message="6-20 chars")
    private String password;
}