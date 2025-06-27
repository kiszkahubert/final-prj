package com.kiszka.prj.repositories;

import com.kiszka.prj.entities.KidsTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface KidsTaskRepository extends JpaRepository<KidsTask, KidsTask.KidsTaskId> {
    List<KidsTask> findByTaskId(Integer taskId);
    List<KidsTask> findByKidId(Integer kidId);
    List<KidsTask> findByParentId(Integer parentId);
    @Modifying
    @Query("DELETE FROM KidsTask kt WHERE kt.taskId = :taskId")
    void deleteByTaskId(@Param("taskId") Integer taskId);
    @Modifying
    @Query("DELETE FROM KidsTask kt WHERE kt.taskId = :taskId AND kt.kidId = :kidId")
    void deleteByTaskIdAndKidId(@Param("taskId") Integer taskId, @Param("kidId") Integer kidId);
    @Modifying
    @Query("DELETE FROM KidsTask kt WHERE kt.taskId = :taskId AND kt.parentId = :parentId AND kt.kidId = :kidId")
    void deleteByTaskIdAndParentIdAndKidId(@Param("taskId") Integer taskId, @Param("parentId") Integer parentId, @Param("kidId") Integer kidId);
}
