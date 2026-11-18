package com.example.template.service.dto;

public record ItemCommand(String name, Integer age, String description) {

  public static ItemCommand of(String name, Integer age, String description) {
    return new ItemCommand(name, age, description);
  }
}
