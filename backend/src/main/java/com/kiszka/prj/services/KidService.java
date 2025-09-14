package com.kiszka.prj.services;

import com.kiszka.prj.entities.Kid;
import com.kiszka.prj.entities.Parent;
import com.kiszka.prj.repositories.KidRepository;
import com.kiszka.prj.repositories.ParentRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class KidService {
    private final KidRepository kidRepository;
    private final ParentRepository parentRepository;

    public KidService(KidRepository kidRepository, ParentRepository parentRepository) {
        this.kidRepository = kidRepository;
        this.parentRepository = parentRepository;
    }
    public Kid addKid(Kid kid, int parentId){
        Parent parent = parentRepository.findById(parentId)
                .orElseThrow(() -> new RuntimeException("Parent not found"));
        Kid savedKid = kidRepository.save(kid);
        parent.getKids().add(savedKid);
        savedKid.getParents().add(parent);
        parentRepository.save(parent);
        return savedKid;
    }
    public void deleteKid(int id) {
        kidRepository.deleteById(id);
    }
    public Optional<Kid> getKidById(int id) {
        return kidRepository.findById(id);
    }
    public boolean isParentOfKid(Integer parentId, Integer kidId) {
        return kidRepository.findById(kidId)
                .map(kid -> kid.getParents()
                        .stream()
                        .anyMatch(parent -> parent.getId() == parentId))
                .orElse(false);
    }
}
