package com.commerce.project.service.impl;

import com.commerce.project.Util.AuthUtil;
import com.commerce.project.exception.APIException;
import com.commerce.project.exception.ResourceNotFoundException;
import com.commerce.project.model.Cart;
import com.commerce.project.model.CartItem;
import com.commerce.project.model.Product;
import com.commerce.project.model.User;
import com.commerce.project.payload.CartDTO;
import com.commerce.project.payload.CartItemDTO;
import com.commerce.project.payload.ProductDTO;
import com.commerce.project.repository.CartItemRepository;
import com.commerce.project.repository.CartRepository;
import com.commerce.project.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CartServiceImplTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private AuthUtil authUtil;

    @Spy
    private ModelMapper modelMapper = new ModelMapper();

    @InjectMocks
    private CartServiceImpl cartService;

    private User user;
    private Product product;
    private Cart cart;
    private CartItem cartItem;
    private CartDTO cartDTO;
    private ProductDTO productDTO;
    private Long cartId;
    private Long productId;
    private List<CartItemDTO> cartItemDTOs;
    private String email;

    @BeforeEach
    void setUp() {
        // Set up test data
        cartId = 1L;
        productId = 1L;
        email = "test@example.com";

        user = new User();
        user.setUserId(1L);
        user.setUserName("testuser");
        user.setEmail(email);

        product = new Product();
        product.setProductId(productId);
        product.setProductName("Test Product");
        product.setDescription("This is a test product");
        product.setQuantity(10);
        product.setPrice(100.0);
        product.setDiscount(10.0);
        product.setSpecialPrice(90.0);

        cart = new Cart();
        cart.setCartId(cartId);
        cart.setUser(user);
        cart.setTotalPrice(0.0);
        cart.setCartItems(new ArrayList<>());

        cartItem = new CartItem();
        cartItem.setCartItemId(1L);
        cartItem.setCart(cart);
        cartItem.setProduct(product);
        cartItem.setQuantity(1);
        cartItem.setPrice(product.getSpecialPrice());
        cartItem.setDiscount(product.getDiscount());

        productDTO = new ProductDTO();
        productDTO.setProductId(productId);
        productDTO.setProductName("Test Product");
        productDTO.setDescription("This is a test product");
        productDTO.setQuantity(10);
        productDTO.setPrice(100.0);
        productDTO.setDiscount(10.0);
        productDTO.setSpecialPrice(90.0);

        cartDTO = new CartDTO();
        cartDTO.setCartId(cartId);
        cartDTO.setTotalPrice(0.0);
        cartDTO.setProducts(new ArrayList<>());

        CartItemDTO cartItemDTO = new CartItemDTO();
        cartItemDTO.setProductId(productId);
        cartItemDTO.setQuantity(1);

        cartItemDTOs = new ArrayList<>();
        cartItemDTOs.add(cartItemDTO);
    }

    @Test
    void addProductToCart_ShouldReturnCartDTO_WhenProductNotInCartAndInStock() {
        // Arrange
        when(authUtil.loggedInEmail()).thenReturn(email);
        when(cartRepository.findCartByEmail(email)).thenReturn(null);
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(cartItemRepository.findCartItemByProductIdAndCartId(cartId, productId)).thenReturn(null);
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(cartItem);
        when(authUtil.loggedInUser()).thenReturn(user);

        // Act
        CartDTO result = cartService.addProductToCart(productId, 1);

        // Assert
        assertNotNull(result);
        assertEquals(cartId, result.getCartId());
        verify(productRepository, times(1)).findById(productId);
        verify(cartRepository, times(2)).save(any(Cart.class));
        verify(cartItemRepository, times(1)).save(any(CartItem.class));
    }

    @Test
    void addProductToCart_ShouldThrowAPIException_WhenProductAlreadyInCart() {
        // Arrange
        when(authUtil.loggedInEmail()).thenReturn(email);
        when(cartRepository.findCartByEmail(email)).thenReturn(cart);
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(cartItemRepository.findCartItemByProductIdAndCartId(cartId, productId)).thenReturn(cartItem);

        // Act & Assert
        assertThrows(APIException.class, () -> cartService.addProductToCart(productId, 1));
        verify(productRepository, times(1)).findById(productId);
        verify(cartRepository, never()).save(any(Cart.class));
        verify(cartItemRepository, never()).save(any(CartItem.class));
    }

    @Test
    void addProductToCart_ShouldThrowAPIException_WhenProductOutOfStock() {
        // Arrange
        when(authUtil.loggedInEmail()).thenReturn(email);
        when(cartRepository.findCartByEmail(email)).thenReturn(cart);

        Product outOfStockProduct = new Product();
        outOfStockProduct.setProductId(productId);
        outOfStockProduct.setProductName("Out of Stock Product");
        outOfStockProduct.setQuantity(0);

        when(productRepository.findById(productId)).thenReturn(Optional.of(outOfStockProduct));
        when(cartItemRepository.findCartItemByProductIdAndCartId(cartId, productId)).thenReturn(null);

        // Act & Assert
        assertThrows(APIException.class, () -> cartService.addProductToCart(productId, 1));
        verify(productRepository, times(1)).findById(productId);
        verify(cartRepository, never()).save(any(Cart.class));
        verify(cartItemRepository, never()).save(any(CartItem.class));
    }

    @Test
    void getAllCarts_ShouldReturnCartDTOs() {
        // Arrange
        List<Cart> carts = Arrays.asList(cart);
        when(cartRepository.findAll()).thenReturn(carts);

        // Act
        List<CartDTO> result = cartService.getAllCarts();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(cartId, result.get(0).getCartId());
        verify(cartRepository, times(1)).findAll();
    }

    @Test
    void getAllCarts_ShouldThrowAPIException_WhenNoCartsExist() {
        // Arrange
        when(cartRepository.findAll()).thenReturn(new ArrayList<>());

        // Act & Assert
        assertThrows(APIException.class, () -> cartService.getAllCarts());
        verify(cartRepository, times(1)).findAll();
    }

    @Test
    void getCart_ShouldReturnCartDTO() {
        // Arrange
        when(cartRepository.findCartByEmailAndCartId(email, cartId)).thenReturn(cart);

        // Act
        CartDTO result = cartService.getCart(email, cartId);

        // Assert
        assertNotNull(result);
        assertEquals(cartId, result.getCartId());
        verify(cartRepository, times(1)).findCartByEmailAndCartId(email, cartId);
    }

    @Test
    void getCart_ShouldThrowResourceNotFoundException_WhenCartDoesNotExist() {
        // Arrange
        when(cartRepository.findCartByEmailAndCartId(email, cartId)).thenReturn(null);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> cartService.getCart(email, cartId));
        verify(cartRepository, times(1)).findCartByEmailAndCartId(email, cartId);
    }

    @Test
    void createOrUpdateCartWithItems_ShouldReturnSuccessMessage() {
        // Arrange
        when(authUtil.loggedInEmail()).thenReturn(email);
        when(cartRepository.findCartByEmail(email)).thenReturn(null);
        when(authUtil.loggedInUser()).thenReturn(user);
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(cartItem);

        // Act
        String result = cartService.createOrUpdateCartWithItems(cartItemDTOs);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("successfully"));
        verify(authUtil, times(1)).loggedInEmail();
        verify(authUtil, times(1)).loggedInUser();
        verify(cartRepository, times(2)).save(any(Cart.class));
        verify(productRepository, times(1)).findById(productId);
        verify(cartItemRepository, times(1)).save(any(CartItem.class));
    }

    @Test
    void updateProductQuantityInCart_ShouldIncreaseQuantity() {
        // Arrange
        when(authUtil.loggedInEmail()).thenReturn(email);
        when(cartRepository.findCartByEmail(email)).thenReturn(cart);
        when(cartRepository.findById(cartId)).thenReturn(Optional.of(cart));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(cartItemRepository.findCartItemByProductIdAndCartId(cartId, productId)).thenReturn(cartItem);
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(cartItem);

        // Act
        CartDTO result = cartService.updateProductQuantityInCart(productId, 1);

        // Assert
        assertNotNull(result);
        assertEquals(cartId, result.getCartId());
        verify(authUtil, times(1)).loggedInEmail();
        verify(cartRepository, times(1)).findCartByEmail(email);
        verify(cartRepository, times(1)).findById(cartId);
        verify(productRepository, times(1)).findById(productId);
        verify(cartItemRepository, times(1)).findCartItemByProductIdAndCartId(cartId, productId);
        verify(cartRepository, times(1)).save(any(Cart.class));
        verify(cartItemRepository, times(1)).save(any(CartItem.class));
    }

    @Test
    void deleteProductFromCart_ShouldReturnSuccessMessage() {
        // Arrange
        when(cartRepository.findById(cartId)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findCartItemByProductIdAndCartId(cartId, productId)).thenReturn(cartItem);
        doNothing().when(cartItemRepository).deleteCartItemByProductIdAndCartId(cartId, productId);

        // Act
        String result = cartService.deleteProductFromCart(cartId, productId);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("removed from the cart"));
        verify(cartRepository, times(1)).findById(cartId);
        verify(cartItemRepository, times(1)).findCartItemByProductIdAndCartId(cartId, productId);
        verify(cartItemRepository, times(1)).deleteCartItemByProductIdAndCartId(cartId, productId);
    }
}