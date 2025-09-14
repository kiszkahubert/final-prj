package com.kiszka.prj.controllers;

import com.kiszka.prj.DTOs.KidSuggestionDTO;
import com.kiszka.prj.entities.Parent;
import com.kiszka.prj.services.KidSuggestionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/suggestions")
public class KidSuggestionController {
    private final KidSuggestionService suggestionService;

    public KidSuggestionController(KidSuggestionService suggestionService) {
        this.suggestionService = suggestionService;
    }
    @PostMapping("/create/{kidId}")
    public ResponseEntity<KidSuggestionDTO> createSuggestion(@PathVariable Integer kidId, @RequestBody KidSuggestionDTO suggestionDTO) {
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
    public ResponseEntity<List<KidSuggestionDTO>> getSuggestionsByKid(@PathVariable Integer kidId) {
        return ResponseEntity.ok(suggestionService.getSuggestionsByKid(kidId));
    }
    @GetMapping("/parent")
    public ResponseEntity<List<KidSuggestionDTO>> getSuggestionsForParent(Authentication authentication) {
        Parent parent = (Parent) authentication.getPrincipal();
        return ResponseEntity.ok(suggestionService.getSuggestionsForParent(parent.getId()));
    }
}
