package com.example.template.exception;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.validation.FieldError;

@Schema(description = "필드 검증 에러 상세")
public record ErrorInfo(
    @Schema(description = "에러가 발생한 필드명", example = "name") String fieldName,
    @Schema(description = "에러가 발생한 필드에 들어온 값", example = "") Object fieldValue,
    @Schema(description = "에러 메시지", example = "공백일 수 없습니다") String message
) {

  public static ErrorInfo from(FieldError fieldError) {
    return new ErrorInfo(
        fieldError.getField(), fieldError.getRejectedValue(), resolveMessage(fieldError)
    );
  }

  public static ErrorInfo fromInvalidFormat(
      String fieldName, Object rejectedValue, Class<?> targetType
  ) {
    String message = targetType != null ? resolveTypeMessage(targetType) : "데이터 형식이 올바르지 않습니다.";
    return new ErrorInfo(fieldName, rejectedValue, message);
  }

  public static ErrorInfo of(String fieldName, Object fieldValue, String message) {
    return new ErrorInfo(fieldName, fieldValue, message);
  }

  private static String resolveMessage(FieldError fieldError) {
    String[] codes = fieldError.getCodes() != null ? fieldError.getCodes() : new String[0];
    boolean isTypeMismatch = Arrays.stream(codes).anyMatch(code -> code.startsWith("typeMismatch"));
    if (!isTypeMismatch) {
      return fieldError.getDefaultMessage();
    }
    return extractQualifiedTypeName(codes)
        .flatMap(ErrorInfo::loadClass)
        .map(ErrorInfo::resolveTypeMessage)
        .orElse("데이터 형식이 올바르지 않습니다.");
  }

  private static String resolveTypeMessage(Class<?> targetType) {
    return switch (targetType.getSimpleName()) {
      case "LocalDate" -> "날짜 형식이 올바르지 않습니다. (yyyy-MM-dd)";
      case "LocalDateTime" -> "날짜/시간 형식이 올바르지 않습니다. (yyyy-MM-dd'T'HH:mm:ss)";
      case "LocalTime" -> "시간 형식이 올바르지 않습니다. (HH:mm:ss)";
      case "Integer", "Long", "Double", "Float", "BigDecimal" -> "숫자를 입력해주세요.";
      case "Boolean" -> "올바른 값을 입력해주세요. (true/false)";
      default -> targetType.isEnum() ? buildEnumMessage(targetType) : "데이터 형식이 올바르지 않습니다.";
    };
  }

  private static String buildEnumMessage(Class<?> enumClass) {
    String validValues =
        Arrays.stream(enumClass.getEnumConstants())
            .map(Object::toString)
            .collect(Collectors.joining(", "));
    return "올바른 값을 입력해주세요. (" + validValues + ")";
  }

  private static Optional<String> extractQualifiedTypeName(String[] codes) {
    return Arrays.stream(codes)
        .filter(code -> code.startsWith("typeMismatch."))
        .map(code -> code.substring("typeMismatch.".length()))
        .filter(code -> code.contains("."))
        .filter(code -> {
          String simpleName = code.substring(code.lastIndexOf('.') + 1);
          return !simpleName.isEmpty() && Character.isUpperCase(simpleName.charAt(0));
        })
        .findFirst();
  }

  private static Optional<Class<?>> loadClass(String name) {
    try {
      return Optional.of(Class.forName(name));
    } catch (ClassNotFoundException e) {
      return Optional.empty();
    }
  }
}
