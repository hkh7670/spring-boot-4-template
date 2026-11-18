package com.example.template.exception;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "검증 에러 목록 응답")
public record ErrorInfoResponse(@Schema(description = "필드별 에러 목록") List<ErrorInfo> errorList) {}
