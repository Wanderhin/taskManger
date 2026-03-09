package com.deployfast.taskmanager.services.interfaces;

import com.deployfast.taskmanager.dtos.AuthDtos;

public interface AuthService {
    AuthDtos.AuthResponse register(AuthDtos.RegisterRequest request);
    AuthDtos.AuthResponse login(AuthDtos.LoginRequest request);
}
