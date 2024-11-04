package com.example.demo;

import java.io.File;

import org.springframework.core.io.FileSystemResource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ImageController {
    @GetMapping("/images/{filename:.+}")
    public FileSystemResource getImage(@PathVariable String filename) {
        return new FileSystemResource(new File("C:\\E-commerce\\" + filename));
    }
}
