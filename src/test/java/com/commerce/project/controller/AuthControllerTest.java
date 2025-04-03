package com.commerce.project.controller;

import com.commerce.project.model.AppRole;
import com.commerce.project.model.Role;
import com.commerce.project.model.User;
import com.commerce.project.repository.RoleRepository;
import com.commerce.project.repository.UserRepository;
import com.commerce.project.security.jwt.JwtUtils;
import com.commerce.project.security.request.LoginRequest;
import com.commerce.project.security.request.SignupRequest;
import com.commerce.project.security.response.LoginResponse;
import com.commerce.project.security.response.MessageResponse;
import com.commerce.project.security.service.UserDetailsImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthControllerTest {

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthController authController;

    private LoginRequest loginRequest;
    private SignupRequest signupRequest;
    private User user;
    private Role userRole;
    private UserDetailsImpl userDetails;
    private String jwtToken;

    @BeforeEach
    void setUp() {
        // Set up test data
        loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password");

        signupRequest = new SignupRequest();
        signupRequest.setUsername("newuser");
        signupRequest.setEmail("newuser@example.com");
        signupRequest.setPassword("password");
        Set<String> roles = new HashSet<>();
        roles.add("user");
        signupRequest.setRole(roles);

        user = new User();
        user.setUserId(1L);
        user.setUserName("testuser");
        user.setEmail("test@example.com");
        user.setPassword("encodedPassword");

        userRole = new Role();
        userRole.setRoleId(1);
        userRole.setRoleName(AppRole.ROLE_USER);

        Collection<SimpleGrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority(AppRole.ROLE_USER.name()));

        userDetails = new UserDetailsImpl();
        userDetails.setId(1L);
        userDetails.setUsername("testuser");
        userDetails.setEmail("test@example.com");
        userDetails.setPassword("encodedPassword");
        userDetails.setAuthorities(authorities);

        jwtToken = "test.jwt.token";
    }

    @Test
    void authenticateUser_Success() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(jwtUtils.generateTokenFromUsername(any(UserDetailsImpl.class))).thenReturn(jwtToken);

        // Act
        ResponseEntity<?> response = authController.authenticateUser(loginRequest);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof LoginResponse);
        LoginResponse loginResponse = (LoginResponse) response.getBody();
        assertEquals(jwtToken, loginResponse.getJwtToken());
        assertEquals(userDetails.getUsername(), loginResponse.getUsername());
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtUtils, times(1)).generateTokenFromUsername(any(UserDetailsImpl.class));
    }

    @Test
    void registerUser_UsernameAlreadyTaken() {
        // Arrange
        when(userRepository.existsByUserName(signupRequest.getUsername())).thenReturn(true);

        // Act
        ResponseEntity<?> response = authController.registerUser(signupRequest);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody() instanceof MessageResponse);
        MessageResponse messageResponse = (MessageResponse) response.getBody();
        assertEquals("Error: Username is already taken!", messageResponse.getMessage());
        verify(userRepository, times(1)).existsByUserName(signupRequest.getUsername());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registerUser_EmailAlreadyInUse() {
        // Arrange
        when(userRepository.existsByUserName(signupRequest.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(signupRequest.getEmail())).thenReturn(true);

        // Act
        ResponseEntity<?> response = authController.registerUser(signupRequest);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody() instanceof MessageResponse);
        MessageResponse messageResponse = (MessageResponse) response.getBody();
        assertEquals("Error: Email is already in use!", messageResponse.getMessage());
        verify(userRepository, times(1)).existsByUserName(signupRequest.getUsername());
        verify(userRepository, times(1)).existsByEmail(signupRequest.getEmail());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registerUser_Success() {
        // Arrange
        when(userRepository.existsByUserName(signupRequest.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(signupRequest.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(signupRequest.getPassword())).thenReturn("encodedPassword");
        when(roleRepository.findByRoleName(AppRole.ROLE_USER)).thenReturn(Optional.of(userRole));
        when(userRepository.save(any(User.class))).thenReturn(user);

        // Act
        ResponseEntity<?> response = authController.registerUser(signupRequest);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof MessageResponse);
        MessageResponse messageResponse = (MessageResponse) response.getBody();
        assertEquals("User registered successfully", messageResponse.getMessage());
        verify(userRepository, times(1)).existsByUserName(signupRequest.getUsername());
        verify(userRepository, times(1)).existsByEmail(signupRequest.getEmail());
        verify(passwordEncoder, times(1)).encode(signupRequest.getPassword());
        verify(roleRepository, times(1)).findByRoleName(AppRole.ROLE_USER);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void signOutUser() {
        // Act
        ResponseEntity<?> response = authController.signOutUser();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof MessageResponse);
        MessageResponse messageResponse = (MessageResponse) response.getBody();
        assertEquals("You've been logged out successfully", messageResponse.getMessage());
    }

    @Test
    void currentUserName_WhenAuthenticated() {
        // Arrange
        when(authentication.getName()).thenReturn("testuser");

        // Act
        String username = authController.currentUserName(authentication);

        // Assert
        assertEquals("testuser", username);
        verify(authentication, times(1)).getName();
    }

    @Test
    void currentUserName_WhenNotAuthenticated() {
        // Act
        String username = authController.currentUserName(null);

        // Assert
        assertEquals("NULL", username);
    }

    @Test
    void currentUserDetails() {
        // Arrange
        when(authentication.getPrincipal()).thenReturn(userDetails);

        // Act
        ResponseEntity<LoginResponse> response = authController.currentUserDetails(authentication);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(userDetails.getId(), response.getBody().getId());
        assertEquals(userDetails.getUsername(), response.getBody().getUsername());
        assertEquals(userDetails.getEmail(), response.getBody().getEmail());
        verify(authentication, times(1)).getPrincipal();
    }
}