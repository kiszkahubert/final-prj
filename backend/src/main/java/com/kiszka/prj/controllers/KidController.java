package com.kiszka.prj.controllers;

import com.kiszka.prj.DTOs.KidDTO;
import com.kiszka.prj.components.KidMapper;
import com.kiszka.prj.entities.Kid;
import com.kiszka.prj.entities.Parent;
import com.kiszka.prj.services.ChildAccessTokenService;
import com.kiszka.prj.services.KidService;
import com.kiszka.prj.services.ParentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;


@RestController
@RequestMapping("/api/kids")
public class KidController {
    private final KidService kidService;
    private final ChildAccessTokenService childAccessTokenService;
    private final ParentService parentService;

    public KidController(KidService kidService, ChildAccessTokenService childAccessTokenService, ParentService parentService) {
        this.kidService = kidService;
        this.childAccessTokenService = childAccessTokenService;
        this.parentService = parentService;
    }
    @PostMapping("/new")
    public ResponseEntity<KidDTO> addKid(@RequestBody Kid kid, Authentication authentication) {
        Parent parent = (Parent) authentication.getPrincipal();
        Kid savedKid = kidService.addKid(kid,parent.getId());
        KidDTO kidDTO = KidMapper.toDTO(savedKid);
        childAccessTokenService.generateTokenForParent(parent, savedKid);
        return ResponseEntity.ok(kidDTO);
    }
    @DeleteMapping("/{kidId}")
    public ResponseEntity<String> deleteKid(Authentication authentication, @PathVariable int kidId) {
        Parent parent = (Parent) authentication.getPrincipal();
        var kids = parentService.getKidsByParent(parent.getId());
        System.out.println(kids.toString());
        boolean hasAccess = kids.stream().anyMatch(k->k.getId() == kidId);
        if(!hasAccess){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }
        if (kidService.getKidById(kidId).isPresent()) {
            kidService.deleteKid(kidId);
            return ResponseEntity.ok("Kid deleted");
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    @Deprecated
    @GetMapping("/{kidId}")
    public ResponseEntity<KidDTO> getKid(Authentication authentication,@PathVariable int kidId) {
        Parent parent = (Parent) authentication.getPrincipal();
        var kids = parentService.getKidsByParent(parent.getId());
        boolean hasAccess = kids.stream().anyMatch(k->k.getId() == kidId);
        if(!hasAccess){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }
        return kidService.getKidById(kidId)
                .map(KidMapper::toDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    @PutMapping("/{kidId}")
    public ResponseEntity<KidDTO> updateKid(@PathVariable int kidId, @RequestBody Kid updatedKid, Authentication authentication) {
        Parent parent = (Parent) authentication.getPrincipal();
        var kids = parentService.getKidsByParent(parent.getId());
        boolean hasAccess = kids.stream().anyMatch(k -> k.getId() == kidId);
        if (!hasAccess) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }
        Optional<Kid> existingKidOpt = kidService.getKidById(kidId);
        if (existingKidOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Kid updatedKidResult = kidService.updateKid(kidId, updatedKid);
        KidDTO kidDTO = KidMapper.toDTO(updatedKidResult);
        return ResponseEntity.ok(kidDTO);
    }
    //TODO CHECK THIS ENDPOINT
    @GetMapping
    public ResponseEntity<List<KidDTO>> getAllKids(Authentication authentication) {
        Parent parent = (Parent) authentication.getPrincipal();
        var kids = parentService.getKidsByParent(parent.getId()).stream()
                .map(KidMapper::toDTO)
                .toList();
        return ResponseEntity.ok(kids);
    }
}
