package com.deployfast.taskmanager.mappers;

import com.deployfast.taskmanager.dtos.AuthDtos;
import com.deployfast.taskmanager.entities.User;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-03-09T10:15:00+0100",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.1 (Oracle Corporation)"
)
@Component
public class UserMapperImpl implements UserMapper {

    @Override
    public User toEntity(AuthDtos.RegisterRequest request) {
        if ( request == null ) {
            return null;
        }

        User.UserBuilder user = User.builder();

        user.email( request.email() );
        user.password( request.password() );
        user.fullName( request.fullName() );
        user.role( request.role() );

        return user.build();
    }
}
