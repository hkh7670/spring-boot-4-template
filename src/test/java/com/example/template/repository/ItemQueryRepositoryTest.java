package com.example.template.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.template.config.JpaAuditingConfig;
import com.example.template.config.QuerydslConfig;
import com.example.template.domain.Item;
import com.example.template.service.dto.ItemResult;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

// @DataJpaTest는 JPA 관련 빈만 올리므로 QueryDSL 설정, Auditing 설정, 조회 리포지토리를 직접 가져온다.
@DataJpaTest
@ActiveProfiles("test")
@Import({QuerydslConfig.class, JpaAuditingConfig.class, ItemQueryRepository.class})
class ItemQueryRepositoryTest {

  @Autowired ItemRepository itemRepository;
  @Autowired ItemQueryRepository itemQueryRepository;

  @Test
  @DisplayName("QueryDSL projection으로 저장된 아이템을 ItemResult로 조회한다")
  void findAllProjected_returnsAllItemsAsResult() {
    Item pen = itemRepository.save(Item.of("볼펜", 3, "파란색 볼펜"));
    Item pencil = itemRepository.save(Item.of("연필", 5, "빨간색 연필"));

    List<ItemResult> results = itemQueryRepository.findAllProjected();

    assertThat(results)
        .containsExactlyInAnyOrder(
            new ItemResult(pen.getId(), "볼펜", 3, "파란색 볼펜"),
            new ItemResult(pencil.getId(), "연필", 5, "빨간색 연필")
        );
  }

  @Test
  @DisplayName("저장하면 생성/수정 일시가 자동으로 채워진다")
  void save_fillsAuditingDatetimes() {
    Item saved = itemRepository.saveAndFlush(Item.of("볼펜", 3, "파란색 볼펜"));

    assertThat(saved.getCreatedDatetime()).isNotNull();
    assertThat(saved.getUpdatedDatetime()).isNotNull();
  }
}
