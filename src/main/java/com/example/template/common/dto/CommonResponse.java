package com.example.template.common.dto;

import com.example.template.enums.ResponseCodeEnum;
import com.example.template.exception.ErrorInfo;
import com.example.template.exception.ErrorInfoResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "공통 응답")
public record CommonResponse<T>(
    @Schema(description = "결과 코드", example = "0000") String resultCode,
    @Schema(description = "결과 메시지", example = "OK") String resultMsg,
    @Schema(description = "응답 데이터 (실패 시 null 또는 에러 상세)") T data
) {

  public static <T> CommonResponse<T> successResponse() {
    return new CommonResponse<>(
        ResponseCodeEnum.OK.getResultCode(), ResponseCodeEnum.OK.getResultMsg(), null
    );
  }

  public static <T> CommonResponse<T> successResponse(T data) {
    return new CommonResponse<>(
        ResponseCodeEnum.OK.getResultCode(), ResponseCodeEnum.OK.getResultMsg(), data
    );
  }

  public static <T> CommonResponse<T> createdResponse(T data) {
    return new CommonResponse<>(
        ResponseCodeEnum.CREATED.getResultCode(), ResponseCodeEnum.CREATED.getResultMsg(), data
    );
  }

  public static <T> CommonResponse<T> createdResponse() {
    return new CommonResponse<>(
        ResponseCodeEnum.CREATED.getResultCode(), ResponseCodeEnum.CREATED.getResultMsg(), null
    );
  }

  public static <T> CommonResponse<T> response(ResponseCodeEnum responseCodeEnum, T data) {
    return new CommonResponse<>(
        responseCodeEnum.getResultCode(), responseCodeEnum.getResultMsg(), data
    );
  }

  public static <T> CommonResponse<T> failResponse(ResponseCodeEnum responseCodeEnum) {
    return new CommonResponse<>(
        responseCodeEnum.getResultCode(), responseCodeEnum.getResultMsg(), null
    );
  }

  public static CommonResponse<ErrorInfoResponse> schemaValidateErrorResponse(
      List<ErrorInfo> errorInfoList
  ) {
    return new CommonResponse<>(
        ResponseCodeEnum.SCHEMA_VALIDATE_ERROR.getResultCode(),
        ResponseCodeEnum.SCHEMA_VALIDATE_ERROR.getResultMsg(),
        new ErrorInfoResponse(errorInfoList)
    );
  }

  public static <T> CommonResponse<T> internalServerErrorResponse() {
    return new CommonResponse<>(
        ResponseCodeEnum.INTERNAL_SERVER_ERROR.getResultCode(),
        ResponseCodeEnum.INTERNAL_SERVER_ERROR.getResultMsg(),
        null
    );
  }
}
