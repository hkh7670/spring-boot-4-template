package com.example.template.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

// 메인 클래스에 두면 @WebMvcTest 같은 슬라이스 테스트에서도 JPA 메타모델을 요구해 실패하므로 분리한다.
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {}
