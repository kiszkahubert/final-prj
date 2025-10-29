package com.kiszka.prj.DTOs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor @NoArgsConstructor
public class ChatMessageDTO {
    private String senderType;
    private int senderId;
    private String content;
    private LocalDateTime sentAt;
}
