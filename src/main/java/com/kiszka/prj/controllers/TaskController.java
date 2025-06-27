package com.kiszka.prj.controllers;

import com.kiszka.prj.DTOs.TaskDTO;
import com.kiszka.prj.entities.Task;
import com.kiszka.prj.services.TaskService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {
    private final TaskService taskService;
    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }
    @PostMapping
    public ResponseEntity<?> createTask(@RequestBody TaskDTO taskDTO) {
        try {
            Task createdTask = taskService.createTask(taskDTO);
            return ResponseEntity.ok(createdTask);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @GetMapping
    public ResponseEntity<List<Task>> getAllTasks() {
        List<Task> tasks = taskService.getAllTasks();
        return ResponseEntity.ok(tasks);
    }
    @GetMapping("/{taskId}")
    public ResponseEntity<Task> getTaskById(@PathVariable Integer taskId) {
        Optional<Task> task = taskService.getTaskById(taskId);
        return task.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
    @GetMapping("/parent/{parentId}")
    public ResponseEntity<List<Task>> getTasksByParent(@PathVariable Integer parentId) {
        List<Task> tasks = taskService.getTasksByParentId(parentId);
        return ResponseEntity.ok(tasks);
    }
    @GetMapping("/kid/{kidId}")
    public ResponseEntity<List<Task>> getTasksForKid(@PathVariable Integer kidId) {
        List<Task> tasks = taskService.getTasksForKid(kidId);
        return ResponseEntity.ok(tasks);
    }
    @GetMapping("/parent/{parentId}/kid/{kidId}")
    public ResponseEntity<List<Task>> getTasksByParentAndKid(
            @PathVariable Integer parentId, @PathVariable Integer kidId) {
        List<Task> tasks = taskService.getTasksByParentAndKid(parentId, kidId);
        return ResponseEntity.ok(tasks);
    }
    @PutMapping("/{taskId}")
    public ResponseEntity<?> updateTask(@PathVariable Integer taskId, @RequestBody TaskDTO taskDTO) {
        try {
            Task updatedTask = taskService.updateTask(taskId, taskDTO);
            return ResponseEntity.ok(updatedTask);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @DeleteMapping("/{taskId}")
    public ResponseEntity<?> deleteTask(@PathVariable Integer taskId) {
        try {
            taskService.deleteTask(taskId);
            return ResponseEntity.ok("Task deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @PostMapping("/{taskId}/kids/{kidId}")
    public ResponseEntity<?> addKidToTask(
            @PathVariable Integer taskId,
            @PathVariable Integer kidId,
            @RequestParam(value = "isSynced", defaultValue = "false") String isSynced) {
        try {
            taskService.addKidToTask(taskId, kidId, isSynced);
            return ResponseEntity.ok("Kid added to task successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @DeleteMapping("/{taskId}/kids/{kidId}")
    public ResponseEntity<?> removeKidFromTask(@PathVariable Integer taskId, @PathVariable Integer kidId) {
        try {
            taskService.removeKidFromTask(taskId, kidId);
            return ResponseEntity.ok("Kid removed from task successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
