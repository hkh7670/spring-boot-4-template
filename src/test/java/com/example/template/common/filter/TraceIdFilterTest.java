package com.example.template.common.filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import jakarta.servlet.FilterChain;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class TraceIdFilterTest {

  private final TraceIdFilter filter = new TraceIdFilter();

  @AfterEach
  void clearMdc() {
    MDC.clear();
  }

  @Test
  @DisplayName("요청을 처리하는 동안 MDC에 UUID 형태의 traceId가 들어 있다")
  void doFilter_duringRequest_putsUuidTraceIdIntoMdc() throws Exception {
    List<String> captured = new ArrayList<>();
    FilterChain chain = (req, res) -> captured.add(MDC.get(TraceIdFilter.TRACE_ID));

    filter.doFilter(new MockHttpServletRequest(), new MockHttpServletResponse(), chain);

    assertThat(captured).hasSize(1);
    String traceId = captured.getFirst();
    assertThat(traceId).isNotBlank();
    assertThat(UUID.fromString(traceId).toString()).isEqualTo(traceId);
  }

  @Test
  @DisplayName("요청이 끝나면 MDC에서 traceId를 지운다")
  void doFilter_afterRequest_removesTraceId() throws Exception {
    filter.doFilter(new MockHttpServletRequest(), new MockHttpServletResponse(), (req, res) -> {});

    assertThat(MDC.get(TraceIdFilter.TRACE_ID)).isNull();
  }

  @Test
  @DisplayName("처리 중 예외가 나도 MDC에서 traceId를 지운다")
  void doFilter_whenChainThrows_stillRemovesTraceId() {
    FilterChain failingChain = (req, res) -> {
      throw new IllegalStateException("boom");
    };

    assertThatThrownBy(() -> filter.doFilter(
        new MockHttpServletRequest(), new MockHttpServletResponse(), failingChain
    )).isInstanceOf(IllegalStateException.class);
    assertThat(MDC.get(TraceIdFilter.TRACE_ID)).isNull();
  }

  @Test
  @DisplayName("요청마다 서로 다른 traceId를 만든다")
  void doFilter_eachRequest_generatesDifferentTraceId() throws Exception {
    List<String> captured = new ArrayList<>();
    FilterChain chain = (req, res) -> captured.add(MDC.get(TraceIdFilter.TRACE_ID));

    filter.doFilter(new MockHttpServletRequest(), new MockHttpServletResponse(), chain);
    filter.doFilter(new MockHttpServletRequest(), new MockHttpServletResponse(), chain);

    assertThat(captured).hasSize(2).doesNotHaveDuplicates();
  }
}
