package com.kiszka.prj.repositories;

import com.kiszka.prj.entities.ChildAccessToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChildAccessTokenRepository extends JpaRepository<ChildAccessToken, Integer> {
    Optional<ChildAccessToken> findByPin(String pin);
    List<ChildAccessToken> findAllByParent_Id(int parentId);
    Optional<ChildAccessToken> findByQrHash(String qrHash);
}
