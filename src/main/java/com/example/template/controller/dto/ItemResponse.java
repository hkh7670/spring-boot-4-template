package com.example.template.controller.dto;

import com.example.template.service.dto.ItemResult;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "아이템 응답")
public record ItemResponse(
    @Schema(description = "아이템 id", example = "1") Long id,
    @Schema(description = "이름", example = "볼펜") String name,
    @Schema(description = "나이", example = "3") Integer age,
    @Schema(description = "요약", example = "파란색 볼펜") String description
) {

  public static ItemResponse from(ItemResult result) {
    return new ItemResponse(result.id(), result.name(), result.age(), result.description());
  }
}
