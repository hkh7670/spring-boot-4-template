package com.example.template.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.template.domain.Item;
import com.example.template.enums.ResponseCodeEnum;
import com.example.template.exception.ApiErrorException;
import com.example.template.repository.ItemQueryRepository;
import com.example.template.repository.ItemRepository;
import com.example.template.service.dto.ItemCommand;
import com.example.template.service.dto.ItemResult;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

  @Mock ItemRepository itemRepository;
  @Mock ItemQueryRepository itemQueryRepository;
  @InjectMocks ItemService itemService;

  @Test
  @DisplayName("id에 해당하는 아이템이 있으면 결과로 변환해 반환한다")
  void findById_existingItem_returnsResult() {
    // Arrange
    Item item = itemWithId(1L, "볼펜", 3, "파란색 볼펜");
    when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

    // Act
    ItemResult result = itemService.findById(1L);

    // Assert
    assertThat(result).isEqualTo(new ItemResult(1L, "볼펜", 3, "파란색 볼펜"));
  }

  @Test
  @DisplayName("id에 해당하는 아이템이 없으면 NOT_FOUND 예외를 던진다")
  void findById_missingItem_throwsNotFound() {
    when(itemRepository.findById(99L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> itemService.findById(99L))
        .isInstanceOf(ApiErrorException.class)
        .extracting("responseCodeEnum")
        .isEqualTo(ResponseCodeEnum.NOT_FOUND);
  }

  @Test
  @DisplayName("생성 요청 값으로 아이템을 저장하고 저장 결과를 반환한다")
  void create_validCommand_savesAndReturnsResult() {
    when(itemRepository.save(any(Item.class))).thenAnswer(inv -> inv.getArgument(0));

    ItemResult result = itemService.create(ItemCommand.of("볼펜", 3, "파란색 볼펜"));

    assertThat(result.name()).isEqualTo("볼펜");
    assertThat(result.age()).isEqualTo(3);
    assertThat(result.description()).isEqualTo("파란색 볼펜");
    verify(itemRepository).save(any(Item.class));
  }

  @Test
  @DisplayName("수정 요청 값으로 아이템의 모든 필드를 바꾼다")
  void update_existingItem_updatesAllFields() {
    Item item = itemWithId(1L, "볼펜", 3, "파란색 볼펜");
    when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

    ItemResult result = itemService.update(1L, ItemCommand.of("연필", 5, "빨간색 연필"));

    assertThat(result).isEqualTo(new ItemResult(1L, "연필", 5, "빨간색 연필"));
  }

  @Test
  @DisplayName("삭제 대상이 없으면 NOT_FOUND 예외를 던지고 삭제하지 않는다")
  void delete_missingItem_throwsNotFoundWithoutDeleting() {
    when(itemRepository.existsById(99L)).thenReturn(false);

    assertThatThrownBy(() -> itemService.delete(99L)).isInstanceOf(ApiErrorException.class);
    verify(itemRepository, never()).deleteById(any());
  }

  @Test
  @DisplayName("삭제 대상이 있으면 삭제한다")
  void delete_existingItem_deletes() {
    when(itemRepository.existsById(1L)).thenReturn(true);

    itemService.delete(1L);

    verify(itemRepository).deleteById(1L);
  }

  // id는 DB가 생성하는 값이라 setter가 없어서, 단위 테스트에서만 리플렉션으로 채운다.
  private Item itemWithId(Long id, String name, Integer age, String description) {
    Item item = Item.of(name, age, description);
    ReflectionTestUtils.setField(item, "id", id);
    return item;
  }
}
