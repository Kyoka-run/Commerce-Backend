package com.commerce.project.controller;

import com.commerce.project.model.Category;
import com.commerce.project.payload.CategoryDTO;
import com.commerce.project.repository.CategoryRepository;
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
public class CategoryControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CategoryRepository categoryRepository;

    @BeforeEach
    void setUp() {
        // Clear existing categories
        categoryRepository.deleteAll();

        // Create test categories
        for (int i = 1; i <= 3; i++) {
            Category category = new Category();
            category.setCategoryName("Test Category " + i);
            categoryRepository.save(category);
        }
    }

    @Test
    void getAllCategories_ShouldReturnAllCategories() throws Exception {
        // Test getting all categories (public endpoint)
        mockMvc.perform(get("/api/public/categories"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(3)))
                .andExpect(jsonPath("$.content[0].categoryName", containsString("Test Category")))
                .andExpect(jsonPath("$.totalElements").value(3));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createCategory_ShouldCreateNewCategory() throws Exception {
        // Create category request
        CategoryDTO categoryDTO = new CategoryDTO();
        categoryDTO.setCategoryName("New Test Category");

        // Test creating a new category
        mockMvc.perform(post("/api/public/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoryDTO)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.categoryName").value("New Test Category"));

        // Verify the category was created
        List<Category> categories = categoryRepository.findAll();
        assert(categories.size() == 4);
        assert(categories.stream().anyMatch(c -> c.getCategoryName().equals("New Test Category")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateCategory_ShouldUpdateCategory() throws Exception {
        // Get an existing category
        Category existingCategory = categoryRepository.findAll().get(0);
        Long categoryId = existingCategory.getCategoryId();

        // Create update request
        CategoryDTO updateDTO = new CategoryDTO();
        updateDTO.setCategoryName("Updated Category Name");

        // Test updating the category
        mockMvc.perform(put("/api/public/categories/" + categoryId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categoryName").value("Updated Category Name"));

        // Verify the category was updated
        Category updatedCategory = categoryRepository.findById(categoryId).orElseThrow();
        assert(updatedCategory.getCategoryName().equals("Updated Category Name"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteCategory_ShouldDeleteCategory() throws Exception {
        // Get an existing category
        Category existingCategory = categoryRepository.findAll().get(0);
        Long categoryId = existingCategory.getCategoryId();

        int initialCount = categoryRepository.findAll().size();

        // Test deleting the category
        mockMvc.perform(delete("/api/admin/categories/" + categoryId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categoryName").value(existingCategory.getCategoryName()));

        // Verify the category was deleted
        List<Category> remainingCategories = categoryRepository.findAll();
        assert(remainingCategories.size() == initialCount - 1);
        assert(categoryRepository.findById(categoryId).isEmpty());
    }
}