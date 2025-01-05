package com.commerce.project.service.impl;

import com.commerce.project.exception.ResourceNotFoundException;
import com.commerce.project.model.Product;
import com.commerce.project.payload.ProductDTO;
import com.commerce.project.repository.ProductRepository;
import com.commerce.project.service.FileService;
import org.modelmapper.ModelMapper;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

public class FileServiceImpl implements FileService {

    @Override
    public String uploadImage(String path, MultipartFile file) throws IOException {
        // Get filename of current file
        String originalFilename = file.getOriginalFilename();

        // Generate a unique filename
        String randomId = UUID.randomUUID().toString();
        // xx.jpg -> 1234.jpg
        String fileName = randomId.concat(originalFilename.substring(originalFilename.lastIndexOf('.')));
        // File.separator == "/",but File.separator can work on different system
        String filePath = path + File.separator + fileName;

        // Check if path exist and create
        File folder = new File(path);
        if(!folder.exists()) {
            folder.mkdir();
        }

        // Upload to server
        Files.copy(file.getInputStream(), Paths.get(filePath));

        // Returning filename
        return fileName;
    }
}
