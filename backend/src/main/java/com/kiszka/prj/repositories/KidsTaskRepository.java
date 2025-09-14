package com.kiszka.prj.repositories;

import com.kiszka.prj.entities.KidsTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KidsTaskRepository extends JpaRepository<KidsTask, KidsTask.KidsTaskId> {
    void deleteByTaskId(int taskId);
    void deleteByTaskIdAndKidId(int taskId, int kidId);
}
