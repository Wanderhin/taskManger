package com.deployfast.taskmanager.mappers;

import com.deployfast.taskmanager.dtos.AuthDtos;
import com.deployfast.taskmanager.entities.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User toEntity(AuthDtos.RegisterRequest request);
}
