package com.kiszka.prj.repositories;

import com.kiszka.prj.entities.Kid;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface KidRepository extends JpaRepository<Kid, Integer> {}
