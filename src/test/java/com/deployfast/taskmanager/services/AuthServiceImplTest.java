package com.deployfast.taskmanager.services;

import com.deployfast.taskmanager.dtos.AuthDtos;
import com.deployfast.taskmanager.entities.Role;
import com.deployfast.taskmanager.entities.User;
import com.deployfast.taskmanager.exceptions.EmailAlreadyUsedException;
import com.deployfast.taskmanager.mappers.UserMapper;
import com.deployfast.taskmanager.repositories.UserRepository;
import com.deployfast.taskmanager.security.config.UserUserDetails;
import com.deployfast.taskmanager.security.jwt.JwtService;
import com.deployfast.taskmanager.services.implementations.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitaires - AuthService")
class AuthServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private UserMapper userMapper;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;
    @Mock private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthServiceImpl authService;

    private AuthDtos.RegisterRequest registerRequest;
    private AuthDtos.RegisterRequest registerAdminRequest;
    private AuthDtos.LoginRequest loginRequest;
    private User simpleUser;
    private User adminUser;

    @BeforeEach
    void setUp() {
        registerRequest      = new AuthDtos.RegisterRequest("Jean Dupont", "jean@test.com", "password123", null);
        registerAdminRequest = new AuthDtos.RegisterRequest("Admin Boss", "admin@test.com", "password123", Role.ADMIN);
        loginRequest         = new AuthDtos.LoginRequest("jean@test.com", "password123");

        simpleUser = new User();
        simpleUser.setId(1L);
        simpleUser.setFullName("Jean Dupont");
        simpleUser.setEmail("jean@test.com");
        simpleUser.setPassword("encoded");
        simpleUser.setRole(Role.SIMPLE_USER);

        adminUser = new User();
        adminUser.setId(2L);
        adminUser.setFullName("Admin Boss");
        adminUser.setEmail("admin@test.com");
        adminUser.setPassword("encoded");
        adminUser.setRole(Role.ADMIN);
    }

    @Test
    @DisplayName("register - SIMPLE_USER par défaut quand role est null")
    void register_defaultRole_isSimpleUser() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userMapper.toEntity(any())).thenReturn(simpleUser);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(userRepository.save(any())).thenReturn(simpleUser);
        when(jwtService.generateToken(any(UserUserDetails.class))).thenReturn("jwt_token");

        AuthDtos.AuthResponse response = authService.register(registerRequest);

        assertThat(response.role()).isEqualTo(Role.SIMPLE_USER);
        assertThat(response.token()).isEqualTo("jwt_token");
    }

    @Test
    @DisplayName("register - ADMIN si role ADMIN fourni")
    void register_withAdminRole() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userMapper.toEntity(any())).thenReturn(adminUser);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(userRepository.save(any())).thenReturn(adminUser);
        when(jwtService.generateToken(any(UserUserDetails.class))).thenReturn("admin_token");

        AuthDtos.AuthResponse response = authService.register(registerAdminRequest);

        assertThat(response.role()).isEqualTo(Role.ADMIN);
    }

    @Test
    @DisplayName("register - échec : email déjà utilisé lève EmailAlreadyUsedException")
    void register_emailAlreadyUsed_throwsException() {
        when(userRepository.existsByEmail("jean@test.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(EmailAlreadyUsedException.class)
                .hasMessageContaining("jean@test.com");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("login - succès : retourne token et role")
    void login_success_returnsTokenAndRole() {
        when(authenticationManager.authenticate(any())).thenReturn(null);
        when(userRepository.findByEmail("jean@test.com")).thenReturn(Optional.of(simpleUser));
        // generateToken reçoit un UserUserDetails, pas un User brut
        when(jwtService.generateToken(any(UserUserDetails.class))).thenReturn("jwt_token");

        AuthDtos.AuthResponse response = authService.login(loginRequest);

        assertThat(response.token()).isEqualTo("jwt_token");
        assertThat(response.role()).isEqualTo(Role.SIMPLE_USER);
    }

    @Test
    @DisplayName("login - échec : mauvais mot de passe lève BadCredentialsException")
    void login_badCredentials_throwsException() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Credentials invalides"));

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(BadCredentialsException.class);
    }
}