package com.commerce.project.controller;

import com.commerce.project.model.Address;
import com.commerce.project.model.AppRole;
import com.commerce.project.model.Role;
import com.commerce.project.model.User;
import com.commerce.project.payload.AddressDTO;
import com.commerce.project.repository.AddressRepository;
import com.commerce.project.repository.RoleRepository;
import com.commerce.project.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class AddressControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;
    private Address testAddress;

    @BeforeEach
    void setUp() {
        // Clean up existing users
        userRepository.deleteAll();

        // Create user role
        Role userRole = roleRepository.findByRoleName(AppRole.ROLE_USER)
                .orElseGet(() -> {
                    Role role = new Role(AppRole.ROLE_USER);
                    return roleRepository.save(role);
                });

        // Create test user
        testUser = new User();
        testUser.setUserName("testuser");
        testUser.setEmail("test@test.com");
        testUser.setPassword(passwordEncoder.encode("password"));
        Set<Role> roles = new HashSet<>();
        roles.add(userRole);
        testUser.setRoles(roles);
        testUser = userRepository.save(testUser);

        // Create test address
        testAddress = new Address();
        testAddress.setStreet("123 Test St");
        testAddress.setCity("Test City");
        testAddress.setCountry("Test Country");
        testAddress.setPostcode("12345");
        testAddress.setUser(testUser);
        testAddress = addressRepository.save(testAddress);

        // Update user's addresses list
        if (testUser.getAddresses() == null) {
            testUser.setAddresses(new ArrayList<>());
        }
        testUser.getAddresses().add(testAddress);
    }

    @Test
    @WithMockUser(username = "testuser")
    void createAddress_ShouldCreateNewAddress() throws Exception {
        // Create address request
        AddressDTO addressDTO = new AddressDTO();
        addressDTO.setStreet("456 New St");
        addressDTO.setCity("New City");
        addressDTO.setCountry("New Country");
        addressDTO.setPostcode("67890");

        mockMvc.perform(post("/api/addresses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addressDTO)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.street").value("456 New St"))
                .andExpect(jsonPath("$.city").value("New City"))
                .andExpect(jsonPath("$.country").value("New Country"))
                .andExpect(jsonPath("$.postcode").value("67890"));

        // Verify address was created
        assert(addressRepository.findAll().size() == 2);
    }

    @Test
    @WithMockUser(username = "testuser")
    void getAddressById_ShouldReturnAddress() throws Exception {
        mockMvc.perform(get("/api/addresses/" + testAddress.getAddressId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.addressId").value(testAddress.getAddressId()))
                .andExpect(jsonPath("$.street").value("123 Test St"))
                .andExpect(jsonPath("$.city").value("Test City"))
                .andExpect(jsonPath("$.country").value("Test Country"))
                .andExpect(jsonPath("$.postcode").value("12345"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void getUserAddresses_ShouldReturnUserAddresses() throws Exception {
        mockMvc.perform(get("/api/users/addresses"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].addressId").value(testAddress.getAddressId()))
                .andExpect(jsonPath("$[0].street").value("123 Test St"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void updateAddress_ShouldUpdateAddress() throws Exception {
        // Create update request
        AddressDTO updateDTO = new AddressDTO();
        updateDTO.setStreet("789 Updated St");
        updateDTO.setCity("Updated City");
        updateDTO.setCountry("Updated Country");
        updateDTO.setPostcode("54321");

        mockMvc.perform(put("/api/addresses/" + testAddress.getAddressId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.street").value("789 Updated St"))
                .andExpect(jsonPath("$.city").value("Updated City"));

        // Verify address was updated
        Address updatedAddress = addressRepository.findById(testAddress.getAddressId()).orElseThrow();
        assert(updatedAddress.getStreet().equals("789 Updated St"));
        assert(updatedAddress.getCity().equals("Updated City"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void deleteAddress_ShouldDeleteAddress() throws Exception {
        int initialCount = addressRepository.findAll().size();

        mockMvc.perform(delete("/api/addresses/" + testAddress.getAddressId()))
                .andDo(print())
                .andExpect(status().isOk());

        // Verify address was deleted
        assert(addressRepository.findById(testAddress.getAddressId()).isEmpty());
    }

    @Test
    void getAddressById_ShouldReturnUnauthorized_WhenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/addresses/" + testAddress.getAddressId()))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }
}