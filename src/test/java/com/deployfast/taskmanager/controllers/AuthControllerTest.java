package com.deployfast.taskmanager.controllers;

import com.deployfast.taskmanager.dtos.AuthDtos;
import com.deployfast.taskmanager.entities.Role;
import com.deployfast.taskmanager.services.interfaces.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import(com.deployfast.taskmanager.security.config.SecurityConfig.class)
@DisplayName("Tests contrôleur - AuthController")
class AuthControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private AuthService authService;
    @MockBean private com.deployfast.taskmanager.security.jwt.JwtService jwtService;
    @MockBean private com.deployfast.taskmanager.services.implementations.UserDetailsServiceImpl userDetailsService;
    @MockBean private com.deployfast.taskmanager.security.jwt.JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    void setUp() throws Exception {
        // --- CORRECTION ---
        // On force le filtre mocké à faire suivre la requête
        doAnswer(invocation -> {
            ServletRequest request = invocation.getArgument(0);
            ServletResponse response = invocation.getArgument(1);
            FilterChain chain = invocation.getArgument(2);
            chain.doFilter(request, response);
            return null;
        }).when(jwtAuthenticationFilter).doFilter(any(), any(), any());
        // ------------------
    }

    @Test
    @DisplayName("POST /auth/register - 201 avec token")
    void register_returns201() throws Exception {
        AuthDtos.RegisterRequest request = new AuthDtos.RegisterRequest(
                "Jean Dupont", "jean@test.com", "password123", null);
        AuthDtos.AuthResponse response = new AuthDtos.AuthResponse(
                "jwt_token", "jean@test.com", "Jean Dupont", Role.SIMPLE_USER);

        when(authService.register(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("jwt_token"))
                .andExpect(jsonPath("$.role").value("SIMPLE_USER"));
    }

    @Test
    @DisplayName("POST /auth/register - 400 si corps invalide")
    void register_returns400_whenInvalidBody() throws Exception {
        AuthDtos.RegisterRequest invalidRequest = new AuthDtos.RegisterRequest(
                "", "not-an-email", "ab", null);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /auth/login - 200 avec token JWT")
    void login_returns200() throws Exception {
        AuthDtos.LoginRequest request = new AuthDtos.LoginRequest("jean@test.com", "password123");
        AuthDtos.AuthResponse response = new AuthDtos.AuthResponse(
                "jwt_token", "jean@test.com", "Jean Dupont", Role.SIMPLE_USER);

        when(authService.login(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt_token"));
    }

    @Test
    @DisplayName("POST /auth/login - 401 si mauvais identifiants")
    void login_returns401_whenBadCredentials() throws Exception {
        AuthDtos.LoginRequest request = new AuthDtos.LoginRequest("jean@test.com", "wrong");
        when(authService.login(any())).thenThrow(new BadCredentialsException("Invalide"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
}