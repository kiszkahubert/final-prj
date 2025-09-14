package com.kiszka.prj.repositories;

import com.kiszka.prj.entities.Kid;
import com.kiszka.prj.entities.KidSuggestion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface KidSuggestionRepository extends JpaRepository<KidSuggestion, Integer> {
    List<KidSuggestion> findByCreatedBy_Id(Integer kidId);
    List<KidSuggestion> findByCreatedByIn(List<Kid> kids);
}
