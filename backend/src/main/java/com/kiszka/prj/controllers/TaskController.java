package com.kiszka.prj.controllers;

import com.kiszka.prj.DTOs.TaskDTO;
import com.kiszka.prj.DTOs.TaskWithKidsDTO;
import com.kiszka.prj.components.TaskMapper;
import com.kiszka.prj.entities.Parent;
import com.kiszka.prj.entities.Task;
import com.kiszka.prj.services.JWTService;
import com.kiszka.prj.services.TaskService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {
    private final TaskService taskService;
    private final JWTService jwtService;

    public TaskController(TaskService taskService, JWTService jwtService) {
        this.taskService = taskService;
        this.jwtService = jwtService;
    }
    @PostMapping
    public ResponseEntity<?> createTask(Authentication authentication, @RequestBody TaskDTO taskDTO) {
        Parent parent = (Parent) authentication.getPrincipal();
        Task createdTask = taskService.createTask(taskDTO, parent.getId());
        return ResponseEntity.ok(createdTask);
    }
    @GetMapping("/parent")
    public ResponseEntity<List<TaskWithKidsDTO>> getTasksByParent(Authentication authentication) {
        Parent parent = (Parent) authentication.getPrincipal();
        List<TaskWithKidsDTO> tasks = taskService.getTasksByParentWithNames(parent.getId());
        return ResponseEntity.ok(tasks);
    }
    @GetMapping()
    public ResponseEntity<List<TaskDTO>> getTasksForKid(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        Integer kidId = jwtService.extractKidId(token);
        List<Task> tasks = taskService.getTasksForKid(kidId);
        List<TaskDTO> taskDTOS = tasks.stream()
                .map(TaskMapper::toDTO)
                .toList();
        return ResponseEntity.ok(taskDTOS);
    }
    @PutMapping("/{taskId}")
    public ResponseEntity<?> updateTask(Authentication authentication, @PathVariable Integer taskId, @RequestBody TaskDTO taskDTO) {
        Parent parent = (Parent) authentication.getPrincipal();
        Optional<Task> taskOptional = taskService.getTaskById(taskId);
        if(taskOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        var task = taskOptional.get();
        if(!task.getParentId().equals(parent.getId())){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        Task updatedTask = taskService.updateTask(taskId, taskDTO);
        return ResponseEntity.ok(updatedTask);
    }
    @DeleteMapping("/{taskId}")
    public ResponseEntity<?> deleteTask(Authentication authentication, @PathVariable Integer taskId) {
        Parent parent = (Parent) authentication.getPrincipal();
        Optional<Task> taskOptional = taskService.getTaskById(taskId);
        if(taskOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        var task = taskOptional.get();
        if(!task.getParentId().equals(parent.getId())){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        taskService.deleteTask(taskId);
        return ResponseEntity.ok("Task deleted successfully");
    }
    @PatchMapping("/{taskId}/complete")
    public ResponseEntity<?> markTaskAsDoneForKid(@RequestHeader("Authorization") String authHeader, @PathVariable Integer taskId) {
        String token = authHeader.substring(7);
        Integer kidId = jwtService.extractKidId(token);
        List<Task> tasksForKid = taskService.getTasksForKid(kidId);
        boolean assigned = tasksForKid.stream().anyMatch(task -> task.getTaskId().equals(taskId));
        if (!assigned)
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        taskService.updateTaskStatusForKid(taskId, kidId, "DONE");
        return ResponseEntity.ok().build();
    }
    @GetMapping("/today")
    public ResponseEntity<List<TaskWithKidsDTO>> getAllFamilyTasksForToday(Authentication authentication) {
        Parent parent = (Parent) authentication.getPrincipal();
        List<TaskWithKidsDTO> familyTasks = taskService.getAllFamilyTasksForTodayWithNames(parent.getId());
        return ResponseEntity.ok(familyTasks);
    }
}
