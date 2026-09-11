package com.demo.place.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ApiVersionConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class ApiVersionConfig implements WebMvcConfigurer {

    @Override
    public void configureApiVersioning(ApiVersionConfigurer configurer) {
        configurer
        .usePathSegment(
            1,
            path -> path.pathWithinApplication()
                        .value()
                        .startsWith("/api/")
        )
        .addSupportedVersions("v1", "v2")
        .setDefaultVersion("v1");
    }
}
