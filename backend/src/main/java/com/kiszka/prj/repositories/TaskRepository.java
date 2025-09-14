package com.kiszka.prj.repositories;

import com.kiszka.prj.entities.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Integer> {
    List<Task> findByParentId(Integer parentId);
    List<Task> findTasksAssignedToKid(Integer kidId);
}
