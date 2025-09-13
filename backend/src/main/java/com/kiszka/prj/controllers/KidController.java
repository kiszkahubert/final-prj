package com.kiszka.prj.controllers;

import com.kiszka.prj.DTOs.KidDTO;
import com.kiszka.prj.components.KidMapper;
import com.kiszka.prj.entities.Kid;
import com.kiszka.prj.entities.Parent;
import com.kiszka.prj.services.ChildAccessTokenService;
import com.kiszka.prj.services.KidService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/kids")
public class KidController {
    private final KidService kidService;
    private final ChildAccessTokenService childAccessTokenService;
    public KidController(KidService kidService, ChildAccessTokenService childAccessTokenService) {
        this.kidService = kidService;
        this.childAccessTokenService = childAccessTokenService;
    }
    @PostMapping("/new")
    public ResponseEntity<KidDTO> addKid(@RequestBody Kid kid, Authentication authentication) {
        try {
            Parent parent = (Parent) authentication.getPrincipal();
            Kid savedKid = kidService.addKid(kid,parent.getId());
            KidDTO kidDTO = KidMapper.toDTO(savedKid);
            childAccessTokenService.generateTokenForParent(parent, savedKid);
            return ResponseEntity.ok(kidDTO);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteKid(@PathVariable int id) {
        try {
            if (kidService.getKidById(id).isPresent()) {
                kidService.deleteKid(id);
                return new ResponseEntity<>("Kid deleted", HttpStatus.OK);
            } else {
                return new ResponseEntity<>("Kid of such ID does not exist", HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return new ResponseEntity<>("Error deleting kid", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @GetMapping("/{id}")
    public ResponseEntity<KidDTO> getKid(@PathVariable int id) {
        return kidService.getKidById(id)
                .map(KidMapper::toDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    @GetMapping
    public ResponseEntity<List<KidDTO>> getAllKids(Authentication authentication) {
        Parent parent = (Parent) authentication.getPrincipal();
        List<KidDTO> kids = kidService.getKidsByParentId(parent.getId()).stream()
                .map(KidMapper::toDTO)
                .toList();
        return ResponseEntity.ok(kids);
    }
}
