package com.commerce.project.service.impl;

import com.commerce.project.exception.APIException;
import com.commerce.project.exception.ResourceNotFoundException;
import com.commerce.project.model.Cart;
import com.commerce.project.model.Category;
import com.commerce.project.model.Product;
import com.commerce.project.payload.ProductDTO;
import com.commerce.project.payload.ProductResponse;
import com.commerce.project.repository.CartRepository;
import com.commerce.project.repository.CategoryRepository;
import com.commerce.project.repository.ProductRepository;
import com.commerce.project.service.CartService;
import com.commerce.project.service.FileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private FileService fileService;

    @Mock
    private CartService cartService;

    @Mock
    private CartRepository cartRepository;

    @Spy
    private ModelMapper modelMapper = new ModelMapper();

    @InjectMocks
    private ProductServiceImpl productService;

    private Product product;
    private ProductDTO productDTO;
    private Category category;
    private Long productId;
    private Long categoryId;
    private List<Product> productList;
    private Page<Product> productPage;
    private String imagePath;
    private String imageBaseUrl;

    @BeforeEach
    void setUp() {
        // Initialize test data
        productId = 1L;
        categoryId = 1L;
        imagePath = "images/";
        imageBaseUrl = "http://localhost:8080/images";

        // Set up category
        category = new Category();
        category.setCategoryId(categoryId);
        category.setCategoryName("Test Category");
        category.setProducts(new ArrayList<>());

        // Set up product
        product = new Product();
        product.setProductId(productId);
        product.setProductName("Test Product");
        product.setDescription("This is a test product");
        product.setImage("test.jpg");
        product.setQuantity(10);
        product.setPrice(100.0);
        product.setDiscount(10.0);
        product.setSpecialPrice(90.0);
        product.setCategory(category);

        // Set up product DTO
        productDTO = new ProductDTO();
        productDTO.setProductId(productId);
        productDTO.setProductName("Test Product");
        productDTO.setDescription("This is a test product");
        productDTO.setImage("test.jpg");
        productDTO.setQuantity(10);
        productDTO.setPrice(100.0);
        productDTO.setDiscount(10.0);
        productDTO.setSpecialPrice(90.0);

        // Set up product list and page
        productList = new ArrayList<>();
        productList.add(product);
        productPage = new PageImpl<>(productList);

        // Set fields using reflection
        ReflectionTestUtils.setField(productService, "path", imagePath);
        ReflectionTestUtils.setField(productService, "imageBaseUrl", imageBaseUrl);
    }

    @Test
    void getAllProducts_ShouldReturnProductResponse() {
        // Arrange
        when(productRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(productPage);

        // Act
        ProductResponse result = productService.getAllProducts(0, 10, "productId", "asc", null, null);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(productDTO.getProductId(), result.getContent().get(0).getProductId());
        assertEquals(productDTO.getProductName(), result.getContent().get(0).getProductName());
        assertEquals(0, result.getPageNumber());
        assertEquals(1, result.getTotalPages());
        assertTrue(result.isLastPage());
        verify(productRepository, times(1)).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void addProduct_ShouldReturnProductDTO_WhenProductNameIsUnique() {
        // Arrange
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        // Set up empty product list for category to indicate product name is unique
        category.setProducts(new ArrayList<>());

        // Act
        ProductDTO result = productService.addProduct(categoryId, productDTO);

        // Assert
        assertNotNull(result);
        assertEquals(productDTO.getProductId(), result.getProductId());
        assertEquals(productDTO.getProductName(), result.getProductName());
        verify(categoryRepository, times(1)).findById(categoryId);
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void addProduct_ShouldThrowAPIException_WhenProductNameExists() {
        // Arrange
        // Create a product with same name as the one we're trying to add
        Product existingProduct = new Product();
        existingProduct.setProductId(2L);
        existingProduct.setProductName(productDTO.getProductName());

        List<Product> categoryProducts = new ArrayList<>();
        categoryProducts.add(existingProduct);

        Category categoryWithProduct = new Category();
        categoryWithProduct.setCategoryId(categoryId);
        categoryWithProduct.setCategoryName("Test Category");
        categoryWithProduct.setProducts(categoryProducts);

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(categoryWithProduct));

        // Act & Assert
        assertThrows(APIException.class, () -> productService.addProduct(categoryId, productDTO));
        verify(categoryRepository, times(1)).findById(categoryId);
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void addProduct_ShouldThrowResourceNotFoundException_WhenCategoryNotFound() {
        // Arrange
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> productService.addProduct(categoryId, productDTO));
        verify(categoryRepository, times(1)).findById(categoryId);
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void searchByCategory_ShouldReturnProductResponse() {
        // Arrange
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(productRepository.findByCategoryOrderByPriceAsc(eq(category), any(Pageable.class))).thenReturn(productPage);

        // Act
        ProductResponse result = productService.searchByCategory(categoryId, 0, 10, "productId", "asc");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(productDTO.getProductId(), result.getContent().get(0).getProductId());
        verify(categoryRepository, times(1)).findById(categoryId);
        verify(productRepository, times(1)).findByCategoryOrderByPriceAsc(eq(category), any(Pageable.class));
    }

    @Test
    void searchByCategory_ShouldThrowResourceNotFoundException_WhenCategoryNotFound() {
        // Arrange
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> productService.searchByCategory(categoryId, 0, 10, "productId", "asc"));
        verify(categoryRepository, times(1)).findById(categoryId);
        verify(productRepository, never()).findByCategoryOrderByPriceAsc(any(Category.class), any(Pageable.class));
    }

    @Test
    void searchProductByKeyword_ShouldReturnProductResponse() {
        // Arrange
        String keyword = "test";
        when(productRepository.findByProductNameLikeIgnoreCase(anyString(), any(Pageable.class))).thenReturn(productPage);

        // Act
        ProductResponse result = productService.searchProductByKeyword(keyword, 0, 10, "productId", "asc");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(productDTO.getProductId(), result.getContent().get(0).getProductId());
        verify(productRepository, times(1)).findByProductNameLikeIgnoreCase(anyString(), any(Pageable.class));
    }

    @Test
    void updateProduct_ShouldReturnUpdatedProductDTO() {
        // Arrange
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        // Create a cart list for products
        List<Cart> emptyCartList = new ArrayList<>();
        when(cartRepository.findCartsByProductId(productId)).thenReturn(emptyCartList);

        // Act
        ProductDTO result = productService.updateProduct(productId, productDTO);

        // Assert
        assertNotNull(result);
        assertEquals(productDTO.getProductId(), result.getProductId());
        assertEquals(productDTO.getProductName(), result.getProductName());
        verify(productRepository, times(1)).findById(productId);
        verify(productRepository, times(1)).save(any(Product.class));
        verify(cartRepository, times(1)).findCartsByProductId(productId);
    }

    @Test
    void updateProduct_ShouldThrowResourceNotFoundException_WhenProductNotFound() {
        // Arrange
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> productService.updateProduct(productId, productDTO));
        verify(productRepository, times(1)).findById(productId);
        verify(productRepository, never()).save(any(Product.class));
        verify(cartRepository, never()).findCartsByProductId(anyLong());
    }

    @Test
    void deleteProduct_ShouldReturnDeletedProductDTO() {
        // Arrange
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        doNothing().when(productRepository).delete(any(Product.class));

        // Create empty cart list for the product
        List<Cart> emptyCartList = new ArrayList<>();
        when(cartRepository.findCartsByProductId(productId)).thenReturn(emptyCartList);

        // Act
        ProductDTO result = productService.deleteProduct(productId);

        // Assert
        assertNotNull(result);
        assertEquals(productDTO.getProductId(), result.getProductId());
        assertEquals(productDTO.getProductName(), result.getProductName());
        verify(productRepository, times(1)).findById(productId);
        verify(productRepository, times(1)).delete(any(Product.class));
        verify(cartRepository, times(1)).findCartsByProductId(productId);
    }

    @Test
    void deleteProduct_ShouldThrowResourceNotFoundException_WhenProductNotFound() {
        // Arrange
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> productService.deleteProduct(productId));
        verify(productRepository, times(1)).findById(productId);
        verify(productRepository, never()).delete(any(Product.class));
        verify(cartRepository, never()).findCartsByProductId(anyLong());
    }

    @Test
    void updateProductImage_ShouldReturnUpdatedProductDTO() throws IOException {
        // Arrange
        String newImageName = "new_image.jpg";
        MockMultipartFile mockImage = new MockMultipartFile(
                "image", "new_image.jpg", "image/jpeg", "test image content".getBytes()
        );

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(fileService.uploadImage(anyString(), any(MultipartFile.class))).thenReturn(newImageName);
        when(productRepository.save(any(Product.class))).thenReturn(product);

        // Act
        ProductDTO result = productService.updateProductImage(productId, mockImage);

        // Assert
        assertNotNull(result);
        assertEquals(productDTO.getProductId(), result.getProductId());
        verify(productRepository, times(1)).findById(productId);
        verify(fileService, times(1)).uploadImage(anyString(), any(MultipartFile.class));
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void updateProductImage_ShouldThrowResourceNotFoundException_WhenProductNotFound() throws IOException {
        // Arrange
        MockMultipartFile mockImage = new MockMultipartFile(
                "image", "new_image.jpg", "image/jpeg", "test image content".getBytes()
        );

        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> productService.updateProductImage(productId, mockImage));
        verify(productRepository, times(1)).findById(productId);
        verify(fileService, never()).uploadImage(anyString(), any(MultipartFile.class));
        verify(productRepository, never()).save(any(Product.class));
    }
}