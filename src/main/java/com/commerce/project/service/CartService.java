package com.commerce.project.service;

import com.commerce.project.payload.CartDTO;
import com.commerce.project.payload.CartItemDTO;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface CartService {
    CartDTO addProductToCart(Long productId, Integer quantity);
    List<CartDTO> getAllCarts();
    CartDTO getCart(String emailId, Long cartId);
    @Transactional
    CartDTO updateProductQuantityInCart(Long productId, Integer quantity);
    @Transactional
    String deleteProductFromCart(Long cartId, Long productId);
    void updateProductInCarts(Long cartId, Long productId);
    String createOrUpdateCartWithItems(List<CartItemDTO> cartItems);
}
