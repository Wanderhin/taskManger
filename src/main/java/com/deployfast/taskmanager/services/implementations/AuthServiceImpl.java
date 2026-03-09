package com.deployfast.taskmanager.services.implementations;

import com.deployfast.taskmanager.dtos.AuthDtos;
import com.deployfast.taskmanager.entities.Role;
import com.deployfast.taskmanager.entities.User;
import com.deployfast.taskmanager.exceptions.EmailAlreadyUsedException;
import com.deployfast.taskmanager.mappers.UserMapper;
import com.deployfast.taskmanager.repositories.UserRepository;
import com.deployfast.taskmanager.security.config.UserUserDetails;
import com.deployfast.taskmanager.security.jwt.JwtService;
import com.deployfast.taskmanager.services.interfaces.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Override
    @Transactional
    public AuthDtos.AuthResponse register(AuthDtos.RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyUsedException(request.email());
        }

        User user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole((request.role() != null) ? request.role() : Role.SIMPLE_USER);

        User savedUser = userRepository.save(user);
        String token = jwtService.generateToken(new UserUserDetails(savedUser));

        return new AuthDtos.AuthResponse(token, savedUser.getEmail(), savedUser.getFullName(), savedUser.getRole());
    }

    @Override
    public AuthDtos.AuthResponse login(AuthDtos.LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur introuvable"));

        String token = jwtService.generateToken(new UserUserDetails(user));

        return new AuthDtos.AuthResponse(token, user.getEmail(), user.getFullName(), user.getRole());
    }
}
