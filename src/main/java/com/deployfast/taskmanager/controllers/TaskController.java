package com.deployfast.taskmanager.controllers;

import com.deployfast.taskmanager.dtos.TaskDtos;
import com.deployfast.taskmanager.entities.TaskStatus;
import com.deployfast.taskmanager.entities.User;
import com.deployfast.taskmanager.security.config.UserUserDetails;
import com.deployfast.taskmanager.services.interfaces.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
@Tag(name = "Tâches", description = "CRUD et gestion du statut des tâches")
@SecurityRequirement(name = "BearerAuth")
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    @Operation(summary = "Créer une tâche")
    @PreAuthorize("hasAnyRole('SIMPLE_USER', 'ADMIN')")
    public ResponseEntity<TaskDtos.TaskResponse> createTask(
            @Valid @RequestBody TaskDtos.TaskRequest request,
            @AuthenticationPrincipal UserUserDetails currentUser) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(taskService.createTask(request, currentUser.getUser().getId()));
    }

    @GetMapping
    @Operation(summary = "Lister les tâches — SIMPLE_USER : ses tâches | ADMIN : toutes")
    @PreAuthorize("hasAnyRole('SIMPLE_USER', 'ADMIN')")
    public ResponseEntity<TaskDtos.PagedTaskResponse> getAllTasks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) TaskStatus status,
            @AuthenticationPrincipal UserUserDetails currentUser) {
        return ResponseEntity.ok(taskService.getAllTasks(currentUser.getUser(), page, size, status));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Récupérer une tâche")
    @PreAuthorize("hasAnyRole('SIMPLE_USER', 'ADMIN')")
    public ResponseEntity<TaskDtos.TaskResponse> getTaskById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserUserDetails currentUser) {
        return ResponseEntity.ok(taskService.getTaskById(id, currentUser.getUser()));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier une tâche")
    @PreAuthorize("hasAnyRole('SIMPLE_USER', 'ADMIN')")
    public ResponseEntity<TaskDtos.TaskResponse> updateTask(
            @PathVariable Long id,
            @Valid @RequestBody TaskDtos.TaskRequest request,
            @AuthenticationPrincipal UserUserDetails currentUser) {
        return ResponseEntity.ok(taskService.updateTask(id, request, currentUser.getUser()));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer une tâche")
    @PreAuthorize("hasAnyRole('SIMPLE_USER', 'ADMIN')")
    public ResponseEntity<Void> deleteTask(
            @PathVariable Long id,
            @AuthenticationPrincipal UserUserDetails currentUser) {
        taskService.deleteTask(id, currentUser.getUser());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/status/in-progress")
    @Operation(summary = "Passer une tâche EN COURS")
    @PreAuthorize("hasAnyRole('SIMPLE_USER', 'ADMIN')")
    public ResponseEntity<TaskDtos.TaskResponse> markInProgress(
            @PathVariable Long id,
            @AuthenticationPrincipal UserUserDetails currentUser) {
        return ResponseEntity.ok(taskService.updateTaskStatus(id, TaskStatus.IN_PROGRESS, currentUser.getUser()));
    }

    @PatchMapping("/{id}/status/done")
    @Operation(summary = "Terminer une tâche")
    @PreAuthorize("hasAnyRole('SIMPLE_USER', 'ADMIN')")
    public ResponseEntity<TaskDtos.TaskResponse> markDone(
            @PathVariable Long id,
            @AuthenticationPrincipal UserUserDetails currentUser) {
        return ResponseEntity.ok(taskService.updateTaskStatus(id, TaskStatus.DONE, currentUser.getUser()));
    }

    @PatchMapping("/{id}/status/cancelled")
    @Operation(summary = "Annuler une tâche")
    @PreAuthorize("hasAnyRole('SIMPLE_USER', 'ADMIN')")
    public ResponseEntity<TaskDtos.TaskResponse> markCancelled(
            @PathVariable Long id,
            @AuthenticationPrincipal UserUserDetails currentUser) {
        return ResponseEntity.ok(taskService.updateTaskStatus(id, TaskStatus.CANCELLED, currentUser.getUser()));
    }
}
