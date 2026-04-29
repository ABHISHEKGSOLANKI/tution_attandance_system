package com.tuition.attendance.service;

import com.tuition.attendance.config.StorageProperties;
import com.tuition.attendance.exception.ApiException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class LocalStorageService {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of("image/jpeg", "image/png", "image/jpg", "image/webp");
    private final Path rootPath;
    private final StorageProperties storageProperties;

    public LocalStorageService(StorageProperties storageProperties) {
        this.storageProperties = storageProperties;
        this.rootPath = Path.of(storageProperties.getUploadDir()).toAbsolutePath().normalize();
        try {
            Files.createDirectories(rootPath);
        } catch (IOException exception) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not initialize local file storage");
        }
    }

    public String storeRegistrationPhoto(MultipartFile photo, String admissionId) {
        if (photo == null || photo.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Photo is required");
        }
        String contentType = photo.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Only JPG, PNG, or WEBP images are allowed");
        }

        String extension = extractExtension(photo.getOriginalFilename());
        String filename = admissionId.toLowerCase() + "-" + UUID.randomUUID() + extension;
        Path target = rootPath.resolve(filename).normalize();

        try {
            Files.copy(photo.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException exception) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not save uploaded photo");
        }

        return storageProperties.getPublicPath() + "/" + filename;
    }

    private String extractExtension(String originalFilename) {
        if (originalFilename == null || !originalFilename.contains(".")) {
            return ".jpg";
        }
        return originalFilename.substring(originalFilename.lastIndexOf('.')).toLowerCase();
    }
}
