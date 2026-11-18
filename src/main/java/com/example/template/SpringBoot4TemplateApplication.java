package com.example.template;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class SpringBoot4TemplateApplication {

  public static void main(String[] args) {
    SpringApplication.run(SpringBoot4TemplateApplication.class, args);
  }
}
