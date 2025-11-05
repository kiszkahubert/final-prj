package com.kiszka.prj;

import com.kiszka.prj.DTOs.KidSuggestionDTO;
import com.kiszka.prj.entities.Kid;
import com.kiszka.prj.entities.KidSuggestion;
import com.kiszka.prj.entities.Parent;
import com.kiszka.prj.repositories.KidRepository;
import com.kiszka.prj.repositories.KidSuggestionRepository;
import com.kiszka.prj.repositories.ParentRepository;
import com.kiszka.prj.services.KidSuggestionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class KidSuggestionServiceTest {
    @Mock
    KidSuggestionRepository suggestionRepository;
    @Mock
    KidRepository kidRepository;
    @Mock
    ParentRepository parentRepository;
    @InjectMocks
    KidSuggestionService suggestionService;
    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getPendingSuggestionsForParent_shouldReturnMappedSuggestions() {
        // given
        Parent parent = new Parent();
        Kid kid = new Kid();
        kid.setId(1);
        parent.setKids(Set.of(kid));
        KidSuggestion suggestion = new KidSuggestion();
        suggestion.setId(1);
        suggestion.setCreatedBy(kid);
        suggestion.setStatus("PENDING");
        when(parentRepository.findById(1)).thenReturn(Optional.of(parent));
        when(suggestionRepository.findByCreatedByInAndStatus(anyList(), eq("PENDING"))).thenReturn(List.of(suggestion));
        // when
        List<KidSuggestionDTO> result = suggestionService.getPendingSuggestionsForParent(1);
        // then
        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getId());
    }
    @Test
    void createSuggestion_shouldSaveAndReturnDTO() {
        // given
        Kid kid = new Kid();
        kid.setId(1);
        when(kidRepository.findById(1)).thenReturn(Optional.of(kid));
        KidSuggestion saved = new KidSuggestion();
        saved.setId(1);
        saved.setCreatedBy(kid);
        saved.setTitle("Test");
        when(suggestionRepository.save(any())).thenReturn(saved);
        // when
        KidSuggestionDTO dto = suggestionService.createSuggestion(1, "description", "title", LocalDateTime.now(), LocalDateTime.now());
        // then
        assertEquals(1,dto.getId());
        verify(suggestionRepository).save(any());
    }
    @Test
    void reviewSuggestion_shouldAcceptSuggestion() {
        // given
        Parent parent = new Parent();
        parent.setId(1);
        Kid kid = new Kid();
        kid.setId(1);
        parent.setKids(Set.of(kid));
        KidSuggestion suggestion = new KidSuggestion();
        suggestion.setId(1);
        suggestion.setCreatedBy(kid);
        suggestion.setProposedEnd(LocalDateTime.now().plusMinutes(1));
        when(suggestionRepository.findById(1)).thenReturn(Optional.of(suggestion));
        when(parentRepository.findById(1)).thenReturn(Optional.of(parent));
        when(suggestionRepository.save(any())).thenReturn(suggestion);
        // when
        KidSuggestionDTO dto = suggestionService.reviewSuggestion(1, 1, true);
        // then
        assertEquals("ACCEPTED", suggestion.getStatus());
        assertEquals("ACCEPTED", dto.getStatus());
        verify(suggestionRepository).save(suggestion);
    }
    @Test
    void reviewSuggestion_shouldThrowIfNotParentOfKid() {
        // given
        Parent parent = new Parent();
        parent.setId(1);
        parent.setKids(Set.of());
        Kid kid = new Kid();
        kid.setId(1);
        KidSuggestion suggestion = new KidSuggestion();
        suggestion.setCreatedBy(kid);
        suggestion.setProposedEnd(LocalDateTime.now().plusHours(1));
        when(suggestionRepository.findById(1)).thenReturn(Optional.of(suggestion));
        when(parentRepository.findById(1)).thenReturn(Optional.of(parent));
        // then
        assertThrows(RuntimeException.class, () -> suggestionService.reviewSuggestion(1, 1, true));
    }
    @Test
    void reviewSuggestion_shouldThrowIfAfterDeadline() {
        // given
        Parent parent = new Parent();
        parent.setId(1);
        Kid kid = new Kid();
        kid.setId(1);
        parent.setKids(Set.of(kid));
        KidSuggestion suggestion = new KidSuggestion();
        suggestion.setCreatedBy(kid);
        suggestion.setProposedEnd(LocalDateTime.now().minusHours(1));
        when(suggestionRepository.findById(1)).thenReturn(Optional.of(suggestion));
        when(parentRepository.findById(1)).thenReturn(Optional.of(parent));
        // then
        assertThrows(RuntimeException.class, () -> suggestionService.reviewSuggestion(1, 1, true));
    }
}
