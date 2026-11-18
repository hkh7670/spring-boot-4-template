package com.example.template.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.template.enums.ResponseCodeEnum;
import com.example.template.exception.ApiErrorException;
import com.example.template.service.ItemService;
import com.example.template.service.dto.ItemCommand;
import com.example.template.service.dto.ItemResult;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ItemController.class)
class ItemControllerTest {

  @Autowired MockMvc mockMvc;
  @MockitoBean ItemService itemService;

  @Test
  @DisplayName("단건 조회 성공 시 200과 공통 응답 포맷으로 아이템을 반환한다")
  void findById_existingItem_returnsOk() throws Exception {
    when(itemService.findById(1L)).thenReturn(new ItemResult(1L, "볼펜", 3, "파란색 볼펜"));

    mockMvc
        .perform(get("/api/items/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.resultCode").value("0000"))
        .andExpect(jsonPath("$.data.id").value(1))
        .andExpect(jsonPath("$.data.name").value("볼펜"));
  }

  @Test
  @DisplayName("목록 조회는 data.itemList로 감싸서 반환한다")
  void findAll_returnsWrappedList() throws Exception {
    when(itemService.findAll())
        .thenReturn(List.of(new ItemResult(1L, "볼펜", 3, "파란색 볼펜")));

    mockMvc
        .perform(get("/api/items"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.itemList.length()").value(1))
        .andExpect(jsonPath("$.data.itemList[0].name").value("볼펜"));
  }

  @Test
  @DisplayName("생성 성공 시 201을 반환한다")
  void create_validRequest_returnsCreated() throws Exception {
    when(itemService.create(any(ItemCommand.class)))
        .thenReturn(new ItemResult(1L, "볼펜", 3, "파란색 볼펜"));

    mockMvc
        .perform(
            post("/api/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"name": "볼펜", "age": 3, "description": "파란색 볼펜"}
                    """
                )
        )
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.resultCode").value("0001"))
        .andExpect(jsonPath("$.data.id").value(1));
    verify(itemService).create(eq(ItemCommand.of("볼펜", 3, "파란색 볼펜")));
  }

  @Test
  @DisplayName("이름이 비어 있으면 400과 필드별 에러 목록을 반환하고 서비스를 호출하지 않는다")
  void create_blankName_returnsBadRequest() throws Exception {
    mockMvc
        .perform(
            post("/api/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"name": ""}
                    """
                )
        )
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.resultCode").value("8000"))
        .andExpect(jsonPath("$.data.errorList[0].fieldName").value("name"));
    verify(itemService, never()).create(any());
  }

  @Test
  @DisplayName("존재하지 않는 아이템이면 404와 resultCode 1000을 반환한다")
  void findById_missingItem_returnsNotFound() throws Exception {
    when(itemService.findById(99L)).thenThrow(new ApiErrorException(ResponseCodeEnum.NOT_FOUND));

    mockMvc
        .perform(get("/api/items/99"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.resultCode").value("1000"));
  }

  @Test
  @DisplayName("존재하지 않는 경로(정적 리소스 포함)는 500이 아니라 404를 반환한다")
  void unknownPath_returnsNotFound() throws Exception {
    mockMvc
        .perform(get("/favicon.ico"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.resultCode").value("1000"));
  }

  @Test
  @DisplayName("지원하지 않는 HTTP 메서드는 500이 아니라 405와 resultCode 8001을 반환한다")
  void unsupportedMethod_returnsMethodNotAllowed() throws Exception {
    mockMvc
        .perform(patch("/api/items"))
        .andExpect(status().isMethodNotAllowed())
        .andExpect(jsonPath("$.resultCode").value("8001"));
  }

  @Test
  @DisplayName("지원하지 않는 Content-Type은 500이 아니라 415를 반환한다")
  void unsupportedContentType_returnsUnsupportedMediaType() throws Exception {
    mockMvc
        .perform(post("/api/items").contentType(MediaType.TEXT_PLAIN).content("x"))
        .andExpect(status().isUnsupportedMediaType())
        .andExpect(jsonPath("$.resultCode").value("8001"));
  }

  @Test
  @DisplayName("예상하지 못한 예외는 500과 resultCode 9998을 반환한다")
  void unexpectedException_returnsInternalServerError() throws Exception {
    when(itemService.findById(1L)).thenThrow(new IllegalStateException("boom"));

    mockMvc
        .perform(get("/api/items/1"))
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.resultCode").value("9998"));
  }

  @Test
  @DisplayName("삭제 성공 시 본문 없이 204를 반환한다")
  void delete_existingItem_returnsNoContent() throws Exception {
    mockMvc.perform(delete("/api/items/1")).andExpect(status().isNoContent());
    verify(itemService).delete(1L);
  }
}
