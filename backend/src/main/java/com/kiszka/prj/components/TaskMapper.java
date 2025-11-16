package com.kiszka.prj.components;

import com.kiszka.prj.DTOs.TaskDTO;
import com.kiszka.prj.entities.KidsTask;
import com.kiszka.prj.entities.Task;
import org.springframework.stereotype.Component;

@Component
public class TaskMapper {
    public static TaskDTO toDTO(Task task) {
        return new TaskDTO(
                task.getTaskId(),
                task.getTitle(),
                task.getDescription(),
                task.getTaskStart(),
                task.getTaskEnd(),
                task.getStatus(),
                task.getNote(),
                task.getParentId(),
                task.getKidsAssignments().stream().map(KidsTask::getKidId).toList()
        );
    }
}
