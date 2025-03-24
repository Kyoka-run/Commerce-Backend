package com.commerce.project.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FileServiceImplTest {

    @InjectMocks
    private FileServiceImpl fileService;

    @TempDir
    Path tempDir;

    private MultipartFile mockFile;
    private String fileName;

    @BeforeEach
    void setUp() {
        // Set up test data
        fileName = "test.jpg";

        // Create mock multipart file
        mockFile = new MockMultipartFile(
                "file", fileName, "image/jpeg", "test image content".getBytes()
        );
    }

    @Test
    void uploadImage_ShouldReturnFileName() throws IOException {
        // Arrange
        String path = tempDir.toString();
        String fixedUuid = "fixed-uuid";

        try (MockedStatic<UUID> mockedUUID = mockStatic(UUID.class)) {
            // Mock UUID to return a fixed value for testing
            UUID mockUuid = mock(UUID.class);
            when(UUID.randomUUID()).thenReturn(mockUuid);
            when(mockUuid.toString()).thenReturn(fixedUuid);

            // Act
            String result = fileService.uploadImage(path, mockFile);

            // Assert
            assertNotNull(result);
            assertTrue(result.contains(fixedUuid));
            assertTrue(result.endsWith(".jpg"));

            // Verify file was created
            File folder = new File(path);
            assertTrue(folder.exists(), "Folder should exist");
        }
    }
}