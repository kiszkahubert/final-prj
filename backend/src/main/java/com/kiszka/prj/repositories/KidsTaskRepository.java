package com.kiszka.prj.repositories;

import com.kiszka.prj.entities.KidsTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface KidsTaskRepository extends JpaRepository<KidsTask, KidsTask.KidsTaskId> {
    void deleteByTaskId(int taskId);
    void deleteByTaskIdAndKidId(int taskId, int kidId);
    List<KidsTask> findByKidIdAndParentIdAndIsSynced(Integer kidId, Integer parentId, String isSynced);
    List<KidsTask> findByTaskIdIn(List<Integer> taskIds);
    Optional<KidsTask> findByTaskIdAndKidId(Integer taskId, Integer kidId);
}
