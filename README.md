# spring-boot-4-template

Spring Boot 4 + Java 25 기반 신규 프로젝트용 베이스 템플릿입니다.

## 스택

- Spring Boot 4.1.1
- Java 25 (Gradle Toolchain으로 고정)
- Gradle 9.9.1 (Groovy DSL)
- Spring Data JPA + MySQL (local/테스트는 H2 인메모리)
- Spring Validation
- QueryDSL — [OpenFeign이 포크해 유지보수 중인 버전](https://github.com/OpenFeign/querydsl) (`io.github.openfeign.querydsl`, 원본 `com.querydsl`은 사실상 유지보수 중단 상태)
- Spring Cloud OpenFeign
- Spring Boot Actuator (`/actuator/health`)
- springdoc-openapi (Swagger UI)
- Logback (`logback-spring.xml`)
- Lombok (생성자/Getter 등 보일러플레이트 축소 목적으로 제한적으로 사용)
- Google Java Style 기반 코드 스타일 (`.editorconfig`)

## 프로필

| 프로필 | DB | 비고 |
|--------|----|------|
| `local` | H2 인메모리 | 프로필 미지정 시 기본값, 테이블 자동 생성, SQL 로그 출력 |
| `dev` | MySQL (환경변수) | SQL 로그 출력 |
| `stg` | MySQL (환경변수) | |
| `prod` | MySQL (환경변수) | |
| `test` | H2 인메모리 | 테스트 전용 (`src/test/resources/application-test.yml`) |

- dev/stg/prod는 `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD` 환경변수가 필요하고, 로그 디렉터리는 `LOG_DIR`(기본 `/logs`)로 지정합니다.
- local은 별도 DB 없이 바로 실행됩니다. 인메모리라 재시작하면 데이터가 초기화됩니다.
- 실행: `./gradlew bootRun --args='--spring.profiles.active=dev'` 또는 `SPRING_PROFILES_ACTIVE=dev`.

### 스키마 관리

dev/stg/prod는 스키마를 DDL로 직접 관리합니다(`spring.jpa.hibernate.ddl-auto: none`).
H2를 쓰는 `local`과 `test` 프로필에서만 `create-drop`으로 엔티티 기준 테이블을 자동 생성합니다.
샘플 테이블 DDL(MySQL):

```sql
CREATE TABLE items (
    id               BIGINT       NOT NULL AUTO_INCREMENT,
    name             VARCHAR(255) NULL COMMENT '이름',
    age              INT          NULL COMMENT '나이',
    description      VARCHAR(255) NULL COMMENT '요약',
    created_datetime DATETIME(6)  NOT NULL COMMENT '생성일시',
    updated_datetime DATETIME(6)  NOT NULL COMMENT '수정일시',
    PRIMARY KEY (id)
);
```

### 생성/수정 일시

엔티티가 `common.domain.BaseTimeEntity`를 상속하면 JPA Auditing(`JpaAuditingConfig`)이
`created_datetime`, `updated_datetime`을 자동으로 채웁니다.

### 운영 관련

- 헬스체크: `GET /actuator/health` → `{"status":"UP"}` (상세 정보는 노출하지 않음). nginx 포트 스위칭 배포의 준비 확인용으로 사용합니다.
- Graceful shutdown: 종료 신호를 받으면 새 요청은 받지 않고, 처리 중인 요청을 최대 30초(`spring.lifecycle.timeout-per-shutdown-phase`)까지 기다린 뒤 종료합니다.
- 로그 파일: `app_{포트}.log`(INFO 이상 전체), `error_{포트}.log`(ERROR만). 파일명에 `server.port`를 넣어서, nginx 포트 스위칭 배포 중 블루/그린 두 컨테이너가 같은 로그 볼륨을 써도 서로 다른 파일에 기록됩니다(같은 파일을 두 프로세스가 롤링하면 로그가 섞이거나 유실됩니다).
- 요청 추적: `TraceIdFilter`가 요청마다 UUID를 만들어 MDC(`traceId`)에 넣고, 로그 패턴에 `[traceId]`로 찍힙니다. 한 요청에서 남은 로그를 이 id로 묶어 찾을 수 있습니다. 요청 밖의 로그(기동/종료 등)는 `[]`로 나옵니다.
- 예외 로그 레벨: 클라이언트 오류(4xx, 검증 실패, 없는 리소스)는 스택트레이스 없이 `WARN`, 서버 오류(5xx)만 `ERROR`로 남겨서 `error_{포트}.log`에는 실제 장애만 쌓입니다.

## 테스트

`./gradlew test`로 실행합니다. 테스트는 H2 기반 `test` 프로필을 씁니다.

| 대상 | 방식 | 예시 |
|------|------|------|
| Service | JUnit 5 + Mockito 단위 테스트 | `service/ItemServiceTest` |
| Controller | `@WebMvcTest` + MockMvc (서비스는 `@MockitoBean`) | `controller/ItemControllerTest` |
| Repository | `@DataJpaTest` + QueryDSL/Auditing 설정 `@Import` | `repository/ItemQueryRepositoryTest` |

## 코드 스타일

Google Java Style(2칸 들여쓰기, 100자 제한)을 기반으로 하고, `.editorconfig`로 편집기 설정을 맞춥니다.
단, 인자/파라미터 목록이 여러 줄로 넘어가면 닫는 괄호는 마지막 인자에 붙이지 않고 단독 줄에 둡니다.

```java
public static ErrorInfo fromInvalidFormat(
    String fieldName, Object rejectedValue, Class<?> targetType
) {
```

google-java-format은 이 규칙을 지원하지 않아서(항상 닫는 괄호를 붙임) Spotless 같은 자동 포맷터는 쓰지 않습니다.

## 샘플 CRUD (Item)

`Item` 리소스 하나를 Controller → Service → Repository → Entity 전 계층에 걸쳐 예제로 포함했습니다.

| Method | Path             | 설명        |
|--------|------------------|-------------|
| POST   | `/api/items`     | 생성        |
| GET    | `/api/items`     | 목록 조회 (QueryDSL projection) |
| GET    | `/api/items/{id}`| 단건 조회   |
| PUT    | `/api/items/{id}`| 수정        |
| DELETE | `/api/items/{id}`| 삭제        |

- 목록 조회(`GET /api/items`)는 `ItemQueryRepository`(QueryDSL)가 필요한 필드만 DTO로 조회합니다. Entity 전체를 그대로 반환하지 않고 항상 DTO로 변환합니다.
- QueryDSL 조회 결과를 DTO로 받을 때는 `Projections.constructor` 대신 **`@QueryProjection`**을 씁니다. DTO 생성자에 `@QueryProjection`을 붙이면 `Q<DTO>` 클래스가 생성되어(`new QItemResult(...)`), 인자 개수나 타입이 어긋나면 런타임이 아니라 컴파일 단계에서 오류가 납니다.
- 조회 트랜잭션은 `@Transactional(readOnly = true)`, 쓰기 트랜잭션은 `@Transactional`을 사용합니다.

### 계층별 DTO 규칙

| 계층 | 입력 | 출력 | 위치 |
|------|------|------|------|
| Controller | `ItemRequest` (검증 애노테이션) | `ItemResponse`, `ItemListResponse` | `controller/dto` |
| Service | `ItemCommand` | `ItemResult` | `service/dto` |

- 서비스는 컨트롤러 DTO를 알지 못합니다. 컨트롤러가 `ItemRequest` → `ItemCommand`, `ItemResult` → `ItemResponse`로 변환합니다.
- 변환은 정적 팩토리 메서드로 합니다(`ItemCommand.of(...)`, `ItemResult.from(Item)`, `ItemResponse.from(ItemResult)`). 서비스 계층 객체는 컨트롤러 DTO를 받지 않고 값만 받아서, 의존 방향이 항상 컨트롤러 → 서비스가 되게 합니다.
- QueryDSL `@QueryProjection`은 서비스 계층 객체(`ItemResult`)에 붙입니다. 리포지토리도 컨트롤러 DTO를 참조하지 않습니다.
- 목록을 반환할 때는 `CommonResponse<List<...>>`가 아니라 래퍼 클래스로 한 번 더 감쌉니다(`CommonResponse<ItemListResponse>` → `{"data": {"itemList": [...]}}`).

## 공통 응답 / 예외 처리

`com.example.template.common.dto.CommonResponse<T>`로 모든 API 응답을 감싸고, `com.example.template.exception.ApiCommonAdvice`(`@RestControllerAdvice`)가 예외를 공통 포맷으로 변환합니다.

- `ApiErrorException(ResponseCodeEnum)` — 도메인 에러를 던질 때 사용하는 공통 예외. 새로운 에러가 필요하면 `ResponseCodeEnum`에 코드를 추가하고 이 예외로 던지면 됩니다.
- `MethodArgumentNotValidException`, `HttpMessageNotReadableException`, `MethodArgumentTypeMismatchException`, `HandlerMethodValidationException` — Bean Validation/역직렬화 관련 예외를 400으로, 필드별 에러 상세를 `ErrorInfo` 목록으로 변환합니다.
- 그 외 처리되지 않은 예외는 500으로 응답하며 상세 스택트레이스는 클라이언트에 노출하지 않습니다.
- Spring Security를 도입하면 `AccessDeniedException` 핸들러를 `ApiCommonAdvice`에 추가하세요 (현재는 Security 의존성이 없어 제외되어 있습니다).

## OpenFeign 클라이언트 추가하기

`@EnableFeignClients`가 이미 활성화되어 있습니다. 외부 API 클라이언트가 필요하면 인터페이스만 추가하면 됩니다.

```java
@FeignClient(name = "example-client", url = "${example.client.base-url}")
public interface ExampleClient {

    @GetMapping("/resource/{id}")
    ExampleResponse getResource(@PathVariable("id") Long id);
}
```

## Spring Boot 4로 넘어오며 바뀐 것들 (템플릿 구성 시 확인한 내용)

- 스타터 이름 변경: `spring-boot-starter-web` → `spring-boot-starter-webmvc`. H2 콘솔도 `spring-boot-h2console`로 분리.
- `spring-boot-starter-test` 하나로 묶여 있던 테스트 지원이 스타터별로 분리(`spring-boot-starter-data-jpa-test`, `spring-boot-starter-webmvc-test` 등).
- Jackson이 3.x로 올라가며 데이터바인딩 패키지가 `com.fasterxml.jackson.databind.*` → `tools.jackson.databind.*`로 이동 (단, `jackson-annotations`는 기존 `com.fasterxml.jackson.annotation` 패키지 유지). `JacksonException.Reference#getFieldName()`도 `getPropertyName()`으로 이름이 바뀌었습니다.

## 다른 프로젝트에 적용하기

1. `group`/`rootProject.name`/`package` 이름(`com.example.template`)을 프로젝트에 맞게 변경
2. `Item` 샘플 CRUD 슬라이스를 참고해 실제 도메인으로 대체
3. 각 프로필의 DB 접속 정보와 테이블 DDL을 프로젝트에 맞게 준비
