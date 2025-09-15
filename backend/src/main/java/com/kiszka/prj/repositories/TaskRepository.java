package com.kiszka.prj.repositories;

import com.kiszka.prj.entities.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Integer> {
    List<Task> findByParentId(Integer parentId);
    @Query("SELECT t FROM Task t JOIN t.kidsAssignments kt WHERE kt.kidId = :kidId")
    List<Task> findTasksAssignedToKid(@Param("kidId") Integer kidId);

}
