package com.kiszka.prj.services;

import com.kiszka.prj.DTOs.TaskDTO;
import com.kiszka.prj.DTOs.TaskWithKidsDTO;
import com.kiszka.prj.components.TaskMapper;
import com.kiszka.prj.entities.Kid;
import com.kiszka.prj.entities.KidsTask;
import com.kiszka.prj.entities.Parent;
import com.kiszka.prj.entities.Task;
import com.kiszka.prj.repositories.KidsTaskRepository;
import com.kiszka.prj.repositories.TaskRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class TaskService {
    private final TaskRepository taskRepository;
    private final KidsTaskRepository kidsTaskRepository;
    private final ParentService parentService;
    private final KidService kidService;
    public TaskService(TaskRepository taskRepository, KidsTaskRepository kidsTaskRepository, ParentService parentService, KidService kidService) {
        this.taskRepository = taskRepository;
        this.kidsTaskRepository = kidsTaskRepository;
        this.parentService = parentService;
        this.kidService = kidService;
    }
    public Task createTask(TaskDTO taskDTO, int parentId) {
        Task task = new Task();
        task.setTitle(taskDTO.getTitle());
        task.setDescription(taskDTO.getDescription());
        task.setTaskStart(taskDTO.getTaskStart());
        task.setTaskEnd(taskDTO.getTaskEnd());
        task.setStatus(taskDTO.getStatus());
        task.setNote(taskDTO.getNote());
        task.setParentId(parentId);
        Task savedTask = taskRepository.save(task);
        if (taskDTO.getKidIds() != null && !taskDTO.getKidIds().isEmpty()) {
            assignTaskToKids(savedTask, taskDTO.getKidIds(), taskDTO.getIsSynced());
        }
        return savedTask;
    }
    public Task updateTask(Integer taskId, TaskDTO taskDTO) {
        Task task = taskRepository.findById(taskId).orElseThrow(() -> new RuntimeException("Task not found"));
        task.setTitle(taskDTO.getTitle());
        task.setDescription(taskDTO.getDescription());
        task.setTaskStart(taskDTO.getTaskStart());
        task.setTaskEnd(taskDTO.getTaskEnd());
        task.setStatus(taskDTO.getStatus());
        task.setNote(taskDTO.getNote());
        Task updatedTask = taskRepository.save(task);
        if (taskDTO.getKidIds() != null) {
            updateTaskAssignments(updatedTask, taskDTO.getKidIds(), taskDTO.getIsSynced());
        }
        return updatedTask;
    }
    public void deleteTask(Integer taskId) {
        Task task = taskRepository.findById(taskId).orElseThrow(() -> new RuntimeException("Task not found"));
        kidsTaskRepository.deleteByTaskId(taskId);
        taskRepository.delete(task);
    }
    private void assignTaskToKids(Task task, List<Integer> kidIds, String isSynced) {
        for (Integer kidId : kidIds) {
            KidsTask kidsTask = new KidsTask();
            kidsTask.setTaskId(task.getTaskId());
            kidsTask.setParentId(task.getParentId());
            kidsTask.setKidId(kidId);
            kidsTask.setIsSynced(isSynced);
            kidsTask.setTask(task);
            kidsTaskRepository.save(kidsTask);
        }
    }
    private void updateTaskAssignments(Task task, List<Integer> kidIds, String isSynced) {
        kidsTaskRepository.deleteByTaskId(task.getTaskId());
        assignTaskToKids(task, kidIds, isSynced);
    }
    public Optional<Task> getTaskById(Integer taskId) {
        return taskRepository.findById(taskId);
    }
    public List<Task> getTasksByParentId(Integer parentId) {
        return taskRepository.findByParentId(parentId);
    }
    public List<Task> getTasksForKid(Integer kidId) {
        return taskRepository.findTasksAssignedToKid(kidId);
    }
    public void removeKidFromTask(Integer taskId, Integer kidId) {
        kidsTaskRepository.deleteByTaskIdAndKidId(taskId, kidId);
    }
    public void addKidToTask(Integer taskId, Integer kidId) {
        Task task = taskRepository.findById(taskId).orElseThrow(() -> new RuntimeException("Task not found"));
        KidsTask kidsTask = new KidsTask();
        kidsTask.setTaskId(taskId);
        kidsTask.setParentId(task.getParentId());
        kidsTask.setKidId(kidId);
        kidsTask.setIsSynced("false");
        kidsTask.setTask(task);
        kidsTaskRepository.save(kidsTask);
    }
    public List<TaskDTO> getAllFamilyTasksForToday(Integer parentId) {
        List<TaskDTO> result = new ArrayList<>();
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime endOfDay = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59).withNano(999999999);
        Set<Kid> parentKids = parentService.getKidsByParent(parentId);
        for (Kid kid : parentKids) {
            Set<Parent> allParentsOfKid = kid.getParents();
            for (Parent parentOfKid : allParentsOfKid) {
                List<Task> tasksForKid = taskRepository.findTasksAssignedToKidByParentAndDateRange(
                        kid.getId(),
                        parentOfKid.getId(),
                        startOfDay,
                        endOfDay
                );
                tasksForKid.forEach(task -> {
                    TaskDTO taskDTO = TaskMapper.toDTO(task);
                    result.add(taskDTO);
                });
            }
        }
        return result.stream()
                .distinct()
                .collect(Collectors.toList());
    }
    public List<TaskWithKidsDTO> getAllFamilyTasksForTodayWithNames(Integer parentId) {
        List<TaskDTO> tasks = getAllFamilyTasksForToday(parentId);

        return tasks.stream()
                .map(task -> {
                    TaskWithKidsDTO dto = new TaskWithKidsDTO();
                    dto.setTaskId(task.getTaskId());
                    dto.setTitle(task.getTitle());
                    dto.setDescription(task.getDescription());
                    dto.setTaskStart(task.getTaskStart());
                    dto.setTaskEnd(task.getTaskEnd());
                    dto.setStatus(task.getStatus());
                    dto.setNote(task.getNote());
                    dto.setParentId(task.getParentId());
                    dto.setKidIds(task.getKidIds());
                    dto.setIsSynced(task.getIsSynced());
                    if (task.getKidIds() != null) {
                        dto.setKidNames(
                                task.getKidIds().stream()
                                        .map(kidService::getKidNameById)
                                        .toList()
                        );
                    }
                    return dto;
                })
                .toList();
    }
    public List<TaskWithKidsDTO> getTasksByParentWithNames(Integer parentId) {
        return getTasksByParentId(parentId).stream()
                .map(TaskMapper::toDTO)
                .map(task -> {
                    TaskWithKidsDTO dto = new TaskWithKidsDTO();
                    dto.setTaskId(task.getTaskId());
                    dto.setTitle(task.getTitle());
                    dto.setDescription(task.getDescription());
                    dto.setTaskStart(task.getTaskStart());
                    dto.setTaskEnd(task.getTaskEnd());
                    dto.setStatus(task.getStatus());
                    dto.setNote(task.getNote());
                    dto.setParentId(task.getParentId());
                    dto.setKidIds(task.getKidIds());
                    dto.setIsSynced(task.getIsSynced());
                    if (task.getKidIds() != null) {
                        dto.setKidNames(
                                task.getKidIds().stream()
                                        .map(kidService::getKidNameById)
                                        .toList()
                        );
                    }
                    return dto;
                })
                .toList();
    }
}