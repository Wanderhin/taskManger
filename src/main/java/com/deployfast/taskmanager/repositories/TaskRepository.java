package com.deployfast.taskmanager.repositories;

import com.deployfast.taskmanager.entities.Task;
import com.deployfast.taskmanager.entities.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long> {

    // SIMPLE_USER : ses tâches uniquement
    Page<Task> findByUserId(Long userId, Pageable pageable);
    Page<Task> findByUserIdAndStatus(Long userId, TaskStatus status, Pageable pageable);
    Optional<Task> findByIdAndUserId(Long id, Long userId);

    // ADMIN : toutes les tâches (findAll déjà fourni par JpaRepository)
    Page<Task> findByStatus(TaskStatus status, Pageable pageable);
}
