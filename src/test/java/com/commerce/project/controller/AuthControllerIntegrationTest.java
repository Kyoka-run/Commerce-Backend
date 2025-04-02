package com.commerce.project.controller;

import com.commerce.project.model.AppRole;
import com.commerce.project.model.Role;
import com.commerce.project.model.User;
import com.commerce.project.repository.RoleRepository;
import com.commerce.project.repository.UserRepository;
import com.commerce.project.security.request.LoginRequest;
import com.commerce.project.security.request.SignupRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final String TEST_USERNAME = "test1";
    private final String TEST_EMAIL = "test@gmail.com";
    private final String TEST_PASSWORD = "password";

    @BeforeEach
    void setUp() {
        // Ensure required roles exist
        if (roleRepository.findByRoleName(AppRole.ROLE_USER).isEmpty()) {
            Role userRole = new Role(AppRole.ROLE_USER);
            roleRepository.save(userRole);
        }

        if (roleRepository.findByRoleName(AppRole.ROLE_ADMIN).isEmpty()) {
            Role adminRole = new Role(AppRole.ROLE_ADMIN);
            roleRepository.save(adminRole);
        }

        // Clean up any existing test users
        userRepository.findByUserName(TEST_USERNAME).ifPresent(user -> userRepository.delete(user));
    }

    @Test
    void signup_ShouldReturnSuccess() throws Exception {
        // Create signup request
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setUsername(TEST_USERNAME);
        signupRequest.setEmail(TEST_EMAIL);
        signupRequest.setPassword(TEST_PASSWORD);

        Set<String> roles = new HashSet<>();
        roles.add("user");
        signupRequest.setRole(roles);

        // Execute request
        ResultActions resultActions = mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andDo(print());

        // Verify response
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User registered successfully"));
    }

    @Test
    void signup_ShouldReturnError_WhenUsernameTaken() throws Exception {
        // Create an existing user
        User user = new User();
        user.setUserName(TEST_USERNAME);
        user.setEmail("other@test.com");
        user.setPassword(passwordEncoder.encode(TEST_PASSWORD));

        // Add roles to user
        Role userRole = roleRepository.findByRoleName(AppRole.ROLE_USER).orElseThrow();
        Set<Role> roles = new HashSet<>();
        roles.add(userRole);
        user.setRoles(roles);

        userRepository.save(user);

        // Try to register with the same username
        SignupRequest duplicateUsernameRequest = new SignupRequest();
        duplicateUsernameRequest.setUsername(TEST_USERNAME);
        duplicateUsernameRequest.setEmail(TEST_EMAIL);
        duplicateUsernameRequest.setPassword(TEST_PASSWORD);

        ResultActions duplicateUsernameAction = mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateUsernameRequest)))
                .andDo(print());

        duplicateUsernameAction
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Error: Username is already taken!"));
    }

    @Test
    void signin_ShouldReturnJwtToken_WhenCredentialsValid() throws Exception {
        // First create a user to test with
        User user = new User();
        user.setUserName(TEST_USERNAME);
        user.setEmail(TEST_EMAIL);
        user.setPassword(passwordEncoder.encode(TEST_PASSWORD));

        // Add roles to user
        Role userRole = roleRepository.findByRoleName(AppRole.ROLE_USER).orElseThrow();
        Set<Role> roles = new HashSet<>();
        roles.add(userRole);
        user.setRoles(roles);

        userRepository.save(user);

        // Login request with valid credentials
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername(TEST_USERNAME);
        loginRequest.setPassword(TEST_PASSWORD);

        ResultActions resultActions = mockMvc.perform(post("/api/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andDo(print());

        resultActions
                .andExpect(status().isOk())
                .andExpect(cookie().exists("SpringBootCommerce"))
                .andExpect(jsonPath("$.username").value(TEST_USERNAME))
                .andExpect(jsonPath("$.email").value(TEST_EMAIL))
                .andExpect(jsonPath("$.roles").isArray())
                .andExpect(jsonPath("$.roles[0]").value("ROLE_USER"));
    }

    @Test
    void signin_ShouldReturnError_WhenCredentialsInvalid() throws Exception {
        // Login request with invalid credentials
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("wronguser");
        loginRequest.setPassword("wrongpass");

        ResultActions resultActions = mockMvc.perform(post("/api/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andDo(print());

        resultActions
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Bad credentials"));
    }
}