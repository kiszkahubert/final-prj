package com.kiszka.prj.services;

import com.kiszka.prj.DTOs.TaskDTO;
import com.kiszka.prj.entities.KidsTask;
import com.kiszka.prj.entities.Task;
import com.kiszka.prj.repositories.KidsTaskRepository;
import com.kiszka.prj.repositories.TaskRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class TaskService {
    private final TaskRepository taskRepository;
    private final KidsTaskRepository kidsTaskRepository;
    public TaskService(TaskRepository taskRepository, KidsTaskRepository kidsTaskRepository) {
        this.taskRepository = taskRepository;
        this.kidsTaskRepository = kidsTaskRepository;
    }
    public Task createTask(TaskDTO taskDTO) {
        Task task = new Task();
        task.setTitle(taskDTO.getTitle());
        task.setDescription(taskDTO.getDescription());
        task.setTaskStart(taskDTO.getTaskStart());
        task.setTaskEnd(taskDTO.getTaskEnd());
        task.setStatus(taskDTO.getStatus());
        task.setNote(taskDTO.getNote());
        task.setParentId(taskDTO.getParentId());
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
    public List<Task> getAllTasks() {
        return taskRepository.findAll();
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
    public List<Task> getTasksByParentAndKid(Integer parentId, Integer kidId) {
        return taskRepository.findByParentIdAndKidId(parentId, kidId);
    }
    public void removeKidFromTask(Integer taskId, Integer kidId) {
        kidsTaskRepository.deleteByTaskIdAndKidId(taskId, kidId);
    }
    public void addKidToTask(Integer taskId, Integer kidId, String isSynced) {
        Task task = taskRepository.findById(taskId).orElseThrow(() -> new RuntimeException("Task not found"));
        KidsTask kidsTask = new KidsTask();
        kidsTask.setTaskId(taskId);
        kidsTask.setParentId(task.getParentId());
        kidsTask.setKidId(kidId);
        kidsTask.setIsSynced(isSynced);
        kidsTask.setTask(task);
        kidsTaskRepository.save(kidsTask);
    }
}