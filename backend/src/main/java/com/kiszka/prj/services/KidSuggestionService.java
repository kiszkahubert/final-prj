package com.kiszka.prj.services;

import com.kiszka.prj.DTOs.KidSuggestionDTO;
import com.kiszka.prj.components.KidSuggestionMapper;
import com.kiszka.prj.entities.Kid;
import com.kiszka.prj.entities.KidSuggestion;
import com.kiszka.prj.entities.Parent;
import com.kiszka.prj.repositories.KidRepository;
import com.kiszka.prj.repositories.KidSuggestionRepository;
import com.kiszka.prj.repositories.ParentRepository;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static com.kiszka.prj.components.KidSuggestionMapper.toDTO;

@Service
public class KidSuggestionService {
    private final KidSuggestionRepository suggestionRepository;
    private final KidRepository kidRepository;
    private final ParentRepository parentRepository;

    public KidSuggestionService(KidSuggestionRepository suggestionRepository, KidRepository kidRepository, ParentRepository parentRepository) {
        this.suggestionRepository = suggestionRepository;
        this.kidRepository = kidRepository;
        this.parentRepository = parentRepository;
    }
    public KidSuggestionDTO createSuggestion(Integer kidId, String description, String title, Date startDate, Date endDate) {
        Kid kid = kidRepository.findById(kidId).orElseThrow(() -> new RuntimeException("Kid not found"));
        KidSuggestion suggestion = new KidSuggestion();
        suggestion.setDescription(description);
        suggestion.setTitle(title);
        suggestion.setProposedStart(startDate);
        suggestion.setProposedEnd(endDate);
        suggestion.setStatus("PENDING");
        suggestion.setCreatedAt(new Date());
        suggestion.setCreatedBy(kid);
        return toDTO(suggestionRepository.save(suggestion));
    }
    public KidSuggestionDTO reviewSuggestion(Integer suggestionId, Integer parentId, boolean accepted) {
        KidSuggestion suggestion = suggestionRepository.findById(suggestionId).orElseThrow(() -> new RuntimeException("Suggestion not found"));
        Parent parent = parentRepository.findById(parentId).orElseThrow(() -> new RuntimeException("Parent not found"));
        Kid kid = suggestion.getCreatedBy();
        if (!parent.getKids().contains(kid)) {
            throw new RuntimeException("Brak dostepu propozycja nie nalezy do dziecka tego rodzica");
        }
        suggestion.setReviewedBy(parent);
        suggestion.setReviewedAt(new Date());
        suggestion.setStatus(accepted ? "ACCEPTED" : "REJECTED");
        return toDTO(suggestionRepository.save(suggestion));
    }
    public List<KidSuggestionDTO> getSuggestionsByKid(Integer kidId) {
        return suggestionRepository.findByCreatedBy_Id(kidId).stream()
                .map(KidSuggestionMapper::toDTO)
                .collect(Collectors.toList());
    }
    public KidSuggestionDTO getSuggestionById(Integer suggestionId) {
        KidSuggestion suggestion = suggestionRepository.findById(suggestionId).orElseThrow(() -> new RuntimeException("Suggestion not found with id: " + suggestionId));
        return KidSuggestionMapper.toDTO(suggestion);
    }
    public void deleteSuggestion(Integer suggestionId){
        suggestionRepository.deleteById(suggestionId);
    }
    public List<KidSuggestionDTO> getSuggestionsForParent(Integer parentId) {
        Parent parent = parentRepository.findById(parentId).orElseThrow(() -> new RuntimeException("Parent not found"));
        return suggestionRepository.findByCreatedByIn(parent.getKids().stream().toList()).stream()
                .map(KidSuggestionMapper::toDTO)
                .collect(Collectors.toList());
    }
}