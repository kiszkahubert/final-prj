package com.kiszka.prj.services;

import com.kiszka.prj.entities.Kid;
import com.kiszka.prj.entities.Parent;
import com.kiszka.prj.repositories.KidRepository;
import com.kiszka.prj.repositories.ParentRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
public class ParentService {
    private final ParentRepository parentRepository;
    private final PasswordEncoder passwordEncoder;
    public ParentService(ParentRepository parentRepository, PasswordEncoder passwordEncoder) {
        this.parentRepository = parentRepository;
        this.passwordEncoder = passwordEncoder;
    }
    public Set<Kid> getKidsByParent(int parentId) {
        Parent parent = parentRepository.findById(parentId).orElseThrow(() -> new RuntimeException("Parent not found"));
        Set<Kid> familyKids = new HashSet<>();
        for (Kid kid : parent.getKids()) {
            familyKids.add(kid);
            for (Parent otherParent : kid.getParents()) {
                familyKids.addAll(otherParent.getKids());
            }
        }
        return familyKids;
    }
    public Optional<Parent> getParentById(int parentId) {
        return parentRepository.findById(parentId);
    }
    public Parent updateUsername(int parentId, String newUsername) {
        Parent parent = parentRepository.findById(parentId).orElseThrow(() -> new RuntimeException("Parent not found"));
        parent.setUsername(newUsername);
        return parentRepository.save(parent);
    }
    public Parent updatePassword(int parentId, String newPassword) {
        Parent parent = parentRepository.findById(parentId).orElseThrow(() -> new RuntimeException("Parent not found"));
        parent.setPassword(passwordEncoder.encode(newPassword));
        return parentRepository.save(parent);
    }
    public void deleteParent(int parentId) {
        if (!parentRepository.existsById(parentId)) {
            throw new RuntimeException("Parent not found");
        }
        parentRepository.deleteById(parentId);
    }
}
