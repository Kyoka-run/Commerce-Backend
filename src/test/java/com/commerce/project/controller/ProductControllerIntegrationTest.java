package com.commerce.project.controller;

import com.commerce.project.model.Category;
import com.commerce.project.model.Product;
import com.commerce.project.payload.ProductDTO;
import com.commerce.project.repository.CategoryRepository;
import com.commerce.project.repository.ProductRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class ProductControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private Category testCategory;

    @BeforeEach
    void setUp() {
        // Clear existing test data
        productRepository.deleteAll();
        categoryRepository.deleteAll();

        // Create test category
        testCategory = new Category();
        testCategory.setCategoryName("Test Category");
        testCategory = categoryRepository.save(testCategory);

        // Create test products
        for (int i = 1; i <= 3; i++) {
            Product product = new Product();
            product.setProductName("Test Product " + i);
            product.setDescription("Description for Test Product " + i);
            product.setImage("default.png");
            product.setQuantity(10 * i);
            product.setPrice(100.0 * i);
            product.setDiscount(10.0);
            product.setSpecialPrice(90.0 * i);
            product.setCategory(testCategory);
            productRepository.save(product);
        }
    }

    @Test
    void getAllProducts_ShouldReturnAllProducts() throws Exception {
        // Test getting all products (public endpoint)
        mockMvc.perform(get("/api/public/products"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(3)))
                .andExpect(jsonPath("$.content[0].productName", containsString("Test Product")))
                .andExpect(jsonPath("$.totalElements").value(3));
    }

    @Test
    void getProductsByCategory_ShouldReturnProductsInCategory() throws Exception {
        // Test getting products by category
        mockMvc.perform(get("/api/public/categories/" + testCategory.getCategoryId() + "/products"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(3)))
                .andExpect(jsonPath("$.content[0].productName", containsString("Test Product")));
    }

    @Test
    void searchProducts_ShouldReturnMatchingProducts() throws Exception {
        // Test searching for products by keyword
        mockMvc.perform(get("/api/public/products/keyword/Test"))
                .andDo(print())
                .andExpect(status().isFound())
                .andExpect(jsonPath("$.content", hasSize(3)));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void addProduct_ShouldCreateNewProduct() throws Exception {
        // Create product request
        ProductDTO productDTO = new ProductDTO();
        productDTO.setProductName("New Test Product");
        productDTO.setDescription("Description for New Test Product");
        productDTO.setQuantity(50);
        productDTO.setPrice(250.0);
        productDTO.setDiscount(15.0);
        productDTO.setSpecialPrice(212.5);

        // Test creating a new product
        mockMvc.perform(post("/api/admin/categories/" + testCategory.getCategoryId() + "/product")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productDTO)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.productName").value("New Test Product"))
                .andExpect(jsonPath("$.price").value(250.0));

        // Verify the product was created
        List<Product> products = productRepository.findAll();
        assert(products.size() == 4);
        assert(products.stream().anyMatch(p -> p.getProductName().equals("New Test Product")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateProduct_ShouldUpdateProduct() throws Exception {
        // Get an existing product
        Product existingProduct = productRepository.findAll().get(0);
        Long productId = existingProduct.getProductId();

        // Create update request
        ProductDTO updateDTO = new ProductDTO();
        updateDTO.setProductName("Updated Product Name");
        updateDTO.setDescription("Updated Description");
        updateDTO.setPrice(199.99);
        updateDTO.setQuantity(25);
        updateDTO.setDiscount(5.0);
        updateDTO.setSpecialPrice(189.99);

        // Test updating the product
        mockMvc.perform(put("/api/admin/products/" + productId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productName").value("Updated Product Name"))
                .andExpect(jsonPath("$.price").value(199.99));

        // Verify the product was updated
        Product updatedProduct = productRepository.findById(productId).orElseThrow();
        assert(updatedProduct.getProductName().equals("Updated Product Name"));
        assert(updatedProduct.getPrice() == 199.99);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteProduct_ShouldDeleteProduct() throws Exception {
        // Get an existing product
        Product existingProduct = productRepository.findAll().get(0);
        Long productId = existingProduct.getProductId();

        int initialCount = productRepository.findAll().size();

        // Test deleting the product
        mockMvc.perform(delete("/api/admin/products/" + productId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productName").value(existingProduct.getProductName()));

        // Verify the product was deleted
        List<Product> remainingProducts = productRepository.findAll();
        assert(remainingProducts.size() == initialCount - 1);
        assert(productRepository.findById(productId).isEmpty());
    }

    @Test
    void createProduct_ShouldReturnUnauthorized_WhenNotAuthenticated() throws Exception {
        // Create product request
        ProductDTO productDTO = new ProductDTO();
        productDTO.setProductName("New Test Product");
        productDTO.setDescription("Description for New Test Product");
        productDTO.setQuantity(50);
        productDTO.setPrice(250.0);

        // Test creating a new product without authentication
        mockMvc.perform(post("/api/admin/categories/" + testCategory.getCategoryId() + "/product")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productDTO)))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }
}