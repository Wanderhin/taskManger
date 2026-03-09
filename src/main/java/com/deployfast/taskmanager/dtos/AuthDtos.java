package com.deployfast.taskmanager.dtos;

import com.deployfast.taskmanager.entities.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AuthDtos {

    public record RegisterRequest(
            @NotBlank(message = "Le nom complet est obligatoire")
            @Size(min = 2, max = 100, message = "Le nom doit contenir entre 2 et 100 caractères")
            String fullName,

            @NotBlank(message = "L'email est obligatoire")
            @Email(message = "Format email invalide")
            String email,

            @NotBlank(message = "Le mot de passe est obligatoire")
            @Size(min = 6, message = "Le mot de passe doit contenir au moins 6 caractères")
            String password,

            // Optionnel : si absent, SIMPLE_USER est attribué par défaut
            Role role
    ) {}

    public record LoginRequest(
            @NotBlank(message = "L'email est obligatoire")
            @Email(message = "Format email invalide")
            String email,

            @NotBlank(message = "Le mot de passe est obligatoire")
            String password
    ) {}

    public record AuthResponse(
            String token,
            String email,
            String fullName,
            Role role
    ) {}
}
