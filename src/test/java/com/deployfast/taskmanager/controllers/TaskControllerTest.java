package com.deployfast.taskmanager.controllers;

import com.deployfast.taskmanager.dtos.TaskDtos;
import com.deployfast.taskmanager.entities.Role;
import com.deployfast.taskmanager.entities.TaskStatus;
import com.deployfast.taskmanager.entities.User;
import com.deployfast.taskmanager.exceptions.ResourceNotFoundException;
import com.deployfast.taskmanager.security.config.UserUserDetails;
import com.deployfast.taskmanager.services.interfaces.TaskService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TaskController.class)
@Import(com.deployfast.taskmanager.security.config.SecurityConfig.class)
@DisplayName("Tests contrôleur - TaskController")
class TaskControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private TaskService taskService;
    @MockBean private com.deployfast.taskmanager.security.jwt.JwtService jwtService;
    @MockBean private com.deployfast.taskmanager.services.implementations.UserDetailsServiceImpl userDetailsService;
    @MockBean private com.deployfast.taskmanager.security.jwt.JwtAuthenticationFilter jwtAuthenticationFilter;

    private UserUserDetails simpleUserDetails;
    private UserUserDetails adminUserDetails;
    private TaskDtos.TaskResponse taskResponse;
    private TaskDtos.TaskResponse taskResponseWithOwner;

    @BeforeEach
    void setUp() {
        User simpleUser = new User();
        simpleUser.setId(1L);
        simpleUser.setEmail("user@test.com");
        simpleUser.setFullName("User");
        simpleUser.setRole(Role.SIMPLE_USER);
        simpleUser.setPassword("enc");

        User adminUser = new User();
        adminUser.setId(2L);
        adminUser.setEmail("admin@test.com");
        adminUser.setFullName("Admin");
        adminUser.setRole(Role.ADMIN);
        adminUser.setPassword("enc");

        simpleUserDetails = new UserUserDetails(simpleUser);
        adminUserDetails  = new UserUserDetails(adminUser);

        taskResponse = new TaskDtos.TaskResponse(10L, "Tâche test", "Desc",
                TaskStatus.TODO, null, LocalDateTime.now(), LocalDateTime.now(), null);
        taskResponseWithOwner = new TaskDtos.TaskResponse(20L, "Autre tâche", "Desc",
                TaskStatus.TODO, null, LocalDateTime.now(), LocalDateTime.now(), "other@test.com");
    }

    @Test
    @DisplayName("POST /tasks - SIMPLE_USER : 201 tâche créée")
    void createTask_simpleUser_returns201() throws Exception {
        TaskDtos.TaskRequest request = new TaskDtos.TaskRequest("Tâche test", "Desc", null);
        when(taskService.createTask(any(), eq(1L))).thenReturn(taskResponse);

        mockMvc.perform(post("/api/v1/tasks")
                        .with(SecurityMockMvcRequestPostProcessors.user(simpleUserDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10));
    }

    @Test
    @DisplayName("GET /tasks - SIMPLE_USER : ses tâches uniquement")
    void getAllTasks_simpleUser() throws Exception {
        TaskDtos.PagedTaskResponse paged = new TaskDtos.PagedTaskResponse(List.of(taskResponse), 0, 10, 1L, 1);
        when(taskService.getAllTasks(any(), eq(0), eq(10), isNull())).thenReturn(paged);

        mockMvc.perform(get("/api/v1/tasks")
                        .with(SecurityMockMvcRequestPostProcessors.user(simpleUserDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @DisplayName("GET /tasks - ADMIN : toutes les tâches avec ownerEmail")
    void getAllTasks_admin_withOwnerEmail() throws Exception {
        TaskDtos.PagedTaskResponse paged = new TaskDtos.PagedTaskResponse(
                List.of(taskResponse, taskResponseWithOwner), 0, 10, 2L, 1);
        when(taskService.getAllTasks(any(), eq(0), eq(10), isNull())).thenReturn(paged);

        mockMvc.perform(get("/api/v1/tasks")
                        .with(SecurityMockMvcRequestPostProcessors.user(adminUserDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content[1].ownerEmail").value("other@test.com"));
    }

    @Test
    @DisplayName("GET /tasks/{id} - SIMPLE_USER : 404 si tâche d'un autre")
    void getTaskById_simpleUser_otherTask_returns404() throws Exception {
        when(taskService.getTaskById(eq(20L), any()))
                .thenThrow(ResourceNotFoundException.taskNotFound(20L));

        mockMvc.perform(get("/api/v1/tasks/20")
                        .with(SecurityMockMvcRequestPostProcessors.user(simpleUserDetails)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /tasks/{id} - ADMIN : 200 avec ownerEmail")
    void getTaskById_admin_returns200() throws Exception {
        when(taskService.getTaskById(eq(20L), any())).thenReturn(taskResponseWithOwner);

        mockMvc.perform(get("/api/v1/tasks/20")
                        .with(SecurityMockMvcRequestPostProcessors.user(adminUserDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ownerEmail").value("other@test.com"));
    }

    @Test
    @DisplayName("DELETE /tasks/{id} - 204 après suppression")
    void deleteTask_returns204() throws Exception {
        doNothing().when(taskService).deleteTask(eq(10L), any());

        mockMvc.perform(delete("/api/v1/tasks/10")
                        .with(SecurityMockMvcRequestPostProcessors.user(simpleUserDetails)))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("PATCH /tasks/{id}/status/in-progress - 200")
    void markInProgress_returns200() throws Exception {
        TaskDtos.TaskResponse inProgress = new TaskDtos.TaskResponse(10L, "T", "D",
                TaskStatus.IN_PROGRESS, null, LocalDateTime.now(), LocalDateTime.now(), null);
        when(taskService.updateTaskStatus(eq(10L), eq(TaskStatus.IN_PROGRESS), any())).thenReturn(inProgress);

        mockMvc.perform(patch("/api/v1/tasks/10/status/in-progress")
                        .with(SecurityMockMvcRequestPostProcessors.user(simpleUserDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    @Test
    @DisplayName("GET /tasks - 401 si non authentifié")
    void getAllTasks_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/tasks"))
                .andExpect(status().isUnauthorized());
    }
}