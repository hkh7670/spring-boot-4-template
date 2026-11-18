package com.example.template.controller.dto;

import com.example.template.service.dto.ItemResult;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "아이템 목록 응답")
public record ItemListResponse(
    @Schema(description = "아이템 목록") List<ItemResponse> itemList
) {

  public static ItemListResponse from(List<ItemResult> resultList) {
    return new ItemListResponse(resultList.stream().map(ItemResponse::from).toList());
  }
}
