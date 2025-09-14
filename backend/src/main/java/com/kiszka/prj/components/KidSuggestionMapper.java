package com.kiszka.prj.components;

import com.kiszka.prj.DTOs.KidSuggestionDTO;
import com.kiszka.prj.entities.KidSuggestion;
import org.springframework.stereotype.Component;

@Component
public class KidSuggestionMapper {
    public static KidSuggestionDTO toDTO(KidSuggestion suggestion) {
        return new KidSuggestionDTO(
                suggestion.getId(),
                suggestion.getDescription(),
                suggestion.getTitle(),
                suggestion.getProposedDate(),
                suggestion.getStatus(),
                suggestion.getCreatedAt(),
                suggestion.getReviewedAt(),
                suggestion.getReviewedBy() != null ? suggestion.getReviewedBy().getId() : null,
                suggestion.getCreatedBy() != null ? suggestion.getCreatedBy().getId() : null
        );
    }
}
