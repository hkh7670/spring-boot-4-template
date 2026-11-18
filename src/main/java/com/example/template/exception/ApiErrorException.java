package com.example.template.exception;

import com.example.template.enums.ResponseCodeEnum;
import lombok.Getter;

@Getter
public class ApiErrorException extends RuntimeException {

  private final ResponseCodeEnum responseCodeEnum;

  public ApiErrorException(ResponseCodeEnum responseCodeEnum) {
    super(responseCodeEnum.getResultMsg());
    this.responseCodeEnum = responseCodeEnum;
  }

}
