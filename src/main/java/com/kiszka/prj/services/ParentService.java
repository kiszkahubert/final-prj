package com.kiszka.prj.services;

import com.kiszka.prj.entities.Kid;
import com.kiszka.prj.entities.Parent;
import com.kiszka.prj.repositories.KidRepository;
import com.kiszka.prj.repositories.ParentRepository;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class ParentService {
    private final ParentRepository parentRepository;
    private final KidRepository kidRepository;
    public ParentService(ParentRepository parentRepository, KidRepository kidRepository) {
        this.parentRepository = parentRepository;
        this.kidRepository = kidRepository;
    }
    public Parent addKidToParent(int parentId, int kidId) {
        Parent parent = parentRepository.findById(parentId).orElseThrow(() -> new RuntimeException("Parent not found"));
        Kid kid = kidRepository.findById(kidId).orElseThrow(() -> new RuntimeException("Kid not found"));
        parent.getKids().add(kid);
        kid.getParents().add(parent);
        return parentRepository.save(parent);
    }
    public Set<Kid> getKidsByParent(int parentId) {
        Parent parent = parentRepository.findById(parentId).orElseThrow(() -> new RuntimeException("Parent not found"));
        return parent.getKids();
    }
}
