package com.example.template.common.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

// 요청마다 UUID trace id를 MDC에 넣어 그 요청에서 남는 모든 로그에 같은 id가 찍히게 한다.
// 다른 필터의 로그에도 찍히도록 가장 먼저 실행한다.
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TraceIdFilter extends OncePerRequestFilter {

  public static final String TRACE_ID = "traceId";

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain
  ) throws ServletException, IOException {
    MDC.put(TRACE_ID, UUID.randomUUID().toString());
    try {
      filterChain.doFilter(request, response);
    } finally {
      // 톰캣이 스레드를 재사용하므로 지우지 않으면 다음 요청 로그에 이전 id가 남는다.
      MDC.remove(TRACE_ID);
    }
  }
}
