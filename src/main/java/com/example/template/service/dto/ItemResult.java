package com.example.template.service.dto;

import com.example.template.domain.Item;
import com.querydsl.core.annotations.QueryProjection;

public record ItemResult(Long id, String name, Integer age, String description) {

  @QueryProjection
  public ItemResult {}

  public static ItemResult from(Item item) {
    return new ItemResult(item.getId(), item.getName(), item.getAge(), item.getDescription());
  }
}
