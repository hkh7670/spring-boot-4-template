package com.example.template.exception;

import com.example.template.common.dto.CommonResponse;
import com.example.template.common.dto.ResponseHandler;
import com.example.template.enums.ResponseCodeEnum;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import tools.jackson.databind.exc.InvalidFormatException;
import tools.jackson.databind.exc.MismatchedInputException;

@Slf4j
@RestControllerAdvice
public class ApiCommonAdvice {

  // 클라이언트 잘못(4xx)은 스택트레이스 없이 WARN, 서버 문제(5xx)만 ERROR로 남겨 error 로그에는 장애만 쌓이게 한다.
  @ExceptionHandler(ApiErrorException.class)
  public <T> ResponseEntity<CommonResponse<T>> handleApiErrorException(ApiErrorException e) {
    ResponseCodeEnum responseCodeEnum = e.getResponseCodeEnum();
    if (responseCodeEnum.getHttpStatus().is5xxServerError()) {
      log.error(e.getMessage(), e);
    } else {
      log.warn(e.getMessage());
    }
    return ResponseHandler.fail(responseCodeEnum.getHttpStatus(), responseCodeEnum);
  }

  // RequestBody / ModelAttribute 유효성 에러 처리
  @ExceptionHandler(MethodArgumentNotValidException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public CommonResponse<ErrorInfoResponse> handleMethodArgumentNotValidException(
      MethodArgumentNotValidException e
  ) {
    log.warn(e.getMessage());
    List<ErrorInfo> errorInfoList =
        e.getBindingResult().getFieldErrors().stream().map(ErrorInfo::from).toList();
    return CommonResponse.schemaValidateErrorResponse(errorInfoList);
  }

  // RequestBody JSON 파싱 에러 처리 (타입 불일치 등)
  @ExceptionHandler(HttpMessageNotReadableException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public CommonResponse<?> handleHttpMessageNotReadableException(
      HttpMessageNotReadableException e
  ) {
    log.warn(e.getMessage());

    Throwable cause = e.getCause();
    if (cause instanceof MismatchedInputException mismatchedInputException
        && !mismatchedInputException.getPath().isEmpty()
    ) {
      String fieldName = mismatchedInputException.getPath().getFirst().getPropertyName();
      if (mismatchedInputException instanceof InvalidFormatException invalidFormatException) {
        return CommonResponse.schemaValidateErrorResponse(
            List.of(
                ErrorInfo.fromInvalidFormat(
                    fieldName,
                    invalidFormatException.getValue(),
                    invalidFormatException.getTargetType()
                )
            )
        );
      }
      return CommonResponse.schemaValidateErrorResponse(
          List.of(ErrorInfo.of(fieldName, null, "필수 입력값 입니다."))
      );
    }

    return CommonResponse.failResponse(ResponseCodeEnum.BAD_REQUEST);
  }

  // QueryString 타입 불일치 에러 처리
  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public CommonResponse<ErrorInfoResponse> handleMethodArgumentTypeMismatchException(
      MethodArgumentTypeMismatchException e
  ) {
    log.warn(e.getMessage());
    return CommonResponse.schemaValidateErrorResponse(
        List.of(ErrorInfo.fromInvalidFormat(e.getName(), e.getValue(), e.getRequiredType()))
    );
  }

  // QueryString Bean Validation 에러 처리
  @ExceptionHandler(HandlerMethodValidationException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public CommonResponse<ErrorInfoResponse> handleHandlerMethodValidationException(
      HandlerMethodValidationException e
  ) {
    log.warn(e.getMessage());
    List<ErrorInfo> errorInfoList =
        e.getParameterValidationResults().stream()
            .flatMap(result -> {
              String fieldName = result.getMethodParameter().getParameterName();
              Object fieldValue = result.getArgument();
              return result.getResolvableErrors().stream()
                  .map(error -> ErrorInfo.of(fieldName, fieldValue, error.getDefaultMessage()));
            })
            .toList();
    return CommonResponse.schemaValidateErrorResponse(errorInfoList);
  }

  // 참고 원본은 Spring Security의 AccessDeniedException도 처리하지만, 이 템플릿에는
  // spring-security 의존성이 없어 그 핸들러는 포팅하지 않았다. Security 도입 시 추가할 것.

  // Spring MVC 표준 예외(404 경로 없음, 405 메서드, 415 Content-Type, 필수 파라미터 누락 등)는
  // ErrorResponse를 구현해 원래 상태 코드를 갖고 있으므로, 500으로 바꾸지 않고 그 상태 코드로 응답한다.
  // @ResponseStatus는 Swagger 문서용이며, 실제 상태 코드는 반환하는 ResponseEntity가 우선한다.
  @ExceptionHandler(Exception.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public <T> ResponseEntity<CommonResponse<T>> handleException(Exception e) {
    if (e instanceof ErrorResponse errorResponse
        && errorResponse.getStatusCode().is4xxClientError()
    ) {
      log.warn(e.getMessage());
      ResponseCodeEnum responseCodeEnum =
          errorResponse.getStatusCode().value() == HttpStatus.NOT_FOUND.value()
              ? ResponseCodeEnum.NOT_FOUND
              : ResponseCodeEnum.BAD_REQUEST;
      return ResponseEntity.status(errorResponse.getStatusCode())
          .body(CommonResponse.failResponse(responseCodeEnum));
    }
    log.error(e.getMessage(), e);
    return ResponseEntity.internalServerError().body(CommonResponse.internalServerErrorResponse());
  }
}
