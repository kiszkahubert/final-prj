package com.kiszka.prj.services;

import com.kiszka.prj.entities.Kid;
import com.kiszka.prj.repositories.KidRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class KidService {
    private final KidRepository kidRepository;
    public KidService(KidRepository kidRepository) {
        this.kidRepository = kidRepository;
    }
    public Kid addKid(Kid kid){
        return kidRepository.save(kid);
    }
    public void deleteKid(int id) {
        kidRepository.deleteById(id);
    }
    public Optional<Kid> getKidById(int id) {
        return kidRepository.findById(id);
    }
    public List<Kid> getAllKids() {
        return kidRepository.findAll();
    }
}
