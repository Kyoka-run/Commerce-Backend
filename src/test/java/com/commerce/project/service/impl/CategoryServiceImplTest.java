package com.commerce.project.service.impl;

import com.commerce.project.exception.APIException;
import com.commerce.project.exception.ResourceNotFoundException;
import com.commerce.project.model.Category;
import com.commerce.project.payload.CategoryDTO;
import com.commerce.project.payload.CategoryResponse;
import com.commerce.project.repository.CategoryRepository;
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
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Spy
    private ModelMapper modelMapper = new ModelMapper();

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private Category category;
    private CategoryDTO categoryDTO;
    private Long categoryId;
    private List<Category> categoryList;
    private Page<Category> categoryPage;

    @BeforeEach
    void setUp() {
        // Set up test data
        categoryId = 1L;

        category = new Category();
        category.setCategoryId(categoryId);
        category.setCategoryName("Test Category");

        categoryDTO = new CategoryDTO();
        categoryDTO.setCategoryId(categoryId);
        categoryDTO.setCategoryName("Test Category");

        categoryList = new ArrayList<>();
        categoryList.add(category);

        categoryPage = new PageImpl<>(categoryList);
    }

    @Test
    void getAllCategories_ShouldReturnCategoryResponse() {
        // Arrange
        when(categoryRepository.findAll(any(Pageable.class))).thenReturn(categoryPage);

        // Act
        CategoryResponse result = categoryService.getAllCategories(0, 10, "categoryId", "asc");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(categoryDTO.getCategoryId(), result.getContent().get(0).getCategoryId());
        assertEquals(categoryDTO.getCategoryName(), result.getContent().get(0).getCategoryName());
        assertEquals(0, result.getPageNumber());
        assertEquals(1, result.getPageSize());
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getTotalPages());
        assertTrue(result.isLastPage());
        verify(categoryRepository, times(1)).findAll(any(Pageable.class));
    }

    @Test
    void getAllCategories_ShouldThrowAPIException_WhenNoCategoriesFound() {
        // Arrange
        when(categoryRepository.findAll(any(Pageable.class))).thenReturn(Page.empty());

        // Act & Assert
        assertThrows(APIException.class, () -> categoryService.getAllCategories(0, 10, "categoryId", "asc"));
        verify(categoryRepository, times(1)).findAll(any(Pageable.class));
    }

    @Test
    void createCategory_ShouldReturnCategoryDTO() {
        // Arrange
        when(categoryRepository.findByCategoryName(anyString())).thenReturn(null);
        when(categoryRepository.save(any(Category.class))).thenReturn(category);

        // Act
        CategoryDTO result = categoryService.createCategory(categoryDTO);

        // Assert
        assertNotNull(result);
        assertEquals(categoryDTO.getCategoryId(), result.getCategoryId());
        assertEquals(categoryDTO.getCategoryName(), result.getCategoryName());
        verify(categoryRepository, times(1)).findByCategoryName(category.getCategoryName());
        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    @Test
    void createCategory_ShouldThrowAPIException_WhenCategoryAlreadyExists() {
        // Arrange
        when(categoryRepository.findByCategoryName(anyString())).thenReturn(category);

        // Act & Assert
        assertThrows(APIException.class, () -> categoryService.createCategory(categoryDTO));
        verify(categoryRepository, times(1)).findByCategoryName(category.getCategoryName());
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void deleteCategory_ShouldReturnDeletedCategoryDTO() {
        // Arrange
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        doNothing().when(categoryRepository).delete(any(Category.class));

        // Act
        CategoryDTO result = categoryService.deleteCategory(categoryId);

        // Assert
        assertNotNull(result);
        assertEquals(categoryDTO.getCategoryId(), result.getCategoryId());
        assertEquals(categoryDTO.getCategoryName(), result.getCategoryName());
        verify(categoryRepository, times(1)).findById(categoryId);
        verify(categoryRepository, times(1)).delete(any(Category.class));
    }

    @Test
    void deleteCategory_ShouldThrowResourceNotFoundException_WhenCategoryNotFound() {
        // Arrange
        when(categoryRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> categoryService.deleteCategory(categoryId));
        verify(categoryRepository, times(1)).findById(categoryId);
        verify(categoryRepository, never()).delete(any(Category.class));
    }

    @Test
    void updateCategory_ShouldReturnUpdatedCategoryDTO() {
        // Arrange
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(categoryRepository.save(any(Category.class))).thenReturn(category);

        // Act
        CategoryDTO result = categoryService.updateCategory(categoryDTO, categoryId);

        // Assert
        assertNotNull(result);
        assertEquals(categoryDTO.getCategoryId(), result.getCategoryId());
        assertEquals(categoryDTO.getCategoryName(), result.getCategoryName());
        verify(categoryRepository, times(1)).findById(categoryId);
        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    @Test
    void updateCategory_ShouldThrowResponseStatusException_WhenCategoryNotFound() {
        // Arrange
        when(categoryRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> categoryService.updateCategory(categoryDTO, categoryId));
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        verify(categoryRepository, times(1)).findById(categoryId);
        verify(categoryRepository, never()).save(any(Category.class));
    }
}