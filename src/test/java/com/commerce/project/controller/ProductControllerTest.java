package com.commerce.project.controller;

import com.commerce.project.payload.ProductDTO;
import com.commerce.project.payload.ProductResponse;
import com.commerce.project.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductControllerTest {

    @Mock
    private ProductService productService;

    @InjectMocks
    private ProductController productController;

    private ProductDTO productDTO;
    private List<ProductDTO> productList;
    private ProductResponse productResponse;
    private Long productId;
    private Long categoryId;
    private MockMultipartFile mockImage;

    @BeforeEach
    void setUp() {
        // Set up test data
        productId = 1L;
        categoryId = 1L;

        productDTO = new ProductDTO();
        productDTO.setProductId(productId);
        productDTO.setProductName("Test Product");
        productDTO.setDescription("This is a test product");
        productDTO.setImage("test.jpg");
        productDTO.setQuantity(10);
        productDTO.setPrice(100.0);
        productDTO.setDiscount(10.0);
        productDTO.setSpecialPrice(90.0);

        productList = new ArrayList<>();
        productList.add(productDTO);

        productResponse = new ProductResponse();
        productResponse.setContent(productList);
        productResponse.setPageNumber(0);
        productResponse.setPageSize(10);
        productResponse.setTotalElements(1L);
        productResponse.setTotalPages(1);
        productResponse.setLastPage(true);

        mockImage = new MockMultipartFile(
                "image", "test.jpg", "image/jpeg", "test image content".getBytes()
        );
    }

    @Test
    void addProduct_ShouldReturnAddedProduct() {
        // Arrange
        when(productService.addProduct(anyLong(), any(ProductDTO.class))).thenReturn(productDTO);

        // Act
        ResponseEntity<ProductDTO> response = productController.addProduct(productDTO, categoryId);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(productDTO, response.getBody());
        verify(productService, times(1)).addProduct(categoryId, productDTO);
    }

    @Test
    void getAllProducts_ShouldReturnProductResponse() {
        // Arrange
        when(productService.getAllProducts(anyInt(), anyInt(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(productResponse);

        // Act
        ResponseEntity<ProductResponse> response = productController.getAllProducts(
                "keyword", "category", 0, 10, "productId", "asc"
        );

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(productResponse, response.getBody());
        verify(productService, times(1)).getAllProducts(
                0, 10, "productId", "asc", "keyword", "category"
        );
    }

    @Test
    void getProductsByCategory_ShouldReturnProductResponse() {
        // Arrange
        when(productService.searchByCategory(anyLong(), anyInt(), anyInt(), anyString(), anyString()))
                .thenReturn(productResponse);

        // Act
        ResponseEntity<ProductResponse> response = productController.getProductsByCategory(
                categoryId, 0, 10, "productId", "asc"
        );

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(productResponse, response.getBody());
        verify(productService, times(1)).searchByCategory(
                categoryId, 0, 10, "productId", "asc"
        );
    }

    @Test
    void getProductsByKeyword_ShouldReturnProductResponse() {
        // Arrange
        String keyword = "test";
        when(productService.searchProductByKeyword(anyString(), anyInt(), anyInt(), anyString(), anyString()))
                .thenReturn(productResponse);

        // Act
        ResponseEntity<ProductResponse> response = productController.getProductsByKeyword(
                keyword, 0, 10, "productId", "asc"
        );

        // Assert
        assertEquals(HttpStatus.FOUND, response.getStatusCode());
        assertEquals(productResponse, response.getBody());
        verify(productService, times(1)).searchProductByKeyword(
                keyword, 0, 10, "productId", "asc"
        );
    }

    @Test
    void updateProduct_ShouldReturnUpdatedProduct() {
        // Arrange
        when(productService.updateProduct(anyLong(), any(ProductDTO.class))).thenReturn(productDTO);

        // Act
        ResponseEntity<ProductDTO> response = productController.updateProduct(productDTO, productId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(productDTO, response.getBody());
        verify(productService, times(1)).updateProduct(productId, productDTO);
    }

    @Test
    void deleteProduct_ShouldReturnDeletedProduct() {
        // Arrange
        when(productService.deleteProduct(anyLong())).thenReturn(productDTO);

        // Act
        ResponseEntity<ProductDTO> response = productController.deleteProduct(productId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(productDTO, response.getBody());
        verify(productService, times(1)).deleteProduct(productId);
    }

    @Test
    void updateProductImage_ShouldReturnUpdatedProduct() throws IOException {
        // Arrange
        when(productService.updateProductImage(anyLong(), any(MultipartFile.class))).thenReturn(productDTO);

        // Act
        ResponseEntity<ProductDTO> response = productController.updateProductImage(productId, mockImage);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(productDTO, response.getBody());
        verify(productService, times(1)).updateProductImage(productId, mockImage);
    }
}