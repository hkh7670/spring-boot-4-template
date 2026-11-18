package com.example.template.controller;

import com.example.template.common.dto.CommonResponse;
import com.example.template.common.dto.ResponseHandler;
import com.example.template.controller.dto.ItemListResponse;
import com.example.template.controller.dto.ItemRequest;
import com.example.template.controller.dto.ItemResponse;
import com.example.template.service.ItemService;
import com.example.template.service.dto.ItemCommand;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Item", description = "아이템 CRUD API")
@RestController
@RequestMapping("/api/items")
@RequiredArgsConstructor
public class ItemController {

  private final ItemService itemService;

  @Operation(summary = "아이템 목록 조회", description = "전체 아이템 목록을 조회합니다.")
  @ApiResponse(responseCode = "200", description = "조회 성공")
  @GetMapping
  public ResponseEntity<CommonResponse<ItemListResponse>> findAll() {
    return ResponseHandler.ok(ItemListResponse.from(itemService.findAll()));
  }

  @Operation(summary = "아이템 단건 조회", description = "id로 아이템 하나를 조회합니다.")
  @ApiResponse(responseCode = "200", description = "조회 성공")
  @ApiResponse(
      responseCode = "404",
      description = "아이템이 존재하지 않음 (resultCode: 1000)",
      content = @Content(schema = @Schema(implementation = CommonResponse.class))
  )
  @GetMapping("/{id}")
  public ResponseEntity<CommonResponse<ItemResponse>> findById(
      @Parameter(description = "아이템 id", example = "1") @PathVariable Long id
  ) {
    return ResponseHandler.ok(ItemResponse.from(itemService.findById(id)));
  }

  @Operation(summary = "아이템 생성", description = "새 아이템을 생성합니다.")
  @ApiResponse(responseCode = "201", description = "생성 성공")
  @PostMapping
  public ResponseEntity<CommonResponse<ItemResponse>> create(
      @Valid @RequestBody ItemRequest request
  ) {
    ItemCommand command = ItemCommand.of(request.name(), request.age(), request.description());
    return ResponseHandler.created(ItemResponse.from(itemService.create(command)));
  }

  @Operation(summary = "아이템 수정", description = "id에 해당하는 아이템의 모든 값을 요청 값으로 바꿉니다.")
  @ApiResponse(responseCode = "200", description = "수정 성공")
  @ApiResponse(
      responseCode = "404",
      description = "아이템이 존재하지 않음 (resultCode: 1000)",
      content = @Content(schema = @Schema(implementation = CommonResponse.class))
  )
  @PutMapping("/{id}")
  public ResponseEntity<CommonResponse<ItemResponse>> update(
      @Parameter(description = "아이템 id", example = "1") @PathVariable Long id,
      @Valid @RequestBody ItemRequest request
  ) {
    ItemCommand command = ItemCommand.of(request.name(), request.age(), request.description());
    return ResponseHandler.ok(
        ItemResponse.from(itemService.update(id, command))
    );
  }

  @Operation(summary = "아이템 삭제", description = "id에 해당하는 아이템을 삭제합니다.")
  @ApiResponse(responseCode = "204", description = "삭제 성공 (응답 본문 없음)")
  @ApiResponse(
      responseCode = "404",
      description = "아이템이 존재하지 않음 (resultCode: 1000)",
      content = @Content(schema = @Schema(implementation = CommonResponse.class))
  )
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(
      @Parameter(description = "아이템 id", example = "1") @PathVariable Long id
  ) {
    itemService.delete(id);
    return ResponseEntity.noContent().build();
  }
}
