package com.kiszka.prj.repositories;

import com.kiszka.prj.entities.ParentKid;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ParentKidRepository extends JpaRepository<ParentKid, ParentKid.ParentKidId> {
    List<ParentKid> findByParentId(int parentId);
    List<ParentKid> findByKidId(int kidId);
    Optional<ParentKid> findByParentIdAndKidId(int parentId, int kidId);
    void deleteByParentIdAndKidId(int parentId, int kidId);
}
