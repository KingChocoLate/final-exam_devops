package net.orderzone.idcard.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Service
public class PhotoStorageService {

    @Value("${app.photo-dir:uploads/photos}")
    private String photoDir;

    public String store(MultipartFile file) throws IOException {
        String contentType = file.getContentType();
        if (contentType == null ||
                (!contentType.equals("image/jpeg") && !contentType.equals("image/png"))) {
            throw new IllegalArgumentException("Only JPEG and PNG images are allowed");
        }
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("File size must be less than 5MB");
        }

        Path dir = Paths.get(photoDir);
        Files.createDirectories(dir);

        String ext = contentType.equals("image/png") ? ".png" : ".jpg";
        String fileName = UUID.randomUUID() + ext;
        Files.copy(file.getInputStream(), dir.resolve(fileName),
                StandardCopyOption.REPLACE_EXISTING);
        return fileName;
    }

    public byte[] load(String fileName) throws IOException {
        Path file = Paths.get(photoDir).resolve(fileName);
        return Files.readAllBytes(file);
    }

    public void delete(String fileName) throws IOException {
        Path file = Paths.get(photoDir).resolve(fileName);
        Files.deleteIfExists(file);
    }
}