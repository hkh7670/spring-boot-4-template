package com.example.template.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "아이템 생성/수정 요청")
public record ItemRequest(
    @Schema(description = "이름", example = "볼펜") @NotBlank String name,
    @Schema(description = "나이", example = "3") Integer age,
    @Schema(description = "요약", example = "파란색 볼펜") String description
) {}
