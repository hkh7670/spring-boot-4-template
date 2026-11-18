package com.example.template.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

  @Bean
  public OpenAPI openApi() {
    Info info =
        new Info()
            .title("spring-boot-4-template API")
            .description("Spring Boot 4 + Java 25 기반 템플릿 API 문서")
            .version("v1");
    return new OpenAPI().info(info);
  }
}
