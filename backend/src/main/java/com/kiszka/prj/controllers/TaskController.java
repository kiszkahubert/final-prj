package com.kiszka.prj.controllers;

import com.kiszka.prj.DTOs.TaskDTO;
import com.kiszka.prj.components.TaskMapper;
import com.kiszka.prj.entities.Parent;
import com.kiszka.prj.entities.Task;
import com.kiszka.prj.services.JWTService;
import com.kiszka.prj.services.KidService;
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
    private final KidService kidService;
    private final JWTService jwtService;

    public TaskController(TaskService taskService, KidService kidService, JWTService jwtService) {
        this.taskService = taskService;
        this.kidService = kidService;
        this.jwtService = jwtService;
    }
    @PostMapping
    public ResponseEntity<?> createTask(Authentication authentication, @RequestBody TaskDTO taskDTO) {
        Parent parent = (Parent) authentication.getPrincipal();
        Task createdTask = taskService.createTask(taskDTO, parent.getId());
        return ResponseEntity.ok(createdTask);
    }
    @GetMapping("/{taskId}")
    public ResponseEntity<Task> getTaskById(Authentication authentication, @PathVariable Integer taskId) {
        Parent parent = (Parent) authentication.getPrincipal();
        Optional<Task> taskOptional = taskService.getTaskById(taskId);
        if(taskOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        var task = taskOptional.get();
        if(!task.getParentId().equals(parent.getId())){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(task);
    }
    @GetMapping("/parent")
    public ResponseEntity<List<Task>> getTasksByParent(Authentication authentication) {
        Parent parent = (Parent) authentication.getPrincipal();
        List<Task> tasks = taskService.getTasksByParentId(parent.getId());
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
    @GetMapping("/kid/{kidId}")
    public ResponseEntity<List<TaskDTO>> getTasksForKid(Authentication authentication, @PathVariable Integer kidId) {
        Parent parent = (Parent) authentication.getPrincipal();
        if(!kidService.isParentOfKid(parent.getId(),kidId)){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
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
    @PostMapping("/{taskId}/kids/{kidId}")
    public ResponseEntity<?> addKidToTask(Authentication authentication, @PathVariable Integer taskId, @PathVariable Integer kidId) {
        Parent parent = (Parent) authentication.getPrincipal();
        Optional<Task> taskOptional = taskService.getTaskById(taskId);
        if(taskOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        var task = taskOptional.get();
        if(!task.getParentId().equals(parent.getId())){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        if(!kidService.isParentOfKid(parent.getId(),kidId)){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        taskService.addKidToTask(taskId, kidId);
        return ResponseEntity.ok("Kid added to task successfully");
    }
    @DeleteMapping("/{taskId}/kids/{kidId}")
    public ResponseEntity<?> removeKidFromTask(Authentication authentication, @PathVariable Integer taskId, @PathVariable Integer kidId) {
        Parent parent = (Parent) authentication.getPrincipal();
        Optional<Task> taskOptional = taskService.getTaskById(taskId);
        if(taskOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        var task = taskOptional.get();
        if(!task.getParentId().equals(parent.getId())){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        if(!kidService.isParentOfKid(parent.getId(),kidId)){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        taskService.removeKidFromTask(taskId, kidId);
        return ResponseEntity.ok("Kid removed from task successfully");
    }
    @GetMapping("today")
    public ResponseEntity<List<TaskDTO>> getAllFamilyTasksForToday(Authentication authentication) {
        Parent parent = (Parent) authentication.getPrincipal();
        List<TaskDTO> familyTasks = taskService.getAllFamilyTasksForToday(parent.getId());
        return ResponseEntity.ok(familyTasks);
    }
}
