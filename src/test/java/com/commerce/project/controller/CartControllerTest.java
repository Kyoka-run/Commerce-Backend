package com.commerce.project.controller;

import com.commerce.project.Util.AuthUtil;
import com.commerce.project.model.Cart;
import com.commerce.project.payload.CartDTO;
import com.commerce.project.payload.CartItemDTO;
import com.commerce.project.repository.CartRepository;
import com.commerce.project.service.CartService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CartControllerTest {

    @Mock
    private CartService cartService;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private AuthUtil authUtil;

    @InjectMocks
    private CartController cartController;

    private CartDTO cartDTO;
    private Cart cart;
    private Long cartId;
    private Long productId;
    private List<CartItemDTO> cartItems;
    private String email;

    @BeforeEach
    void setUp() {
        // Set up test data
        cartId = 1L;
        productId = 1L;
        email = "test@example.com";

        cartDTO = new CartDTO();
        cartDTO.setCartId(cartId);
        cartDTO.setTotalPrice(100.0);

        cart = new Cart();
        cart.setCartId(cartId);
        cart.setTotalPrice(100.0);

        CartItemDTO cartItemDTO = new CartItemDTO();
        cartItemDTO.setProductId(productId);
        cartItemDTO.setQuantity(2);

        cartItems = new ArrayList<>();
        cartItems.add(cartItemDTO);
    }

    @Test
    void createOrUpdateCart_ShouldReturnSuccessMessage() {
        // Arrange
        String successMessage = "Cart created/updated successfully";
        when(cartService.createOrUpdateCartWithItems(anyList())).thenReturn(successMessage);

        // Act
        ResponseEntity<String> response = cartController.createOrUpdateCart(cartItems);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(successMessage, response.getBody());
        verify(cartService, times(1)).createOrUpdateCartWithItems(cartItems);
    }

    @Test
    void addProductToCart_ShouldReturnCartWithAddedProduct() {
        // Arrange
        when(cartService.addProductToCart(anyLong(), anyInt())).thenReturn(cartDTO);

        // Act
        ResponseEntity<CartDTO> response = cartController.addProductToCart(productId, 2);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(cartDTO, response.getBody());
        verify(cartService, times(1)).addProductToCart(productId, 2);
    }

    @Test
    void getCarts_ShouldReturnAllCarts() {
        // Arrange
        List<CartDTO> cartDTOs = Arrays.asList(cartDTO);
        when(cartService.getAllCarts()).thenReturn(cartDTOs);

        // Act
        ResponseEntity<List<CartDTO>> response = cartController.getCarts();

        // Assert
        assertEquals(HttpStatus.FOUND, response.getStatusCode());
        assertEquals(cartDTOs, response.getBody());
        verify(cartService, times(1)).getAllCarts();
    }

    @Test
    void getCartById_ShouldReturnUserCart() {
        // Arrange
        when(authUtil.loggedInEmail()).thenReturn(email);
        when(cartRepository.findCartByEmail(email)).thenReturn(cart);
        when(cartService.getCart(anyString(), anyLong())).thenReturn(cartDTO);

        // Act
        ResponseEntity<CartDTO> response = cartController.getCartById();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(cartDTO, response.getBody());
        verify(authUtil, times(1)).loggedInEmail();
        verify(cartRepository, times(1)).findCartByEmail(email);
        verify(cartService, times(1)).getCart(email, cartId);
    }

    @Test
    void updateCartProduct_WithDelete_ShouldDecreaseQuantity() {
        // Arrange
        when(cartService.updateProductQuantityInCart(anyLong(), anyInt())).thenReturn(cartDTO);

        // Act
        ResponseEntity<CartDTO> response = cartController.updateCartProduct(productId, "delete");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(cartDTO, response.getBody());
        verify(cartService, times(1)).updateProductQuantityInCart(productId, -1);
    }

    @Test
    void updateCartProduct_WithAdd_ShouldIncreaseQuantity() {
        // Arrange
        when(cartService.updateProductQuantityInCart(anyLong(), anyInt())).thenReturn(cartDTO);

        // Act
        ResponseEntity<CartDTO> response = cartController.updateCartProduct(productId, "add");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(cartDTO, response.getBody());
        verify(cartService, times(1)).updateProductQuantityInCart(productId, 1);
    }

    @Test
    void deleteProductFromCart_ShouldReturnSuccessMessage() {
        // Arrange
        String successMessage = "Product removed from cart successfully";
        when(cartService.deleteProductFromCart(anyLong(), anyLong())).thenReturn(successMessage);

        // Act
        ResponseEntity<String> response = cartController.deleteProductFromCart(cartId, productId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(successMessage, response.getBody());
        verify(cartService, times(1)).deleteProductFromCart(cartId, productId);
    }
}