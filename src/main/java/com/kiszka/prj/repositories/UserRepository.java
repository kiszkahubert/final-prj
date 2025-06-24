package com.kiszka.prj.repositories;

import com.kiszka.prj.entities.Parent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<Parent, Integer> {
    Optional<Parent> findByUsername(String username);
}
