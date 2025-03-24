package com.commerce.project.controller;

import com.commerce.project.model.*;
import com.commerce.project.payload.CartItemDTO;
import com.commerce.project.repository.*;
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

import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class CartControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;
    private Product testProduct;
    private Category testCategory;
    private Cart testCart;

    @BeforeEach
    void setUp() {
        // Create test user
        setupTestUser();

        // Create test category
        testCategory = new Category();
        testCategory.setCategoryName("Test Category");
        testCategory = categoryRepository.save(testCategory);

        // Create test product
        testProduct = new Product();
        testProduct.setProductName("Test Product");
        testProduct.setDescription("Test Product Description");
        testProduct.setImage("default.png");
        testProduct.setQuantity(100);
        testProduct.setPrice(50.0);
        testProduct.setDiscount(10.0);
        testProduct.setSpecialPrice(45.0);
        testProduct.setCategory(testCategory);
        testProduct = productRepository.save(testProduct);

        // Create test cart
        testCart = new Cart();
        testCart.setUser(testUser);
        testCart.setTotalPrice(0.0);
        testCart.setCartItems(new ArrayList<>());
        testCart = cartRepository.save(testCart);
    }

    private void setupTestUser() {
        userRepository.deleteAll();
        roleRepository.deleteAll();

        // Create user role
        Role userRole = new Role(AppRole.ROLE_USER);
        userRole = roleRepository.save(userRole);

        // Create test user
        testUser = new User();
        testUser.setUserName("testuser");
        testUser.setEmail("test@test.com");
        testUser.setPassword(passwordEncoder.encode("password"));
        Set<Role> roles = new HashSet<>();
        roles.add(userRole);
        testUser.setRoles(roles);
        testUser = userRepository.save(testUser);
    }

    @Test
    @WithMockUser(username = "testuser")
    void addProductToCart_ShouldAddProductToCart() throws Exception {
        mockMvc.perform(post("/api/carts/products/" + testProduct.getProductId() + "/quantity/2")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.totalPrice").value(90.0)); // 2 * 45.0 = 90.0

        // Verify cart has product
        Cart updatedCart = cartRepository.findById(testCart.getCartId()).orElseThrow();
        assert(!updatedCart.getCartItems().isEmpty());
        assert(updatedCart.getTotalPrice() == 90.0);
    }

    @Test
    @WithMockUser(username = "testuser")
    void createOrUpdateCart_ShouldCreateNewCart() throws Exception {
        // Setup cart items for request
        CartItemDTO cartItemDTO = new CartItemDTO();
        cartItemDTO.setProductId(testProduct.getProductId());
        cartItemDTO.setQuantity(3);

        List<CartItemDTO> cartItems = List.of(cartItemDTO);

        mockMvc.perform(post("/api/cart/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cartItems)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(content().string(containsString("successfully")));

        // Verify cart was created
        Cart userCart = cartRepository.findCartByEmail(testUser.getEmail());
        assert(userCart != null);
        assert(userCart.getTotalPrice() == 135.0); // 3 * 45.0 = 135.0
    }

    @Test
    @WithMockUser(username = "testuser")
    void getCartById_ShouldReturnUserCart() throws Exception {
        // First add a product to the cart
        CartItem cartItem = new CartItem();
        cartItem.setCart(testCart);
        cartItem.setProduct(testProduct);
        cartItem.setQuantity(2);
        cartItem.setPrice(testProduct.getSpecialPrice());
        cartItem.setDiscount(testProduct.getDiscount());

        testCart.getCartItems().add(cartItem);
        testCart.setTotalPrice(90.0); // 2 * 45.0
        testCart = cartRepository.save(testCart);

        mockMvc.perform(get("/api/carts/users/cart"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cartId").value(testCart.getCartId()))
                .andExpect(jsonPath("$.totalPrice").value(90.0))
                .andExpect(jsonPath("$.products").isArray())
                .andExpect(jsonPath("$.products[0].productName").value("Test Product"));
    }

    @Test
    void getCartById_ShouldReturnUnauthorized_WhenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/carts/users/cart"))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }
}