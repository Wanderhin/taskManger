package com.deployfast.taskmanager.mappers;

import com.deployfast.taskmanager.dtos.TaskDtos;
import com.deployfast.taskmanager.entities.Task;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface TaskMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Task toEntity(TaskDtos.TaskRequest request);

    @Mapping(target = "ownerEmail", source = "user.email")
    TaskDtos.TaskResponse toResponse(Task task);

    @Mapping(target = "ownerEmail", ignore = true)
    TaskDtos.TaskResponse toResponseWithoutOwner(Task task);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromRequest(TaskDtos.TaskRequest request, @MappingTarget Task task);
}
