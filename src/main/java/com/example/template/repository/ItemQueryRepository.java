package com.example.template.repository;

import static com.example.template.domain.QItem.item;

import com.example.template.service.dto.ItemResult;
import com.example.template.service.dto.QItemResult;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ItemQueryRepository {

  private final JPAQueryFactory queryFactory;

  public List<ItemResult> findAllProjected() {
    return queryFactory
        .select(new QItemResult(item.id, item.name, item.age, item.description))
        .from(item)
        .fetch();
  }
}
