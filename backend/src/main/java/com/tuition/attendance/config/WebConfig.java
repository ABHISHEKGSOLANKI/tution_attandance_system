package com.tuition.attendance.config;

import java.nio.file.Path;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final StorageProperties storageProperties;

    public WebConfig(StorageProperties storageProperties) {
        this.storageProperties = storageProperties;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path uploadPath = Path.of(storageProperties.getUploadDir()).toAbsolutePath().normalize();
        registry.addResourceHandler(storageProperties.getPublicPath() + "/**")
                .addResourceLocations(uploadPath.toUri().toString());
    }
}
