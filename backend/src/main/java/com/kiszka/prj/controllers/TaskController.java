package com.kiszka.prj.controllers;

import com.kiszka.prj.DTOs.TaskDTO;
import com.kiszka.prj.DTOs.TaskWithKidsDTO;
import com.kiszka.prj.components.TaskMapper;
import com.kiszka.prj.entities.Parent;
import com.kiszka.prj.entities.Task;
import com.kiszka.prj.services.GoogleCalendarService;
import com.kiszka.prj.services.JWTService;
import com.kiszka.prj.services.KidService;
import com.kiszka.prj.services.TaskService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {
    private final TaskService taskService;
    private final KidService kidService;
    private final JWTService jwtService;
    private final GoogleCalendarService googleCalendarService;

    public TaskController(TaskService taskService, KidService kidService, JWTService jwtService, GoogleCalendarService googleCalendarService) {
        this.taskService = taskService;
        this.kidService = kidService;
        this.jwtService = jwtService;
        this.googleCalendarService = googleCalendarService;
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
    @GetMapping("/today")
    public ResponseEntity<List<TaskWithKidsDTO>> getAllFamilyTasksForToday(Authentication authentication) {
        Parent parent = (Parent) authentication.getPrincipal();
        List<TaskWithKidsDTO> familyTasks = taskService.getAllFamilyTasksForTodayWithNames(parent.getId());
        return ResponseEntity.ok(familyTasks);
    }
    @GetMapping("/unsynced")
    public ResponseEntity<List<TaskDTO>> getAllUnsyncedFamilyTasks(Authentication authentication) {
        Parent parent = (Parent) authentication.getPrincipal();
        List<TaskDTO> unsyncedTasks = taskService.getAllUnsyncedFamilyTasks(parent.getId());
        return ResponseEntity.ok(unsyncedTasks);
    }
    @PostMapping("/sync")
    public ResponseEntity<String> syncTasksWithGoogleCalendar(
            @RegisteredOAuth2AuthorizedClient("google") OAuth2AuthorizedClient authorizedClient) {
        OAuth2AccessToken accessToken = authorizedClient.getAccessToken();
        String tokenValue = accessToken.getTokenValue();
        List<TaskDTO> unsyncedTasks = taskService.getAllUnsyncedFamilyTasks(1);
        if (unsyncedTasks.isEmpty()) {
            return ResponseEntity.ok("No new tasks to sync.");
        }
        int successCount = 0;
        int errorCount = 0;
        for (var task : unsyncedTasks) {
            try {
                googleCalendarService.createEventFromTask(task, tokenValue);
                taskService.markTaskAsSynced(unsyncedTasks);
                successCount++;
            } catch (Exception e) {
                System.err.println("Failed to sync task " + task.getTaskId() + ": " + e.getMessage());
                errorCount++;
            }
        }
        return ResponseEntity.ok(String.format("Sync complete. Success: %d, Failed: %d", successCount, errorCount));
    }
//    @PostMapping("/sync")
//    public ResponseEntity<String> syncTasksWithGoogleCalendar(
//            Authentication authentication,
//            @RegisteredOAuth2AuthorizedClient("google") OAuth2AuthorizedClient authorizedClient) {
//        OAuth2AccessToken accessToken = authorizedClient.getAccessToken();
//        String tokenValue = accessToken.getTokenValue();
//        Parent parent = (Parent) authentication.getPrincipal();
//        List<TaskDTO> unsyncedTasks = taskService.getAllUnsyncedFamilyTasks(parent.getId());
//        if (unsyncedTasks.isEmpty()) {
//            return ResponseEntity.ok("No new tasks to sync.");
//        }
//        int successCount = 0;
//        int errorCount = 0;
//        for (var task : unsyncedTasks) {
//            try {
//                googleCalendarService.createEventFromTask(task, tokenValue);
//                taskService.markTaskAsSynced(unsyncedTasks);
//                successCount++;
//            } catch (Exception e) {
//                System.err.println("Failed to sync task " + task.getTaskId() + ": " + e.getMessage());
//                errorCount++;
//            }
//        }
//        return ResponseEntity.ok(String.format("Sync complete. Success: %d, Failed: %d", successCount, errorCount));
//    }
}
