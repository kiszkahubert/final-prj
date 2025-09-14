package com.kiszka.prj.controllers;

import com.kiszka.prj.DTOs.KidSuggestionDTO;
import com.kiszka.prj.entities.Parent;
import com.kiszka.prj.services.JWTService;
import com.kiszka.prj.services.KidSuggestionService;
import com.kiszka.prj.services.ParentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/suggestions")
public class KidSuggestionController {
    private final KidSuggestionService suggestionService;
    private final ParentService parentService;
    private final JWTService jWTService;

    public KidSuggestionController(KidSuggestionService suggestionService, ParentService parentService, JWTService jWTService) {
        this.suggestionService = suggestionService;
        this.parentService = parentService;
        this.jWTService = jWTService;
    }
    @PostMapping("/create")
    public ResponseEntity<KidSuggestionDTO> createSuggestion(@RequestHeader("Authorization") String authHeader, @RequestBody KidSuggestionDTO suggestionDTO) {
        String token = authHeader.substring(7);
        Integer kidId = jWTService.extractKidId(token);
        return ResponseEntity.ok(
                suggestionService.createSuggestion(
                        kidId,
                        suggestionDTO.getTitle(),
                        suggestionDTO.getDescription()
                )
        );
    }
    @PostMapping("/{suggestionId}/review")
    public ResponseEntity<KidSuggestionDTO> reviewSuggestion(@PathVariable Integer suggestionId, @RequestParam boolean accepted, Authentication authentication) {
        Parent parent = (Parent) authentication.getPrincipal();
        return ResponseEntity.ok(suggestionService.reviewSuggestion(suggestionId, parent.getId(), accepted));
    }
    @GetMapping("/kid/{kidId}")
    public ResponseEntity<List<KidSuggestionDTO>> getSuggestionsByKid(@PathVariable Integer kidId, Authentication authentication) {
        Parent parent = (Parent) authentication.getPrincipal();
        var kids = parentService.getKidsByParent(parent.getId());
        boolean hasAccess = kids.stream().anyMatch(k->k.getId() == kidId);
        if(!hasAccess){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }
        return ResponseEntity.ok(suggestionService.getSuggestionsByKid(kidId));
    }
    @GetMapping("/parent")
    public ResponseEntity<List<KidSuggestionDTO>> getSuggestionsForParent(Authentication authentication) {
        Parent parent = (Parent) authentication.getPrincipal();
        return ResponseEntity.ok(suggestionService.getSuggestionsForParent(parent.getId()));
    }
}
