package com.example.template.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ResponseCodeEnum {

  // Common (0000 ~ 0999)
  OK(HttpStatus.OK, "0000", "OK"),
  CREATED(HttpStatus.CREATED, "0001", "Created"),
  VALIDATE_SUCCESS(HttpStatus.OK, "0002", "입력 값 검증에 성공했습니다."),

  // Resource Error (1000 ~)
  NOT_FOUND(HttpStatus.NOT_FOUND, "1000", "요청한 리소스를 찾을 수 없습니다."),

  // Auth Error (4000 ~)
  ACCESS_DENIED(HttpStatus.FORBIDDEN, "4001", "유효하지 않은 접근 입니다."),

  // Client Error (8000 ~)
  SCHEMA_VALIDATE_ERROR(HttpStatus.BAD_REQUEST, "8000", "요청 필드에 대한 검증에 실패하였습니다."),
  BAD_REQUEST(HttpStatus.BAD_REQUEST, "8001", "유효하지 않은 요청입니다."),

  // Server Error (9000 ~)
  INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "9998", "내부 서버 오류 입니다. 관리자에게 문의해주세요."),
  ;

  private final HttpStatus httpStatus;
  private final String resultCode;
  private final String resultMsg;
}
