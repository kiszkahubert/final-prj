package com.kiszka.prj.DTOs;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserDTO {
    @NotBlank(message="username cant be empty")
    @Size(min=6,max=20,message="6-20 chars")
    private String username;
    @NotBlank(message="password cant be empty")
    @Size(min=6,max=20,message="6-20 chars")
    private String password;
}