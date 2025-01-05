package com.commerce.project.service;

import com.commerce.project.payload.ProductDTO;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public interface FileService {
    String uploadImage(String path, MultipartFile image) throws IOException;
}
