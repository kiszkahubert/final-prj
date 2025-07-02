package com.kiszka.prj.repositories;

import com.kiszka.prj.entities.KidsTask;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface KidsTaskRepository extends JpaRepository<KidsTask, KidsTask.KidsTaskId> {
    List<KidsTask> findByTaskId(int taskId);
    List<KidsTask> findByKidId(int kidId);
    List<KidsTask> findByParentId(int parentId);
    void deleteByTaskId(int taskId);
    void deleteByTaskIdAndKidId(int taskId, int kidId);
    void deleteByTaskIdAndParentIdAndKidId(int taskId, int parentId, int kidId);
}
