package com.deployfast.taskmanager.services.interfaces;

import com.deployfast.taskmanager.dtos.TaskDtos;
import com.deployfast.taskmanager.entities.TaskStatus;
import com.deployfast.taskmanager.entities.User;

public interface TaskService {

    // --- SIMPLE_USER : opérations sur ses propres tâches ---
    TaskDtos.TaskResponse createTask(TaskDtos.TaskRequest request, Long userId);
    TaskDtos.TaskResponse getTaskById(Long taskId, User currentUser);
    TaskDtos.PagedTaskResponse getAllTasks(User currentUser, int page, int size, TaskStatus status);
    TaskDtos.TaskResponse updateTask(Long taskId, TaskDtos.TaskRequest request, User currentUser);
    void deleteTask(Long taskId, User currentUser);
    TaskDtos.TaskResponse updateTaskStatus(Long taskId, TaskStatus newStatus, User currentUser);
}
