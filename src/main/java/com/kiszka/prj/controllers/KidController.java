package com.kiszka.prj.controllers;

import com.kiszka.prj.entities.Kid;
import com.kiszka.prj.services.KidService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/kids")
public class KidController {
    private final KidService kidService;
    public KidController(KidService kidService) {
        this.kidService = kidService;
    }
    @PostMapping("/new")
    public ResponseEntity<Kid> addKid(@RequestBody Kid kid) {
        try {
            Kid savedKid = kidService.addKid(kid);
            return new ResponseEntity<>(savedKid, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteKid(@PathVariable int id) {
        try {
            if (kidService.getKidById(id).isPresent()) {
                kidService.deleteKid(id);
                return new ResponseEntity<>("Kid deleted", HttpStatus.OK);
            } else {
                return new ResponseEntity<>("Kid of such ID does not exist", HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return new ResponseEntity<>("Error deleting kid", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @GetMapping("/{id}")
    public ResponseEntity<Kid> getKidById(@PathVariable int id) {
        return kidService.getKidById(id)
                .map(kid -> new ResponseEntity<>(kid, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
}
