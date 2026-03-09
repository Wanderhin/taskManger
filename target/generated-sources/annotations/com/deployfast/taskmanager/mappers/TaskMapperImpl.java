package com.deployfast.taskmanager.mappers;

import com.deployfast.taskmanager.dtos.TaskDtos;
import com.deployfast.taskmanager.entities.Task;
import com.deployfast.taskmanager.entities.TaskStatus;
import com.deployfast.taskmanager.entities.User;
import java.time.LocalDateTime;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-03-09T10:15:00+0100",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.1 (Oracle Corporation)"
)
@Component
public class TaskMapperImpl implements TaskMapper {

    @Override
    public Task toEntity(TaskDtos.TaskRequest request) {
        if ( request == null ) {
            return null;
        }

        Task.TaskBuilder task = Task.builder();

        task.title( request.title() );
        task.description( request.description() );
        task.dueDate( request.dueDate() );

        return task.build();
    }

    @Override
    public TaskDtos.TaskResponse toResponse(Task task) {
        if ( task == null ) {
            return null;
        }

        String ownerEmail = null;
        Long id = null;
        String title = null;
        String description = null;
        TaskStatus status = null;
        LocalDateTime dueDate = null;
        LocalDateTime createdAt = null;
        LocalDateTime updatedAt = null;

        ownerEmail = taskUserEmail( task );
        id = task.getId();
        title = task.getTitle();
        description = task.getDescription();
        status = task.getStatus();
        dueDate = task.getDueDate();
        createdAt = task.getCreatedAt();
        updatedAt = task.getUpdatedAt();

        TaskDtos.TaskResponse taskResponse = new TaskDtos.TaskResponse( id, title, description, status, dueDate, createdAt, updatedAt, ownerEmail );

        return taskResponse;
    }

    @Override
    public TaskDtos.TaskResponse toResponseWithoutOwner(Task task) {
        if ( task == null ) {
            return null;
        }

        Long id = null;
        String title = null;
        String description = null;
        TaskStatus status = null;
        LocalDateTime dueDate = null;
        LocalDateTime createdAt = null;
        LocalDateTime updatedAt = null;

        id = task.getId();
        title = task.getTitle();
        description = task.getDescription();
        status = task.getStatus();
        dueDate = task.getDueDate();
        createdAt = task.getCreatedAt();
        updatedAt = task.getUpdatedAt();

        String ownerEmail = null;

        TaskDtos.TaskResponse taskResponse = new TaskDtos.TaskResponse( id, title, description, status, dueDate, createdAt, updatedAt, ownerEmail );

        return taskResponse;
    }

    @Override
    public void updateEntityFromRequest(TaskDtos.TaskRequest request, Task task) {
        if ( request == null ) {
            return;
        }

        task.setTitle( request.title() );
        task.setDescription( request.description() );
        task.setDueDate( request.dueDate() );
    }

    private String taskUserEmail(Task task) {
        if ( task == null ) {
            return null;
        }
        User user = task.getUser();
        if ( user == null ) {
            return null;
        }
        String email = user.getEmail();
        if ( email == null ) {
            return null;
        }
        return email;
    }
}
