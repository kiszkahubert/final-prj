package com.kiszka.prj.controllers;

import com.kiszka.prj.DTOs.ChatMessageDTO;
import com.kiszka.prj.DTOs.PeopleInfoDTO;
import com.kiszka.prj.configs.KidPrincipal;
import com.kiszka.prj.entities.Message;
import com.kiszka.prj.entities.Parent;
import com.kiszka.prj.services.JWTService;
import com.kiszka.prj.services.MessageService;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

@Controller
public class ChatController {
    private final MessageService messageService;
    private final JWTService jwtService;

    public ChatController(MessageService messageService, JWTService jwtService) {
        this.messageService = messageService;
        this.jwtService = jwtService;
    }
    @MessageMapping("/sendMessage")
    @SendTo("/topic/familyChat")
    public ChatMessageDTO sendMessage(ChatMessageDTO dto, Principal principal) {
        Integer senderId;
        String senderType;
        if (principal instanceof Authentication auth) {
            Parent parent = (Parent) auth.getPrincipal();
            senderId = parent.getId();
            senderType = "PARENT";
        } else if (principal instanceof KidPrincipal kidPrincipal) {
            senderId = kidPrincipal.getKidId();
            senderType = "KID";
        } else {
            throw new SecurityException("Unauthorized message attempt");
        }
        Message msg = new Message();
        msg.setSenderType(senderType);
        msg.setSenderId(senderId);
        msg.setContent(dto.getContent());
        var currTime = LocalDateTime.now();
        msg.setSentAt(currTime);
        messageService.saveMessage(msg);
        dto.setSenderType(senderType);
        dto.setSenderId(senderId);
        dto.setSentAt(currTime);
        return dto;
    }
    @GetMapping("/api/chat/messages")
    @ResponseBody
    public ResponseEntity<List<Message>> getAllMessages(Authentication authentication, @RequestHeader("Authorization") String authHeader){
        String token = authHeader.substring(7);
        Integer kidId = jwtService.extractKidId(token);
        if(kidId != null){
            return ResponseEntity.ok(messageService.getAllRelatedMessagesForKid(kidId));
        }
        Parent parent = (Parent) authentication.getPrincipal();
        return ResponseEntity.ok(messageService.getAllRelatedMessagesForParent(parent.getId()));
    }
    @GetMapping("/api/family/people")
    @ResponseBody
    public ResponseEntity<List<PeopleInfoDTO>> getFamilyCredentials(Authentication authentication, @RequestHeader("Authorization") String authHeader){
        String token = authHeader.substring(7);
        Integer kidId = jwtService.extractKidId(token);
        if(kidId != null){
            return ResponseEntity.ok(messageService.getAllRelatedPeopleForKid(kidId));
        }
        Parent parent = (Parent) authentication.getPrincipal();
        return ResponseEntity.ok(messageService.getAllRelatedPeopleForParent(parent.getId()));
    }
    @PostMapping("/chat/messages")
    @ResponseBody
    public ResponseEntity<?> saveNewMessage(
            @RequestBody ChatMessageDTO chatMessageDTO
    ){
        Message message = new Message();
        message.setSenderType(chatMessageDTO.getSenderType());
        message.setSenderId(chatMessageDTO.getSenderId());
        message.setContent(chatMessageDTO.getContent());
        messageService.saveMessage(message);
        return ResponseEntity.ok().build();
    }
}
