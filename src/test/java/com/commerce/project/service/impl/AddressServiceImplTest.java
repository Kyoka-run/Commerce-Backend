package com.commerce.project.service.impl;

import com.commerce.project.exception.ResourceNotFoundException;
import com.commerce.project.model.Address;
import com.commerce.project.model.User;
import com.commerce.project.payload.AddressDTO;
import com.commerce.project.repository.AddressRepository;
import com.commerce.project.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AddressServiceImplTest {

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private UserRepository userRepository;

    @Spy
    private ModelMapper modelMapper = new ModelMapper();

    @InjectMocks
    private AddressServiceImpl addressService;

    private User user;
    private Address address;
    private AddressDTO addressDTO;
    private Long addressId;
    private List<Address> addressList;

    @BeforeEach
    void setUp() {
        // Set up test data
        addressId = 1L;

        user = new User();
        user.setUserId(1L);
        user.setUserName("testuser");
        user.setEmail("test@example.com");
        user.setAddresses(new ArrayList<>());

        address = new Address();
        address.setAddressId(addressId);
        address.setStreet("123 Test Street");
        address.setCity("Test City");
        address.setCountry("Test Country");
        address.setPostcode("12345");
        address.setUser(user);

        addressDTO = new AddressDTO();
        addressDTO.setAddressId(addressId);
        addressDTO.setStreet("123 Test Street");
        addressDTO.setCity("Test City");
        addressDTO.setCountry("Test Country");
        addressDTO.setPostcode("12345");

        addressList = new ArrayList<>();
        addressList.add(address);

        user.setAddresses(addressList);
    }

    @Test
    void createAddress_ShouldReturnAddressDTO() {
        // Arrange
        when(addressRepository.save(any(Address.class))).thenReturn(address);

        // Act
        AddressDTO result = addressService.createAddress(addressDTO, user);

        // Assert
        assertNotNull(result);
        assertEquals(addressDTO.getAddressId(), result.getAddressId());
        assertEquals(addressDTO.getStreet(), result.getStreet());
        assertEquals(addressDTO.getCity(), result.getCity());
        assertEquals(addressDTO.getCountry(), result.getCountry());
        assertEquals(addressDTO.getPostcode(), result.getPostcode());
        verify(addressRepository, times(1)).save(any(Address.class));
    }

    @Test
    void getAddresses_ShouldReturnListOfAddressDTOs() {
        // Arrange
        when(addressRepository.findAll()).thenReturn(addressList);

        // Act
        List<AddressDTO> result = addressService.getAddresses();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(addressDTO.getAddressId(), result.get(0).getAddressId());
        assertEquals(addressDTO.getStreet(), result.get(0).getStreet());
        verify(addressRepository, times(1)).findAll();
    }

    @Test
    void getAddressesById_ShouldReturnAddressDTO_WhenAddressExists() {
        // Arrange
        when(addressRepository.findById(addressId)).thenReturn(Optional.of(address));

        // Act
        AddressDTO result = addressService.getAddressesById(addressId);

        // Assert
        assertNotNull(result);
        assertEquals(addressDTO.getAddressId(), result.getAddressId());
        assertEquals(addressDTO.getStreet(), result.getStreet());
        verify(addressRepository, times(1)).findById(addressId);
    }

    @Test
    void getAddressesById_ShouldThrowException_WhenAddressDoesNotExist() {
        // Arrange
        when(addressRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> addressService.getAddressesById(addressId));
        verify(addressRepository, times(1)).findById(addressId);
    }

    @Test
    void getUserAddresses_ShouldReturnUserAddressList() {
        // Act
        List<AddressDTO> result = addressService.getUserAddresses(user);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(addressDTO.getAddressId(), result.get(0).getAddressId());
    }

    @Test
    void updateAddress_ShouldReturnUpdatedAddressDTO_WhenAddressExists() {
        // Arrange
        when(addressRepository.findById(addressId)).thenReturn(Optional.of(address));
        when(addressRepository.save(any(Address.class))).thenReturn(address);

        // Create a modified address DTO
        AddressDTO modifiedAddressDTO = new AddressDTO();
        modifiedAddressDTO.setAddressId(addressId);
        modifiedAddressDTO.setStreet("456 New Street");
        modifiedAddressDTO.setCity("New City");
        modifiedAddressDTO.setCountry("New Country");
        modifiedAddressDTO.setPostcode("67890");

        // Act
        AddressDTO result = addressService.updateAddress(addressId, modifiedAddressDTO);

        // Assert
        assertNotNull(result);
        assertEquals(modifiedAddressDTO.getStreet(), result.getStreet());
        assertEquals(modifiedAddressDTO.getCity(), result.getCity());
        assertEquals(modifiedAddressDTO.getCountry(), result.getCountry());
        assertEquals(modifiedAddressDTO.getPostcode(), result.getPostcode());
        verify(addressRepository, times(1)).findById(addressId);
        verify(addressRepository, times(1)).save(any(Address.class));
    }

    @Test
    void updateAddress_ShouldThrowException_WhenAddressDoesNotExist() {
        // Arrange
        when(addressRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> addressService.updateAddress(addressId, addressDTO));
        verify(addressRepository, times(1)).findById(addressId);
        verify(addressRepository, never()).save(any(Address.class));
    }

    @Test
    void deleteAddress_ShouldReturnSuccessMessage_WhenAddressExists() {
        // Arrange
        when(addressRepository.findById(addressId)).thenReturn(Optional.of(address));
        doNothing().when(addressRepository).delete(any(Address.class));

        // Act
        String result = addressService.deleteAddress(addressId);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("Address deleted successfully"));
        verify(addressRepository, times(1)).findById(addressId);
        verify(addressRepository, times(1)).delete(any(Address.class));
    }

    @Test
    void deleteAddress_ShouldThrowException_WhenAddressDoesNotExist() {
        // Arrange
        when(addressRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> addressService.deleteAddress(addressId));
        verify(addressRepository, times(1)).findById(addressId);
        verify(addressRepository, never()).delete(any(Address.class));
    }
}