package com.deployfast.taskmanager.dtos;

import com.deployfast.taskmanager.entities.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public class TaskDtos {

    public record TaskRequest(
            @NotBlank(message = "Le titre est obligatoire")
            @Size(max = 200, message = "Le titre ne peut pas dépasser 200 caractères")
            String title,

            String description,

            LocalDateTime dueDate
    ) {}

    public record TaskResponse(
            Long id,
            String title,
            String description,
            TaskStatus status,
            LocalDateTime dueDate,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            // Visible uniquement dans la vue admin (null pour SIMPLE_USER)
            String ownerEmail
    ) {}

    public record TaskStatusUpdateRequest(
            TaskStatus status
    ) {}

    public record PagedTaskResponse(
            java.util.List<TaskResponse> content,
            int page,
            int size,
            long totalElements,
            int totalPages
    ) {}
}
