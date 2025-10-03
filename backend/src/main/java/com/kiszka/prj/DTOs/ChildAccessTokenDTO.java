package com.kiszka.prj.DTOs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor @NoArgsConstructor
public class ChildAccessTokenDTO {
    private String pin;
    private String qrHash;
    private int kidId;
}
