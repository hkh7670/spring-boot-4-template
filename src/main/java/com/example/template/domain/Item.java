package com.example.template.domain;

import com.example.template.common.domain.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "items")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Item extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "name", comment = "이름")
  private String name;

  @Column(name = "age", comment = "나이")
  private Integer age;

  @Column(name = "description", comment = "요약")
  private String description;

  @Builder(access = AccessLevel.PRIVATE)
  private Item(String name, Integer age, String description) {
    this.name = name;
    this.age = age;
    this.description = description;
  }

  public static Item of(
      String name,
      Integer age,
      String description
  ) {
    return Item.builder()
        .name(name)
        .age(age)
        .description(description)
        .build();
  }

  public void updateName(String name) {
    this.name = name;
  }

  public void updateAge(Integer age) {
    this.age = age;
  }

  public void updateDescription(String description) {
    this.description = description;
  }
}
