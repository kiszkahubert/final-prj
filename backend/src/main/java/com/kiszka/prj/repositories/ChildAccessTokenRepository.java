package com.kiszka.prj.repositories;

import com.kiszka.prj.entities.ChildAccessToken;
import com.kiszka.prj.entities.Parent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChildAccessTokenRepository extends JpaRepository<ChildAccessToken, Integer> {
    Optional<ChildAccessToken> findByParentId(int parentId);
    Optional<ChildAccessToken> findByPin(String pin);
}
