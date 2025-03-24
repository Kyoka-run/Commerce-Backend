package com.commerce.project.controller;

import com.commerce.project.payload.CategoryDTO;
import com.commerce.project.payload.CategoryResponse;
import com.commerce.project.service.CategoryService;
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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CategoryControllerTest {

    @Mock
    private CategoryService categoryService;

    @InjectMocks
    private CategoryController categoryController;

    private CategoryDTO categoryDTO;
    private List<CategoryDTO> categoryList;
    private CategoryResponse categoryResponse;
    private Long categoryId;

    @BeforeEach
    void setUp() {
        // Set up test data
        categoryId = 1L;

        categoryDTO = new CategoryDTO();
        categoryDTO.setCategoryId(categoryId);
        categoryDTO.setCategoryName("Test Category");

        categoryList = new ArrayList<>();
        categoryList.add(categoryDTO);

        categoryResponse = new CategoryResponse();
        categoryResponse.setContent(categoryList);
        categoryResponse.setPageNumber(0);
        categoryResponse.setPageSize(10);
        categoryResponse.setTotalElements(1L);
        categoryResponse.setTotalPages(1);
        categoryResponse.setLastPage(true);
    }

    @Test
    void getAllCategories_ShouldReturnCategoryResponse() {
        // Arrange
        when(categoryService.getAllCategories(anyInt(), anyInt(), anyString(), anyString()))
                .thenReturn(categoryResponse);

        // Act
        ResponseEntity<CategoryResponse> response = categoryController.getAllCategories(0, 10, "categoryId", "asc");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(categoryResponse, response.getBody());
        verify(categoryService, times(1)).getAllCategories(0, 10, "categoryId", "asc");
    }

    @Test
    void createCategory_ShouldReturnCreatedCategory() {
        // Arrange
        when(categoryService.createCategory(any(CategoryDTO.class))).thenReturn(categoryDTO);

        // Act
        ResponseEntity<CategoryDTO> response = categoryController.createCategory(categoryDTO);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(categoryDTO, response.getBody());
        verify(categoryService, times(1)).createCategory(categoryDTO);
    }

    @Test
    void deleteCategory_ShouldReturnDeletedCategory() {
        // Arrange
        when(categoryService.deleteCategory(anyLong())).thenReturn(categoryDTO);

        // Act
        ResponseEntity<CategoryDTO> response = categoryController.deleteCategory(categoryId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(categoryDTO, response.getBody());
        verify(categoryService, times(1)).deleteCategory(categoryId);
    }

    @Test
    void updateCategory_ShouldReturnUpdatedCategory() {
        // Arrange
        when(categoryService.updateCategory(any(CategoryDTO.class), anyLong())).thenReturn(categoryDTO);

        // Act
        ResponseEntity<CategoryDTO> response = categoryController.updateCategory(categoryDTO, categoryId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(categoryDTO, response.getBody());
        verify(categoryService, times(1)).updateCategory(categoryDTO, categoryId);
    }
}