package com.kiszka.prj.repositories;

import com.kiszka.prj.entities.Kid;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface KidRepository extends JpaRepository<Kid, Integer> {
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM kids WHERE kid_id = :kidId", nativeQuery = true)
    void deleteKidNative(@Param("kidId") int kidId);
    @Query("SELECT k.name FROM Kid k WHERE k.id = :id")
    String findNameById(@Param("id") Integer id);
}
