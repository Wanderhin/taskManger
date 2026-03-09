package com.deployfast.taskmanager.services;

import com.deployfast.taskmanager.dtos.TaskDtos;
import com.deployfast.taskmanager.entities.Role;
import com.deployfast.taskmanager.entities.Task;
import com.deployfast.taskmanager.entities.TaskStatus;
import com.deployfast.taskmanager.entities.User;
import com.deployfast.taskmanager.exceptions.ResourceNotFoundException;
import com.deployfast.taskmanager.mappers.TaskMapper;
import com.deployfast.taskmanager.repositories.TaskRepository;
import com.deployfast.taskmanager.repositories.UserRepository;
import com.deployfast.taskmanager.services.implementations.TaskServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitaires - TaskService")
class TaskServiceImplTest {

    @Mock private TaskRepository taskRepository;
    @Mock private UserRepository userRepository;
    @Mock private TaskMapper taskMapper;

    @InjectMocks
    private TaskServiceImpl taskService;

    private User simpleUser;
    private User adminUser;
    private User otherUser;
    private Task ownTask;
    private Task otherTask;
    private TaskDtos.TaskRequest taskRequest;
    private TaskDtos.TaskResponse taskResponse;
    private TaskDtos.TaskResponse taskResponseWithOwner;

    @BeforeEach
    void setUp() {
        simpleUser = new User();
        simpleUser.setId(1L);
        simpleUser.setEmail("user@test.com");
        simpleUser.setFullName("User");
        simpleUser.setRole(Role.SIMPLE_USER);

        adminUser = new User();
        adminUser.setId(2L);
        adminUser.setEmail("admin@test.com");
        adminUser.setFullName("Admin");
        adminUser.setRole(Role.ADMIN);

        otherUser = new User();
        otherUser.setId(3L);
        otherUser.setEmail("other@test.com");
        otherUser.setFullName("Other");
        otherUser.setRole(Role.SIMPLE_USER);

        ownTask = new Task();
        ownTask.setId(10L);
        ownTask.setTitle("Ma tâche");
        ownTask.setDescription("Desc");
        ownTask.setStatus(TaskStatus.TODO);
        ownTask.setUser(simpleUser);
        ownTask.setCreatedAt(LocalDateTime.now());
        ownTask.setUpdatedAt(LocalDateTime.now());

        otherTask = new Task();
        otherTask.setId(20L);
        otherTask.setTitle("Tâche autre");
        otherTask.setDescription("Desc");
        otherTask.setStatus(TaskStatus.TODO);
        otherTask.setUser(otherUser);
        otherTask.setCreatedAt(LocalDateTime.now());
        otherTask.setUpdatedAt(LocalDateTime.now());

        taskRequest  = new TaskDtos.TaskRequest("Ma tâche", "Desc", null);
        taskResponse = new TaskDtos.TaskResponse(10L, "Ma tâche", "Desc",
                TaskStatus.TODO, null, LocalDateTime.now(), LocalDateTime.now(), null);
        taskResponseWithOwner = new TaskDtos.TaskResponse(20L, "Tâche autre", "Desc",
                TaskStatus.TODO, null, LocalDateTime.now(), LocalDateTime.now(), "other@test.com");
    }

    @Test
    @DisplayName("createTask - succès : tâche créée pour l'utilisateur connecté")
    void createTask_success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(simpleUser));
        when(taskMapper.toEntity(taskRequest)).thenReturn(ownTask);
        when(taskRepository.save(any())).thenReturn(ownTask);
        when(taskMapper.toResponseWithoutOwner(ownTask)).thenReturn(taskResponse);

        TaskDtos.TaskResponse result = taskService.createTask(taskRequest, 1L);

        assertThat(result.title()).isEqualTo("Ma tâche");
        verify(taskRepository).save(any());
    }

    @Test
    @DisplayName("createTask - utilisateur inexistant lève ResourceNotFoundException")
    void createTask_userNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.createTask(taskRequest, 99L))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(taskRepository, never()).save(any());
    }

    @Test
    @DisplayName("getTaskById - SIMPLE_USER : accède à sa propre tâche sans ownerEmail")
    void getTaskById_simpleUser_ownTask() {
        when(taskRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(ownTask));
        when(taskMapper.toResponseWithoutOwner(ownTask)).thenReturn(taskResponse);

        TaskDtos.TaskResponse result = taskService.getTaskById(10L, simpleUser);

        assertThat(result.ownerEmail()).isNull();
    }

    @Test
    @DisplayName("getTaskById - SIMPLE_USER : tâche d'un autre utilisateur lève ResourceNotFoundException")
    void getTaskById_simpleUser_otherUserTask_throws() {
        when(taskRepository.findByIdAndUserId(20L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.getTaskById(20L, simpleUser))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("getTaskById - ADMIN : accède à n'importe quelle tâche avec ownerEmail")
    void getTaskById_admin_anyTask() {
        when(taskRepository.findById(20L)).thenReturn(Optional.of(otherTask));
        when(taskMapper.toResponse(otherTask)).thenReturn(taskResponseWithOwner);

        TaskDtos.TaskResponse result = taskService.getTaskById(20L, adminUser);

        assertThat(result.ownerEmail()).isEqualTo("other@test.com");
    }

    @Test
    @DisplayName("getAllTasks - SIMPLE_USER : retourne uniquement ses tâches")
    void getAllTasks_simpleUser_onlyOwnTasks() {
        Page<Task> page = new PageImpl<>(List.of(ownTask), PageRequest.of(0, 10), 1);
        when(taskRepository.findByUserId(eq(1L), any(Pageable.class))).thenReturn(page);
        when(taskMapper.toResponseWithoutOwner(ownTask)).thenReturn(taskResponse);

        TaskDtos.PagedTaskResponse result = taskService.getAllTasks(simpleUser, 0, 10, null);

        assertThat(result.totalElements()).isEqualTo(1);
        assertThat(result.content().get(0).ownerEmail()).isNull();
    }

    @Test
    @DisplayName("getAllTasks - ADMIN : retourne toutes les tâches avec ownerEmail")
    void getAllTasks_admin_allTasksWithOwner() {
        Page<Task> page = new PageImpl<>(List.of(ownTask, otherTask), PageRequest.of(0, 10), 2);
        when(taskRepository.findAll(any(Pageable.class))).thenReturn(page);
        when(taskMapper.toResponse(ownTask)).thenReturn(taskResponse);
        when(taskMapper.toResponse(otherTask)).thenReturn(taskResponseWithOwner);

        TaskDtos.PagedTaskResponse result = taskService.getAllTasks(adminUser, 0, 10, null);

        assertThat(result.totalElements()).isEqualTo(2);
    }

    @Test
    @DisplayName("getAllTasks - ADMIN avec filtre status : utilise findByStatus")
    void getAllTasks_admin_withStatusFilter() {
        Page<Task> page = new PageImpl<>(List.of(ownTask));
        when(taskRepository.findByStatus(eq(TaskStatus.TODO), any(Pageable.class))).thenReturn(page);
        when(taskMapper.toResponse(ownTask)).thenReturn(taskResponse);

        taskService.getAllTasks(adminUser, 0, 10, TaskStatus.TODO);

        verify(taskRepository).findByStatus(eq(TaskStatus.TODO), any(Pageable.class));
    }

    @Test
    @DisplayName("updateTask - SIMPLE_USER : met à jour sa propre tâche")
    void updateTask_simpleUser_ownTask() {
        when(taskRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(ownTask));
        when(taskRepository.save(ownTask)).thenReturn(ownTask);
        when(taskMapper.toResponseWithoutOwner(ownTask)).thenReturn(taskResponse);

        TaskDtos.TaskResponse result = taskService.updateTask(10L, taskRequest, simpleUser);

        assertThat(result).isNotNull();
        verify(taskMapper).updateEntityFromRequest(taskRequest, ownTask);
    }

    @Test
    @DisplayName("updateTask - ADMIN : met à jour la tâche d'un autre utilisateur")
    void updateTask_admin_otherUserTask() {
        when(taskRepository.findById(20L)).thenReturn(Optional.of(otherTask));
        when(taskRepository.save(otherTask)).thenReturn(otherTask);
        when(taskMapper.toResponse(otherTask)).thenReturn(taskResponseWithOwner);

        TaskDtos.TaskResponse result = taskService.updateTask(20L, taskRequest, adminUser);

        assertThat(result.ownerEmail()).isEqualTo("other@test.com");
    }

    @Test
    @DisplayName("deleteTask - SIMPLE_USER : supprime sa propre tâche")
    void deleteTask_simpleUser_ownTask() {
        when(taskRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(ownTask));

        assertThatCode(() -> taskService.deleteTask(10L, simpleUser)).doesNotThrowAnyException();
        verify(taskRepository).delete(ownTask);
    }

    @Test
    @DisplayName("deleteTask - ADMIN : peut supprimer n'importe quelle tâche")
    void deleteTask_admin_anyTask() {
        when(taskRepository.findById(20L)).thenReturn(Optional.of(otherTask));

        assertThatCode(() -> taskService.deleteTask(20L, adminUser)).doesNotThrowAnyException();
        verify(taskRepository).delete(otherTask);
    }

    @Test
    @DisplayName("updateTaskStatus - IN_PROGRESS pour SIMPLE_USER")
    void updateTaskStatus_inProgress_simpleUser() {
        when(taskRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(ownTask));
        when(taskRepository.save(ownTask)).thenReturn(ownTask);
        when(taskMapper.toResponseWithoutOwner(ownTask)).thenReturn(taskResponse);

        taskService.updateTaskStatus(10L, TaskStatus.IN_PROGRESS, simpleUser);

        assertThat(ownTask.getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
    }

    @Test
    @DisplayName("updateTaskStatus - ADMIN : change statut d'une tâche quelconque")
    void updateTaskStatus_admin_anyTask() {
        when(taskRepository.findById(20L)).thenReturn(Optional.of(otherTask));
        when(taskRepository.save(otherTask)).thenReturn(otherTask);
        when(taskMapper.toResponse(otherTask)).thenReturn(taskResponseWithOwner);

        taskService.updateTaskStatus(20L, TaskStatus.DONE, adminUser);

        assertThat(otherTask.getStatus()).isEqualTo(TaskStatus.DONE);
    }
}