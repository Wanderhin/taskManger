package com.deployfast.taskmanager.exceptions;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public static ResourceNotFoundException taskNotFound(Long id) {
        return new ResourceNotFoundException("Tâche introuvable avec l'id : " + id);
    }

    public static ResourceNotFoundException userNotFound(String email) {
        return new ResourceNotFoundException("Utilisateur introuvable avec l'email : " + email);
    }
}
