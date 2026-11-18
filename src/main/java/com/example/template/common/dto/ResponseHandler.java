package com.example.template.common.dto;

import com.example.template.enums.ResponseCodeEnum;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public final class ResponseHandler {

  private ResponseHandler() {}

  // 200 응답 (Response O)
  public static <T> ResponseEntity<CommonResponse<T>> ok(T response) {
    return ResponseEntity.ok(CommonResponse.successResponse(response));
  }

  // 200 응답 (Response X)
  public static <T> ResponseEntity<CommonResponse<T>> ok() {
    return ResponseEntity.ok(CommonResponse.successResponse());
  }

  // 201 Created 응답 (Response O)
  public static <T> ResponseEntity<CommonResponse<T>> created(T response) {
    return new ResponseEntity<>(CommonResponse.createdResponse(response), HttpStatus.CREATED);
  }

  // 201 Created 응답 (Response X)
  public static <T> ResponseEntity<CommonResponse<T>> created() {
    return new ResponseEntity<>(CommonResponse.createdResponse(), HttpStatus.CREATED);
  }

  // 실패 응답
  public static <T> ResponseEntity<CommonResponse<T>> fail(
      HttpStatus httpStatus, ResponseCodeEnum responseCodeEnum
  ) {
    return new ResponseEntity<>(CommonResponse.failResponse(responseCodeEnum), httpStatus);
  }
}
