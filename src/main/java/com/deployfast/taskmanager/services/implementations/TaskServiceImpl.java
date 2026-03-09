package com.deployfast.taskmanager.services.implementations;

import com.deployfast.taskmanager.dtos.TaskDtos;
import com.deployfast.taskmanager.entities.Task;
import com.deployfast.taskmanager.entities.TaskStatus;
import com.deployfast.taskmanager.entities.User;
import com.deployfast.taskmanager.exceptions.ForbiddenException;
import com.deployfast.taskmanager.exceptions.ResourceNotFoundException;
import com.deployfast.taskmanager.mappers.TaskMapper;
import com.deployfast.taskmanager.repositories.TaskRepository;
import com.deployfast.taskmanager.repositories.UserRepository;
import com.deployfast.taskmanager.services.interfaces.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final TaskMapper taskMapper;

    @Override
    @Transactional
    public TaskDtos.TaskResponse createTask(TaskDtos.TaskRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable avec l'id : " + userId));

        Task task = taskMapper.toEntity(request);
        task.setUser(user);
        task.setStatus(TaskStatus.TODO);

        return taskMapper.toResponseWithoutOwner(taskRepository.save(task));
    }

    @Override
    @Transactional(readOnly = true)
    public TaskDtos.TaskResponse getTaskById(Long taskId, User currentUser) {
        Task task = resolveTaskAccess(taskId, currentUser);
        return currentUser.isAdmin()
                ? taskMapper.toResponse(task)
                : taskMapper.toResponseWithoutOwner(task);
    }

    @Override
    @Transactional(readOnly = true)
    public TaskDtos.PagedTaskResponse getAllTasks(User currentUser, int page, int size, TaskStatus status) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<Task> taskPage = currentUser.isAdmin()
                ? fetchAllTasksForAdmin(status, pageable)
                : fetchTasksForSimpleUser(currentUser.getId(), status, pageable);

        List<TaskDtos.TaskResponse> content = taskPage.getContent().stream()
                .map(task -> currentUser.isAdmin()
                        ? taskMapper.toResponse(task)
                        : taskMapper.toResponseWithoutOwner(task))
                .toList();

        return new TaskDtos.PagedTaskResponse(
                content,
                taskPage.getNumber(),
                taskPage.getSize(),
                taskPage.getTotalElements(),
                taskPage.getTotalPages()
        );
    }

    @Override
    @Transactional
    public TaskDtos.TaskResponse updateTask(Long taskId, TaskDtos.TaskRequest request, User currentUser) {
        Task task = resolveTaskAccess(taskId, currentUser);
        taskMapper.updateEntityFromRequest(request, task);
        return currentUser.isAdmin()
                ? taskMapper.toResponse(taskRepository.save(task))
                : taskMapper.toResponseWithoutOwner(taskRepository.save(task));
    }

    @Override
    @Transactional
    public void deleteTask(Long taskId, User currentUser) {
        Task task = resolveTaskAccess(taskId, currentUser);
        taskRepository.delete(task);
    }

    @Override
    @Transactional
    public TaskDtos.TaskResponse updateTaskStatus(Long taskId, TaskStatus newStatus, User currentUser) {
        Task task = resolveTaskAccess(taskId, currentUser);
        task.setStatus(newStatus);
        return currentUser.isAdmin()
                ? taskMapper.toResponse(taskRepository.save(task))
                : taskMapper.toResponseWithoutOwner(taskRepository.save(task));
    }

    // --- Helpers privés ---

    /**
     * ADMIN : accède à toute tâche existante.
     * SIMPLE_USER : accède uniquement à ses propres tâches.
     */
    private Task resolveTaskAccess(Long taskId, User currentUser) {
        if (currentUser.isAdmin()) {
            return taskRepository.findById(taskId)
                    .orElseThrow(() -> ResourceNotFoundException.taskNotFound(taskId));
        }
        return taskRepository.findByIdAndUserId(taskId, currentUser.getId())
                .orElseThrow(() -> ResourceNotFoundException.taskNotFound(taskId));
    }

    private Page<Task> fetchAllTasksForAdmin(TaskStatus status, Pageable pageable) {
        return (status != null)
                ? taskRepository.findByStatus(status, pageable)
                : taskRepository.findAll(pageable);
    }

    private Page<Task> fetchTasksForSimpleUser(Long userId, TaskStatus status, Pageable pageable) {
        return (status != null)
                ? taskRepository.findByUserIdAndStatus(userId, status, pageable)
                : taskRepository.findByUserId(userId, pageable);
    }
}
