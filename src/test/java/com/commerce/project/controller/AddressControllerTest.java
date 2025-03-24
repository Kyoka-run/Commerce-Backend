package com.commerce.project.controller;

import com.commerce.project.Util.AuthUtil;
import com.commerce.project.model.User;
import com.commerce.project.payload.AddressDTO;
import com.commerce.project.service.AddressService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AddressControllerTest {

    @Mock
    private AuthUtil authUtil;

    @Mock
    private AddressService addressService;

    @InjectMocks
    private AddressController addressController;

    private User testUser;
    private AddressDTO addressDTO;
    private List<AddressDTO> addressList;
    private Long addressId;

    @BeforeEach
    void setUp() {
        // Set up test data
        testUser = new User();
        testUser.setUserId(1L);
        testUser.setUserName("testuser");
        testUser.setEmail("test@example.com");

        addressId = 1L;

        addressDTO = new AddressDTO();
        addressDTO.setAddressId(addressId);
        addressDTO.setStreet("123 Test Street");
        addressDTO.setCity("Test City");
        addressDTO.setCountry("Test Country");
        addressDTO.setPostcode("12345");

        addressList = new ArrayList<>();
        addressList.add(addressDTO);
    }

    @Test
    void createAddress_ShouldReturnCreatedAddress() {
        // Arrange
        when(authUtil.loggedInUser()).thenReturn(testUser);
        when(addressService.createAddress(any(AddressDTO.class), any(User.class))).thenReturn(addressDTO);

        // Act
        ResponseEntity<AddressDTO> response = addressController.createAddress(addressDTO);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(addressDTO, response.getBody());
        verify(authUtil, times(1)).loggedInUser();
        verify(addressService, times(1)).createAddress(eq(addressDTO), eq(testUser));
    }

    @Test
    void getAddresses_ShouldReturnListOfAddresses() {
        // Arrange
        when(addressService.getAddresses()).thenReturn(addressList);

        // Act
        ResponseEntity<List<AddressDTO>> response = addressController.getAddresses();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(addressList, response.getBody());
        assertEquals(1, response.getBody().size());
        verify(addressService, times(1)).getAddresses();
    }

    @Test
    void getAddressById_ShouldReturnAddress() {
        // Arrange
        when(addressService.getAddressesById(addressId)).thenReturn(addressDTO);

        // Act
        ResponseEntity<AddressDTO> response = addressController.getAddressById(addressId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(addressDTO, response.getBody());
        verify(addressService, times(1)).getAddressesById(addressId);
    }

    @Test
    void getUserAddresses_ShouldReturnUserAddresses() {
        // Arrange
        when(authUtil.loggedInUser()).thenReturn(testUser);
        when(addressService.getUserAddresses(testUser)).thenReturn(addressList);

        // Act
        ResponseEntity<List<AddressDTO>> response = addressController.getUserAddresses();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(addressList, response.getBody());
        verify(authUtil, times(1)).loggedInUser();
        verify(addressService, times(1)).getUserAddresses(testUser);
    }

    @Test
    void updateAddress_ShouldReturnUpdatedAddress() {
        // Arrange
        when(addressService.updateAddress(eq(addressId), any(AddressDTO.class))).thenReturn(addressDTO);

        // Act
        ResponseEntity<AddressDTO> response = addressController.updateAddress(addressId, addressDTO);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(addressDTO, response.getBody());
        verify(addressService, times(1)).updateAddress(addressId, addressDTO);
    }

    @Test
    void deleteAddress_ShouldReturnSuccessMessage() {
        // Arrange
        String successMessage = "Address deleted successfully";
        when(addressService.deleteAddress(addressId)).thenReturn(successMessage);

        // Act
        ResponseEntity<String> response = addressController.updateAddress(addressId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(successMessage, response.getBody());
        verify(addressService, times(1)).deleteAddress(addressId);
    }
}