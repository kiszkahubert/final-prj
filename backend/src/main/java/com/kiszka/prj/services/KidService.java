package com.kiszka.prj.services;

import com.kiszka.prj.entities.Kid;
import com.kiszka.prj.entities.Parent;
import com.kiszka.prj.repositories.KidRepository;
import com.kiszka.prj.repositories.ParentRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

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
    @Transactional
    public void deleteKid(int id) {
        if (kidRepository.existsById(id)) {
            kidRepository.deleteKidNative(id);
        }
    }
    public Optional<Kid> getKidById(int id) {
        return kidRepository.findById(id);
    }
    public Kid updateKid(int kidId, Kid updatedKid) {
        return kidRepository.findById(kidId)
                .map(existingKid -> {
                    if (updatedKid.getName() != null) {
                        existingKid.setName(updatedKid.getName());
                    }
                    if (updatedKid.getBirthDate() != null) {
                        existingKid.setBirthDate(updatedKid.getBirthDate());
                    }
                    return kidRepository.save(existingKid);
                })
                .orElseThrow(() -> new RuntimeException("Kid not found"));
    }
    public String getKidNameById(Integer kidId) {
        return kidRepository.findNameById(kidId);
    }
}
