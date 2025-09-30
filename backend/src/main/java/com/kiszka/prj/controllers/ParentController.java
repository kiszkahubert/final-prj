package com.kiszka.prj.controllers;

import com.kiszka.prj.DTOs.ParentDTO;
import com.kiszka.prj.entities.Parent;
import com.kiszka.prj.services.ParentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/parent")
public class ParentController {
    private final ParentService parentService;
    public ParentController(ParentService parentService) {
        this.parentService = parentService;
    }
    @PutMapping("/username")
    public ResponseEntity<Void> updateUsername(Authentication authentication, @RequestParam String newUsername) {
        Parent parent = (Parent) authentication.getPrincipal();
        parentService.updateUsername(parent.getId(), newUsername);
        return ResponseEntity.ok().build();
    }
    @PutMapping("/password")
    public ResponseEntity<Void> updatePassword(Authentication authentication, @RequestParam String oldPassword, @RequestParam String newPassword) {
        Parent parent = (Parent) authentication.getPrincipal();
        parentService.updatePassword(parent.getId(), oldPassword, newPassword);
        return ResponseEntity.ok().build();
    }
    @DeleteMapping
    public ResponseEntity<Void> deleteParent(Authentication authentication) {
        Parent parent = (Parent) authentication.getPrincipal();
        parentService.deleteParent(parent.getId());
        return ResponseEntity.noContent().build();
    }
}